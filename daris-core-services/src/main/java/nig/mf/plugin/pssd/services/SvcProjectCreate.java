package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.mf.plugin.event.SystemEventChannel;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.plugin.pssd.Application;
import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.PSSDObjectEvent;
import nig.mf.plugin.pssd.PSSDObjectEvent.Action;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.ProjectMember;
import nig.mf.plugin.pssd.method.Method;
import nig.mf.plugin.pssd.user.UserCredential;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.Role;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;

public class SvcProjectCreate extends PluginService {
    private Interface _defn;
    private static final ReentrantLock lock_ = new ReentrantLock();

    public SvcProjectCreate() {
        _defn = new Interface();
        _defn.add(new Element("cid-root-name", StringType.DEFAULT,
                "Specify the named citable ID root for the collection. Defaults to 'pssd.project'. Using other named roots allows projects to be created in a CID sandbox, perhaps for testing.",
                0, 1));
        _defn.add(new Interface.Element("project-number",
                IntegerType.POSITIVE_ONE,
                "Specifies the project number (under the local server's project CID root) for the identifier. If specified, then there cannot be any other asset/object with this identity assigned. Use with extreme caution as you must be certain the CID exists nowhere else.",
                0, 1));
        _defn.add(new Element("fillin", BooleanType.DEFAULT,
                "If the project-number is not given, fill in the Project allocator space (re-use allocated CIDs with no assets), otherwise create the next available CID at the end of the CID pool. Use with extreme caution.  Defaults to false. Concurrency issues mean that this argument may be ignored if many Projects are being created simultaneously.",
                0, 1));
        addInterfaceDefn(_defn);
        _defn.add(new Element("self-admin", BooleanType.DEFAULT,
                "Give the caller project-administrator access as well (regardless of whether specified in 'member' or not).  Defaults to true.",
                0, 1));
    }

