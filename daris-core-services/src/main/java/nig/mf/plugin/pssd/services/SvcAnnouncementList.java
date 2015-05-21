package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.announcement.Announcement;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAnnouncementList extends PluginService {

    private Interface _defn;

    public SvcAnnouncementList() {
        _defn = new Interface();
        _defn.add(new Interface.Element("size", IntegerType.DEFAULT,
                "The size of the list. Defaults to 100.", 0, 1));
        _defn.add(new Interface.Element(
                "idx",
                LongType.DEFAULT,
                "Absolute cursor position. Starts from 1. If used, the cursor will be positioned starting at 'idx'.",
                0, 1));
        _defn.add(new Interface.Element("count", BooleanType.DEFAULT,
                "If set, returns the number of matches in the set.", 0, 1));
        _defn.add(new Interface.Element("order", new EnumType(new String[] {
                "desc", "asc" }),
                "Sort ascending or descending. Default is descending.", 0, 1));
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
        return "list system announcements.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String idx = args.value("idx");
        String size = args.value("size");
        String order = args.stringValue("order", "desc");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", Announcement.DOC_TYPE + " has value");
        if (size != null) {
            dm.add("size", size);
        }
        if (idx != null) {
            dm.add("idx", idx);
        }
        if (args.value("count") != null) {
            dm.add("count", args.booleanValue("count"));
        }
        dm.push("sort");
        dm.add("key", "meta/" + Announcement.DOC_TYPE + "/uid");
        dm.add("order", order);
        dm.pop();
        dm.add("action", "get-value");
        dm.add("xpath", new String[] { "ename", "uid" }, "meta/"
                + Announcement.DOC_TYPE + "/uid");
        dm.add("xpath", new String[] { "ename", "created" }, "meta/"
                + Announcement.DOC_TYPE + "/created");
        dm.add("xpath", new String[] { "ename", "title" }, "meta/"
                + Announcement.DOC_TYPE + "/title");
        XmlDoc.Element r = executor().execute("asset.query", dm.root());
        List<XmlDoc.Element> aes = r.elements("asset");
        if (aes != null) {
            for (XmlDoc.Element ae : aes) {
                w.add("announcement",
                        new String[] { "uid", ae.value("uid"), "created",
                                ae.value("created"), "title",
                                ae.value("title") });
            }
        }
        XmlDoc.Element ce = r.element("cursor");
        if (ce != null) {
            w.add(ce);
        }
    }

    @Override
    public String name() {
        return "om.pssd.announcement.list";
    }

}
