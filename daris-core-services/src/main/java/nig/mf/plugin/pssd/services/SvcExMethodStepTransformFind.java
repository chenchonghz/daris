package nig.mf.plugin.pssd.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nig.mf.plugin.pssd.transform.Transform;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcExMethodStepTransformFind extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.ex-method.step.transform.find";
    public static final long DEFAULT_IDX = 1;
    public static final int DEFAULT_SIZE = 100;

    private Interface _defn;

    public SvcExMethodStepTransformFind() {
        _defn = new Interface();
        Interface.Element me = new Interface.Element("id", CiteableIdType.DEFAULT,
                "The identity of the executing ExMethod.", 1, 1);
        me.add(new Interface.Attribute(
                "proute",
                CiteableIdType.DEFAULT,
                "In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.",
                0));
        _defn.add(me);

        _defn.add(new Interface.Element("step", CiteableIdType.DEFAULT, "The step within the method to describe.", 1, 1));

        _defn.add(new Interface.Element("size", IntegerType.DEFAULT,
                "The size of the result collection. Defaults to 100.", 0, 1));
        _defn.add(new Interface.Element("idx", LongType.DEFAULT,
                "Absolute cursor position. Starts from 1. If used, the cursor will be positioned starting at 'idx'.",
                0, 1));
    }

    @Override
    public Access access() {
        return ACCESS_ACCESS;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Find the transforms initiated from the given step by looking for the tuids (transform uids) of the result data sets within the given ex-method.";
    }

    @Override
    public void execute(Element args, Inputs in, Outputs arg2, XmlWriter w) throws Throwable {
        String id = args.value("id");
        String step = args.value("step");
        long idx = args.longValue("idx", DEFAULT_IDX);
        int size = args.intValue("size", DEFAULT_SIZE);

        /*
         * find the transform definition associated with the step
         */
        XmlDoc.Element re = executor().execute(SvcExMethodStepDescribe.SERVICE_NAME,
                "<args><id>" + id + "</id><step>" + step + "</step></args>", null, null);
        XmlDoc.Element te = re.element("ex-method/step/transform");
        if (te == null) {
            throw new Exception("Step " + step + " in ex-method " + id + " is not a transform step.");
        }
        long defnId = te.longValue("definition");
        int defnVersion = te.intValue("definition/@version", 0);

        /*
         * find all the transforms from the definition.
         */
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("definition", new String[] { "version", defnVersion > 0 ? Integer.toString(defnVersion) : null }, defnId);
        dm.add("size", "infinity");
        // TODO: see other people's transforms?
        // dm.add("self", false);
        List<Long> tuids1 = executor().execute(Transform.SERVICE_TRANSFORM_LIST, dm.root())
                .longValues("transform/@uid");
        if (tuids1 == null || tuids1.isEmpty()) {
            return;
        }

        /*
         * find all tuids from the output datasets.
         */
        dm = new XmlDocMaker("args");
        dm.add("where", "cid starts with '" + id
                + "' and model='om.pssd.dataset' and xpath(daris:pssd-transform/tuid) has value");
        dm.add("size", "infinity");
        dm.add("xpath", new String[] { "ename", "tuid" }, "meta/daris:pssd-transform/tuid");
        dm.add("action", "get-value");
        List<Long> tuids2 = executor().execute("asset.query", dm.root()).longValues("asset/tuid");
        if (tuids2 == null || tuids2.isEmpty()) {
            return;
        }

        /*
         * intersection
         */
        Set<Long> tuidSet1 = new HashSet<Long>();
        tuidSet1.addAll(tuids1);

        Set<Long> tuidSet2 = new HashSet<Long>();
        tuidSet2.addAll(tuids2);

        if (!tuidSet1.retainAll(tuidSet2)) {
            return;
        }

        if (tuidSet1.isEmpty()) {
            return;
        }

        /*
         * output
         */
        SvcTransformFind.describe(executor(), tuidSet1, idx, size, w);

    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