    public static void addInterfaceDefn(Interface defn) {
        defn.add(new Interface.Element("namespace", StringType.DEFAULT,
                "The namespace in which to create this project (must pre-exist). Defaults to 'pssd'.",
                0, 1));
        defn.add(new Interface.Element("name", StringType.DEFAULT,
                "The name of this project.", 0, 1));
        defn.add(new Interface.Element("description", StringType.DEFAULT,
                "An arbitrary description for the project.", 0, 1));

        // NB: "method", "member", "role-member"and "data-use" must all match
        // elements in the daris:pssd-project document type.
        // Method meta-data
        Interface.Element me = new Interface.Element("method",
                XmlDocType.DEFAULT,
                "Method utilized by this project.  In a federation must be managed by the local server.",
                0, Integer.MAX_VALUE);
        me.add(new Interface.Element("id", CiteableIdType.DEFAULT,
                "The identity of the method (must be local to this server).", 1,
                1));
        me.add(new Interface.Element("notes", StringType.DEFAULT,
                "Arbitrary notes associated with the use of this method.", 0,
                1));
        defn.add(me);

        // Project team user member
        me = new Interface.Element("member", XmlDocType.DEFAULT,
                "User to become a member of this project. In a federation must be local to this server.",
                0, Integer.MAX_VALUE);
        //
        Interface.Element ie = new Interface.Element("authority",
                StringType.DEFAULT,
                "The authority of interest. Defaults to local.", 0, 1);
        ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
                "The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.",
                0));
        me.add(ie);
        //
        me.add(new Interface.Element("domain", StringType.DEFAULT,
                "The domain name of the member.", 1, 1));
        me.add(new Interface.Element("user", StringType.DEFAULT,
                "The user name within the domain.", 1, 1));
        me.add(new Interface.Element("role",
                new EnumType(new String[] { Project.ADMINISTRATOR_ROLE_NAME,
                        Project.SUBJECT_ADMINISTRATOR_ROLE_NAME,
                        Project.MEMBER_ROLE_NAME, Project.GUEST_ROLE_NAME }),
                "The project-team role bestowed on the user member.", 1, 1));
        me.add(new Interface.Element("data-use",
                new EnumType(new String[] { Project.CONSENT_SPECIFIC_ROLE_NAME,
                        Project.CONSENT_EXTENDED_ROLE_NAME,
                        Project.CONSENT_UNSPECIFIED_ROLE_NAME }),
                "Specify how this member (only if role is 'member' or 'guest') will use data from this project (defaults to 'specific')",
                0, 1));
        defn.add(me);

        // Project team role member
        me = new Interface.Element("role-member", XmlDocType.DEFAULT,
                "Role to become a member of this project. In a federation must be local to this server.",
                0, Integer.MAX_VALUE);
        me.add(new Interface.Element("member", StringType.DEFAULT,
                "The role to become a member of the Project.", 0, 1));
        me.add(new Interface.Element("role",
                new EnumType(new String[] { Project.ADMINISTRATOR_ROLE_NAME,
                        Project.SUBJECT_ADMINISTRATOR_ROLE_NAME,
                        Project.MEMBER_ROLE_NAME, Project.GUEST_ROLE_NAME }),
                "The project-team role bestowed on the role member.", 1, 1));
        me.add(new Interface.Element("data-use",
                new EnumType(new String[] { Project.CONSENT_SPECIFIC_ROLE_NAME,
                        Project.CONSENT_EXTENDED_ROLE_NAME,
                        Project.CONSENT_UNSPECIFIED_ROLE_NAME }),
                "Specify how this member (only if role is 'member' or 'guest') will use data from this project (defaults to 'specific')",
                0, 1));
        defn.add(me);

        // Project data-use
        me = new Interface.Element("data-use",
                new EnumType(new String[] { Project.CONSENT_SPECIFIC_ROLE_NAME,
                        Project.CONSENT_EXTENDED_ROLE_NAME,
                        Project.CONSENT_UNSPECIFIED_ROLE_NAME }),
                "Specifies the type of consent for the use of data for this project: 1) 'specific' means use the data only for the original specific intent, 2) 'extended' means use the data for related projects and 3) 'unspecified' means use the data for any research",
                1, 1);
        defn.add(me);

        defn.add(new Interface.Element("allow-incomplete-meta",
                BooleanType.DEFAULT,
                "Should the metadata be accepted if incomplete? Defaults to false.",
                0, 1));

        //
        me = new Interface.Element("meta", XmlDocType.DEFAULT,
                "Optional metadata - a list of asset documents.", 0, 1);
        me.setIgnoreDescendants(true);
        defn.add(me);

    }

    public String name() {
        return "om.pssd.project.create";
    }

    public String description() {
        return "Creates a PSSD Project on the local server.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out,
            XmlWriter w) throws Throwable {

        // Projects are always created on the local server
        String proute = null;

        // Get the citable root name. The standard is 'pssd.project' and we
        // allow that to be auto-created. However,
        // we want to control other ones a little more and require that they be
        // explicitly created first.
        // Otherwise we could end up with a big mess of uncontrolled roots
        String cidRootName = args.stringValue("cid-root-name", "pssd.project");
        if (!cidRootName.equals("pssd.project")) {
            XmlDoc.Element r = executor().execute("citeable.named.id.describe");
            String t = r.value("id[@name='" + cidRootName + "']");
            if (t == null) {
                throw new Exception("The citable ID root '" + cidRootName
                        + "' does not exist. Please create first with citeable.named.id.create");
            }
        }
        String projectRoot = nig.mf.pssd.plugin.util.CiteableIdUtil
                .citeableIDRoot(executor(), proute, cidRootName);

        // Creator must have project creation role..
        ModelUser.checkHasRole(proute == null ? null : new ServerRoute(proute),
                executor(), Role.projectCreatorRoleName());

        // Validate inputs before we start creating roles and assets...
        // Exception if no good
        checkMembersExist(args);
        checkMethodsLocal(executor(), args);

        // If the user does not give project-number, we may want to fill in
        // any holes in the allocator space for Projects as sometimes we use
        // large numbers for 'service' activities.
        boolean fillIn = args.booleanValue("fillin", false);
        long projectNumber = args.longValue("project-number", -1);
        boolean selfAdmin = args.booleanValue("self-admin", true);

        // Generate CID, filling in local allocator space if desired
        // We set the distation (when allocating the next CID) to null as we
        // only want to be able to create Projects under the local root.
        String pdist = null;

        if (fillIn) {
            // Concurrent threads may find the same naked CID and attempt to
            // create multiple Projects
            // with the same CID. Therefore we lock the process. But if after
            // 1s, we are still waiting,
            // just give up the fillin request and proceed to allocate the next
            // CID
            if (!lock_.tryLock(1L, TimeUnit.SECONDS))
                fillIn = false;
        }

        try {
            String cid = nig.mf.pssd.plugin.util.CiteableIdUtil
                    .generateCiteableID(executor(), projectRoot, pdist,
                            projectNumber, fillIn);
            // Create the team-member and the data re-use (consent) roles
            // locally
            String selfMemberRole = null;
            

            // Create the project and grant the Project team their Project roles
            try {
            	
            	// create project specific roles
                Project.createProjectRoles(executor(), cid);

                // create project specific dictionary namespace
                Project.createProjectSpecificDictionaryNamespace(executor(), cid);
                Project.grantProjectSpecificDictionaryNamespacePermissions(
                        executor(), cid);
                
                selfMemberRole = createProjectAsset(executor(), args, cid);

            } catch (Throwable t) {
                // Cleanup project and roles and rethrow
                cleanUp(executor(), cid);
                Project.destroyProjectSpecificDictionaryNamespace(executor(),
                        cid);
                throw t;
            }

            // Fix up 'self-admin' role (admin rights granted to caller in
            // createProjectAsset to faciliate
            // trigger scripts access to Project asset)
            String adminRole = Project.projectAdministratorRoleName(cid);
            if (selfAdmin) {
                // Revoke any lesser than project-admin team role that caller
                // holds leaving just the admin role behind
                if (selfMemberRole != null
                        && !selfMemberRole.equals(adminRole)) {
                    PSSDUtils.revokeRoleToSelf(executor(), selfMemberRole);
                }
            } else {
                // Revoke admin role as appropriate
                if (selfMemberRole != null) {
                    if (!selfMemberRole.equals(adminRole)) {
                        // Caller is a lesser than admin team member
                        PSSDUtils.revokeRoleToSelf(executor(), adminRole);
                    }
                } else {
                    // Caller is not a team member
                    PSSDUtils.revokeRoleToSelf(executor(), adminRole);
                }
            }

            // Generate system event
            SystemEventChannel
                    .generate(new PSSDObjectEvent(Action.CREATE, cid, 0));
            w.add("id", cid);
        } finally {
            if (fillIn) {
                lock_.unlock();
            }
        }
    }

    /**
     * Checks all of the specified project members already exist.
     * 
     * @param args
     * @throws Throwable
     */
    private void checkMembersExist(XmlDoc.Element args) throws Throwable {

        // Check user members first
        {
            Collection<XmlDoc.Element> members = args.elements("member");

            if (members != null) {
                for (XmlDoc.Element me : members) {

                    // Authority will be absent for local accounts and present
                    // for accounts from other authorities. In the PSSD
                    // implementation
                    // the local server will require accounts from other
                    // authorities
                    // so we can always check locally.
                    XmlDoc.Element authority = me.element("authority");
                    String domain = me.value("domain");
                    String user = me.value("user");

                    XmlDocMaker dm = new XmlDocMaker("args");
                    if (authority != null)
                        dm.add(authority);
                    dm.add("domain", domain);
                    dm.add("user", user);

                    // user.exists won't find LDAP users
                    XmlDoc.Element r = executor()
                            .execute("authentication.user.exists", dm.root());
                    if (!r.booleanValue("exists")) {
                        throw new Exception(
                                "Cannot add project member: the domain ("
                                        + domain + ") and/or user (" + user
                                        + ") does not exist.");
                    }
                }
            }
        }

        // Now check the role members
        {
            Collection<XmlDoc.Element> members = args.elements("role-member");

            if (members != null) {
                for (XmlDoc.Element me : members) {
                    String member = me.value("member");
                    XmlDocMaker dm = new XmlDocMaker("args");
                    dm.add("role", member);
                    XmlDoc.Element r = executor()
                            .execute("authorization.role.exists", dm.root());
                    if (!r.booleanValue("exists")) {
                        throw new Exception(
                                "Cannot add project member-role: the member-role ("
                                        + member + ") does not exist.");
                    }
                }
            }
        }
    }

    /**
     * Checks that the specified Methods are managed (i.e. they are primary) by
     * the local (executing) server (where we are creating the Project).
     * 
     * @param args
     * @throws Throwable
     */
    private void checkMethodsLocal(ServiceExecutor executor,
            XmlDoc.Element args) throws Throwable {

        Collection<XmlDoc.Element> methods = args.elements("method");
        if (methods != null) {
            for (XmlDoc.Element me : methods) {
                String id = me.value("id");
                Method.isMethodLocal(executor, id); // Exception if not
            }
        }
    }

    public static String createProjectAsset(ServiceExecutor executor,
            XmlDoc.Element args, String cid) throws Throwable {
        String selfMemberRole = null;

        // Namespace must pre-exist. This is because it needs to be associated
        // with a store
        // and with auto-creation, assets many end up in the data base
        String parentNS = args.stringValue("namespace",
                Application.defaultProjectNamespace(executor));
        if (!executor.execute("asset.namespace.exists",
                "<args><namespace>" + parentNS + "</namespace></args>", null,
                null).booleanValue("exists", false)) {
            throw new Exception("The given namespace '" + parentNS
                    + "' does not exist.  Please contact the DaRIS administrator to create and associate with a Mediaflux store");
        }

        String projectNS = parentNS + "/" + cid;

        XmlDocMaker dm = new XmlDocMaker("args");
        if (cid != null) {
            dm.add("cid", cid);
            dm.add("name", "project " + cid);
            dm.add("namespace", new String[] { "create", "true" }, projectNS);
        } else {
            dm.add("namespace", parentNS);
        }

        dm.add("model", Project.MODEL);

        dm.add("allow-incomplete-meta",
                args.booleanValue("allow-incomplete-meta", false));

        dm.push("meta");

        // Set the standard PSSD object meta-data (matches Doc Type
        // "daris:pssd-object")
        PSSDUtils.setObjectMeta(dm, Project.TYPE, args.value("name"),
                args.value("description"), true);

        // Set the generic meta-data
        PSSDUtils.setObjectOptionalMeta(dm, args.element("meta"),
                "om.pssd.project");

        // Get the project 'data-use' element that has been set. We use this to
        // check the validity of the member 'data-use' values
        String projectDataUse = args.value("data-use"); // Required element

        // Fetch the 'method' meta-data from the user
        Collection<XmlDoc.Element> methods = args.elements("method");

        // Fetch the member meta-data from the user
        Collection<XmlDoc.Element> members = args.elements("member");
        Collection<XmlDoc.Element> roleMembers = args.elements("role-member");

        if (methods != null || members != null || roleMembers != null) {
            dm.push("daris:pssd-project");

            // Prepare method meta-data for Project
            if (methods != null) {
                for (XmlDoc.Element me : methods) {
                    // Check Method exists
                    String id = me.value("id");
                    String proute = null;
                    String pdist = "0";
                    Boolean pssdOnly = true;
                    if (!DistributedAssetUtil.assetExists(executor, proute,
                            pdist, id, ResultAssetType.primary, false, pssdOnly,
                            null)) {
                        throw new Exception("The Method with cid '" + id
                                + "' does not exist");
                    }
                    dm.add(me);
                }
            }

            // Grant project roles to user members
            if (members != null) {

                // Describe the caller
                UserCredential selfUserCred = makeSelfCredential(executor);

                for (XmlDoc.Element me : members) {
                    UserCredential userCred = new UserCredential(
                            me.value("authority"),
                            me.value("authority/@protocol"), me.value("domain"),
                            me.value("user"));

                    // Find callers project-specific role, if any
                    if (userCred.equals(selfUserCred)) {
                        String t = me.value("role");
                        selfMemberRole = Project.setSpecificRoleName(t, cid);
                    }

                    // Set defaults or over-rides for team-member's 'data-use'.
                    ProjectMember.setValidProjectMemberDataUse(projectDataUse,
                            me);

                    // Grant the hierarchical team role to the user-member
                    Project.grantProjectRole(executor, cid, userCred,
                            me.value("role"), false);
                            // We want an error if the role has not been created
                            // not

                    // Grant data-use role to non admin user-members
                    // The "data-use" field will be null for admins.
                    Project.grantProjectRole(executor, cid, userCred,
                            me.value("data-use"), false);
                }
            }

            // Grant project roles to role members
            if (roleMembers != null) {
                for (XmlDoc.Element me : roleMembers) {

                    // Set defaults or over-rides for team-member's 'data-use'.
                    ProjectMember.setValidProjectMemberDataUse(projectDataUse,
                            me);

                    // Grant the hierarchical team role to the role-member
                    Project.grantProjectRole(executor, cid, me.value("member"),
                            me.value("role"), false);

                    // Grant data-use role to non p-admin role-members
                    Project.grantProjectRole(executor, cid, me.value("member"),
                            me.value("data-use"), false);
                }
            }

            if (projectDataUse != null) {
                dm.add("data-use", projectDataUse);
            }

            dm.pop();
        }
        dm.pop();

        // Grant access to 'self' to the project we just created.
        // Any trigger scripts that run when the asset is created, will need
        // access
        // to the newly created project.
        PSSDUtils.grantRoleToSelf(executor,
                Project.projectAdministratorRoleName(cid));

        // Add ACLs to the Project roles
        PSSDUtils.addProjectACLs(dm, cid);

        // Finally create the Project
        executor.execute("asset.create", dm.root());

        setProjectNamespaceACLs(executor, projectNS, cid);
        //
        return selfMemberRole;
    }

    private static void setProjectNamespaceACLs(ServiceExecutor executor,
            String projectNS, String projectCid) throws Throwable {

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("namespace", projectNS);

        // pssd.object.admin
        addProjectNamespaceACL(dm, Role.objectAdminRoleName(),
                new String[] { "administer", "access", "create", "execute",
                        "destroy" },
                new String[] { "access", "create", "modify", "destroy",
                        "licence" },
                new String[] { "access", "modify" });

        // pssd.object.guest
        addProjectNamespaceACL(dm, Role.objectGuestRoleName(),
                new String[] { "access" }, new String[] { "none" },
                new String[] { "none" });

        // pssd.project.admin
        addProjectNamespaceACL(dm,
                Project.projectAdministratorRoleName(projectCid),
                new String[] { "administer", "access", "create", "execute",
                        "destroy" },
                new String[] { "access", "create", "modify", "destroy",
                        "licence" },
                new String[] { "access", "modify" });

        // pssd.project.subject.admin
        addProjectNamespaceACL(dm,
                Project.subjectAdministratorRoleName(projectCid),
                new String[] { "access", "create", "execute", "destroy" },
                new String[] { "access", "create", "modify", "destroy",
                        "licence" },
                new String[] { "access", "modify" });

        // pssd.project.member
        addProjectNamespaceACL(dm, Project.memberRoleName(projectCid),
                new String[] { "access", "create", "execute", "destroy" },
                new String[] { "access", "create", "modify", "destroy",
                        "licence" },
                new String[] { "access", "modify" });

        // pssd.project.guest
        addProjectNamespaceACL(dm, Project.guestRoleName(projectCid),
                new String[] { "access" }, new String[] { "access" },
                new String[] { "access" });
        executor.execute("asset.namespace.acl.set", dm.root());
    }

    private static void addProjectNamespaceACL(XmlDocMaker dm, String role,
            String[] namespaceAccesses, String[] assetAccesses,
            String[] assetContentAccesses) throws Throwable {
        dm.push("acl");
        dm.add("actor", new String[] { "type", "role" }, role);
        dm.push("access");
        for (String namespaceAccess : namespaceAccesses) {
            dm.add("namespace", namespaceAccess);
        }
        for (String assetAccess : assetAccesses) {
            dm.add("asset", assetAccess);
        }
        for (String assetContentAccess : assetContentAccesses) {
            dm.add("asset-content", assetContentAccess);
        }
        dm.pop();
        dm.pop();
    }

    /**
     * FInd out who the caller is. Bit messy because you have to piece together
     * from actor.describe and domain description
     * 
     * @param executor
     * @return
     * @throws Throwable
     */
    static UserCredential makeSelfCredential(ServiceExecutor executor)
            throws Throwable {

        // Get domain:user
        XmlDoc.Element r = executor.execute("actor.self.describe");
        String name = r.value("actor/@name");
        if (name == null)
            return null;
        String[] parts = name.split(":");
        int nParts = parts.length;
        if (nParts < 2)
            return null;
        String user = parts[nParts - 1];
        String domain = parts[nParts - 2];

        // Now get authority/protocol
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("domain", domain);
        r = executor.execute("authentication.domain.describe", dm.root());
        String authority = r.value("domain/@authority");
        String protocol = r.value("domain/@protocol");
        //
        return new UserCredential(authority, protocol, domain, user);
    }

    /**
     * Cleanup after failure
     * 
     * @param executor
     * @param cid
     * @throws Throwable
     */
    private void cleanUp(ServiceExecutor executor, String cid)
            throws Throwable {

        // Destroy the asset if it exists. This service will destroy any
        // project associated roles too. Else just destroy the roles.
        // There is no need to revoke roles as well

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", cid);
        XmlDoc.Element r = executor.execute("om.pssd.object.exists", dm.root());
        if (r.booleanValue("exists")) {
            dm = new XmlDocMaker("args");
            dm.add("cid", cid);
            executor.execute("om.pssd.object.destroy", dm.root());
        } else {
            Project.destroyRoles(executor, cid);
        }
    }

}
