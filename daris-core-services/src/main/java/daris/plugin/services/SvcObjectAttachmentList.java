package daris.plugin.services;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcObjectAttachmentList extends PluginService {

    public static final String SERVICE_NAME = "daris.object.attachment.list";

    private Interface _defn;

    public SvcObjectAttachmentList() {
        _defn = new Interface();

        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the object.", 0, 1));
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the object.", 0, 1));

    }

    public String name() {
        return SERVICE_NAME;
    }

    public String description() {
        return "List all attachments of the specified object.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_ACCESS;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        SimpleEntry<String, String> ids = ServiceUtils.getObjectIdentifiers(executor(), args);
        String id = ids.getKey();
        List<XmlDoc.Element> aaes = executor().execute("asset.get",
                "<args><id>" + id + "</id><get-related-meta>true</get-related-meta></args>", null, null)
                .elements("asset/related[@type='" + SvcObjectAttach.RELATIONSHIP_TYPE + "']/asset");
        if (aaes != null) {
            for (XmlDoc.Element aae : aaes) {
                w.push("attachment", new String[] { "id", aae.value("@id") });
                w.add("name", aae.value("name"));
                String desc = aae.value("description");
                if (desc != null) {
                    w.add("description", desc);
                }
                w.add(aae.element("content/type"), true);
                w.add(aae.element("content/size"), true);
                w.pop();
            }
        }
    }
}