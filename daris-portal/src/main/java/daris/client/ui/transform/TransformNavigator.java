package daris.client.ui.transform;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.paging.PagingControl;
import arc.gui.gwt.widget.paging.PagingListener;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.Transformer;
import arc.mf.event.Filter;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventChannel;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.object.ObjectResolveHandler;
import daris.client.Resource;
import daris.client.model.transform.Transform;
import daris.client.model.transform.Transform.Status.State;
import daris.client.model.transform.TransformCollectionRef;
import daris.client.model.transform.TransformRef;
import daris.client.model.transform.events.TransformEvent;

public class TransformNavigator extends ContainerWidget implements PagingListener, arc.mf.event.Subscriber {

    public static final String ICON_PENDING = Resource.INSTANCE.loading16a().getSafeUri().asString();
    public static final String ICON_RUNNING = Resource.INSTANCE.loading16b().getSafeUri().asString();
    public static final String ICON_SUSPENDED = Resource.INSTANCE.pause16().getSafeUri().asString();
    public static final String ICON_TERMINATED = Resource.INSTANCE.stop16a().getSafeUri().asString();
    public static final String ICON_FAILED = Resource.INSTANCE.error16().getSafeUri().asString();
    public static final String ICON_UNKNOWN = Resource.INSTANCE.question16().getSafeUri().asString();

    private ListGrid<TransformRef> _lg;

    private PagingControl _pc;

    private TransformCollectionRef _rc;

    private TransformRef _selected;

    private List<SelectionHandler<TransformRef>> _shs;

    private List<TransformRef> _data;

    private List<ObjectResolveHandler<List<TransformRef>>> _rhs;

    public TransformNavigator() {
        this(new TransformCollectionRef());
    }

