package daris.client.model.announcement.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.announcement.AnnouncementRef;

public class AnnouncementList extends ObjectMessage<List<AnnouncementRef>> {

    private long _idx = 1;
    private int _size = 100;
    private boolean _ascend = false;
    private boolean _count = false;

    public AnnouncementList(long idx, int size, boolean ascend, boolean count) {
        _idx = idx;
        _size = size;
        _ascend = ascend;
        _count = count;
    }

    public AnnouncementList(long idx, int size) {
        this(idx, size, false, false);
    }

    public AnnouncementList() {
        this(1, 100, false, false);
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        if (_count) {
            w.add("count", _count);
        }
        if (_idx > 0) {
            w.add("idx", _idx);
        }
        if (_size > 0) {
            w.add("size", _size);
        }
        if (_ascend) {
            w.add("order", "asc");
        }
    }

    @Override
    protected String messageServiceName() {
        return "om.pssd.announcement.list";
    }

    @Override
    protected List<AnnouncementRef> instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            List<XmlElement> aes = xe.elements("announcement");
            if (aes != null) {
                List<AnnouncementRef> as = new ArrayList<AnnouncementRef>(
                        aes.size());
                for (XmlElement ae : aes) {
                    as.add(new AnnouncementRef(ae.longValue("@uid"), ae
                            .dateValue("@created"), ae.value("@title")));
                }
                return as;
            }
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return "announcement";
    }

    @Override
    protected String idToString() {
        return null;
    }

}
