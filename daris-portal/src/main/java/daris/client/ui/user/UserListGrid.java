package daris.client.ui.user;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.model.authentication.DomainRef;
import arc.mf.model.authentication.UserRef;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.user.messages.UserDescribe;
import daris.client.ui.DObjectGUIRegistry;

public class UserListGrid extends ListGrid<UserRef> {

    private static final int PAGE_SIZE = Integer.MAX_VALUE;
    private DomainRef _domain;
    private String _filterString;
    private Filter _filter;

    public UserListGrid(DomainRef domain) {
        super(ScrollPolicy.AUTO);
        _domain = domain;
        _filter = new Filter() {
            @Override
            public boolean matches(Object o) {
                if (_filterString == null || _filterString.trim().isEmpty()) {
                    return true;
                }
                String str = _filterString.trim().toLowerCase();
                UserRef u = (UserRef) o;
                String name = u.name();
                String email = u.email();
                String fullName = u.personName() == null ? null
                        : u.personName().toLowerCase();
                return str.equalsIgnoreCase(name) || str.equalsIgnoreCase(email)
                        || (fullName != null && (fullName.startsWith(str)
                                || fullName.toLowerCase().endsWith(str)));
            }
        };
        setDataSource(new DataSource<ListGridEntry<UserRef>>() {

            @Override
            public boolean isRemote() {
                return true;
            }

            @Override
            public boolean supportCursor() {
                return false;
            }

            @Override
            public void load(final Filter filter, final long start,
                    final long end,
                    final DataLoadHandler<ListGridEntry<UserRef>> lh) {
                new UserDescribe(_domain)
                        .send(new ObjectMessageResponse<List<UserRef>>() {

                    @Override
                    public void responded(List<UserRef> users) {
                        if (users != null && !users.isEmpty()) {
                            List<ListGridEntry<UserRef>> entries = new ArrayList<ListGridEntry<UserRef>>();
                            for (UserRef user : users) {
                                if (filter == null || filter.matches(user)) {
                                    ListGridEntry<UserRef> entry = new ListGridEntry<UserRef>(
                                            user);
                                    entry.set("domain", user.domain());
                                    entry.set("user", user.name());
                                    entry.set("name", user.personName());
                                    entry.set("email", user.email());
                                    entries.add(entry);
                                }
                            }
                            lh.loaded(start, end, entries.size(), entries,
                                    DataLoadAction.REPLACE);
                            return;
                        }
                        lh.loaded(0, 0, 0, null, null);
                    }
                });
            }
        }, false);
        setClearSelectionOnRefresh(false);
        setMultiSelect(false);
        setEmptyMessage("");
        setLoadingMessage("");
        setCursorSize(PAGE_SIZE);
        fitToParent();
        addColumnDefn("domain", "domain").setWidth(150);
        addColumnDefn("user", "user").setWidth(150);
        addColumnDefn("name", "name").setWidth(150);
        addColumnDefn("email", "email").setWidth(200);
        setObjectRegistry(DObjectGUIRegistry.get());
        enableRowDrag();
        refresh();
    }

    public void setFilters(DomainRef domain, String filterString) {
        _domain = domain;
        _filterString = filterString;
        refresh(_filter, true);
    }

}
