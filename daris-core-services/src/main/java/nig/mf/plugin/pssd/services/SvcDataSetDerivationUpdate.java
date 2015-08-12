package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.DataSet;
import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.PSSDObjectEvent;
import nig.mf.plugin.pssd.PSSDObjectEvent.Action;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.mf.plugin.event.SystemEventChannel;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDataSetDerivationUpdate extends PluginService {
    private Interface _defn;

    public SvcDataSetDerivationUpdate() throws Throwable {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
                "The identity of the derivation DataSet. Must be managed by the local server.", 1, 1));
        _defn.add(new Interface.Element("name", StringType.DEFAULT, "The name of this data set.", 0, 1));
        _defn.add(new Interface.Element("description", StringType.DEFAULT,
                "An arbitrary description for the data set.", 0, 1));

        Interface.Element fn = new Interface.Element("filename", StringType.DEFAULT,
                "The original file name to be preserved on download", 0, 1);
        fn.add(new Interface.Attribute(
                "private",
                BooleanType.DEFAULT,
                "If public (default) all users can access this meta-data. If private, only users with Subject or project admin role for this Project can access.",
                0));
        _defn.add(fn);

        _defn.add(new Interface.Element("type", StringType.DEFAULT,
                "MIME type of the data set if different from the content.", 0, 1));
        _defn.add(new Interface.Element(
                "ctype",
                StringType.DEFAULT,
                "Encapsulation MIME type of the content, if there is content. If not specified, then auto-typed from the content extension, and/or analyzers. If no 'lctype' is specified, then this is also the logical/interpreted type of for the content.",
                0, 1));
        _defn.add(new Interface.Element(
                "lctype",
                StringType.DEFAULT,
                "ogical MIME type of the content, if there is content. If not specified, then auto-typed from the content extension/analyzers. The logical can be different from the encapulation type, and determines how the content is interpretted. For instance, a logical 'application/abc' type may be encapsulated in an 'application/zip' format.",
                0, 1));

        Interface.Element input = new Interface.Element("input", CiteableIdType.DEFAULT,
                "Input data set(s) from which the derivation was made, if available.", 0, Integer.MAX_VALUE);
        input.add(new Interface.Attribute("vid", StringType.DEFAULT, "The value identifier for the data set.", 1));
        _defn.add(input);

        Interface.Element method = new Interface.Element("method", XmlDocType.DEFAULT,
                "Details about the ExMethod for which this acquisition was made.", 0, 1);
        method.add(new Interface.Element("id", CiteableIdType.DEFAULT,
                "The identity of the ExMethod. If not specified, defaults to the identity of the parent ExMethod.", 0,
                1));
        method.add(new Interface.Element("step", CiteableIdType.DEFAULT, "The execution step within the ExMethod", 1, 1));
        _defn.add(method);

        _defn.add(new Interface.Element("processed", BooleanType.DEFAULT,
                "Set to true to indicate the dataset is processed data rather than raw data. Defaults to false.", 0, 1));
        _defn.add(new Interface.Element("anonymized", BooleanType.DEFAULT,
                "Set to true to indicate the dataset (the actual content) has been anonymized. At present only implemented for DICOM DataSets (sets meta-data daris:pssd-dataset-dicom/anonymized to true). Defaults to false.", 0, 1));

        Interface.Element transform = new Interface.Element("transform", XmlDocType.DEFAULT,
                "If transformed, then details of the transform, if known.", 0, 1);
        transform.add(new Interface.Element("tuid", LongType.POSITIVE_ONE,
                "The unique id of the transform if it is executed via daris-transform(Transform Framework).", 0, 1));
        transform.add(new Interface.Element("mid", CiteableIdType.DEFAULT,
                "The identity of an agreed method of transformation.", 0, 1));
        Interface.Element software = new Interface.Element("software", XmlDocType.DEFAULT,
                "The software application used to perform the transform.", 0, 1);
        software.add(new Interface.Element("name", XmlDocType.DEFAULT, "The name of the software application.", 1, 1));
        software.add(new Interface.Element("version", StringType.DEFAULT, "The version of the software application", 0,
                1));
        Interface.Element command = new Interface.Element("command", XmlDocType.DEFAULT,
                "The command used to perform the transform.", 0, 1);
        command.add(new Interface.Element("name", StringType.DEFAULT, "The name of the command.", 1, 1));
        Interface.Element argument = new Interface.Element("argument", XmlDocType.DEFAULT,
                "The argument for the command.", 0, Integer.MAX_VALUE);
        argument.add(new Interface.Element("name", StringType.DEFAULT, "The name of the argument.", 1, 1));
        argument.add(new Interface.Element("value", StringType.DEFAULT, "The name of the argument.", 0, 1));
        command.add(argument);
        software.add(command);
        transform.add(software);
        transform.add(new Interface.Element("notes", StringType.DEFAULT,
                "Arbitrary description of the transformation.", 0, 1));
        _defn.add(transform);

        _defn.add(new Interface.Element("allow-incomplete-meta", BooleanType.DEFAULT,
                "Should the metadata be accepted if incomplete? Defaults to false.", 0, 1));

        Interface.Element meta = new Interface.Element("meta", XmlDocType.DEFAULT,
                "Optional metadata - a list of asset documents.", 0, 1);
        meta.setIgnoreDescendants(true);
        _defn.add(meta);
    }

    public String name() {
        return "om.pssd.dataset.derivation.update";
    }

    public String description() {
        return "Updates (merges) an existing locally-managed PSSD DataSet that contains data acquired from a subject. The DataSet may have data, or may simply be a container for other DataSets.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_MODIFY;
    }

    public int minNumberOfInputs() {
        return 0;
    }

    public int maxNumberOfInputs() {
        return 1;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        // Set distributed citeable ID for the DataSet. The DataSet is local by
        // definition
        DistributedAsset dID = new DistributedAsset(args.element("id"));

        PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID));
        if (type == null) {
            throw new Exception("The asset associated with " + dID.toString() + " does not exist");
        }
        if (!type.equals(DataSet.TYPE)) {
            throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + DataSet.TYPE);
        }
        if (dID.isReplica()) {
            throw new Exception("The supplied DataSet is a replica and this service cannot modify it.");
        }

        // The DataSet holds information describing the ExMethod and step path
        // that it was
        // created with. Because it only refers to a CID, we don't have to try
        // and check
        // where the actual ExMethod is sourced from. We can check that it
        // exists
        // somewhere in the federation. We do this now before partial writes are
        // done.
        String emid = args.value("method/id");
        if (emid != null) {
            String proute = null;
            String pdist = "infinity";
            if (!DistributedAssetUtil.assetExists(executor(), proute, pdist, emid,
                    DistributedQuery.ResultAssetType.all, false, true, null)) {
                throw new Exception("The ExMethod object (" + emid + ") cannot be found anywhere in the federation.");
            }
        }

        // First update the component that is common between 'primary' and
        // 'derivation'
        boolean isDerivation = true;
        updateGenericAndData(executor(), args, dID.getCiteableID(), in, isDerivation);

        // Now update the derived data-set bits
        updateDerivation(executor(), args, dID.getCiteableID());

        // Generate system event
        SystemEventChannel.generate(new PSSDObjectEvent(Action.MODIFY, dID.getCiteableID(), SvcCollectionMemberCount
                .countMembers(executor(), dID.getCiteableID())));

    }

    /**
     * Update the meta-data that is common between derivation and primary
     * DataSets. This function is reused by SvcDatSetPrimaryUpdate. It is the
     * callers responsibility to have already checked that the Method is
     * (embedded in args) consistent with Federation policy
     * 
     * @param executor
     * @param args
     * @param id
     * @param in
     * @param isDerivation
     * @throws Throwable
     */
    static public void updateGenericAndData(ServiceExecutor executor, XmlDoc.Element args, String id, Inputs in,
            boolean isDerivation) throws Throwable {

        // Prepare new meta
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", id);

        String type = args.value("type");
        if (type != null) {
            dm.add("type", type);
        }
        String ctype = args.value("ctype");
        if (ctype != null) {
            dm.add("ctype", ctype);
        }
        String lctype = args.value("lctype");
        if (lctype != null) {
            dm.add("lctype", lctype);
        }

        dm.push("meta");
        PSSDUtils.setObjectMeta(dm, DataSet.TYPE, args.value("name"), args.value("description"), false);
        PSSDUtils.setObjectOptionalMeta(dm, args.element("meta"), "om.pssd.dataset");

        // Data set type.
        dm.push("daris:pssd-dataset");
        if (isDerivation) {
            dm.add("type", "derivation");
        } else {
            dm.add("type", "primary");
        }
        dm.pop();
        dm.pop();
        dm.add("if", new String[] { "part", "content" }, "changed");

        // Update meta-and data
        executor.execute("asset.set", dm.root(), in, null);

        // daris:pssd-filename
        // Remove first then update (as presence of attribute makes a merge
        // fail)
        XmlDoc.Element fileName = args.element("filename");
        if (fileName != null) {
            dm = new XmlDocMaker("args");
            dm.add("cid", id);
            dm.push("meta", new String[] { "action", "remove" });
            dm.add("daris:pssd-filename");
            executor.execute("asset.set", dm.root());

            // Now set new
            fileName.setName("original");
            dm = new XmlDocMaker("args");
            dm.add("cid", id);
            dm.push("meta");
            dm.push("daris:pssd-filename");
            dm.add(fileName);
            dm.pop();
            dm.pop();
            executor.execute("asset.set", dm.root());
        }

        // Transformation..
        XmlDoc.Element te = args.element("transform");
        if (te != null) {
            updateTransform(executor, args, id);
        }
    }

    private static void updateDerivation(ServiceExecutor executor, XmlDoc.Element args, String id) throws Throwable {

        // Get old meta-data
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", id);
        dm.add("pdist", 0); // Force local

        // retrieve daris:pssd-derivation first
        XmlDoc.Element de = executor.execute("asset.get", dm.root()).element("asset/meta/daris:pssd-derivation");
        boolean changed = false;

        if (de == null) {
            de = new XmlDoc.Element("daris:pssd-derivation");
            changed = true;
        }

        XmlDoc.Element processed = args.element("processed");
        if (processed != null) {
            if (de.element("processed") != null) {
                de.remove(de.element("processed"));
            }
            de.add(processed);
            changed = true;
        }
        
        XmlDoc.Element anonymized = args.element("anonymized");
        if (anonymized != null) {
            if (de.element("anonymized") != null) {
                de.remove(de.element("anonymized"));
            }
            de.add(anonymized);
            changed = true;
        }
    

        XmlDoc.Element method = args.element("method");
        if (method != null) {
            if (de.element("method") != null) {
                de.remove(de.element("method"));
            }
            XmlDoc.Element me = new XmlDoc.Element("method");
            me.add(new XmlDoc.Attribute("step", method.value("step")));
            me.setValue(method.value("id"));
            de.add(me);
            changed = true;
        }

        List<XmlDoc.Element> inputs = args.elements("input");
        if (inputs != null) {
            if (!inputs.isEmpty()) {
                List<XmlDoc.Element> oldInputs = de.elements("input");
                for (XmlDoc.Element oldInput : oldInputs) {
                    de.remove(oldInput);
                }
                for (XmlDoc.Element input : inputs) {
                    de.add(input);
                }
                changed = true;
            }
        }

        // Handle the daris:pssd-derivation separately. There is a subtlety in MF
        // which means to change an attribute (e.g. the state or step) requires
        // you to change the value as well. The only safe way is to remove and
        // replace.
        if (changed) {

            // First remove the existing daris:pssd-derivation
            dm = new XmlDocMaker("args");
            dm.add("cid", id);
            dm.push("meta", new String[] { "action", "remove" });
            dm.add("daris:pssd-derivation");
            dm.pop();
            executor.execute("asset.set", dm.root());

            // Now create the new and add in
            dm = new XmlDocMaker("args");
            dm.add("cid", id);
            dm.add("allow-incomplete-meta", args.booleanValue("allow-incomplete-meta", false));
            dm.push("meta", new String[] { "action", "add" });
            dm.add(de, true);
            dm.pop();

            // Replace meta-data. At the end of all this, this is the equivalent
            // to a 'merge' but one that handles the attributes
            executor.execute("asset.set", dm.root());
        }
    }

    private static void updateTransform(ServiceExecutor executor, XmlDoc.Element args, String id) throws Throwable {
        // Get old meta-data
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", id);
        dm.add("pdist", 0); // Force local

        // retrieve daris:pssd-derivation first
        XmlDoc.Element te = executor.execute("asset.get", dm.root()).element("asset/meta/daris:pssd-transform");

        if (te != null) {
            dm = new XmlDocMaker("args");
            dm.add("cid", id);
            dm.push("meta", new String[] { "action", "remove" });
            dm.add("daris:pssd-transform");
            dm.pop();
            executor.execute("asset.set", dm.root());
        }

        XmlDoc.Element transform = args.element("transform");
        if (transform != null) {
            dm = new XmlDocMaker("args");
            dm.add("cid", id);
            dm.push("meta", new String[] { "action", "add" });
            dm.push("daris:pssd-transform");
            dm.add(transform, false);
            dm.pop();
            dm.pop();
            executor.execute("asset.set", dm.root());
        }
    }

}
