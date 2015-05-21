package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDataSetProcessedDestroy extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.dataset.processed.destroy";
    public static final String SERVICE_DESCRIPTION = "Destroys all the processed datasets within the specified object.";

    private Interface _defn;

    public SvcDataSetProcessedDestroy() {
        _defn = new Interface();
        _defn.add(new Interface.Element(
                "cid",
                CiteableIdType.DEFAULT,
                "The citeable identifier of the containing project/subject/ex-method/study object, or the dataset itself.",
                1, 1));
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
        return SERVICE_DESCRIPTION;
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        String pid = args.value("cid");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("action", "get-cid");
        dm.add("size", "infinity");
        StringBuilder sb = new StringBuilder();
        sb.append("(cid starts with '" + pid + "' or cid='" + pid + "')");
        sb.append(" and model='om.pssd.dataset' and xpath(daris:pssd-derivation/processed)=true");
        sb.append(" and not (type='dicom/series' or mf-dicom-series has value)");
        dm.add("where", sb.toString());
        Collection<String> cids = executor().execute("asset.query", dm.root()).values("cid");

        int destroyed = 0;
        int failed = 0;
        int total = cids == null ? 0 : cids.size();
        if (cids != null) {
            for (String cid : cids) {
                boolean canDestroy = executor().execute("om.pssd.user.can.destroy",
                        "<args><cid>" + cid + "</cid></args>", null, null).booleanValue("can");
                if (canDestroy) {
                    try {
                        executor().execute("om.pssd.object.destroy",
                                "<args><cid>" + cid + "</cid></args>", null, null);
                    } catch (Throwable e) {
                        failed++;
                        e.printStackTrace(System.out);
                    }
                    destroyed++;
                } else {
                    failed++;
                }
            }
        }
        w.add("destroyed",
                new String[] { "total", Integer.toString(total), "failed", Integer.toString(failed) },
                destroyed);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
