package nig.mf.plugin.pssd.services;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import nig.mf.plugin.pssd.tag.ProjectSpecificTagDictionary;
import nig.util.ObjectUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectTagList extends PluginService {

    private Interface _defn;

    public SvcObjectTagList() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the object.", 1, 1));
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
        Map<String, Long> tagIds = getTagIds(cid);
        if (tagIds != null) {
            for (String tag : tagIds.keySet()) {
                w.add("tag", new String[] { "id", Long.toString(tagIds.get(tag)) }, tag);
            }
        }
    }

    @Override
    public String name() {
        return "om.pssd.object.tag.list";
    }

    public static Map<String, Long> getTagIds(String cid) throws Throwable {
        List<XmlDoc.Element> tes = PluginThread.serviceExecutor()
                .execute("asset.get", "<args><cid>" + cid + "</cid></args>", null, null).elements("asset/tag");
        if (tes != null) {
            TreeMap<String, Long> map = new TreeMap<String, Long>();
            for (XmlDoc.Element te : tes) {
                // Includes only the tags from dictionary pssd.<type>.tags.xxx
                if (ObjectUtil.equals(te.value("name/@dictionary"), ProjectSpecificTagDictionary.dictionaryFor(cid)
                        .name())) {
                    map.put(te.value("name"), te.longValue("@id"));
                }
            }
            return map;
        }
        return null;
    }

}
