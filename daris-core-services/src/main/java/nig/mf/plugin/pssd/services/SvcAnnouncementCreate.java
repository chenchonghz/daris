package nig.mf.plugin.pssd.services;

import java.util.Date;

import nig.mf.plugin.pssd.announcement.Announcement;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.DateType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcAnnouncementCreate extends PluginService {

    private Interface _defn;

    public SvcAnnouncementCreate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("title", StringType.DEFAULT,
                "the title of the announcement", 1, 1));
        _defn.add(new Interface.Element("text", StringType.DEFAULT,
                "the text content of the announcement.", 1, 1));
        _defn.add(new Interface.Element("expiry", DateType.DEFAULT,
                "the expected date when the announcement expires.", 0, 1));
        _defn.add(new Interface.Element(
                "broadcast",
                BooleanType.DEFAULT,
                "set to true to broadcast the announcement via system event channel so that all the clients subscribe to the system events can display the announcement automatically. Defaults to false.",
                0, 1));
    }

    @Override
    public Access access() {
        return ACCESS_ADMINISTER;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Create and/or broadcast system announcement.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {

		String title = args.value("title");
        String text = args.value("text");
        Date expiry = args.dateValue("expiry");
        boolean broadcast = args.booleanValue("broadcast");
        Announcement a = Announcement.create(executor(), title, text, expiry,
                broadcast);
        w.add("announcement", new String[] { "uid", Long.toString(a.uid()),
                "asset", a.assetId() });
    }

    @Override
    public String name() {
        return "om.pssd.announcement.create";
    }

}
