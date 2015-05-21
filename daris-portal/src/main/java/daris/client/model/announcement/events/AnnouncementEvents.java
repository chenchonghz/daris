package daris.client.model.announcement.events;

import arc.mf.client.xml.XmlElement;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventFactory;
import arc.mf.event.SystemEventRegistry;
import daris.client.model.announcement.AnnouncementRef;

public class AnnouncementEvents {
    private static boolean _init = false;

    public static void initialize() {
        if (_init) {
            return;
        }

        SystemEventRegistry.add(AnnouncementEvent.SYSTEM_EVENT_NAME, new SystemEventFactory() {
            public SystemEvent instantiate(String type, XmlElement ee) throws Throwable {
                long uid = ee.longValue("uid");
                return new AnnouncementEvent(new AnnouncementRef(uid));
            }
        });

        _init = true;
    }
}