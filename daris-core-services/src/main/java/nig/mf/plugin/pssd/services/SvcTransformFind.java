package nig.mf.plugin.pssd.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nig.mf.plugin.pssd.transform.Transform;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcTransformFind extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.transform.find";
    public static final long DEFAULT_IDX = 1;
    public static final int DEFAULT_SIZE = 100;

    private Interface _defn;

    public SvcTransformFind() {
        _defn = new Interface();
        Interface.Element e = new Interface.Element("definition", IntegerType.POSITIVE_ONE,
                "The unique id of the transform definition.", 1, 1);
        e.add(new Interface.Attribute("version", IntegerType.POSITIVE,
                "The version of the transform definition. Defaults to the latest.", 0));
        _defn.add(e);

        _defn.add(new Interface.Element(
                "scope",
                CiteableIdType.DEFAULT,
                "The citeable id of the parent object that the transform output data sets belongs to. If not specified, all the instances of the transform definition will be returned.",
                0, 1));

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
        return "Finds all the transform instances of the given definition within the context that the transforms output to the data sets belongs to the specified scope(parent).";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        long defnId = args.longValue("definition");
        int defnVersion = args.intValue("definition/version", 0);
        String scope = args.value("scope");
        long idx = args.longValue("idx", DEFAULT_IDX);
        int size = args.intValue("size", DEFAULT_SIZE);

        /*
         * final all transforms from the definition
         */
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("definition", new String[] { "version", defnVersion > 0 ? Integer.toString(defnVersion) : null }, defnId);
        dm.add("size", "infinity");
        // TODO: see other people's transforms?
        // dm.add("self", false);
        Collection<Long> tuids1 = executor().execute(Transform.SERVICE_TRANSFORM_LIST, dm.root()).longValues(
                "transform/@uid");
        if (tuids1 == null || tuids1.isEmpty()) {
            return;
        }

        if (scope == null) {
            describe(executor(), tuids1, idx, size, w);
            return;
        }

        /*
         * find all tuids from the output datasets.
         */
        dm = new XmlDocMaker("args");

        StringBuilder sb = new StringBuilder();
        sb.append("(cid='" + scope + "' or cid starts with '" + scope + "') ");
        sb.append(" and model='om.pssd.dataset' and xpath(daris:pssd-transform/tuid) has value");
        dm.add("where", sb.toString());
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
         * 
         */
        describe(executor(), tuidSet1, idx, size, w);

    }

    public static void describe(ServiceExecutor executor, Collection<Long> transformIds, long startIndex, int size,
            XmlWriter w) throws Throwable {
        int total = transformIds.size();
        if (startIndex > total) {
            w.push("cursor");
            w.add("count", 0);
            w.add("from", 0);
            w.add("to", 0);
            w.add("total", new String[] { "complete", Boolean.toString(true) }, 0);
            w.add("remaining", 0);
            return;
        }
        long endIndex = startIndex - 1 + size;
        if (endIndex > total) {
            endIndex = total;
        }
        List<Long> tuids = new ArrayList<Long>(transformIds);
        Collections.sort(tuids);
        tuids = tuids.subList((int) startIndex - 1, (int) endIndex);
        for (long tuid : tuids) {
            XmlDoc.Element e = executor.execute(Transform.SERVICE_TRANSFORM_DESCRIBE,
                    "<args><uid>" + tuid + "</uid></args>", null, null).element("transform");
            if (e != null) {
                w.add(e, true);
            }
        }
        w.push("cursor");
        w.add("count", endIndex - startIndex + 1);
        w.add("from", startIndex);
        w.add("to", endIndex);
        if (startIndex > 1) {
            w.add("prev", startIndex - size < 0 ? 1 : startIndex - size + 1);
        }
        if (endIndex < total) {
            w.add("next", endIndex + 1);
        }
        w.add("total", new String[] { "complete", Boolean.toString(true) }, total);
        w.add("remaining", total > endIndex ? total - endIndex : 0);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
