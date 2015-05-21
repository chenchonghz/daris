package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import nig.mf.plugin.pssd.DataSet;
import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.PSSDObjectEvent;
import nig.mf.plugin.pssd.PSSDObjectEvent.Action;
import nig.mf.plugin.pssd.Study;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.UrlType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.mf.plugin.event.SystemEventChannel;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDataSetDerivationCreate extends PluginService {
    private Interface _defn;
    private static final ReentrantLock lock_ = new ReentrantLock();

    public SvcDataSetDerivationCreate() throws Throwable {
        _defn = new Interface();

        Interface.Element pid = new Interface.Element("pid", CiteableIdType.DEFAULT,
                "The identity of the parent study.", 1, 1);
        pid.add(new Interface.Attribute(
                "proute",
                CiteableIdType.DEFAULT,
                "In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.",
                0));
        _defn.add(pid);

        _defn.add(new Interface.Element(
                "dataset-number",
                IntegerType.POSITIVE_ONE,
                "Specifies the DataSet number for the identifier. If not given, the next available DataSet is created. If specified, then there cannot be any other asset/object with this identity assigned. Used for importing DataSets from another system.",
                0, 1));
        _defn.add(new Element(
                "fillin",
                BooleanType.DEFAULT,
                "If the dataset-number is not given, fill in the DataSet allocator space (re-use allocated CIDs with no assets), otherwise create the next available CID at the end of the CID pool. Defaults to false; use with extreme care in federated envionment. Concurrency issues mean that this argument may be ignored if many DataSets are being created simultaneously.",
                0, 1));
        _defn.add(new Interface.Element("name", StringType.DEFAULT, "The name of this data set.", 0, 1));
        _defn.add(new Interface.Element("description", StringType.DEFAULT,
                "An arbitrary description for the data set.", 0, 1));
        //
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
                "Details about the ex-method for which this acquisition was made.", 0, 1);
        method.add(new Interface.Element("id", CiteableIdType.DEFAULT,
                "The identity of the ex-method. If not specified, defaults to the identity of the parent ex-method.",
                0, 1));
        method.add(new Interface.Element("step", CiteableIdType.DEFAULT, "The execution step within the ex-method", 1,
                1));
        _defn.add(method);

        _defn.add(new Interface.Element("processed", BooleanType.DEFAULT,
                "Set to true to indicate the dataset is processed data rather than raw data. Defaults to false.", 0, 1));

        Interface.Element transform = new Interface.Element("transform", XmlDocType.DEFAULT,
                "If transformed, then details of the transform, if known.", 0, 1);
        transform.add(new Interface.Element("tuid", LongType.POSITIVE_ONE,
                "The unique id of the transform if it is executed via Transform Framework.", 0, 1));
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

        Interface.Element url = new Interface.Element(
                "url",
                UrlType.DEFAULT,
                "A URL to the content for the data set. If the URL is specified, then no content may be passed with the request. Of course, the given URL must be accessible by the server.",
                0, 1);
        url.add(new Interface.Attribute(
                "by",
                new EnumType(new String[] { "value", "reference" }),
                "Whether the content is stored by value (that is, a copy within the server) or by reference (that is, a link to external content). Defaults to 'value'.",
                0));
        _defn.add(url);

    }

    public String name() {
        return "om.pssd.dataset.derivation.create";
    }

    public String description() {
        return "Creates a derivation PSSD DataSet on the local server. The DataSet contains data acquired from a Subject. The DataSet may have data, or may simply be a container for other DataSets or DataObjects.";
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

        // Distributed ID for parent Study or DataSet. It must be a primary or
        // we are not allowed
        // to create children under it.
        DistributedAsset dPID = new DistributedAsset(args.element("pid"));

        // Validate
        PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dPID));
        if (type == null) {
            throw new Exception("The asset associated with " + dPID.toString() + " does not exist");
        }
        if (!type.equals(Study.TYPE)) {
            throw new Exception("Object " + dPID.getCiteableID() + " [type=" + type + "] is not a " + Study.TYPE);
        }
        if (dPID.isReplica()) {
            throw new Exception(
                    "The supplied parent Study/DataSet is a replica and this service cannot create its child");
        }

        // Make sure parent Study is not for Processed data. If not specified,
        // cannot test.
        XmlDoc.Element studyMeta = dPID.getAsset();
        XmlDoc.Element studyIsProcessed = studyMeta.element("asset/meta/daris:pssd-study/processed");
        if (studyIsProcessed != null) {
            Boolean processed = args.booleanValue("processed", false);
            if (processed && !studyIsProcessed.booleanValue()) {
                throw new Exception(
                        "The parent study does not allow for child processed data sets - cannot use as parent");
            } else if (!processed && studyIsProcessed.booleanValue()) {
                throw new Exception(
                        "The parent study container only allows child processed data sets - cannot use as parent");
            }
        }

        // If the user does not give project-number, we may want to fill in
        // any holes in the allocator space for Projects as sometimes we use
        // large numbers for 'service' activities.
        long datasetNumber = args.longValue("dataset-number", -1);
        boolean fillIn = args.booleanValue("fillin", false);

        // Generate CID, filling in allocator space if desired
        // The parent could be anywhere in the federation so allow Mediaflux to
        // find the
        // parent CID and allocate the child CID by setting the distation to
        // infinity
        // Ignores servers it can't reach.
        String pdist = "infinity";
        if (fillIn) {
            // Concurrent threads may find the same naked CID and attempt to
            // create multiple DataSets
            // with the same CID. Therefore we lock the process. But if after
            // 1s, we are still waiting,
            // just give up the fillin request and proceed to allocate the next
            // CID
            if (!lock_.tryLock(1L, TimeUnit.SECONDS))
                fillIn = false;
        }

        try {
            String cid = nig.mf.pssd.plugin.util.CiteableIdUtil.generateCiteableID(executor(), dPID.getCiteableID(),
                    pdist, datasetNumber, fillIn);
            createDataSetAsset(args, dPID, cid, in);

            // Generate system event
            SystemEventChannel.generate(new PSSDObjectEvent(Action.CREATE, cid, 0));
            w.add("id", cid);
        } finally {
            if (fillIn) {
                lock_.unlock();
            }
        }
    }

    private void createDataSetAsset(XmlDoc.Element args, DistributedAsset dPID, String cid, Inputs in) throws Throwable {
    	
    	// Create meta-data
        XmlDocMaker dm = createDataSetMetaData (executor(), args.booleanValue("allow-incomplete-meta", false), 
        		PSSDUtils.namespace(executor(), dPID), args.value("type"), 
        		args.value("ctype"), args.value("lctype"), args.value("name"), args.value("description"), 
        		args.element("meta"), args.element("filename"), args.booleanValue("processed", false), 
        		args.elements("input"), args.element("method"), args.element("transform"), args.element("url"),
        		dPID, cid);
        
  
        // Create asset on the local server
        executor().execute("asset.create", dm.root(), in, null);
    }
    
    
    public static XmlDocMaker createDataSetMetaData (ServiceExecutor executor, Boolean allowIncompleteMeta, String namespace, String type, String ctype, String lctype,
                                              String name, String description, XmlDoc.Element meta, XmlDoc.Element fileName,
                                              Boolean processed, Collection<XmlDoc.Element> inputs, 
                                              XmlDoc.Element method, XmlDoc.Element transform, XmlDoc.Element url,
                                              DistributedAsset dPID, String cid) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("allow-incomplete-meta", allowIncompleteMeta);
        dm.add("cid", cid);
        if (namespace!=null) dm.add("namespace", namespace);
        dm.add("model", DataSet.MODEL);
        if (type != null) {
            dm.add("type", type);
        }
        if (ctype != null) {
            dm.add("ctype", ctype);
        }
        if (lctype != null) {
            dm.add("lctype", lctype);
        }

        dm.push("meta");
        PSSDUtils.setObjectMeta(dm, DataSet.TYPE, name, description,  true);
        PSSDUtils.setObjectOptionalMeta(dm, meta, "om.pssd.dataset");

        // Data set..
        dm.push("daris:pssd-dataset");
        dm.add("type", "derivation");
        dm.pop();

        // Filename
        if (fileName != null) {
            fileName.setName("original");
            dm.push("daris:pssd-filename");
            dm.add(fileName);
            dm.pop();
        }

        dm.push("daris:pssd-derivation");

        dm.add("processed", processed);

        // Inputs ..
        if (inputs != null) {
            for (XmlDoc.Element input : inputs) {
                dm.add("input", new String[] { "vid", input.value("@vid") }, input.value());
            }
        }

        // Method. If specified, use that. If not specified, infer from the
        // parent study.
        SvcDataSetPrimaryCreate.addMethodAndStep(executor, dm, dPID, method);
        dm.pop();

        // Transformation..
        if (transform != null) {
            dm.push("daris:pssd-transform");
            dm.add(transform, false);
            dm.pop();
        }

        dm.pop();

        if (url != null) {
            dm.add(url);
        }

        // Get the project identifier..this is required for ACLs on the DataSet
        PSSDUtils.addDataSetACLs(dm, dPID.getParentProjectCID());
        
        //
        return dm;

    }

}
