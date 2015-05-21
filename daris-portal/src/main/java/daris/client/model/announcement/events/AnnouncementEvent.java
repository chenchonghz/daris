package daris.client.model.announcement.events;

import arc.mf.client.util.ObjectUtil;
import arc.mf.event.Filter;
import arc.mf.event.SystemEvent;
import daris.client.model.announcement.AnnouncementRef;

public class AnnouncementEvent extends SystemEvent {

    public static final String SYSTEM_EVENT_NAME = "pssd-announcement";

    private AnnouncementRef _o;

    public AnnouncementEvent(AnnouncementRef o) {

        super(SYSTEM_EVENT_NAME, Long.toString(o.uid()));
        _o = o;
    }

    public AnnouncementRef objectRef() {
        return _o;
    }

    @Override
    public boolean matches(Filter f) {

        if (!type().equals(f.type())) {
            return false;
        }

        if (f.object() != null) {
            if (!ObjectUtil.equals(object(), f.object())) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "announcement: " + _o.uid();
    }

}