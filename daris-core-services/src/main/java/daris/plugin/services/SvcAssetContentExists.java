package daris.plugin.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcAssetContentExists extends PluginService {

    public static final String SERVICE_NAME = "daris.asset.content.exists";

    private Interface _defn;

    public SvcAssetContentExists() throws Throwable {

        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the asset.", 0,
                Integer.MAX_VALUE));
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The id of the asset.", 0, Integer.MAX_VALUE));
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

        return "Check if the specified asset(s) has/have contents.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

        Collection<String> cids = args.values("cid");
        if (cids != null) {
            for (String cid : cids) {
                XmlDoc.Element ae = executor().execute("asset.get", "<args><cid>" + cid + "</cid></args>", null, null)
                        .element("asset");
                boolean exists = ae.elementExists("content");
                String id = ae.value("@id");
                w.add("exists", new String[] { "id", id, "cid", cid }, exists);
            }
        }
        Collection<String> ids = args.values("id");
        if (ids != null) {
            for (String id : ids) {
                XmlDoc.Element ae = executor().execute("asset.get", "<args><id>" + id + "</id></args>", null, null)
                        .element("asset");
                boolean exists = ae.elementExists("content");
                String cid = ae.value("cid");
                w.add("exists", new String[] { "id", id, "cid", cid }, exists);
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
