package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.PSSDObjectEvent;
import nig.mf.plugin.pssd.PSSDObjectEvent.Action;
import nig.mf.plugin.pssd.tag.ProjectSpecificTagDictionary;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.mf.plugin.event.SystemEventChannel;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectTagAdd extends PluginService {

    private Interface _defn;

    public SvcObjectTagAdd() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the object to add tag to.",
                1, 1));

        Interface.Element e = new Interface.Element(
                "tag",
                XmlDocType.DEFAULT,
                "The tags to be added to the object. The tag must exist as a term in the tag dictionary for the project.",
                1, Integer.MAX_VALUE);
        e.add(new Interface.Element("name", StringType.DEFAULT, "The name of the tag.", 1, 1));
        e.add(new Interface.Element("description", StringType.DEFAULT, "The description about the tag.", 0, 1));
        _defn.add(e);
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
        return "Add tags to the specified object.";
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        String cid = args.value("cid");
        ProjectSpecificTagDictionary dict = ProjectSpecificTagDictionary.dictionaryFor(cid);
        if (!dict.exists()) {
            throw new Exception("Tag dictionary for " + PSSDObject.typeOf(cid) + " " + cid + " does not exist.");
        }
        List<XmlDoc.Element> tes = args.elements("tag");
        for (XmlDoc.Element te : tes) {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("cid", cid);
            dm.push("tag");
            dm.add("name", new String[] { "dictionary", dict.name() }, te.value("name"));
            String tagDescription = te.value("description");
            if (tagDescription != null) {
                dm.add("description", tagDescription);
            }
            dm.pop();
            PluginThread.serviceExecutor().execute("asset.tag.add", dm.root());
        }

        /*
         * dispatch system event
         */
        SystemEventChannel.generate(new PSSDObjectEvent(Action.MODIFY, cid, SvcCollectionMemberCount.countMembers(
                executor(), cid)));
    }

    @Override
    public String name() {
        return "om.pssd.object.tag.add";
    }

}