    protected TransformNavigator(TransformCollectionRef rc) {
        _rc = rc;
        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        /*
         * List grid
         */
        _lg = new ListGrid<TransformRef>(ScrollPolicy.AUTO) {
            protected void preLoad() {
                _selected = (_lg.selections() != null && !_lg.selections().isEmpty()) ? _lg.selections().get(0) : null;
            }

            protected void postLoad(long start, long end, long total, List<ListGridEntry<TransformRef>> entries) {
                if (entries != null && !entries.isEmpty()) {
                    if (_selected == null) {
                        select(entries.size()-1);
                    } else {
                        for (int i = 0; i < entries.size(); i++) {
                            if (entries.get(i).data().uid() == _selected.uid()) {
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
        _lg.setSelectionHandler(new SelectionHandler<TransformRef>() {

            @Override
            public void selected(TransformRef o) {
                _selected = o;
                if (_shs != null) {
                    for (SelectionHandler<TransformRef> sh : _shs) {
                        sh.selected(o);
                    }
                }
            }

            @Override
            public void deselected(TransformRef o) {
                _selected = null;
                if (_shs != null) {
                    for (SelectionHandler<TransformRef> sh : _shs) {
                        sh.deselected(o);
                    }
                }
            }
        });
        _lg.setEmptyMessage("");
        _lg.setLoadingMessage("");
        _lg.setCursorSize(_rc.defaultPagingSize());
        _lg.fitToParent();
        _lg.addColumnDefn("uid", "uid").setWidth(80);
        _lg.addColumnDefn("name", "name").setWidth(220);
        _lg.addColumnDefn("type", "type").setWidth(80);
        _lg.addColumnDefn("status", "status", null, new WidgetFormatter<TransformRef, Transform.Status.State>() {

            @Override
            public BaseWidget format(TransformRef t, State state) {
                arc.gui.gwt.widget.image.Image i = null;
                switch (state) {
                case pending:
                    i = new arc.gui.gwt.widget.image.Image(ICON_PENDING, 16, 16);
                    break;
                case running:
                    i = new arc.gui.gwt.widget.image.Image(ICON_RUNNING, 16, 16);
                    break;
                case suspended:
                    i = new arc.gui.gwt.widget.image.Image(ICON_SUSPENDED, 16, 16);
                    break;
                case terminated:
                    i = new arc.gui.gwt.widget.image.Image(ICON_TERMINATED, 16, 16);
                    break;
                case failed:
                    i = new arc.gui.gwt.widget.image.Image(ICON_FAILED, 16, 16);
                    break;
                case unknown:
                    i = new arc.gui.gwt.widget.image.Image(ICON_UNKNOWN, 16, 16);
                    break;
                default:
                    break;
                }
                HorizontalPanel hp = new HorizontalPanel();
                hp.setHeight(20);
                hp.add(i);
                hp.setSpacing(3);
                HTML label = new HTML(state.name());
                label.setFontSize(10);
                hp.add(label);
                return hp;
            }

        });
        vp.add(_lg);

        _pc = new PagingControl(_rc.defaultPagingSize());
        _pc.setWidth100();
        _pc.setHeight(22);
        _pc.addPagingListener(this);
        vp.add(_pc);

        initWidget(vp);
    }

    public long offset() {
        return _pc.offset();
    }

    @Override
    public void gotoOffset(final long offset) {
        _rc.resolve(offset, offset + pageSize(), new CollectionResolveHandler<TransformRef>() {
            @Override
            public void resolved(List<TransformRef> os) throws Throwable {
                long total = _rc.totalNumberOfMembers();
                _pc.setOffset(offset, total, true);
                _data = os;
                
                List<ListGridEntry<TransformRef>> entries = null;
                if (_data != null && !_data.isEmpty()) {
                    entries = arc.mf.client.util.Transform.transform(os,
                            new Transformer<TransformRef, ListGridEntry<TransformRef>>() {

                                @Override
                                protected ListGridEntry<TransformRef> doTransform(TransformRef t) throws Throwable {
                                    ListGridEntry<TransformRef> e = new ListGridEntry<TransformRef>(t);
                                    e.set("uid", t.uid());
                                    e.set("name", t.name());
                                    e.set("type", t.type());
                                    e.set("status", t.state());
                                    return e;
                                }
                            });
                }
                _lg.setData(entries);
                
                if(_rhs!=null){
                    for(ObjectResolveHandler<List<TransformRef>> rh : _rhs){
                        rh.resolved(_data);
                    }
                }
            }
        });
    }

    public void addSelectionHandler(SelectionHandler<TransformRef> sh) {
        if (sh == null) {
            return;
        }
        if (_shs == null) {
            _shs = new ArrayList<SelectionHandler<TransformRef>>();
        }
        _shs.add(sh);
    }

    public void removeSelectionHandler(SelectionHandler<TransformRef> sh) {
        if (_shs == null || sh == null) {
            return;
        }
        _shs.remove(sh);
    }

    public void addPageResolveHandler(ObjectResolveHandler<List<TransformRef>> rh) {
        if (rh == null) {
            return;
        }
        if (_rhs == null) {
            _rhs = new ArrayList<ObjectResolveHandler<List<TransformRef>>>();
        }
        _rhs.add(rh);
    }

    public void removePageResolveHandler(ObjectResolveHandler<List<TransformRef>> rh) {
        if (_rhs == null || rh == null) {
            return;
        }
        _rhs.remove(rh);
    }

    public int pageSize() {
        return _rc.pagingSize();
    }

    public void setPageSize(int pageSize) {
        _rc.setDefaultPagingSize(pageSize);
    }

    public void addPagingListener(PagingListener pl) {
        _pc.addPagingListener(pl);
    }

    public List<TransformRef> transformsInCurrentPage() {
        return _data;
    }

    public boolean hasTransformsInCurrentPage() {
        return _data != null && !_data.isEmpty();
    }

    public void subscribe() {
        SystemEventChannel.add(this);
    }

    public void unsubscribe() {
        SystemEventChannel.remove(this);
    }

    public TransformRef selected() {
        return _selected;
    }

    @Override
    public List<Filter> systemEventFilters() {
        List<Filter> filters = new ArrayList<Filter>(1);
        filters.add(new Filter(TransformEvent.SYSTEM_EVENT_NAME));
        return filters;
    }

    @Override
    public void process(SystemEvent se) {
        if (!(se instanceof TransformEvent)) {
            return;
        }
        TransformEvent te = (TransformEvent) se;
        long offset = _pc.offset();
        long total = _rc.totalNumberOfMembers();
        switch (te.action()) {
        case DESTROY:
            if (offset == total - 1) {
                if (offset - _rc.defaultPagingSize() > 0) {
                    offset = offset - _rc.defaultPagingSize();
                }
            }
            break;

        case CREATE:
            break;

        case UPDATE:
            break;

        default:
            break;
        }
        _rc.reset();
        gotoOffset(offset);

    }
}
