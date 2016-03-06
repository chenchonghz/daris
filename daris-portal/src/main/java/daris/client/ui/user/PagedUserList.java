package daris.client.ui.user;

import java.util.ArrayList;
import java.util.List;

import daris.client.ui.DObjectGUIRegistry;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.list.ListGridRowContextMenuHandler;
import arc.gui.gwt.widget.paging.PagingControl;
import arc.gui.gwt.widget.paging.PagingListener;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.ObjectUtil;
import arc.mf.model.authentication.DomainRef;
import arc.mf.model.authentication.UserCollectionRef;
import arc.mf.model.authentication.UserRef;
import arc.mf.object.CollectionResolveHandler;

public class PagedUserList extends ContainerWidget implements PagingListener {

    public static final int PAGE_SIZE = 100;

    private DomainRef _domain;
    private UserCollectionRef _users;

    private ListGrid<UserRef> _listGrid;
    private PagingControl _pagingControl;

    public PagedUserList(DomainRef domain) {
        _domain = domain;
        _users = domain == null ? null : new UserCollectionRef(domain) {
            @Override
            public int defaultPagingSize() {
                return PAGE_SIZE;
            }

            @Override
            public boolean supportsPaging() {
                return true;
            }
        };
        
        if(_users!=null){
            _users.setCountMembers(true);
        }

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        /*
         * List grid
         */
        _listGrid = new ListGrid<UserRef>(ScrollPolicy.AUTO);
        _listGrid.setDataSource(new DataSource<ListGridEntry<UserRef>>() {

            @Override
            public boolean isRemote() {
                return true;
            }

            @Override
            public boolean supportCursor() {
                return true;
            }

            @Override
            public void load(final Filter f, final long start, final long end,
                    final DataLoadHandler<ListGridEntry<UserRef>> lh) {

            }
        }, false);
        _listGrid.setClearSelectionOnRefresh(false);
        _listGrid.setMultiSelect(false);
        _listGrid.setEmptyMessage("");
        _listGrid.setLoadingMessage("");
        _listGrid.setCursorSize(PAGE_SIZE);
        _listGrid.fitToParent();
        _listGrid.addColumnDefn("domain", "domain").setWidth(150);
        _listGrid.addColumnDefn("user", "user").setWidth(150);
        _listGrid.addColumnDefn("email", "email").setWidth(200);
        _listGrid.setObjectRegistry(DObjectGUIRegistry.get());
        _listGrid.enableRowDrag();
        vp.add(_listGrid);

        /*
         * Paging control
         */
        _pagingControl = new PagingControl(PAGE_SIZE);
        _pagingControl.setWidth100();
        _pagingControl.setHeight(22);
        _pagingControl.addPagingListener(this);

        vp.add(_pagingControl);

        /*
         * 
         */
        initWidget(vp);

        /*
         * refresh
         */
        gotoOffset(0);
    }

    @Override
    public void gotoOffset(final long offset) {
        if (_users == null) {
            _listGrid.setData(null);
        } else {
            _users.resolve(offset, offset + PAGE_SIZE,
                    new CollectionResolveHandler<UserRef>() {

                        @Override
                        public void resolved(List<UserRef> users)
                                throws Throwable {
                            long total = _users.totalNumberOfMembers();
                            /*
                             * update paging control
                             */
                            _pagingControl.setOffset(offset, total, true);
                            /*
                             * update list grid
                             */
                            if (users == null || users.isEmpty()) {
                                _listGrid.setData(null);
                            } else {
                                List<ListGridEntry<UserRef>> entries = new ArrayList<ListGridEntry<UserRef>>();
                                for (UserRef user : users) {
                                    ListGridEntry<UserRef> entry = new ListGridEntry<UserRef>(
                                            user);
                                    entry.set("domain", user.domain());
                                    entry.set("user", user.name());
                                    entry.set("email", user.email());
                                    entries.add(entry);
                                }
                                _listGrid.setData(entries);
                            }
                        }
                    });
        }
    }

    public void setDomain(DomainRef domain) {
        if (!ObjectUtil.equals(_domain, domain)) {
            _domain = domain;
            _users = domain == null ? null : new UserCollectionRef(domain);
            gotoOffset(0);
        }
    }

    public void setRowContextMenuHandler(
            ListGridRowContextMenuHandler<UserRef> listGridRowContextMenuHandler) {
        _listGrid.setRowContextMenuHandler(listGridRowContextMenuHandler);
    }

}
