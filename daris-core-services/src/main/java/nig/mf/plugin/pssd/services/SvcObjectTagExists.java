package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectTagExists extends PluginService {

    private Interface _defn;

    public SvcObjectTagExists() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the object.", 1, 1));
        _defn.add(new Interface.Element("tag", StringType.DEFAULT, "The tag name.", 1, 1));
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
        return "List the tags of the specified object.";
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        String cid = args.value("cid");
        String tag = args.value("tag");
        w.add("exists", new String[] { "tag", tag }, tagExists(cid, tag));
    }

    @Override
    public String name() {
        return "om.pssd.object.tag.exists";
    }

    public static boolean tagExists(String cid, String tag) throws Throwable {
        Collection<String> tags = PluginThread.serviceExecutor()
                .execute("asset.get", "<args><cid>" + cid + "</cid></args>", null, null).values("asset/tag/name");
        if (tags != null) {
            return tags.contains(tag);
        }
        return false;
    }
}
