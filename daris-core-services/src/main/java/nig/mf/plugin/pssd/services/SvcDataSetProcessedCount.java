package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDataSetProcessedCount extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.dataset.processed.count";
    public static final String SERVICE_DESCRIPTION = "Counts the processed data sets in the specified object.";

    private Interface _defn;

    public SvcDataSetProcessedCount() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable identifier of the object that may contain processed data sets.", 1, 1));
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
        return SERVICE_DESCRIPTION;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        String cid = args.value("cid");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", SvcDataSetProcessedCount.queryForProcessedDataSets(cid));
        dm.add("size", "infinity");
        dm.add("action", "count");
        w.add(executor().execute("asset.query", dm.root()).element("value"), true);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    public static String queryForProcessedDataSets(String cid) {
        StringBuilder sb = new StringBuilder("(cid starts with '");
        sb.append(cid);
        sb.append("' or cid='");
        sb.append(cid);
        sb.append("') and model='om.pssd.dataset' and xpath(daris:pssd-derivation/processed)=true and not (mf-dicom-series has value or type='dicom/series')");
        return sb.toString();
    }
}
