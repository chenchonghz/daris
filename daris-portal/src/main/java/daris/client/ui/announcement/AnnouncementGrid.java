package daris.client.ui.announcement;

import java.util.Date;
import java.util.List;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridDataSource;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.Transformer;

import com.google.gwt.i18n.client.DateTimeFormat;

import daris.client.model.announcement.AnnouncementCollectionRef;
import daris.client.model.announcement.AnnouncementRef;

public class AnnouncementGrid extends ListGrid<AnnouncementRef> {

    private AnnouncementCollectionRef _c;

    public AnnouncementGrid(int startOffset, int cursorSize) {

        super(ScrollPolicy.AUTO);

        addColumnDefn("uid", "UID").setWidth(50);
        addColumnDefn("created", "Time", "Time when the announcement is made.",
                new WidgetFormatter<AnnouncementRef, Date>() {

                    @Override
                    public BaseWidget format(AnnouncementRef a, Date created) {
                        return new HTML(DateTimeFormat.getFormat(
                                "yyyy-MM-dd HH:mm:ss").format(created));
                    }
                }).setWidth(130);
        addColumnDefn("title", "Title").setWidth(300);

        setShowHeader(true);
        setShowRowSeparators(true);
        setMultiSelect(false);
        setFontSize(10);
        setCellSpacing(0);
        setCellPadding(1);
        setEmptyMessage("No system anouncement.");
        setLoadingMessage("Loading system announcements...");

        _c = new AnnouncementCollectionRef(startOffset, cursorSize);
        setDataSource(new ListGridDataSource<AnnouncementRef>(
                _c,
                new Transformer<AnnouncementRef, ListGridEntry<AnnouncementRef>>() {

                    @Override
                    protected ListGridEntry<AnnouncementRef> doTransform(
                            AnnouncementRef o) throws Throwable {
                        if (o != null) {
                            ListGridEntry<AnnouncementRef> e = new ListGridEntry<AnnouncementRef>(
                                    o);
                            e.set("uid", o.uid());
                            e.set("created", o.created());
                            e.set("title", o.title());
                            return e;
                        }
                        return null;
                    }
                }));

    }

    @Override
    protected void postLoad(long start, long end, long total,
            List<ListGridEntry<AnnouncementRef>> entries) {
        if (entries != null && !entries.isEmpty()) {
            select(0);
        }
    }
    
    public void reset(){
        _c.reset();
    }

    @Override
    public void setCursorSize(int cursorSize) {
        boolean change = _c.defaultPagingSize() != cursorSize;
        if (change) {
            _c.setDefaultPagingSize(cursorSize);
            super.setCursorSize(cursorSize);
            refresh(true);
        }
    }

    public int cursorSize() {
        return _c.defaultPagingSize();
    }

    public void setStartOffset(long startOffset) {
        boolean change = _c.startOffset() != startOffset;
        if (change) {
            _c.setStartOffset(startOffset);
            refresh(true);
        }
    }

    public long startOffset() {
        return _c.startOffset();
    }

    public void firstPage() {
        setStartOffset(0);
    }

    public void lastPage() {
        long total = _c.totalNumberOfMembers();
        if (total > 0) {
            int size = cursorSize();
            long offset = (long) total / size * size;
            if (offset < total) {
                setStartOffset(offset);
            } else {
                setStartOffset(total - size);
            }
        }
    }

    public void nextPage() {
        long total = _c.totalNumberOfMembers();
        if (total > 0) {
            long offset = startOffset() + cursorSize();
            if (offset < total) {
                setStartOffset(offset);
            }
        }
    }

    public void prevPage() {
        long total = _c.totalNumberOfMembers();
        if (total > 0) {
            long offset = startOffset() - cursorSize();
            if (offset >= 0) {
                setStartOffset(offset);
            }
        }
    }

    public int numberOfPages() {
        return _c.numberOfPages();
    }

}
