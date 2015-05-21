package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDataSetProcessedExists extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.dataset.processed.exists";
    public static final String SERVICE_DESCRIPTION = "Checks if there are the processed data sets in the specified object.";

    private Interface _defn;

    public SvcDataSetProcessedExists() {
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
        dm.add("where", SvcDataSetProcessedExists.queryForProcessedDataSets(cid));
        dm.add("size", "infinity");
        dm.add("action", "count");
        int count = executor().execute("asset.query", dm.root()).intValue("value", 0);
        w.add("exists", new String[] { "count", Integer.toString(count) }, count > 0);
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
        sb.append("') and xpath(daris:pssd-derivation/processed)=true and not (mf-dicom-series has value or type='dicom/series')");
        return sb.toString();
    }
}
