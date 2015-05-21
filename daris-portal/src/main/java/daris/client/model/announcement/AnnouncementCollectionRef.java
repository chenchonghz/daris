package daris.client.model.announcement;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.OrderedCollectionRef;

public class AnnouncementCollectionRef extends
        OrderedCollectionRef<AnnouncementRef> {

    private long _startOffset;
    private int _pageSize;

    public AnnouncementCollectionRef(long startOffset, int pageSize) {
        _startOffset = startOffset;
        _pageSize = pageSize;
    }

    public AnnouncementCollectionRef() {
        this(0, 5);
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w, long start, int size,
            boolean count) {
        w.add("idx", start + _startOffset + 1);
        w.add("size", size);

        if (count) {
            w.add("count", count);
        }

    }

    @Override
    protected String resolveServiceName() {
        return "om.pssd.announcement.list";
    }

    @Override
    protected AnnouncementRef instantiate(XmlElement ae) throws Throwable {
        return new AnnouncementRef(ae.longValue("@uid"),
                ae.dateValue("@created"), ae.value("@title"));
    }

    @Override
    protected String referentTypeName() {
        return "announcement";
    }

    @Override
    protected String[] objectElementNames() {
        return new String[] { "announcement" };
    }

    @Override
    protected long total(XmlElement xe) throws Throwable {
        return xe.longValue("cursor/total", 0);
    }

    @Override
    public boolean canResolveEntireCollection() {
        return true;
    }

    @Override
    public int defaultPagingSize() {
        return _pageSize;
    }

    public void setDefaultPagingSize(int size) {
        if (_pageSize != size) {
            _pageSize = size;
            reset();
        }
    }

    public int numberOfPages() {
        long total = totalNumberOfMembers();
        if (total > 0) {
            int size = pagingSize();
            int nop = (int) total / size;
            return total % size == 0 ? nop : nop + 1;
        } else {
            return 0;
        }
    }

    public long startOffset() {
        return _startOffset;
    }

    public void setStartOffset(long offset) {
        if (_startOffset != offset) {
            _startOffset = offset;
            reset();
        }
    }

}
