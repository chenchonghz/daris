package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.List;

import nig.mf.plugin.pssd.Asset;
import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Study;
import nig.mf.plugin.pssd.dicom.DicomElements;
import nig.mf.plugin.pssd.method.Method;
import nig.mf.pssd.Role;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Attribute;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.DictionaryEnumType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcMethodCreate extends PluginService {
    private Interface _defn;

    public SvcMethodCreate() throws Throwable {

        _defn = new Interface();

        addCreateInterface(_defn);
        addInterface(_defn);
    }

    public static Interface.Element metadataInterfaceDefn(String desc, Boolean addGroup) throws Throwable {

        Interface.Element mde = new Interface.Element("metadata", XmlDocType.DEFAULT, desc, 0, Integer.MAX_VALUE);
        Interface.Element de = new Interface.Element("definition", StringType.DEFAULT,
                "The name of a metadata type that must/can be applied.", 1, 1);
        de.add(new Interface.Attribute("requirement", new EnumType(new String[] { "mandatory", "optional" }),
                "Defines whether the metadata is mandatory or optional. Defaults to mandatory.", 0));
        if (addGroup)
            de.add(new Interface.Attribute(
                    "group",
                    StringType.DEFAULT,
                    "Specifies a logical grouping of the document type.  The portal uses this to group in common TABs.",
                    0));
        mde.add(de);

        Interface.Element ve = new Interface.Element(
                "value",
                XmlDocType.DEFAULT,
                "The value of fragments of the definition. These are sub-elements that would be supplied to the document fragment when creating an asset. Constant values are specified by enclosing the value in the function 'constant()'. E.g. 'constant(123)'. Where 'constant' is not specified, the value is considered the default.",
                0, 1);
        ve.setIgnoreDescendants(true);
        mde.add(ve);

        return mde;
    }

    // Re-usable creation interface
    public static void addCreateInterface(Interface defn) throws Throwable {
        defn.add(new Element(
                "cid-root-name",
                StringType.DEFAULT,
                "Specify the named citable ID root for the collection. Defaults to 'pssd.method'. Using other named roots allows Methods to be created in a CID sandbox, perhaps for testing.",
                0, 1));
        defn.add(new Interface.Element("version", StringType.DEFAULT,
                "Version identifying Method object structure, defaults to 1.2", 0, 1));
        defn.add(new Element(
                "fillin",
                BooleanType.DEFAULT,
                "When creating Methods, fill in the Method allocator space (re-use allocated CIDs with no assets), otherwise create the next available CID at the end of the CID pool (ignored if updating an existing Method). Defaults to false.",
                0, 1));
        defn.add(new Interface.Element("namespace", StringType.DEFAULT,
                "The namespace in which to create this method. Defaults to 'pssd/methods'", 0, 1));

    }

    // re-usable create/edit interface
    public static void addInterface(Interface defn) throws Throwable {

        defn.add(new Interface.Element("name", StringType.DEFAULT, "The name of the method.", 0, 1));
        defn.add(new Interface.Element("description", StringType.DEFAULT, "Arbitrary description for the method.", 1, 1));
        defn.add(new Interface.Element("author", CiteableIdType.DEFAULT, "Reference to the author of this method.", 0,
                1));

        Interface.Element se = new Interface.Element(
                "step",
                XmlDocType.DEFAULT,
                "Step in the process. Only one of types must be specified - [subject, method, branch]. The step may result in a study.",
                0, Integer.MAX_VALUE);
        se.add(new Interface.Element("name", StringType.DEFAULT, "Arbitrary name for the step.", 1, 1));
        se.add(new Interface.Element("description", StringType.DEFAULT, "Arbitrary description for the step.", 0, 1));

        Interface.Element sue = new Interface.Element("subject", XmlDocType.DEFAULT,
                "Subject specific metadata for a state.", 0, Integer.MAX_VALUE);
        sue.add(new Interface.Attribute("part", new EnumType(new String[] { "p", "r" }),
                "The target subject component. 'p' is the project subject. 'r' is the real subject. Defaults to 'p'.",
                0));

        Interface.Element mde = metadataInterfaceDefn("Subject specific metadata representing state for the subject.",
                false);
        sue.add(mde);
        se.add(sue);

        Interface.Element ste = new Interface.Element("study", XmlDocType.DEFAULT,
                "Study, if any, generated from this step.", 0, 1);
        ste.add(new Interface.Element("type", new DictionaryEnumType(Study.TYPE_DICTIONARY), "The type of the study.",
                1, 1));
        Interface.Element md2 = new Interface.Element("dicom", XmlDocType.DEFAULT,
                "The DICOM restrictions on this Study type.", 0, 1);
        md2.add(new Interface.Element("modality", new DictionaryEnumType(DicomElements.DICOM_MODALITY_DICTIONARY),
                "The DICOM modality allowed for this Study type. If not specified, any modality is allowed.", 0, Integer.MAX_VALUE));
        ste.add(md2);
        Interface.Element mde2 = metadataInterfaceDefn("Metadata to be located on the Study", true);
        ste.add(mde2);
        se.add(ste);

        Interface.Element me = new Interface.Element("method", XmlDocType.DEFAULT,
                "Utilise another pre-existing method that is primary on the local server.", 0, 1);
        me.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The identity of the method.", 1, 1));

        se.add(me);

        Interface.Element be = new Interface.Element("branch", XmlDocType.DEFAULT,
                "Parallel or selective branch to another pre-existing method that is primary on the local server.", 0,
                1);
        be.add(new Interface.Attribute("type", new EnumType(new String[] { "or", "and" }),
                "The evaluation method of the branch.", 1));
        me = new Interface.Element("method", XmlDocType.DEFAULT, "Method.", 1, Integer.MAX_VALUE);
        me.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The identity of the method.", 1, 1));
        be.add(me);
        se.add(be);

        Interface.Element te = new Interface.Element("transform", XmlDocType.DEFAULT, "A transform step.", 0, 1);
        Interface.Element tde = new Interface.Element(
                "definition",
                StringType.DEFAULT,
                "The id of the transform definition. Depends on the value of the id-type attribute, it could be asset id or transform definition uid.",
                1, 1);
        tde.add(new Attribute("version", IntegerType.POSITIVE,
                "The version of the definition. Defaults to zero, which is the latest version, if not specified.", 0));
        tde.add(new Attribute(
                "id-type",
                new EnumType(new String[] { "uid", "id" }),
                "The type of the transform definition id. Sets to id for asset id, uid for transform definition uid. Defaults to uid.",
                0));
        te.add(tde);
        Interface.Element ide = new Interface.Element(
                "iterator",
                XmlDocType.DEFAULT,
                "A iterator links to a defined parameter, can be used to find all the candidates for the parameter and submit them for executions automatically.",
                0, 1);
        ide.add(new Interface.Element("scope", new EnumType(new String[] { "ex-method", "subject", "project" }),
                "The context/scope where the iterator(parameter) in.", 1, 1));
        ide.add(new Interface.Element("parameter", StringType.DEFAULT, "The parameter that the iterator links to.", 1,
                1));
        ide.add(new Interface.Element("query", StringType.DEFAULT,
                "The query/filter to find the candidates for the parameter.", 1, 1));
        ide.add(new Interface.Element("type", new EnumType(new String[] { "citeable-id", "asset-id" }),
                "Type of of the results. Asset id or citeable id.", 1, 1));
        te.add(ide);
        Interface.Element tpe = new Element("parameter", StringType.DEFAULT,
                "The parameter required by the transform.", 0, Integer.MAX_VALUE);
        tpe.add(new Attribute("name", StringType.DEFAULT, "The name of the parameter", 1));
        te.add(tpe);
        se.add(te);

        defn.add(se);
    }

    public String name() {

        return "om.pssd.method.create";
    }

    public String description() {

        return "Creates a subject independent research method on the local server.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_ADMINISTER;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

	
		// Methods are always created/modified on the local server
        String proute = null;

        // Because we allow editing and creating via the same interface
        // definition (really they should
        // be separated) we must test that the name is specified in this
        // creation service
        XmlDoc.Element name = args.element("name");
        if (name == null) {
            throw new Exception("You must specify the name element as you are creating the Method");
        }

        // Get the citable root name. The standard is 'pssd.method' and we allow
        // that to be auto-created. However,
        // we want to control other ones a little more and require that they be
        // explicitly created first.
        // Otherwise we could end up with a big mess of uncontrolled roots
        String cidRootName = args.stringValue("cid-root-name", "pssd.method");
        String methodRoot = getMethodRoot(executor(), proute, cidRootName);

        Boolean replace = null; // irrelevant to creation
        execute(executor(), methodRoot, null, proute, args, w, replace);
    }

    // Get the citable root name. The standard is 'pssd.method' and we allow
    // that to be auto-created. However,
    // we want to control other ones a little more and require that they be
    // explicitly created first.
    // Otherwise we could end up with a big mess of uncontrolled roots
    public static String getMethodRoot(ServiceExecutor executor, String proute, String cidRootName) throws Throwable {
        if (!cidRootName.equals("pssd.method")) {
            XmlDoc.Element r = executor.execute("citeable.named.id.describe");
            String t = r.value("id[@name='" + cidRootName + "']");
            if (t == null) {
                throw new Exception("The Method citable ID root '" + cidRootName
                        + "' does not exist. Please create first with citeable.named.id.create");
            }
        }
        return nig.mf.pssd.plugin.util.CiteableIdUtil.citeableIDRoot(executor, proute, cidRootName);
    }

    /**
     * 
     * @param executor
     * @param methodRoot
     *            Citeable root ID to create the Method in. If null is set to the value for 'pssd.method'
     * @param id
     *            Citeable id of Method (for updates)
     * @param args
     * @param w
     * @param replace
     *            Only relevant to pre-existing Methods (and may be null for creation) if false, and if the Method
     *            pre-exists (id!=null), then the meta-data are merged.
     * @throws Throwable
     */
    public static void execute(ServiceExecutor executor, String methodRoot, String id, String proute,
            XmlDoc.Element args, XmlWriter w, Boolean replace) throws Throwable {

        // Get any Method IDs and validate them to be primary (they are local by
        // definition; no proute)
        validateMethod(executor, args.value("step/method/id"));
        validateMethod(executor, args.value("step/branch/method/id"));

        List<XmlDoc.Element> tes = args.elements("step/transform");
        if (tes != null) {
            for (XmlDoc.Element te : tes) {
                XmlDoc.Element defnElement = te.element("definition");
                if(defnElement.attribute("version")==null){
                	defnElement.add(new XmlDoc.Attribute("version", 0));
                }
                XmlDoc.Attribute idTypeAttr = defnElement.attribute("id-type");
                boolean useDefnAssetId = idTypeAttr == null ? false : ("id".equals(idTypeAttr.value()) ? true : false);
                validateTransform(executor, te, useDefnAssetId);
                if(idTypeAttr!=null){
                    defnElement.remove(idTypeAttr);
                }
                if(useDefnAssetId){
                    defnElement.setValue(Asset.getById(executor, null, defnElement.value()).value("meta/transform-definition/uid"));
                }
            }
        }

        //
        String ns = args.stringValue("namespace", "pssd/methods");
        String name = args.value("name");
        String description = args.value("description");
        String author = args.value("author");
        Boolean fillIn = args.booleanValue("fillin", false);

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("model", Method.MODEL);

        if (id == null) {
            dm.add("namespace", ns);
            dm.push("meta");
        } else {
            if (replace) {
                dm.push("meta", new String[] { "action", "replace" });
            } else {
                dm.push("meta", new String[] { "action", "merge" });
            }
        }

        dm.push("daris:pssd-object");
        dm.add("type", "method");
        if (name != null)
            dm.add("name", name);

        if (description != null) {
            dm.add("description", description);
        }
        dm.pop();

        dm.push("daris:pssd-method");

        if (author != null) {
            dm.add("author", author);
        }

        // Version 1.2 Added: daris:pssd-method-subject/human [true/false/unset]
        // Version 1.1 Added :dicom element to :study steps
        // First version 1.0 implied by not having version element
        String version = args.stringValue("version", "1.2");
        dm.add("version", version);

        // Add the step and auto-generate the step id when creating or updating
        // Find the pre-existing number of steps and use this to generate the
        // step id. Because the id is auto-generated, you can't edit an existing
        // step, just add them or fully replace them
        int sid = 1;
        if (replace != null && !replace)
            sid = findPreExistingSteps(executor, id) + 1;
        List<XmlDoc.Element> ses = args.elements("step");
        if (ses != null) {
            for (XmlDoc.Element se : ses) {
                dm.push("step", new String[] { "id", String.valueOf(sid++) });
                dm.add(se, false);
                dm.pop();
            }
        }
        dm.pop();

        // OK, if there is subject information, then add..
        XmlDoc.Element humanX = args.element("subject/human");
        Boolean human = null;
        if (humanX!=null) human = humanX.booleanValue();          // Default is false if element does not exist so but long winded
        XmlDoc.Element se = args.element("subject/project");
        if (se != null) {
            dm.push("daris:pssd-method-subject");
            if (human!=null) dm.add("human", human);
            dm.add(se, false);
            dm.pop();
        }

        se = args.element("subject/rsubject");
        if (se != null) {
            dm.push("daris:pssd-method-rsubject");
            if (human!=null) dm.add("human", human);
            dm.add(se, false);
            dm.pop();
        }

        dm.pop();

        // Creation if id is null
        if (id == null) {

            // Set Method root CID if not given to standard 'pssd.method'
            if (methodRoot == null)
                methodRoot = nig.mf.pssd.plugin.util.CiteableIdUtil.methodIDRoot(executor, proute);

            dm.add("action", "get-cid");

            // Set CID if filling in allocator space
            if (fillIn) {
                // TBD:Set distation to ...
                String pdist = null;
                String methodCID = nig.mf.pssd.plugin.util.CiteableIdUtil.generateCiteableID(executor, methodRoot,
                        pdist, -1, true);
                if (methodCID != null) {
                    dm.add("cid", methodCID);
                } else {
                    dm.add("pcid", methodRoot);
                }
            } else {
                dm.add("pcid", methodRoot);
            }

            // Add read-write ACLs for administration
            dm.push("acl");
            dm.add("actor", new String[] { "type", "role" }, Role.objectAdminRoleName());
            dm.add("access", "read-write");
            dm.pop();

            dm.push("acl");
            dm.add("actor", new String[] { "type", "role" }, Role.powerModelUserRoleName());
            dm.add("access", "read-write");
            dm.pop();

            // Add read ACL so all model users can see it
            dm.push("acl");
            dm.add("actor", new String[] { "type", "role" }, Role.modelUserRoleName());
            dm.add("access", "read");
            dm.pop();

            // Create
            XmlDoc.Element r = executor.execute("asset.create", dm.root());
            id = r.value("cid");

            w.add("id", id);
        } else {
            dm.add("cid", id);
            executor.execute("asset.set", dm.root());
        }
    }

    // Private functions
    private static void validateMethod(ServiceExecutor executor, String methodId) throws Throwable {

        if (methodId != null) {
            DistributedAsset dMID = new DistributedAsset(null, methodId);
            PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor, dMID));
            if (!type.equals(Method.TYPE)) {
                throw new Exception("Object " + dMID.getCiteableID() + " [type=" + type + "] is not a " + Method.TYPE);
            }
            if (dMID.isReplica()) {
                throw new Exception("The supplied Method " + methodId
                        + " is a replica and this service cannot utilise it.");
            }
        }
    }

    private static int findPreExistingSteps(ServiceExecutor executor, String id) throws Throwable {
        if (id == null)
            return 0;
        //
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", id);
        XmlDoc.Element r = executor.execute("asset.get", dm.root());
        Collection<XmlDoc.Element> steps = r.elements("asset/meta/daris:pssd-method/step");
        if (steps == null)
            return 0;
        return steps.size();
    }

    /**
     * Validates the parameters specified in the transform step.
     * 
     * @param executor
     * @param te
     *            the transform element in the service args
     * @param useDefnAssetId
     *            indicates the definition id type is asset id
     * @throws Throwable
     */
    private static void validateTransform(ServiceExecutor executor, XmlDoc.Element te, boolean useDefnAssetId)
            throws Throwable {
        List<XmlDoc.Element> pes = te.elements("parameter");
        if (pes == null || pes.isEmpty()) {
            // No parameters specified in the transform step
            return;
        }

        XmlDocMaker dm = new XmlDocMaker("args");
        if (useDefnAssetId) {
            // asset id
            dm.add("id", new String[] { "version", te.value("definition/@version") }, te.value("definition"));
        } else {
            // transform definition uid
            dm.add("uid", new String[] { "version", te.value("definition/@version") }, te.value("definition"));
        }
        XmlDoc.Element r = executor.execute("transform.definition.describe", dm.root());

        List<XmlDoc.Element> pdes = r.elements("transform-definition/parameter");
        if (pdes == null || pdes.isEmpty()) {
            throw new Exception("Unexpected transform parameters.");
        }
        for (XmlDoc.Element pe : pes) {
            String name = pe.value("@name");
            int occurs = r.count("transform-definition/parameter[@name='" + name + "']");
            boolean paramExists = false;
            for (XmlDoc.Element pde : pdes) {
                if (name.equals(pde.value("@name"))) {
                    paramExists = true;
                    if (occurs >= pde.intValue("max-occurs")) {
                        throw new Exception("Invalid number of occurrence of parameter: " + name + ".");
                    }
                    break;
                }
            }
            if (!paramExists) {
                throw new Exception("Unexpected parameter: " + name);
            }
        }
    }

}
