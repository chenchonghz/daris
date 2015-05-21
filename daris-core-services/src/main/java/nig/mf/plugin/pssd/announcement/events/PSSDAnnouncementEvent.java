package nig.mf.plugin.pssd.announcement.events;

import nig.mf.plugin.pssd.announcement.Announcement;
import arc.xml.XmlWriter;

public class PSSDAnnouncementEvent extends arc.event.Event {

    public static final String EVENT_TYPE = "pssd-announcement";

    public static class Filter implements arc.event.Filter {

        private String _uid;

        public Filter(String uid) {

            _uid = uid;
        }

        public Filter() {

            this(null);
        }

        public boolean accept(arc.event.Event e) {

            if (!(e instanceof PSSDAnnouncementEvent)) {
                return false;
            }
            if (!(e.type().equals(PSSDAnnouncementEvent.EVENT_TYPE))) {
                return false;
            }
            PSSDAnnouncementEvent pae = (PSSDAnnouncementEvent) e;
            if (_uid != null) {
                return _uid.equals(pae.uid());
            } else {
                return true;
            }
        }
    }

    private String _uid;

    public PSSDAnnouncementEvent(String uid) {

        super(EVENT_TYPE, true);
        _uid = uid;
    }

    public PSSDAnnouncementEvent(long uid) {
        this(Long.toString(uid));
    }
    
    public PSSDAnnouncementEvent(Announcement a) {
        this(a.uid());
    }

    public String uid() {

        return _uid;
    }

    @Override
    public boolean equals(arc.event.Event e) {

        if (!super.equals(e)) {
            return false;
        }
        if (!(e instanceof PSSDAnnouncementEvent)) {
            return false;
        }
        PSSDAnnouncementEvent pae = (PSSDAnnouncementEvent) e;
        return _uid.equals(pae.uid());
    }

    @Override
    protected void saveState(XmlWriter w) throws Throwable {

        w.add("uid", _uid);
    }
}
