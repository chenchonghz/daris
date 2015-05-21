package daris.client.ui.query;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ContextMenuEvent;

import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.list.ListGridRowContextMenuHandler;
import arc.gui.gwt.widget.paging.PagingControl;
import arc.gui.gwt.widget.paging.PagingListener;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ObjectUtil;
import arc.mf.object.CollectionResolveHandler;
import daris.client.model.query.QueryAssetCollectionRef;
import daris.client.model.query.QueryAssetRef;

public class SavedQueryNavigator extends ContainerWidget implements PagingListener {

    private ListGrid<QueryAssetRef> _lg;
    private PagingControl _pc;

    private QueryAssetCollectionRef _qc;
    private QueryAssetRef _selected;

    private SelectionHandler<QueryAssetRef> _sh;
    
    public SavedQueryNavigator(){
        this(new QueryAssetCollectionRef ());
    }

    public SavedQueryNavigator(QueryAssetCollectionRef qc) {
        _qc = qc;
        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        /*
         * List grid
         */

        _lg = new ListGrid<QueryAssetRef>(ScrollPolicy.AUTO) {
            protected void preLoad() {
                _selected = (_lg.selections() != null && !_lg.selections().isEmpty()) ? _lg.selections().get(0) : null;
            }

            protected void postLoad(long start, long end, long total, List<ListGridEntry<QueryAssetRef>> entries) {
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
        _lg.setSelectionHandler(new SelectionHandler<QueryAssetRef>() {

            @Override
            public void selected(QueryAssetRef o) {
                _selected = o;
                if (_sh != null) {
                    _sh.selected(o);
                }
            }

            @Override
            public void deselected(QueryAssetRef o) {
                _selected = null;
                if (_sh != null) {
                    _sh.deselected(o);
                }
            }
        });
        _lg.setEmptyMessage("");
        _lg.setLoadingMessage("");
        _lg.setLoadingMessage("");
        _lg.setCursorSize(_qc.defaultPagingSize());
        _lg.fitToParent();
        _lg.addColumnDefn("id", "id").setWidth(100);
        _lg.addColumnDefn("name", "name").setWidth(350);
        
        _lg.setRowContextMenuHandler(new ListGridRowContextMenuHandler<QueryAssetRef>(){

            @Override
            public void show(QueryAssetRef data, ContextMenuEvent event) {
                // TODO Auto-generated method stub
                
            }});
        
        vp.add(_lg);

        _pc = new PagingControl(_qc.defaultPagingSize());
        _pc.setWidth100();
        _pc.setHeight(22);
        _pc.addPagingListener(this);
        vp.add(_pc);

        initWidget(vp);

        gotoOffset(0);
    }

    @Override
    public void gotoOffset(final long offset) {
        _qc.resolve(offset, offset + pageSize(), new CollectionResolveHandler<QueryAssetRef>() {

            @Override
            public void resolved(List<QueryAssetRef> os) throws Throwable {
                long total = _qc.totalNumberOfMembers();
                _pc.setOffset(offset, total, true);
                setListGridData(os);
            }
        });
    }

    private void setListGridData(List<QueryAssetRef> os) {
        List<ListGridEntry<QueryAssetRef>> es = null;
        if (os != null && !os.isEmpty()) {
            es = new ArrayList<ListGridEntry<QueryAssetRef>>();
            for (QueryAssetRef ro : os) {
                ListGridEntry<QueryAssetRef> e = new ListGridEntry<QueryAssetRef>(ro);
                e.set("id", ro.id());
                e.set("name", ro.name());
                es.add(e);
            }
        }
        _lg.setData(es);
    }

    public void setSelectionHandler(SelectionHandler<QueryAssetRef> sh) {
        _sh = sh;
    }

    public int pageSize() {
        return _qc.pagingSize();
    }

    public void setPageSize(int pageSize) {
        _qc.setDefaultPagingSize(pageSize);
    }

    public void refresh() {
        _qc.reset();
//        _lg.refresh(true);
        gotoOffset(0);
    }

    public void refreshSelection() {
        if (_selected != null && _sh != null) {
            _sh.selected(_selected);
        }
    }

}