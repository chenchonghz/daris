package daris.client.ui.query.result;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.paging.PagingControl;
import arc.gui.gwt.widget.paging.PagingListener;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ObjectUtil;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.object.ObjectRef;
import daris.client.model.query.AssetResultCollectionRef;
import daris.client.model.query.DObjectResultCollectionRef;
import daris.client.model.query.HasXPathValues;
import daris.client.model.query.ResultCollectionRef;
import daris.client.model.query.XPathValue;
import daris.client.model.query.options.XPath;

public abstract class ResultNavigator<T extends ObjectRef<?>> extends ContainerWidget implements PagingListener {

    private ListGrid<T> _lg;
    private PagingControl _pc;

    private ResultCollectionRef<T> _rc;
    private T _selected;

    private List<SelectionHandler<T>> _shs;

    private List<T> _data;

    public ResultNavigator(ResultCollectionRef<T> rc) {
        _rc = rc;
        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        /*
         * List grid
         */

        _lg = new ListGrid<T>(ScrollPolicy.AUTO) {
            protected void preLoad() {
                _selected = (_lg.selections() != null && !_lg.selections().isEmpty()) ? _lg.selections().get(0) : null;
            }

            protected void postLoad(long start, long end, long total, List<ListGridEntry<T>> entries) {
                if (entries != null && !entries.isEmpty()) {
                    if (_selected == null) {
                        select(0);
                    } else {
                        for (int i = 0; i < entries.size(); i++) {
                            if (ObjectUtil.equals(entries.get(i).data(), _selected)) {
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
        _lg.setSelectionHandler(new SelectionHandler<T>() {

            @Override
            public void selected(T o) {
                _selected = o;
                notifyOfSelectionInPage(o);
            }

            @Override
            public void deselected(T o) {
                _selected = null;
                notifyOfDeselectionInPage(o);
            }
        });
        _lg.setEmptyMessage("");
        _lg.setLoadingMessage("");
        _lg.setLoadingMessage("");
        _lg.setCursorSize(_rc.defaultPagingSize());
        _lg.fitToParent();
        addListGridColumnDefns(_lg);
        List<XPath> xpaths = _rc.query().options().xpaths();
        if (xpaths != null && !xpaths.isEmpty()) {
            for (XPath xpath : xpaths) {
                _lg.addColumnDefn(xpath.value(), xpath.name());
            }
        }
        vp.add(_lg);

        _pc = new PagingControl(_rc.defaultPagingSize());
        _pc.setWidth100();
        _pc.setHeight(22);
        _pc.addPagingListener(this);
        vp.add(_pc);

        initWidget(vp);
    }

    @Override
    public void gotoOffset(final long offset) {
        _rc.resolve(offset, offset + pageSize(), new CollectionResolveHandler<T>() {

            @Override
            public void resolved(List<T> os) throws Throwable {
                long total = _rc.totalNumberOfMembers();
                _pc.setOffset(offset, total, true);
                _data = os;
                updateListGrid();
            }
        });
    }

    private void updateListGrid() {
        List<ListGridEntry<T>> es = null;
        if (_data != null && !_data.isEmpty()) {
            es = new ArrayList<ListGridEntry<T>>();
            for (T ro : _data) {
                ListGridEntry<T> e = new ListGridEntry<T>(ro);
                setListGridEntry(e);
                if(ro instanceof HasXPathValues){
                    List<XPathValue> xpvs = ((HasXPathValues)ro).xpathValues();
                    if(xpvs!=null){
                        for(XPathValue xpv : xpvs){
                            e.set(xpv.xpath(), xpv.value());
                        }
                    }
                }
                es.add(e);
            }
        }
        _lg.setData(es);
    }

    protected abstract void setListGridEntry(ListGridEntry<T> e);

    protected abstract void addListGridColumnDefns(ListGrid<T> lg);

    public void addSelectionHandler(SelectionHandler<T> sh) {
        if (_shs == null) {
            _shs = new ArrayList<SelectionHandler<T>>();
        }
        _shs.add(sh);
    }

    public void removeSelectionHandler(SelectionHandler<T> sh) {
        if (_shs != null) {
            _shs.remove(sh);
        }
    }

    public int pageSize() {
        return _rc.pagingSize();
    }

    public void setPageSize(int pageSize) {
        _rc.setDefaultPagingSize(pageSize);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ObjectRef<?>> ResultNavigator<T> create(ResultCollectionRef<T> rc) {

        if (rc instanceof AssetResultCollectionRef) {
            return (ResultNavigator<T>) new AssetResultNavigator((AssetResultCollectionRef) rc);
        } else if (rc instanceof DObjectResultCollectionRef) {
            return (ResultNavigator<T>) new DObjectResultNavigator((DObjectResultCollectionRef) rc);
        }
        throw new AssertionError(rc.getClass().getName() + " is not supported.");
    }

    public void refreshPage() {
        _lg.refresh(true);
    }

    public void notifyOfSelectionInPage(T o) {
        if (_shs != null) {
            for (SelectionHandler<T> sh : _shs) {
                sh.selected(o);
            }
        }
    }

    public void notifyOfDeselectionInPage(T o) {
        if (_shs != null) {
            for (SelectionHandler<T> sh : _shs) {
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

    public List<T> dataInCurrentPage() {
        return _data;
    }

    public T selected() {
        return _selected;
    }

}
