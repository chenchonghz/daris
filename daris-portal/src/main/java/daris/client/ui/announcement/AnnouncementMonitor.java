package daris.client.ui.announcement;

import java.util.ArrayList;
import java.util.List;

import arc.mf.event.Filter;
import arc.mf.event.Subscriber;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventChannel;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.announcement.Announcement;
import daris.client.model.announcement.AnnouncementRef;
import daris.client.model.announcement.events.AnnouncementEvent;

public class AnnouncementMonitor {

    private static boolean _started = false;
    private static Subscriber _subscriber;

    public static void start() {
        if (_started) {
            return;
        }

        showLatest();

        if (_subscriber == null) {
            _subscriber = new Subscriber() {

                private List<Filter> _filters;

                @Override
                public List<Filter> systemEventFilters() {
                    if (!_started) {
                        return null;
                    }
                    if (_filters == null) {
                        _filters = new ArrayList<Filter>(1);
                        _filters.add(new Filter(AnnouncementEvent.SYSTEM_EVENT_NAME));
                    }
                    return _filters;
                }

                @Override
                public void process(SystemEvent se) {
                    if (!_started) {
                        return;
                    }
                    if (!(se instanceof AnnouncementEvent)) {
                        return;
                    }
                    AnnouncementEvent ae = (AnnouncementEvent) se;
                    AnnouncementRef a = ae.objectRef();
                    AnnouncementDialog dlg = new AnnouncementDialog(a);
                    dlg.show(0.4, 0.4);
                }
            };
        }
        // SystemEventChannel.subscribe();
        SystemEventChannel.add(_subscriber);
        _started = true;
    }

    public static void stop() {
        if (_subscriber != null) {
            SystemEventChannel.remove(_subscriber);
        }
        _started = false;
    }

    private static void showLatest() {

        Announcement.latest(new ObjectResolveHandler<AnnouncementRef>() {

            @Override
            public void resolved(AnnouncementRef a) {
                if (a == null) {
                    Announcement.Cookie.setLatest(0);
                    return;
                }
                if (a.uid() > Announcement.Cookie.getLasest()) {
                    AnnouncementDialog dlg = new AnnouncementDialog(a);
                    dlg.show(0.4, 0.4);
                }
                Announcement.Cookie.setLatest(a.uid());
            }
        });
    }

}
