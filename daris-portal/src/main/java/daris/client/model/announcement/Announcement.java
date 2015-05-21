package daris.client.model.announcement;

import java.util.Date;
import java.util.List;

import arc.mf.client.util.DateTime;
import arc.mf.client.xml.XmlElement;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.session.Session;

import com.google.gwt.user.client.Cookies;

import daris.client.model.announcement.messages.AnnouncementList;

public class Announcement {

    public static class Cookie {
        public static final String LATEST = "pssd-announcement-latest";
        public static final int EXPIRE_IN_DAYS = 30;

        public static long getLasest() {
            String uid = Cookies.getCookie(Session.domainName() + ":" + Session.userName() + ":" + LATEST);
            if (uid != null) {
                try {
                    return Long.parseLong(uid);
                } catch (Throwable e) {
                    return 0;
                }
            }
            return 0;
        }

        public static void setLatest(long id) {
            Date expiry = new Date();
            DateTime.addDays(expiry, EXPIRE_IN_DAYS);
            Cookies.setCookie(Session.domainName() + ":" + Session.userName() + ":" + LATEST, Long.toString(id), expiry);
        }
    }

    public static void latest(final ObjectResolveHandler<AnnouncementRef> rh) {
        new AnnouncementList(1, 1).send(new ObjectMessageResponse<List<AnnouncementRef>>() {

            @Override
            public void responded(List<AnnouncementRef> as) {
                if (as == null || as.isEmpty()) {
                    rh.resolved(null);
                    return;
                }
                rh.resolved(as.get(0));
            }
        });
    }

    private String _assetId;
    private long _uid;
    private String _title;
    private String _text;
    private Date _created;
    private Date _expiry;

    public Announcement(XmlElement ae) throws Throwable {
        _assetId = ae.value("@id");
        _uid = ae.longValue("uid");
        _title = ae.value("title");
        _text = ae.value("text");
        _created = ae.dateValue("created");
        _expiry = ae.dateValue("expiry");
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

}
