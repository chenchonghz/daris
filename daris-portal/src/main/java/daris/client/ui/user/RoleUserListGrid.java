package daris.client.ui.user;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tip.ToolTip;
import arc.gui.gwt.widget.tip.ToolTipHandler;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.user.RoleUser;
import daris.client.model.user.messages.RoleUserList;
import daris.client.ui.DObjectGUIRegistry;

public class RoleUserListGrid extends ListGrid<RoleUser> {

    public RoleUserListGrid() {

        super(ScrollPolicy.AUTO);
        setDataSource(new DataSource<ListGridEntry<RoleUser>>() {

            @Override
            public boolean isRemote() {
                return true;
            }

            @Override
            public boolean supportCursor() {
                return false;
            }

            @Override
            public void load(Filter f, final long start, final long end,
                    final DataLoadHandler<ListGridEntry<RoleUser>> lh) {
                new RoleUserList()
                        .send(new ObjectMessageResponse<List<RoleUser>>() {

                    @Override
                    public void responded(List<RoleUser> rus) {
                        if (rus != null && !rus.isEmpty()) {
                            List<ListGridEntry<RoleUser>> entries = new ArrayList<ListGridEntry<RoleUser>>();
                            for (RoleUser ru : rus) {
                                ListGridEntry<RoleUser> entry = new ListGridEntry<RoleUser>(
                                        ru);
                                entry.set("id", ru.id());
                                entry.set("name", ru.name());
                                entry.set("member", ru.name());
                                entries.add(entry);
                            }
                            lh.loaded(start, end, entries.size(), entries,
                                    DataLoadAction.REPLACE);
                            return;
                        }
                        lh.loaded(0, 0, 0, null, null);
                    }
                });
            }
        });
        addColumnDefn("id", "id").setWidth(60);
        addColumnDefn("name", "name").setWidth(120);
        fitToParent();
        setShowHeader(true);
        setShowRowSeparators(true);
        setMultiSelect(false);
        setFontSize(10);
        setCellSpacing(0);
        setCellPadding(1);
        setEmptyMessage("");
        setLoadingMessage("");
        setCursorSize(Integer.MAX_VALUE);

        setRowToolTip(new ToolTip<RoleUser>() {
            @Override
            public void generate(RoleUser u, ToolTipHandler th) {
                th.setTip(new HTML(u.toHTML()));
            }
        });

        /*
         * enable drag from this list grid.
         */
        setObjectRegistry(DObjectGUIRegistry.get());
        enableRowDrag();
        refresh();
    }

}
