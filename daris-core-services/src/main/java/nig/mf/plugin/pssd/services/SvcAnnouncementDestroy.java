package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.announcement.Announcement;
import nig.mf.plugin.pssd.user.Self;
import nig.mf.pssd.plugin.util.PSSDUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcAnnouncementDestroy extends PluginService {
    private Interface _defn;

    public SvcAnnouncementDestroy() {
        _defn = new Interface();
        _defn.add(new Interface.Element("uid", LongType.POSITIVE_ONE,
                "The unique id of the system announcement.", 1, 1));
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
        return "deletes a system announcement.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w)
            throws Throwable {

		long uid = args.longValue("uid");
        Announcement.destroy(executor(), uid);
    }

    @Override
    public String name() {
        return "om.pssd.announcement.destroy";
    }

}
