package daris.client.ui.archive;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.paging.PagingControl;
import arc.gui.gwt.widget.paging.PagingListener;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ObjectUtil;
import arc.mf.object.CollectionResolveHandler;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.model.archive.messages.ArchiveContentGet;

public class ArchiveEntryNavigator extends ContainerWidget
        implements PagingListener {

    private ListGrid<ArchiveEntry> _lg;
    private PagingControl _pc;

    private ArchiveEntryCollectionRef _arc;
    private ArchiveEntry _selected;

    private List<SelectionHandler<ArchiveEntry>> _shs;

    private List<ArchiveEntry> _data;

    public ArchiveEntryNavigator(ArchiveEntryCollectionRef arc) {
        _arc = arc;
        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        /*
         * List grid
         */

        _lg = new ListGrid<ArchiveEntry>(ScrollPolicy.AUTO) {

            @Override
            protected void preLoad() {
                _selected = (_lg.selections() != null
                        && !_lg.selections().isEmpty())
                                ? _lg.selections().get(0) : null;
            }

            @Override
            protected void postLoad(long start, long end, long total,
                    List<ListGridEntry<ArchiveEntry>> entries) {
                if (entries != null && !entries.isEmpty()) {
                    if (_selected == null) {
                        select(0);
                    } else {
                        for (int i = 0; i < entries.size(); i++) {
                            if (ObjectUtil.equals(entries.get(i).data(),
                                    _selected)) {
                                select(i);
                                break;
                            }
                        }
                    }
                }
            }
        };
        _lg.setClearSelectionOnRefresh(false);
        _lg.setMultiSelect(false);
        _lg.setSelectionHandler(new SelectionHandler<ArchiveEntry>() {

            @Override
            public void selected(ArchiveEntry o) {
                _selected = o;
                notifyOfSelectionInPage(o);
            }

            @Override
            public void deselected(ArchiveEntry o) {
                _selected = null;
                notifyOfDeselectionInPage(o);
            }
        });
        _lg.setEmptyMessage("");
        _lg.setLoadingMessage("");
        _lg.setLoadingMessage("");
        _lg.setCursorSize(_arc.defaultPagingSize());
        _lg.fitToParent();
        _lg.addColumnDefn("idx", "idx", "Ordinal index").setWidth(50);
        _lg.addColumnDefn("name", "name", "File name/path.").setMinWidth(250);
        _lg.addColumnDefn("size", "size (bytes)", "File size",
                new WidgetFormatter<ArchiveEntry, Long>() {

                    @Override
                    public BaseWidget format(ArchiveEntry ae, final Long size) {
                        HTML html = new HTML(
                                size >= 0 ? Long.toString(size) : "");
                        return html;
                    }
                }).setWidth(100);
        _lg.addColumnDefn("idx", "download", "Download",
                new WidgetFormatter<ArchiveEntry, Integer>() {

                    @Override
                    public BaseWidget format(ArchiveEntry ae,
                            final Integer idx) {
                        Button button = new Button("Download");
                        button.addClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent event) {
                                new ArchiveContentGet(_arc, idx).send();
                            }
                        });
                        return button;
                    }
                }).setWidth(80);
        vp.add(_lg);

        _pc = new PagingControl(_arc.defaultPagingSize());
        _pc.setWidth100();
        _pc.setHeight(22);
        _pc.addPagingListener(this);
        vp.add(_pc);

        initWidget(vp);
        gotoOffset(0);
    }

    @Override
    public void gotoOffset(final long offset) {
        _arc.resolve(offset, offset + pageSize(),
                new CollectionResolveHandler<ArchiveEntry>() {
                    @Override
                    public void resolved(List<ArchiveEntry> aes)
                            throws Throwable {
                        long total = _arc.totalNumberOfMembers();
                        _pc.setOffset(offset, total, true);
                        _data = aes;
                        List<ListGridEntry<ArchiveEntry>> lges = null;
                        if (_data != null && !_data.isEmpty()) {
                            lges = new ArrayList<ListGridEntry<ArchiveEntry>>();
                            for (ArchiveEntry ae : _data) {
                                ListGridEntry<ArchiveEntry> lge = new ListGridEntry<ArchiveEntry>(
                                        ae);
                                lge.set("idx", ae.ordinal());
                                lge.set("name", ae.name());
                                lge.set("size", ae.size());
                                lges.add(lge);
                            }
                        }
                        _lg.setData(lges);
                    }
                });
    }

    public void addSelectionHandler(SelectionHandler<ArchiveEntry> sh) {
        if (_shs == null) {
            _shs = new ArrayList<SelectionHandler<ArchiveEntry>>();
        }
        _shs.add(sh);
    }

    public void removeSelectionHandler(SelectionHandler<ArchiveEntry> sh) {
        if (_shs != null) {
            _shs.remove(sh);
        }
    }

    public int pageSize() {
        return _arc.pagingSize();
    }

    public void setPageSize(int pageSize) {
        _arc.setPageSize(pageSize);
    }

    public void refreshPage() {
        _lg.refresh(true);
    }

    protected void notifyOfSelectionInPage(ArchiveEntry o) {
        if (_shs != null) {
            for (SelectionHandler<ArchiveEntry> sh : _shs) {
                sh.selected(o);
            }
        }
    }

    protected void notifyOfDeselectionInPage(ArchiveEntry o) {
        if (_shs != null) {
            for (SelectionHandler<ArchiveEntry> sh : _shs) {
                sh.deselected(o);
            }
        }
    }

    public void addPagingListener(PagingListener pl) {
        _pc.addPagingListener(pl);
    }

    public long offset() {
        return _pc.offset();
    }

    public List<ArchiveEntry> dataInCurrentPage() {
        return _data;
    }

    public ArchiveEntry selected() {
        return _selected;
    }

}
