package nig.mf.plugin.pssd.services;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Study;
import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.DictionaryEnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStudyCreate extends PluginService {
    private Interface _defn;
    private static final ReentrantLock lock_ = new ReentrantLock();

    public SvcStudyCreate() throws Throwable {
        _defn = new Interface();
        Interface.Element me = new Interface.Element("pid", CiteableIdType.DEFAULT,
                "The identity of the parent (ex-method).", 1, 1);
        me.add(new Interface.Attribute(
                "proute",
                CiteableIdType.DEFAULT,
                "In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.",
                0));
        _defn.add(me);
        //
        _defn.add(new Interface.Element(
                "study-number",
                IntegerType.POSITIVE_ONE,
                "Specifies the study number for the identifier. If not given, the next available study is created. If specified, then there cannot be any other asset/object with this citable ID assigned. Used for importing studies from another system.",
                0, 1));
        _defn.add(new Element(
                "fillin",
                BooleanType.DEFAULT,
                "If the study-number is not given, fill in the Study allocator space (re-use allocated CIDs with no assets), otherwise create the next available CID at the end of the CID pool. Defaults to false; use with extreme care in federated envionment. Concurrency issues mean that this argument may be ignored if many Studies are being created simultaneously.",
                0, 1));
        _defn.add(new Interface.Element(
                "step",
                CiteableIdType.DEFAULT,
                "The step within the method that resulted in this study.  If the type is given but the step not, the step is filled in with the first matching the given study type.",
                0, 1));

        addInterfaceDefn(_defn);
    }

    public static void addInterfaceDefn(Interface defn) throws Throwable {
        defn.add(new Interface.Element("type", new DictionaryEnumType(Study.TYPE_DICTIONARY),
                "The type of the study. If not specified, then method step must be specified.", 0, 1));
        defn.add(new Interface.Element("name", StringType.DEFAULT,
                "The name of this study. If not specified, then method must be specified.", 0, 1));
        defn.add(new Interface.Element("description", StringType.DEFAULT, "An arbitrary description for the study.", 0,
                1));
        defn.add(new Interface.Element(
                "processed",
                BooleanType.DEFAULT,
                "Set to [true,false] to indicate the Study is a container for [processed,not-processed] data only.  If not set (default), then the Study can hold any kind of data, processed or not processed.",
                0, 1));

        defn.add(new Interface.Element("allow-incomplete-meta", BooleanType.DEFAULT,
                "Should the metadata be accepted if incomplete? Defaults to false.", 0, 1));

        Interface.Element me = new Element(
                "meta",
                XmlDocType.DEFAULT,
                "Optional metadata - a list of asset documents. If the metadata belongs to a method, then it must have an 'ns' attribute which corresponds to the 'ExMethod CID_Step ID_Group' (e.g. 1.1.1.1_1.1_Neuropsych).  The group component is optional. It can be used by e.g. the portal to group documents into sub-tabs.",
                0, 1);
        me.setIgnoreDescendants(true);

        defn.add(me);
    }

    public String name() {
        return "om.pssd.study.create";
    }

    public String description() {
        return "Creates a PSSD Study on the local server.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        // Distributed ID for parent ExMethod. It must be a primary or we are
        // not allowed
        // to create children under it.
        DistributedAsset dEID = new DistributedAsset(args.element("pid"));

        // Validate
        PSSDObject.Type type = PSSDObject.Type.parse(nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dEID));
        if (type == null) {
            throw new Exception("The asset associated with " + dEID.toString() + " does not exist");
        }
        if (!type.equals(ExMethod.TYPE)) {
            throw new Exception("Object " + dEID.getCiteableID() + " [type=" + type + "] is not an " + ExMethod.TYPE);
        }
        if (dEID.isReplica()) {
            throw new Exception("The supplied parent ExMethod is a replica and this service cannot create its child");
        }

        // If the user does not give study-number, we may want to fill in any
        // holes in the allocator space
        boolean fillIn = args.booleanValue("fillin", false);
        long studyNumber = args.longValue("study-number", -1);

        // We want not set if not specified. args.booleanValue("x") will default
        // to false
        XmlDoc.Element t = args.element("processed");
        Boolean processed = null;
        if (t != null)
            processed = t.booleanValue();

        // Find the parent primary Project. In the creation context we must find
        // the Primary parent project
        Boolean readOnly = false;
        DistributedAsset dPID = dEID.getParentProject(readOnly);
        if (dPID == null) {
            throw new Exception("Cannot find primary Project parent of the given ExMethod");
        }

        // Handle step/type
        String studyType = args.value("type");
        String step = args.value("step");
        // We need both type and step but can source from each other.
        if (studyType == null && step == null) {
            throw new Exception("You must give at least one of step and/or type");
        }
        if (step == null) {
            // Find the first compatible step from the type.
            step = findMethodStep(executor(), dEID, studyType);
        } else if (studyType == null) {
            studyType = findStudyType(executor(), dEID, step);
        }

        String name = args.value("name");
        String description = args.value("description");

        /*
         * String exMethod = dEID.getCiteableID();
         * 
         * if ( name == null || type == null ) { if ( exMethod == null ) { throw
         * new
         * Exception("Must specify method if not specifying name and type."); }
         * 
         * Study s = studyFor(exMethod,step); if ( s == null ) { throw new
         * Exception("Method " + method + ", step " + step +
         * " not found or has no associated study."); }
         * 
         * if ( type == null ) { type = s.type(); }
         * 
         * if ( name == null ) { name = s.type(); }
         * 
         * if ( description == null ) { description = "Method [" + s.methodId()
         * + "] " + s.methodName() + ", Step [" + s.step() + "] " +
         * s.stepName(); } }
         */

        if (fillIn) {
            // Concurrent threads may find the same naked CID and attempt to
            // create multiple Studies
            // with the same CID. Therefore we lock the process. But if after
            // 1s, we are still waiting,
            // just give up the fillin request and proceed to allocate the next
            // CID
            if (!lock_.tryLock(1L, TimeUnit.SECONDS))
                fillIn = false;
        }

        try {
            String cid = Study.create(executor(), dEID, studyNumber, studyType, name, description, processed, step,
                    args.booleanValue("allow-incomplete-meta", false), args.element("meta"), dPID, fillIn);
            w.add("id", cid);
        } finally {
            if (fillIn) {
                lock_.unlock();
            }
        }
    }

    private String findMethodStep(ServiceExecutor executor, DistributedAsset dEID, String type) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("type", type);
        dm.add(dEID.asXmlDoc("id"));
        XmlDoc.Element r = executor.execute("om.pssd.ex-method.study.step.find", dm.root());
        return r.value("ex-method/step"); // First one
    }

    private String findStudyType(ServiceExecutor executor, DistributedAsset dEID, String step) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("step", step);
        dm.add(dEID.asXmlDoc("id"));
        XmlDoc.Element r = executor.execute("om.pssd.ex-method.step.describe", dm.root());
        return r.value("ex-method/step/study/type");
    }

    /**
     * Create Study object from ExMethod CID and Step path
     * 
     * @param exMethod
     *            CID
     * @param step
     *            path
     * @return
     * @throws Throwable
     */
    private Study studyFor(String exMethod, String step) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", exMethod);
        dm.add("pdist", 0); // Force local
        XmlDoc.Element r = executor().execute("asset.get", dm.root());

        // nebk: I don't think this code is correct. The input step is an
        // expanded path; what is in
        // the ExMethod is just the steps per subMethod, so the result of this
        // query will be null.
        XmlDoc.Element se = r.element("asset/meta/daris:pssd-method/step[@id='" + step + "']");
        if (se == null) {
            return null;
        }

        String mname = r.value("asset/meta/daris:pssd-method/name");

        String sname = se.value("name");
        String type = se.value("study/type");
        if (type == null) {
            return null;
        }

        return new Study(type, exMethod, mname, step, sname);
    }

}
