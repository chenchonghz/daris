package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nig.mf.plugin.pssd.Asset;
import nig.mf.plugin.pssd.DataObject;
import nig.mf.plugin.pssd.DataSet;
import nig.mf.plugin.pssd.Metadata;
import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.RSubject;
import nig.mf.plugin.pssd.Study;
import nig.mf.plugin.pssd.Subject;
import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.plugin.pssd.method.Method;
import nig.mf.plugin.pssd.method.MethodSet;
import nig.mf.plugin.pssd.project.DataUse;
import nig.mf.plugin.pssd.project.ProjectMemberMap;
import nig.mf.plugin.pssd.tag.ProjectSpecificTagDictionary;
import nig.mf.plugin.pssd.user.ModelUserRoleSet;
import nig.mf.pssd.Role;
import nig.mf.pssd.plugin.util.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedQuery;
import nig.util.ObjectUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * This service is the core service that finds PSSD objects, applies application
 * level role-based filtering, and reformats the native asset.get meta-data in
 * the PSSD structure. Just about everything should go via this service or its
 * static version thereof.
 * 
 * This service and its static functions are only called in the context of
 * viewing or finding objects, not creating them.
 * 
 * @author jason
 * 
 */
public class SvcObjectFind extends PluginService {
	
	
	// White list of additional non PSSD doc types we want to present
	private static String[] DocTypeWhiteList = {"mf-image", "mf-image-exif", "mf-image-iptc", "mf-image-tiff"};
	        

    private Interface _defn;

