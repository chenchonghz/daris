package nig.mf.plugin.pssd.announcement;

import java.util.Date;

import nig.mf.plugin.pssd.Application;
import nig.mf.plugin.pssd.UniqueID;
import nig.mf.plugin.pssd.announcement.events.PSSDAnnouncementEvent;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.event.SystemEventChannel;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class Announcement {

    public static final String DOC_TYPE = "daris:pssd-system-announcement";

    private String _assetId;
    private long _uid;
    private String _title;
    private String _text;
    private Date _created;
    private Date _expiry;

    public Announcement(XmlDoc.Element ae) throws Throwable {
        _assetId = ae.value("@id");
        _uid = ae.longValue("meta/" + DOC_TYPE + "/uid");
        _title = ae.value("meta/" + DOC_TYPE + "/title");
        _text = ae.value("meta/" + DOC_TYPE + "/text");
        _created = ae.dateValue("meta/" + DOC_TYPE + "/created");
        _expiry = ae.dateValue("meta/" + DOC_TYPE + "/expiry");
    }

    public String assetId() {
        return _assetId;
    }

    public long uid() {
        return _uid;
    }

    public String title() {
        return _title;
    }

    public String text() {
        return _text;
    }

    public Date created() {
        return _created;
    }

    public Date expiry() {
        return _expiry;
    }

    public static Announcement create(ServiceExecutor executor, String title,
            String text, Date expiry, boolean broadcast) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("namespace", Application.defaultNamespace(executor));
        dm.push("meta");
        dm.push(DOC_TYPE);
        dm.add("uid", UniqueID.next(DOC_TYPE));
        dm.add("title", title);
        dm.add("text", text);
        dm.add("created", new Date());
        if (expiry != null) {
            dm.add("expiry", expiry);
        }
        dm.pop();
        dm.pop();
        String assetId = executor.execute("asset.create", dm.root())
                .value("id");
        Announcement ao = get(executor, assetId);
        if (broadcast) {
            SystemEventChannel.generate(new PSSDAnnouncementEvent(ao));
        }
        return ao;
    }

    public static Announcement get(ServiceExecutor executor, String assetId)
            throws Throwable {

        XmlDoc.Element ae = executor.execute("asset.get",
                "<args><id>" + assetId + "</id></args>", null, null).element(
                "asset");
        if (ae != null) {
            return new Announcement(ae);
        } else {
            throw new NullPointerException(
                    "Could you find the announcement asset. (id=" + assetId
                            + ")");
        }
    }

    public static Announcement get(ServiceExecutor executor, long uid)
            throws Throwable {
        XmlDoc.Element ae = executor
                .execute(
                        "asset.query",
                        "<args><where>xpath(daris:pssd-system-announcement/uid) = "
                                + uid
                                + "</where><action>get-meta</action><size>1</size></args>",
                        null, null).element("asset");
        if (ae != null) {
            return new Announcement(ae);
        } else {
            throw new NullPointerException(
                    "Could you find the announcement asset. (uid=" + uid + ")");
        }
    }

    public static void destroy(ServiceExecutor executor, String assetId)
            throws Throwable {
        executor.execute("asset.destroy", "<args><id>" + assetId
                + "</id></args>", null, null);
    }

    public static void destroy(ServiceExecutor executor, long uid)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("where", "xpath(daris:pssd-system-announcement/uid) = " + uid);
        dm.add("size", 1);
        dm.add("action", "pipe");
        dm.add("service", new String[] { "name", "asset.destroy" });
        executor.execute("asset.query", dm.root());
    }

    public void describe(XmlWriter w) throws Throwable {
        w.push("announcement", new String[] { "asset", assetId() });
        w.add("uid", uid());
        w.add("title", title());
        w.add("text", text());
        w.add("created", created());
        if (expiry() != null) {
            w.add("expiry", expiry());
        }
        w.pop();
    }

}
