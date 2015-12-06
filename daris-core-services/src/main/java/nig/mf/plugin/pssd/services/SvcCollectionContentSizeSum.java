package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
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
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", "cid='" + cid + "' or cid starts with '" + cid + "'");
        dm.add("action", "sum");
        dm.add("xpath", "content/size");
        long totalSize = executor().execute("asset.query", dm.root())
                .longValue("value");
        w.add("size", totalSize);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
