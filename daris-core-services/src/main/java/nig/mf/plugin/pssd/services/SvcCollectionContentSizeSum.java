package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcCollectionContentSizeSum extends PluginService {

    public static final String SERVICE_NAME = "daris.collection.content.size.sum";

    private Interface _defn;

    public SvcCollectionContentSizeSum() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the root/parent object."));
        _defn.add(new Interface.Element("where", StringType.DEFAULT,
                "the query to filter/find the objects to be included.", 0, 1));
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
        return "Sum up the total content size of the objects in the specified collection.";
    }

    @Override
    public void execute(Element args, Inputs i, Outputs o, XmlWriter w)
            throws Throwable {
        String cid = args.value("cid");
        String where = args.value("where");
        long totalSize = sumContentSize(executor(), cid, where);
        w.add("size", totalSize);
    }

    public static long sumContentSize(ServiceExecutor executor, String cid,
            String where) throws Throwable {
        StringBuilder sb = new StringBuilder(
                "(cid='" + cid + "' or cid starts with '" + cid + "')");
        if (where != null) {
            sb.append(" and (");
            sb.append(where);
            sb.append(")");
        }
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", sb.toString());
        dm.add("action", "sum");
        dm.add("xpath", "content/size");
        long totalSize = executor.execute("asset.query", dm.root())
                .longValue("value");
        return totalSize;
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
