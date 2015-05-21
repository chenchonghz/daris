package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.PSSDObjectEvent;
import nig.mf.plugin.pssd.PSSDObjectEvent.Action;
import nig.mf.plugin.pssd.tag.ProjectSpecificTagDictionary;
import nig.util.ObjectUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.event.SystemEventChannel;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectTagRemoveAll extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.object.tag.remove.all";
    public static final String SERVICE_DESCRIPTION = "Remove all tags from the given object.";

    private Interface _defn;

    public SvcObjectTagRemoveAll() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the object to remove tag from.", 1, 1));
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
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        String cid = args.value("cid");
        List<XmlDoc.Element> tes = executor().execute("asset.get",
                "<args><cid>" + cid + "</cid></args>", null, null).elements("asset/tag");
        boolean removedAtLeastOneTag = false;
        if (tes != null) {
            for (XmlDoc.Element te : tes) {
                String tagName = te.value("name");
                long tagId = te.longValue("@id");
                // Includes only the tags from dictionary pssd.<type>.tags.xxx
                if (ObjectUtil.equals(te.value("name/@dictionary"), ProjectSpecificTagDictionary
                        .dictionaryFor(cid).name())) {
                    SvcObjectTagRemove.removeTag(executor(), cid, tagName, tagId);
                    removedAtLeastOneTag = true;
                }
            }
        }

        /*
         * dispatch system event
         */
        if (removedAtLeastOneTag) {
            SystemEventChannel.generate(new PSSDObjectEvent(Action.MODIFY, cid,
                    SvcCollectionMemberCount.countMembers(executor(), cid)));
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
