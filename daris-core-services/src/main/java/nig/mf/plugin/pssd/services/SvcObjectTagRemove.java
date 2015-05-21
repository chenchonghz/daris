package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.Map;

import nig.mf.plugin.pssd.PSSDObjectEvent;
import nig.mf.plugin.pssd.PSSDObjectEvent.Action;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.event.SystemEventChannel;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectTagRemove extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.object.tag.remove";
    public static final String SERVICE_DESCRIPTION = "Remove specific tags from the given object.";

    private Interface _defn;

    public SvcObjectTagRemove() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
                "The citeable id of the object to remove tag from.", 1, 1));
        _defn.add(new Interface.Element("tag", StringType.DEFAULT,
                "The tags to be removed from the object.", 1, Integer.MAX_VALUE));
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
        Collection<String> tags = args.values("tag");
        for (String tag : tags) {
            removeTag(executor(), cid, tag);
        }

        /*
         * dispatch system event
         */
        SystemEventChannel.generate(new PSSDObjectEvent(Action.MODIFY, cid,
                SvcCollectionMemberCount.countMembers(executor(), cid)));
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    public static void removeTag(ServiceExecutor executor, String cid, String tagName, long tagId)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", cid);
        dm.add("tid", tagId);
        PluginThread.serviceExecutor().execute("asset.tag.remove", dm.root());
    }

    public static void removeTag(ServiceExecutor executor, String cid, String tagName)
            throws Throwable {
        long tagId = getTagId(cid, tagName);
        if (tagId < 0) {
            throw new Exception("Tag " + tagName + " does not exist on object " + cid + ".");
        }
        removeTag(executor, cid, tagName, tagId);
    }

    public static long getTagId(String cid, String tagName) throws Throwable {
        Map<String, Long> map = SvcObjectTagList.getTagIds(cid);
        if (map != null) {
            Long id = map.get(tagName);
            if (id != null) {
                return id;
            }
        }
        return -1;
    }

}
