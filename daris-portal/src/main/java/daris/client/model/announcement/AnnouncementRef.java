package daris.client.model.announcement;

import java.util.Date;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class AnnouncementRef extends ObjectRef<Announcement> {

    private long _uid;

    private Date _created;
    
    private String _title;

    public AnnouncementRef(long uid, Date created, String title) {
        _uid = uid;
        _created = created;
        _title = title;
    }

    public AnnouncementRef(long uid) {
        this(uid,null, null);
    }

    public long uid() {
        return _uid;
    }
    
    public Date created(){
        return _created;
    }

    public String title() {
        return _title;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        w.add("uid", _uid);
    }

    @Override
    protected String resolveServiceName() {
        return "om.pssd.announcement.describe";
    }

    @Override
    protected Announcement instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            XmlElement ae = xe.element("announcement");
            if (ae != null) {
                Announcement a = new Announcement(ae);
                _title = a.title();
                _created = a.created();
                return a;
            }
        }
        return null;
    }

    @Override
    public String referentTypeName() {
        return "announcement";
    }

    @Override
    public String idToString() {
        return Long.toString(_uid);
    }

}
