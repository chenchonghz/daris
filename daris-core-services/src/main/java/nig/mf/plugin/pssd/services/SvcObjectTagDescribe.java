package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.tag.ProjectSpecificTagDictionary;
import nig.util.ObjectUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectTagDescribe extends PluginService {
    private Interface _defn;

    public SvcObjectTagDescribe() {
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
        return "Describes the tags of the specified object.";
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        String cid = args.value("cid");
        List<XmlDoc.Element> tes = executor().execute("asset.get", "<args><cid>" + cid + "</cid></args>", null, null)
                .elements("asset/tag");
        if (tes != null) {
            for (XmlDoc.Element te : tes) {
                // Includes only the tags from dictionary pssd.<type>.tags.xxx
                if (ObjectUtil.equals(te.value("name/@dictionary"), ProjectSpecificTagDictionary.dictionaryFor(cid)
                        .name())) {
                    w.add(te);
                }
            }
        }
    }

    @Override
    public String name() {
        return "om.pssd.object.tag.describe";
    }

}
