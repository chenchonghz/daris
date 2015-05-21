package nig.mf.plugin.pssd.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import nig.mf.plugin.pssd.method.ActionStep;
import nig.mf.plugin.pssd.method.ExMethod;
import nig.mf.plugin.pssd.method.Method;
import nig.mf.pssd.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcExMethodTransformStepExecute extends PluginService {

    private Interface _defn;

    public SvcExMethodTransformStepExecute() {

        _defn = new Interface();

        Interface.Element me = new Interface.Element("id", CiteableIdType.DEFAULT,
                "The identity of the executing ExMethod object.", 1, 1);
        me.add(new Interface.Attribute(
                "proute",
                CiteableIdType.DEFAULT,
                "In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.",
                0));
        _defn.add(me);

        _defn.add(new Interface.Element("step", CiteableIdType.DEFAULT, "The path to the transform step", 1, 1));
        Interface.Element param = new Interface.Element("parameter", StringType.DEFAULT,
                "The parameter for the transform.", 0, Integer.MAX_VALUE);
        param.add(new Interface.Attribute("name", StringType.DEFAULT, "The name of the parameter.", 1));
        _defn.add(param);

        _defn.add(new Interface.Element("iterate", BooleanType.DEFAULT,
                "Set to false will bypass iterating even if the iterator is defined. Defaults to true.", 0, 1));
    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Executes a transform step within the ex-method.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

        boolean iterate = args.booleanValue("iterate", true);

        List<XmlDoc.Element> pes = args.elements("parameter");

        // Find the ExMethod. Can be primary or replica; we are not editingt it
        // here...
        DistributedAsset dEID = new DistributedAsset(args.element("id"));

        // Instantiate ExMethod from asset
        ExMethod em = ExMethod.lookup(executor(), dEID);

        // Regenerate Method object
        Method m = em.method();
        if (m == null) {
            throw new Exception("Failed to find method for ex-method " + em.id() + ".");
        }

        String sid = args.value("step");
        ActionStep as = m.actionStepByPath(sid);
        List<XmlDoc.Element> tes = as.transformActions();
        if (tes != null) {
            for (XmlDoc.Element te : tes) {
                executeTransform(executor(), em.id(), te, pes, iterate, w);

            }
        }

    }

    public static void executeTransform(ServiceExecutor executor, String emid, Element te, List<Element> pes,
            boolean iterate, XmlWriter w) throws Throwable {

        long defnUid = te.longValue("definition");
        int defnVersion = te.intValue("definition/@version", 0);
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("uid", new String[] { "version", Integer.toString(defnVersion) }, defnUid);
        List<XmlDoc.Element> params = new ArrayList<XmlDoc.Element>();
        List<XmlDoc.Element> pes1 = te.elements("parameter");
        if (pes1 != null) {
            params.addAll(pes1);
        }
        if (pes != null) {
            params.addAll(pes);
        }
        for (XmlDoc.Element pe : params) {
            dm.add(pe, true);
        }

        XmlDoc.Element ie = te.element("iterator");
        if (ie != null && iterate) {
            String scope = ie.value("scope");
            String type = ie.value("type");
            String query = ie.value("query");
            String param = ie.value("parameter");
            XmlDocMaker dm1 = new XmlDocMaker("args");
            if ("citeable-id".equals(type)) {
                dm1.add("action", "get-cid");
            } else {
                dm1.add("action", "get-id");
            }
            StringBuilder sb = new StringBuilder();
            if ("project".equals(scope)) {
                String projectCid = CiteableIdUtil.getParentId(emid, 2);
                sb.append("((cid starts with '" + projectCid + "') or (cid='" + projectCid + "'))");
            } else if ("subject".equals(scope)) {
                String subjectCid = CiteableIdUtil.getParentId(emid);
                sb.append("((cid starts with '" + subjectCid + "') or (cid='" + subjectCid + "'))");
            } else {
                sb.append("((cid starts with '" + emid + "') or (cid='" + emid + "'))");
            }
            sb.append(" and (" + query + ")");
            dm1.add("where", sb.toString());
            XmlDoc.Element r = executor.execute("asset.query", dm1.root());
            Collection<String> ids = null;
            if ("citeable-id".equals(type)) {
                ids = r.values("cid");
            } else {
                ids = r.values("id");
            }
            if (ids != null) {
                for (String id : ids) {
                    List<XmlDoc.Element> params2 = new Vector<XmlDoc.Element>();
                    params2.addAll(params);
                    XmlDoc.Element pe = new XmlDoc.Element("parameter");
                    pe.add(new XmlDoc.Attribute("name", param));
                    pe.setValue(id);
                    params2.add(pe);
                    long tuid = executeTransform(executor, defnUid, defnVersion, params2);
                    w.add("tuid", tuid);
                }
            }
        } else {
            long tuid = executeTransform(executor, defnUid, defnVersion, params);
            w.add("tuid", tuid);
        }

    }

    private static long executeTransform(ServiceExecutor executor, long defnUid, int defnVersion,
            List<XmlDoc.Element> pes) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("execute", true);
        dm.add("definition", new String[] { "version", Integer.toString(defnVersion) }, defnUid);
        if (pes != null) {
            for (XmlDoc.Element pe : pes) {
                dm.add(pe, true);
            }
        }
        XmlDoc.Element r = executor.execute("transform.create", dm.root());
        long tuid = r.longValue("uid");
        return tuid;
    }

    @Override
    public String name() {
        return "om.pssd.ex-method.transform.step.execute";
    }

}
