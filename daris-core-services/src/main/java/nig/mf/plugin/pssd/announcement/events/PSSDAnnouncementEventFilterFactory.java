package nig.mf.plugin.pssd.announcement.events;


public class PSSDAnnouncementEventFilterFactory implements
        arc.event.FilterFactory {

    public static PSSDAnnouncementEventFilterFactory INSTANCE = new PSSDAnnouncementEventFilterFactory();

    private PSSDAnnouncementEventFilterFactory() {

    }

    @Override
    public arc.event.Filter create(String id, boolean descend) {

        return new PSSDAnnouncementEvent.Filter(id);
    }

}
