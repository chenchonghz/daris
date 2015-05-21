package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDataSetProcessedDestroyableExists extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.dataset.processed.destroyable.exists";
    public static final String SERVICE_DESCRIPTION = "Perform a query to detect whether there are processed data sets within the specified object that can be destroyed by the current user. (Note: Returns true if the given object itself is a destroyable processed data set.)";

    private Interface _defn;

    public SvcDataSetProcessedDestroyableExists() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable identifier that may contain processed data sets.", 1, 1));
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
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        String cid = args.value("cid");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", SvcDataSetProcessedCount.queryForProcessedDataSets(cid));
        dm.add("size", "infinity");
        dm.add("action", "get-cid");
        Collection<String> cids = executor().execute("asset.query", dm.root()).values("cid");
        boolean exists = false;
        if (cids != null && !cids.isEmpty()) {
            for (String id : cids) {
                boolean canDestroy = SvcUserCanDestroy.canDestroy(executor(), id);
                if (canDestroy) {
                    exists = true;
                    break;
                }
            }
        }
        w.add("exists", exists);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