    public SvcObjectFind() {

        _defn = new Interface();
        _defn.add(new Interface.Element("type",
                new EnumType(new String[] { Project.TYPE.toString(),
                        Subject.TYPE.toString(), ExMethod.TYPE.toString(),
                        Study.TYPE.toString(), DataSet.TYPE.toString(),
                        DataObject.TYPE.toString(), RSubject.TYPE.toString() }),
                "The type of the object(s) to restrict the search, if any.", 0,
                1));
        _defn.add(new Interface.Element("text", StringType.DEFAULT,
                "Arbitrary search text for free text query.", 0, 1));
        _defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE,
                "Cursor position. Defaults to 1", 0, 1));
        _defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE,
                "Cursor size. Defaults to 100", 0, 1));
        _defn.add(new Interface.Element("foredit", BooleanType.DEFAULT,
                "Indicates whether the object may be edited. If true, then a description of the structure of the data is returned. Defaults to 'false'.",
                0, 1));
        _defn.add(new Interface.Element("asset-type",
                new EnumType(new String[] { "primary", "replica", "all" }),
                "Specify type of asset to find. Defaults to all.", 0, 1));
        _defn.add(new Interface.Element("pdist", IntegerType.DEFAULT,
                "Specifies the peer distance for a distributed query. Defaults to all servers in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",
                0, 1));
    }

    public String name() {

        return "om.pssd.object.find";
    }

    public String description() {

        return "Returns objects that match the given search parameters. It does a distributed query in a federation.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_ACCESS;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out,
            XmlWriter w) throws Throwable {

        String type = args.value("type");
        String text = args.value("text");
        boolean forEdit = args.booleanValue("foredit", false);
        String pdist = args.value("pdist");
        String assetType = args.stringValue("asset-type", "all");

        // Setup query
        StringBuilder query = new StringBuilder();

        if (type == null) {
            query.append("xpath(daris:pssd-object) has value"); // "cid starts
                                                                // with named id
                                                                // '"
            // +
            // CiteableIdUtil.PROJECT_CID_ROOT_NAME
            // + "'";
        } else {
            query.append("xpath(daris:pssd-object/type)='" + type + "'");
        }

        if (text != null) {
            query.append(" and text contains '" + text + "'");
        }

        // Primary/replica/both (default)
        DistributedQuery.appendResultAssetTypePredicate(query,
                DistributedQuery.ResultAssetType.instantiate(assetType));

        // Set up service call
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", query.toString());
        if (forEdit) {
            dm.add("action", "get-template-meta");
        } else {
            dm.add("action", "get-meta");
        }

        dm.add("idx", args.longValue("idx", 1));
        dm.add("size", args.intValue("size", 100));

        dm.add("get-related-meta", "true");
        dm.add("related-type", "attachment");
        if (pdist != null) {
            dm.add("pdist", pdist);
        }

        // asset.query is distributed in a federation
        // We will need to do some clever filtering so that we don't end up with
        // an intractable mess
        // of primary and replica objects in a federated environment
        XmlDoc.Element r = executor().execute("asset.query", dm.root());

        // Parse the XML and reformat
        addPssdObjects(executor(), w, r.elements("asset"), false, forEdit);
    }

    public static void addPssdObjects(ServiceExecutor executor, XmlWriter w,
            XmlDoc.Element r, boolean isleaf, boolean forEdit)
                    throws Throwable {

        addPssdObjects(executor, w, r.elements("asset"), isleaf, forEdit);
    }

    public static void addPssdObjects(ServiceExecutor executor, XmlWriter w,
            List<XmlDoc.Element> aes, boolean isleaf, boolean forEdit)
                    throws Throwable {

        addPssdObjects(executor, w, aes, isleaf, forEdit, false, false, true);
    }

    /**
     * This function takes a collection of meta-data as provided by asset.query,
     * and reformats it for the PSSD data model, applying any PSSD role-based
     * filtering in the process
     * 
     * @param executor
     * @param w
     *            The returned meta-data
     * @param r
     *            The input meta-data from asset.query (could contain remote
     *            objects)
     * @param isleaf
     *            If true, identify whether nodes are leaf nodes or not
     * @param forEdit
     *            If true, then the returned meta-data will be used for editing,
     *            in which case a description of the meta-data is returned
     *            rather than just a specification
     * @param showRSubjectIdentity
     *            If true, show the Identity meta-data on an RSubject object
     *            regardless of the user's role
     * @param showSubjectPrivate
     *            If true, show the "private" field meta-data on a Subject
     *            object regardless of the user's role
     * @throws Throwable
     */
    public static void addPssdObjects(ServiceExecutor executor, XmlWriter w,
            XmlDoc.Element r, boolean isleaf, boolean forEdit,
            boolean showRSubjectIdentity, boolean showSubjectPrivate)
                    throws Throwable {

        addPssdObjects(executor, w, r.elements("asset"), isleaf, forEdit,
                showRSubjectIdentity, showSubjectPrivate, true);
    }

    public static void addPssdObjects(ServiceExecutor executor, XmlWriter w,
            List<XmlDoc.Element> aes, boolean isleaf, boolean forEdit,
            boolean showRSubjectIdentity, boolean showSubjectPrivate,
            boolean sortByCid) throws Throwable {

        if (aes != null) {
            if (sortByCid && aes.size() > 1) {
                Collections.sort(aes, new Comparator<XmlDoc.Element>() {

                    @Override
                    public int compare(XmlDoc.Element ae1, XmlDoc.Element ae2) {

                        String cid1 = null;
                        String cid2 = null;
                        try {
                            cid1 = ae1.value("cid");
                        } catch (Throwable e) {
                        }
                        try {
                            cid2 = ae2.value("cid");
                        } catch (Throwable e) {
                        }
                        return CiteableIdUtil.compare(cid1, cid2);
                    }
                });
            }
            ModelUserRoleSet selfRoles = ModelUser.selfRoles(executor);
            MethodSet methods = MethodSet.load(executor, null, false);
            String lastProute = null;
            ProjectMemberMap pmmap = null;
            for (XmlDoc.Element ae : aes) {
                String nameSpace = ae.value("namespace");
                PSSDObject.Type type = PSSDObject.Type
                        .parse(ae.value("meta/daris:pssd-object/type"));
                if (type != null) { // Only include PSSD objects
                    // if (Model.isPSSDModel(ae.value("model"))) {
                    // Only include PSSD objects (some redundancy with
                    // daris:pssd-object/type)
                    String id = ae.value("cid");
                    String proute = ae.value("@proute");
                    DistributedAsset dAsset = new DistributedAsset(proute, id);
                    if (!ObjectUtil.equals(proute, lastProute)) {
                        methods = MethodSet.load(executor,
                                new ServerRoute(proute), false);
                        lastProute = proute;
                    }

                    // Some objects (currently Subject only) may be filtered out
                    // entirely (currently for ethics/data-use restrictions).
                    // Therefore, before we start adding the generic PSSD
                    // meta-data to the output, we must apply this filtering
                    if (type.equals(Subject.TYPE)) {
                        DataUse subjectDataUse = DataUse.instantiate(
                                ae.value("meta/daris:pssd-subject/data-use"));
                        if (!validateSubjectDataUse(id, subjectDataUse,
                                selfRoles)) {
                            // violates subject data-use, drop it.
                            continue;
                        }
                    }

                    // Establish if the current user has the authorization to
                    // edit the
                    // meta-data for this object (regardless if they *want* to
                    // edit it
                    // or not [forEdit]). Only for objects in PSSD trees
                    DistributedAsset dProject = null;
                    Boolean editable = false;
                    if (!type.equals(Method.TYPE)) {
                        dProject = dAsset.getParentProject(true);
                        editable = isEditable(type, dProject.getCiteableID(),
                                selfRoles);
                    }

                    // Establish if user holds a system admin role or generic
                    // PSSD admin role on the host
                    // that manages the object we are describing
                    boolean userIsAdmin = ModelUser.hasRole(
                            dAsset.getServerRouteObject(), executor,
                            "system-administrator")
                            || ModelUser.hasRole(dAsset.getServerRouteObject(),
                                    executor, Role.objectAdminRoleName());

                    // Further see if the user holds at least Subject admin role
                    // for PSSD tree objects
                    // on the server managing the parent project
                    if (!type.equals(Method.TYPE)) {
                        userIsAdmin = userIsAdmin || ModelUser.hasRole(
                                dProject.getServerRouteObject(), executor,
                                Project.subjectAdministratorRoleName(
                                        dProject.getCiteableID()));
                    }

                    // Start accumulating the PSSD formatted meta-data. Start
                    // with the basic/required meta-data for all objects
                    //
                    w.push("object",
                            new String[] { "type", type.toString(), "editable",
                                    Boolean.toString(editable), "version",
                                    ae.value("@version"), "vid",
                                    ae.value("@vid") });

                    /*
                     * Regardless of whether it is called locally or as a peer,
                     * asset.query will give me back a sensible server route in
                     * a federation
                     */
                    addPssdObject(w, ae, userIsAdmin);

                    /*
                     * Add meta-data appropriately for the type of this object
                     */
                    boolean addMeta = true;
                    boolean addObject = true;
                    if (type == Project.TYPE) {
                        if (pmmap == null) {
                            // TODO: remove this to improve performance. because
                            // it
                            // launches many services calls to check roles
                            if (aes.size() > 1) {
                                pmmap = ProjectMemberMap.load(executor, null);
                            } else {
                                // optimised for single project
                                pmmap = ProjectMemberMap.load(executor, null,
                                        dProject.getCiteableID());
                            }
                        }
                        addPssdProject(executor, proute, w, ae,
                                dProject.getCiteableID(), selfRoles, methods,
                                pmmap);
                    } else if (type == Subject.TYPE) {
                        addObject = addPssdSubject(executor,
                                dProject.getServerRouteObject(), dAsset, w, ae,
                                forEdit, showSubjectPrivate, userIsAdmin);
                    } else if (type == RSubject.TYPE) {
                        addPssdRSubject(executor, dAsset, w, ae, forEdit,
                                showRSubjectIdentity);
                        addMeta = false;
                    } else if (type == ExMethod.TYPE) {
                        addPssdExMethod(executor, w, ae, forEdit);
                    } else if (type == Study.TYPE) {
                        addPssdStudy(w, ae, forEdit);
                    } else if (type == DataSet.TYPE) {
                        addPssdDataSet(w, ae);
                    } else if (type == DataObject.TYPE) {
                        addPssdDataObject(w, ae);
                    } else if (type == Method.TYPE) {
                        addPssdMethod(executor, w, ae);
                    }

                    // If wanted, identify if node is a leaf or not
                    if (isleaf) {
                        XmlDocMaker dm = new XmlDocMaker("args");
                        String query = "cid in '" + dAsset.getCiteableID()
                                + "'";
                        dm.add("where", query);
                        dm.add("action", "count");

                        int nbc = executor.execute("asset.query", dm.root())
                                .intValue("value", 0);
                        w.add("isleaf", nbc == 0);
                        w.add("number-of-children", nbc);
                    }

                    // Now add the rest of the domain-specific extended
                    // meta-data (filters out
                    // the required object-specific meta-data just
                    // consumed).
                    // Some types of objects don't have extended metadata..
                    if (addMeta) {
                        addPssdMeta(w, ae, type);
                    }

                    addPssdAttachments(w, ae);
                    w.pop();
                }
            }
        }

    }

    public static boolean validateSubjectDataUse(String subjectId,
            DataUse subjectDataUse, ModelUserRoleSet selfRoles)
                    throws Throwable {

        // Find the parent project so that we know the CID for project-specific
        // roles and what server to check roles on. Because this class is only
        // used in a viewing/finding context, we can look for primary first and
        // then replica project parents.
        String projectId = CiteableIdUtil.getParentId(subjectId);

        // Check the user's roles on the server managing the Project.
        // A project-admin can see/do anything and by definition a Subject
        // admin is allowed to access all Subjects
        if (selfRoles.hasProjectAdminRole(projectId)
                || selfRoles.hasSubjectAdminRole(projectId)) {
            return true;
        }

        // Get the data-use specification for the subject. If null, then no
        // restrictions apply.
        if (subjectDataUse == null) {
            return true;
        }

        // Now filter. The user's data-use role must not exceed the subject's
        // specification.
        // If the user has no 'data-use' role, this code behaves as if they
        // had 'specific'. However, we only actually need the 'extended' and
        // 'unspecified' roles to be given to a user as 'specific' is implicit.
        if (subjectDataUse == DataUse.specific
                && (selfRoles.hasExtendedDataUseRole(projectId)
                        || selfRoles.hasUnspecifiedDataUseRole(projectId))) {
            return false;
        } else if (subjectDataUse == DataUse.extended
                && selfRoles.hasUnspecifiedDataUseRole(projectId)) {
            return false;
        }
        return true;
    }

    /**
     * Determine if this object should be dropped entirely from the return
     * Currently we filter only on the 'data-use' criterion. This function
     * scrutinizes roles that are assigned to the user on the server that
     * manages the asset.
     * 
     * @param executor
     * @param type
     *            type of PSSD object
     * @param id
     *            CID of object
     * @param ae
     * @return
     * @throws Throwable
     */
    /*
     * private static boolean dropObject(ServiceExecutor executor, String type,
     * DistributedAsset dAsset, XmlDoc.Element ae) throws Throwable {
     * 
     * if (type.equalsIgnoreCase(Subject.TYPE)) {
     * 
     * // Find the parent project so that we know the CID for //
     * project-specific roles // and what server to check roles on. Because this
     * class is only // used in a viewing/finding // context, we can look for
     * primary first and then replica project // parents. Boolean readOnly =
     * true; DistributedAsset dProjectAsset = dAsset.getParentProject(readOnly);
     * if (dProjectAsset == null) { throw new Exception(
     * "Cannot find a parent Project of the given object"); } ServerRoute
     * projectRoute = dProjectAsset.getServerRouteObject(); String projectCID =
     * dProjectAsset.getCiteableID();
     * 
     * // CHeck the user's roles on the server managing the Project // A
     * project-admin can see/do anything and by definition a Subject // admin is
     * allowed // to access all Subjects if (ModelUser.hasRole(projectRoute,
     * executor, Project.subjectAdministratorRoleName(projectCID))) return
     * false;
     * 
     * // Get the data-use specification for this Subject. If null, then no //
     * restrictions apply String subjectDataUse =
     * ae.value("meta/daris:pssd-subject/data-use"); if (subjectDataUse == null)
     * return false;
     * 
     * // Now filter. The user's data-use role must not exceed the // subject's
     * specification // If the user has no 'data-use' role, this code behaves as
     * if they // had 'specific' // However, we only actually need the
     * 'extended' and 'unspecified' // roles to be given // to a user as
     * 'specific' is implicit if
     * (subjectDataUse.equals(Project.CONSENT_SPECIFIC_ROLE_NAME)) { if
     * (ModelUser.hasRole(projectRoute, executor,
     * Project.extendedUseRoleName(projectCID)) ||
     * ModelUser.hasRole(projectRoute, executor,
     * Project.unspecifiedUseRoleName(projectCID))) return true; } else if
     * (subjectDataUse.equals(Project.CONSENT_EXTENDED_ROLE_NAME)) { if
     * (ModelUser.hasRole(projectRoute, executor,
     * Project.unspecifiedUseRoleName(projectCID))) return true; } else if
     * (subjectDataUse.equals(Project.CONSENT_UNSPECIFIED_ROLE_NAME)) { return
     * false; }
     * 
     * } // return false;
     * 
     * }
     */
    /**
     * Is the user authorized to edit the given PSSD object?
     * 
     * @param executor
     * @param type
     *            Type of PSSD object
     * @param dCID
     *            Distributed CID of PSSD object
     * @return
     * @throws Throwable
     */
    /*
     * private static boolean isEditable(ServiceExecutor executor, String type,
     * DistributedAsset dAsset) throws Throwable {
     * 
     * // Find the parent project so that we know the CID for project-specific
     * // roles // and what server to check roles on. Because this class is only
     * used in // a viewing/finding // context, we can look for primary first
     * and then replica project // parents. Boolean readOnly = true;
     * DistributedAsset dProject = dAsset.getParentProject(readOnly); // Null //
     * tested // for // earlier
     * 
     * // Find the highest-level (Project Admin for Projects, Subject-Admin for
     * // Subjects, // currently Member (perhaps should be an admin) for other
     * objects) // project-specific // role names for each object type String
     * projectCID = dProject.getCiteableID(); String role = null; if
     * (type.equalsIgnoreCase(Project.TYPE)) { role =
     * Project.projectAdministratorRoleName(projectCID); } else if
     * (type.equalsIgnoreCase(Subject.TYPE)) { role =
     * Project.subjectAdministratorRoleName(projectCID); } else if
     * (type.equalsIgnoreCase(ExMethod.TYPE)) { role =
     * Project.memberRoleName(projectCID); } else if
     * (type.equalsIgnoreCase(Study.TYPE)) { role =
     * Project.memberRoleName(projectCID); } else if
     * (type.equalsIgnoreCase(DataSet.TYPE)) { role =
     * Project.memberRoleName(projectCID); } else if
     * (type.equalsIgnoreCase(DataObject.TYPE)) { role =
     * Project.memberRoleName(projectCID); } if (role == null) { return false; }
     * 
     * // If the user has the above role, they are authorized to edit the //
     * meta-data // for this object. So now see if the user holds this role on
     * the server // managing // the Project XmlDocMaker dm = new
     * XmlDocMaker("args"); dm.add("role", new String[] { "type", "role" },
     * role); XmlDoc.Element r =
     * executor.execute(dProject.getServerRouteObject(), "actor.self.have",
     * dm.root()); return r.booleanValue("role"); }
     */

    public static boolean isEditable(PSSDObject.Type type, String projectId,
            ModelUserRoleSet selfRoles) {

        if (selfRoles.hasProjectAdminRole(projectId)) {
            return true;
        } else if (selfRoles.hasSubjectAdminRole(projectId)) {
            if (type == Project.TYPE) {
                return false;
            } else {
                return true;
            }
        } else if (selfRoles.hasMemberRole(projectId)) {
            if (type == Project.TYPE || type == Subject.TYPE) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Add basic and required meta-data common to all PSSD objects
     * 
     * @param w
     * @param ae
     * @throws Throwable
     */
    public static void addPssdObject(XmlWriter w, XmlDoc.Element ae,
            Boolean userIsAdmin) throws Throwable {

        XmlDoc.Element poe = ae.element("meta/daris:pssd-object");
        String id = ae.value("cid");
        String rid = ae.value("rid");

        /*
         * Set proute. In a distributed query, this will be filled in even for a
         * local object. However, a simple asset.get (e.g.
         * om.pssd.object.describe) on a local object will not fill in proute
         * even in a federated session. I could fill it in with the local server
         * UUID...
         */
        String proute = ae.value("@proute");
        String name = poe.value("name");
        String description = poe.value("description");
        Boolean destroyed = ae.booleanValue("@destroyed", false);
        w.add("id",
                new String[] { "proute", proute, "asset",
                        ae.attribute("id").value(), "destroyed",
                        destroyed.toString(), "rid", rid },
                id);

        w.add("namespace", ae.value("namespace"));

        if (name != null) {
            w.add("name", name);
        }

        if (description != null) {
            w.add("description", description);
        }

        // daris:pssd-filename. May hold patient details so can be protected as
        // 'private'
        // which is equivalent to our use of namespaces 'pssd.private' in
        // domain-specific meta
        // All PSSD tree objects may have this element, but we have only
        // actually implemented
        // for DataSets
        XmlDoc.Element origFileName = ae.element("meta/daris:pssd-filename");
        if (origFileName != null) {
            Boolean pvt = origFileName.booleanValue("original/@private", false);
            if (pvt && userIsAdmin || !pvt) {
                w.add("filename", origFileName.value("original"));
            }
        }

        List<XmlDoc.Element> tes = ae.elements("tag");
        if (tes != null) {
            for (XmlDoc.Element te : tes) {
                // Includes only the tags from dictionary pssd.tags.xxx
                if (ObjectUtil.equals(te.value("name/@dictionary"),
                        ProjectSpecificTagDictionary.dictionaryFor(id)
                                .name())) {
                    w.add("tag", new String[] { "id", te.value("@id") },
                            te.value("name"));
                }
            }
        }
        XmlDoc.Element lock = ae.element("lock");
        if (lock != null) {
            w.add(lock, true);
        }
    }

    /**
     * Add meta-data specific to PSSD Project objects
     * 
     * @param executor
     * @param w
     * @param dAsset
     *            distributed citeable asset for Project objects
     * @param ae
     *            Holds the input meta-data as returned by asset.query
     * @param editable
     * @throws Throwable
     */
    private static void addPssdProject(ServiceExecutor executor, XmlWriter w,
            String projectId, DistributedAsset dAsset, XmlDoc.Element ae,
            boolean editable, ModelUserRoleSet selfRoles) throws Throwable {

        // Indicate if the user is allowed to create Subject objects for this
        // Project
        boolean screate;
        if (editable) {
            screate = true;
        } else {
            // See if user us allowed to create Subjects
            screate = selfRoles.hasSubjectAdminRole(projectId);
        }
        w.add("subject-create", screate);

        // Get the required meta-data for Project objects (Methods, Team members
        // etc)
        XmlDoc.Element pe = ae.element("meta/daris:pssd-project");
        if (pe == null) {
            return;
        }

        // Get, and expand (dereference), the methods that are registered with
        // this Project
        // Adds a "method" element
        Collection<XmlDoc.Element> methods = pe.elements("method");
        if (methods != null) {
            for (XmlDoc.Element me : methods) {

                String id = me.value("id");
                String notes = me.value("notes");

                // Dereference the Method.. This invokes a call to
                // the server that manages this Method
                addProjectMethod(executor, w, dAsset.getServerRouteObject(), id,
                        notes);
            }
        }

        // TODO:
        // Get the project team members and de-reference role members
        // Now that we have dropped the pssd-projec/member meta-data, we should
        // probably drop the presentation of member meta-data (done via roles)
        XmlDocMaker dm = new XmlDocMaker("args");
        String proute = dAsset.getServerRoute();
        dm.add("id", new String[] { "proute", proute }, dAsset.getCiteableID());
        dm.add("dereference", false);
        XmlDoc.Element r = executor.execute("om.pssd.project.members.list",
                dm.root());
        if (r != null) {

            // user members
            Collection<XmlDoc.Element> members = r.elements("member");
            if (members != null) {
                for (XmlDoc.Element me : members) {
                    w.add(me);
                }
            }

            // role members
            Collection<XmlDoc.Element> roleMembers = r.elements("role-member");
            if (roleMembers != null) {
                for (XmlDoc.Element me : roleMembers) {
                    w.add(me);
                }
            }
        }

        // Data-use
        String dataUse = pe.value("data-use");
        if (dataUse != null) {
            w.add("data-use", dataUse);
        }

    }

    /**
     * 
     * Add a PSSD Method object's meta-data. There is no federation impact on
     * this function
     * 
     * @param executor
     * @param w
     * @param sroute
     *            server route to Project object
     * @param methodId
     * @param methodNotes
     * @throws Throwable
     */
    private static void addProjectMethod(ServiceExecutor executor, XmlWriter w,
            ServerRoute sroute, String methodId, String methodNotes)
                    throws Throwable {

        /*
         * We require that the Method exists on the server that manages the
         * parent Project object. If it does not we will get an exception.
         */
        try {
            /*
             * I think it's wrong to have this information here. The addObject
             * function has already added the basic id/name layer and this
             * appears to be redundant. Probably too hard to eradicate at this
             * point.... [nebk; Jul2010]
             */
            XmlDoc.Element mae = Asset.getByCid(executor, sroute, methodId);
            String type = mae.value("meta/daris:pssd-object/type");
            assert Method.TYPE.equals(type);
            String methodName = mae.value("meta/daris:pssd-object/name");
            String methodDesc = mae.value("meta/daris:pssd-object/description");
            w.push("method", new String[] { "asset", mae.value("@id") });
            w.add("id", methodId);
            w.add("name", methodName);
            if (methodDesc != null) {
                w.add("description", methodDesc);
            }
            if (methodNotes != null) {
                w.add("notes", methodNotes);
            }
            w.pop();
        } catch (Throwable t) {
            // Fall back if we can't find the Method to de-reference it
            w.push("method");
            w.add("id", methodId);
            if (methodNotes != null) {
                w.add("notes", methodNotes);
            }
            w.pop();
        }
    }

    /**
     * Add meta-data specific to PSSD Subject objects
     * 
     * @param executor
     * @param dAsset
     *            Distributed citeable asset for a Subject
     * @param w
     * @param ae
     *            Holds the input meta-data as returned by asset.query
     * @param forEdit
     * @param showSubjectPrivate
     *            If true, show the "private" field meta-data on the Subject
     *            object regardless of the user's role
     * @param userUIsAdmin
     *            - user is system admin or project admin (project or subject
     *            admin)
     * @return If true then show the meta-data for this subject. If false, the
     *         data-use filtering dictates that the user can't see this subject
     *         at all.
     * @throws Throwable
     */
    public static boolean addPssdSubject(ServiceExecutor executor,
            ServerRoute projectRoute, DistributedAsset dSubject, XmlWriter w,
            XmlDoc.Element ae, boolean forEdit, boolean showSubjectPrivate,
            boolean userIsAdmin) throws Throwable {

        // Get all of the meta-data
        XmlDoc.Element me = ae.element("meta");
        if (me == null) {
            return true;
        }

        // Find the parent project so that we know the CID for project-specific
        // roles
        // and what server to check roles on. Because this class is only used in
        // a viewing/finding
        // context, we can look for primary first and then replica project
        // parents.
        Boolean readOnly = true;

        // Iterate through all of the "meta" elements and find the required
        // Subject-specific
        // meta-data.
        List<XmlDoc.Element> mes = me.elements();
        if (mes != null) {

            // First find all of the public meta-data (it's in ns=pssd.public)
            // and
            // add it to the "public" element
            boolean pushed = false;
            for (int i = 0; i < mes.size(); i++) {
                XmlDoc.Element se = mes.get(i);
                String ns = se.value("@ns");
                if (ns != null && ns.equals("pssd.public")) {
                    if (!pushed) {
                        w.push("public");
                        pushed = true;
                    }
                    w.add(se);
                }
            }

            if (pushed) {
                w.pop();
            }

            // Now find the private metadata and add it to the "private" element
            // User role may be over-ridden
            if (showSubjectPrivate || userIsAdmin) {
                pushed = false;
                for (int i = 0; i < mes.size(); i++) {
                    XmlDoc.Element se = (XmlDoc.Element) mes.get(i);
                    String ns = se.value("@ns");
                    if (ns != null && ns.equals("pssd.private")) {
                        if (!pushed) {
                            w.push("private");
                            pushed = true;
                        }
                        w.add(se);
                    }
                }
                if (pushed)
                    w.pop();
            }
        }

        // Is virtual subject?
        w.add("virtual",
                ae.booleanValue("meta/daris:pssd-subject/virtual", false));

        // Now add the Method (with which this Subject was created) to the
        // "method" element
        String method = ae.value("meta/daris:pssd-subject/method");
        if (method != null) {
            addProjectMethod(executor, w, projectRoute, method, null);
        }

        // If there is an RSubject, add it to the "r-subject" element
        String rsid = ae.value("meta/daris:pssd-subject/r-subject");
        if (rsid != null) {
            w.add("r-subject", rsid);
        }

        // If there is a data-use element, add it too
        // Data-use specification for this Subject
        String subjectDataUse = ae.value("meta/daris:pssd-subject/data-use");
        if (subjectDataUse != null) {
            w.add("data-use", subjectDataUse);
        }

        return true;

    }

    /**
     * Add meta-data specific to PSSD RSubject objects. There is federation
     * impact in this function.
     * 
     * @param executor
     * @param dAsset
     *            Distributed citeable asset of RSubject object
     * @param w
     * @param ae
     *            Holds the input meta-data as returned by asset.query
     * @param forEdit
     * @param showRSubjectIdentity
     *            If true, show the Identity meta-data on the RSubject object
     *            regardless of the user's role
     * @throws Throwable
     */
    public static void addPssdRSubject(ServiceExecutor executor,
            DistributedAsset dRSubject, XmlWriter w, XmlDoc.Element ae,
            boolean forEdit, boolean showRSubjectIdentity) throws Throwable {

        // Get all of the meta-data
        XmlDoc.Element me = ae.element("meta");
        if (me == null) {
            return;
        }

        // Iterate through elements
        List<XmlDoc.Element> mes = me.elements();
        String cid = dRSubject.getCiteableID();
        ServerRoute rSubjectRoute = dRSubject.getServerRouteObject();
        if (mes != null) {

            // TODO -- filter based on access permissions..
            // Find the public metadata..
            boolean pushed = false;

            // Generic PSSD R-Subject administrator (we don't use this role as
            // yet
            // as we administer R-Subjects per project). Validate the role
            // on the server that manages the RSubject
            boolean admin = ModelUser.hasRole(rSubjectRoute, executor,
                    Role.rSubjectAdminRoleName());

            if (!admin) {
                // R-Subject admin access for this specific RSubject
                // Generally this is given to the user who created the RSubject
                admin = ModelUser.hasRole(rSubjectRoute, executor,
                        RSubject.administratorRoleName(cid));
            }

            // Generic R-Subject PSSD guest (e.g. other Project.SUbject admin)
            boolean guest = ModelUser.hasRole(rSubjectRoute, executor,
                    Role.rSubjectGuestRoleName());

            if (!guest) {
                // Guest access for this specific R-Subject
                guest = ModelUser.hasRole(rSubjectRoute, executor,
                        RSubject.guestRoleName(cid));
            }

            // SHow the R-Subject Identity information
            // Possibly the R-Subject guest should not be able to see this
            if (showRSubjectIdentity || admin || guest) {
                for (int i = 0; i < mes.size(); i++) {
                    XmlDoc.Element se = mes.get(i);
                    String ns = se.value("@ns");
                    if (ns != null && ns.equals("pssd.identity")) {
                        if (!pushed) {
                            w.push("identity");
                            pushed = true;
                        }

                        w.add(se);
                    }
                }

                if (pushed) {
                    w.pop();
                }
            }

            // Find the public metadata..
            pushed = false;

            if (admin || guest) {
                for (int i = 0; i < mes.size(); i++) {
                    XmlDoc.Element se = (XmlDoc.Element) mes.get(i);
                    String ns = se.value("@ns");
                    if (ns != null && ns.equals("pssd.public")) {
                        if (!pushed) {
                            w.push("public");
                            pushed = true;
                        }

                        w.add(se);
                    }
                }

                if (pushed) {
                    w.pop();
                }
            }

            // Find the private metadata..
            pushed = false;

            if (admin) {
                for (int i = 0; i < mes.size(); i++) {
                    XmlDoc.Element se = (XmlDoc.Element) mes.get(i);
                    String ns = se.value("@ns");
                    if (ns != null && ns.equals("pssd.private")) {
                        if (!pushed) {
                            w.push("private");
                            pushed = true;
                        }

                        w.add(se);
                    }
                }

                if (pushed) {
                    w.pop();
                }
            }
        }

        // The Method must be managed by the same server as the parent Project
        String method = ae.value("meta/daris:pssd-subject/method");
        if (method != null) {
            Boolean readOnly = true;
            DistributedAsset dProject = dRSubject.getParentProject(readOnly);
            addProjectMethod(executor, w, dProject.getServerRouteObject(),
                    method, null);
        }

        XmlDoc.Element se = ae.element("meta/daris:pssd-state");
        if (se != null) {
            w.push("states");
            w.add(se, false);
            w.pop();
        }
    }

    /**
     * Add the ExMethod meta-data. There is no federation impact in this
     * function
     * 
     * @param executor
     * @param w
     * @param ae
     *            Meta-data for this asset
     * @param forEdit
     * @throws Throwable
     */
    public static void addPssdExMethod(ServiceExecutor executor, XmlWriter w,
            XmlDoc.Element ae, boolean forEdit) throws Throwable {

        ExMethod em = new ExMethod();
        em.parseAssetMeta(ae);

        Method m = em.method();

        w.push("method");
        w.add("id", m.id());
        w.add("name", m.name());
        if (m.description() != null) {
            w.add("description", m.description());
        }

        if (m.authors() != null) {
            w.add("author", m.authors());
        }

        m.saveSteps(w);

        w.pop();
        // This status appears to be the overall state of he ExMethod
        // and appears to always be INCOMPLETE
        w.add("state", em.status());
        em.saveSteps(w);

        // w.pop();

        /*
         * String method = ae.value("meta/daris:pssd-ex-method/method"); if (
         * method != null ) { addMethod(executor,w,method,null); }
         * 
         * XmlDoc.Element em = ae.element("meta/daris:pssd-ex-method"); if ( em
         * != null ) { w.add(em,false); }
         */
    }

    public static void addPssdMethod(ServiceExecutor executor, XmlWriter w,
            XmlDoc.Element ae) throws Throwable {

        Method.describe(executor, w, ae, false);
    }

    /**
     * Add a PSSD STudy object's meta-data. There is no federation impact in
     * this function.
     * 
     * @param w
     * @param ae
     * @param forEdit
     * @throws Throwable
     */
    public static void addPssdStudy(XmlWriter w, XmlDoc.Element ae,
            boolean forEdit) throws Throwable {

        XmlDoc.Element se = ae.element("meta/daris:pssd-study");
        if (se == null) {
            return;
        }

        w.add("type", se.value("type"));

        // We don't want to show if not set.
        XmlDoc.Element pc = se.element("processed");
        if (pc != null) {
            w.add("processed", pc.booleanValue());
        }

        /*
         * XmlDoc.Element we = se.element("workflow"); if ( we != null ) {
         * w.add(we); }
         */

        XmlDoc.Element me = se.element("method");
        if (me != null) {

            w.push("method");
            w.add("id", me.value());
            w.add("step", me.value("@step"));

            me = ae.element("meta");
            if (me != null) {

                // Filter out metadata that applies only to the method.
                String id = ae.value("cid");
                String mid = nig.mf.pssd.CiteableIdUtil.getParentId(id);
                String mns = mid + "_";

                boolean pushed = false;

                List<XmlDoc.Element> mes = me.elements();
                if (mes != null) {
                    // TODO -- filter based on access permissions..
                    // Find the public metadata..
                    for (int i = 0; i < mes.size(); i++) {
                        XmlDoc.Element sme = (XmlDoc.Element) mes.get(i);
                        String ns = sme.value("@ns");
                        if (ns != null && ns.startsWith(mns)) {

                            if (!pushed) {
                                w.push("meta");
                                pushed = true;
                            }

                            w.add(sme);
                        }
                    }

                    if (pushed) {
                        w.pop();
                    }

                }

            }

            w.pop();
        }

    }

    /**
     * Add a PSSD DataSet object's meta-data. There is no federation impact on
     * this function
     * 
     * @param w
     * @param ae
     * @throws Throwable
     */
    public static void addPssdDataSet(XmlWriter w, XmlDoc.Element ae)
            throws Throwable {

        XmlDoc.Element se = ae.element("meta/daris:pssd-dataset");
        if (se == null) {
            return;
        }

        w.push("source");
        w.add("type", se.value("type"));
        w.pop();

        addPssdValueId(w, ae);

        addPssdDataSetAcquisition(w, ae);
        addPssdDataSetDerivation(w, ae);
        addPssdDataSetTransform(w, ae);

        String mimeType = ae.value("type");
        if (mimeType != null) {
            w.add("type", mimeType);
        }

        addPssdDataSetContent(w, ae);
    }

    public static void addPssdValueId(XmlWriter w, XmlDoc.Element ae)
            throws Throwable {

        String vid = ae.value("content/@stime");
        if (vid == null) {
            vid = ae.value("meta/@stime");
        }

        if (vid == null) {
            vid = ae.value("stime");
        }

        w.add("vid", vid);
    }

    public static void addPssdDataSetAcquisition(XmlWriter w, XmlDoc.Element ae)
            throws Throwable {

        XmlDoc.Element pae = ae.element("meta/daris:pssd-acquisition");

        if (pae == null) {
            return;
        }

        w.push("acquisition");

        XmlDoc.Element se = pae.element("subject");

        if (se != null) {
            w.push("subject");
            w.add("id", se.value());
            w.add("state", se.value("@state"));
            w.pop();
        }

        XmlDoc.Element me = pae.element("method");
        if (me != null) {
            w.push("method");
            w.add("id", me.value());
            w.add("step", me.value("@step"));
            w.pop();
        }

        w.pop();

    }

    public static void addPssdDataSetDerivation(XmlWriter w, XmlDoc.Element ae)
            throws Throwable {

        XmlDoc.Element pde = ae.element("meta/daris:pssd-derivation");
        if (pde == null) {
            return;
        }

        w.push("derivation");

        if (pde.value("processed") != null) {
            w.add("processed", pde.value("processed"));
        }

        if (pde.value("anonymized") != null) {
            w.add("anonymized", pde.value("anonymized"));
        }
        XmlDoc.Element ie = pde.element("input");
        if (ie != null) {
            w.add("input", new String[] { "vid", ie.value("@vid") },
                    ie.value());
        }

        XmlDoc.Element me = pde.element("method");
        if (me != null) {
            w.push("method");
            w.add("id", me.value());
            w.add("step", me.value("@step"));
            w.pop();
        }

        w.pop();
    }

    private static void addPssdDataSetTransform(XmlWriter w, XmlDoc.Element ae)
            throws Throwable {

        XmlDoc.Element te = ae.element("meta/daris:pssd-transform");
        if (te == null) {
            return;
        }
        if (te.element("mid") != null || te.element("tuid") != null
                || te.element("software") != null) {
            w.push("transform");
            w.add(te, false);
            w.pop();
        }
    }

    public static void addPssdDataSetContent(XmlWriter w, XmlDoc.Element ae)
            throws Throwable {

        XmlDoc.Element ce = ae.element("content");
        if (ce == null) {
            return;
        }

        w.push("data", new String[] { "id", ce.value("@id"), "stime",
                ce.value("@stime"), "version", ce.value("@version") });
        w.add(ce, false);
        w.pop();
    }

    /**
     * Add a PSSD DataObject's object's meta-data. There is no federation impact
     * on this function
     * 
     * @param w
     * @param ae
     * @throws Throwable
     */
    public static void addPssdDataObject(XmlWriter w, XmlDoc.Element ae)
            throws Throwable {

        addPssdDataSetContent(w, ae);
    }

    public static void addPssdMeta(XmlWriter w, XmlDoc.Element ae,
            PSSDObject.Type type) throws Throwable {

        XmlDoc.Element meta = ae.element("meta");
        if (meta == null) {
            return;
        }

        Collection<XmlDoc.Element> mes = meta.elements();
        if (mes == null) {
            return;
        }

        boolean pushMeta = true;

        for (XmlDoc.Element me : mes) {
            if (isPssdObjectMeta(me, type) || isInWhiteList(me)) {
                if (pushMeta) {
                    w.push("meta");
                    pushMeta = false;
                }

                w.add(me);
            }
        }

        if (!pushMeta) {
            w.pop();
        }
    }

    public static void addPssdAttachments(XmlWriter w, XmlDoc.Element ae)
            throws Throwable {

        Collection<XmlDoc.Element> res = ae
                .elements("related[@type='attachment']/asset");
        if (res == null) {
            return;
        }

        for (XmlDoc.Element re : res) {
            String id = re.value("@id");
            w.push("attachment", new String[] { "id", id });
            w.pop();
        }
    }

    /**
     * 
     * @param me
     * @param type
     * @return
     * @throws Throwable
     */
    public static boolean isPssdObjectMeta(XmlDoc.Element me,
            PSSDObject.Type type) throws Throwable {

        String tag = me.value("@tag");
        if (tag != null) {
            if (tag.equalsIgnoreCase("pssd.meta")) {
                return true;
            }
        }

        String ns = me.value("@ns");
        if (ns == null) {
            return false;
        }

        if (ns.equalsIgnoreCase(Metadata.modelNameForType(type))) {
            return true;
        }

        return false;
    }
    
    public static boolean isInWhiteList (XmlDoc.Element me) throws Throwable {
    	String type = me.name();
    	int n = DocTypeWhiteList.length;
    	for (int i=0;i<n;i++) {
    		if (type.equalsIgnoreCase(DocTypeWhiteList[i])) return true;
    	}
    	return false;
    }

    public static void addPssdProject(ServiceExecutor executor, String proute,
            XmlWriter w, XmlDoc.Element ae, String projectId,
            ModelUserRoleSet selfRoles, MethodSet methods, ProjectMemberMap pmm)
                    throws Throwable {

        /*
         * Indicate if the user is allowed to create Subject objects for this
         * project
         */
        w.add("subject-create", selfRoles.hasSubjectAdminRole(projectId));

        /*
         * Get the required meta-data for Project objects (Methods, Team members
         * etc)
         */
        XmlDoc.Element pe = ae.element("meta/daris:pssd-project");
        if (pe == null) {
            return;
        }

        /*
         * Get, and expand (dereference), the methods that are registered with
         * this project. Adds a "method" element
         */
        Collection<XmlDoc.Element> mes = pe.elements("method");
        if (mes != null) {
            for (XmlDoc.Element me : mes) {
                String methodId = me.value("id");
                String methodNotes = me.value("notes");
                String methodName = null;
                String methodVersion = null;
                String methodDesc = null;
                if (methods != null) {
                    Method m = methods.method(methodId);
                    if (m != null) {
                        methodName = m.name();
                        methodVersion = m.version();
                        methodDesc = m.description();
                    }
                }
                w.push("method");
                w.add("id", methodId);
                if (methodVersion != null) {
                    w.add("version", methodVersion);
                }
                if (methodName != null) {
                    w.add("name", methodName);
                }
                if (methodDesc != null) {
                    w.add("description", methodDesc);
                }
                if (methodNotes != null) {
                    w.add("notes", methodNotes);
                }
                w.pop();
            }
        }

        /*
         * Get the project team members and de-reference role members Now that
         * we have dropped the daris:pssd-project/member meta-data, we should
         * probably drop the presentation of member meta-data (done via roles)
         */
        pmm.describeMembers(w, projectId);

        /*
         * Data-use
         */
        String dataUse = pe.value("data-use");
        if (dataUse != null) {
            w.add("data-use", dataUse);
        }

    }

}
