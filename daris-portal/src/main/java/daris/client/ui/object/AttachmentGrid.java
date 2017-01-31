package daris.client.ui.object;

import java.util.List;
import java.util.Vector;

import com.google.gwt.event.dom.client.ContextMenuEvent;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.list.ListGridRowContextMenuHandler;
import arc.gui.gwt.widget.menu.ActionMenu;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tip.ToolTip;
import arc.gui.gwt.widget.tip.ToolTipHandler;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.Action;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.object.Attachment;
import daris.client.model.object.messages.ObjectAttach;
import daris.client.model.object.messages.ObjectAttachmentGet;
import daris.client.model.object.messages.ObjectAttachmentList;
import daris.client.model.object.messages.ObjectDetach;
import daris.client.ui.dti.file.LocalFileSelector;

public class AttachmentGrid extends ListGrid<Attachment> implements DropHandler {

    private static class AttachmentDataSource implements DataSource<ListGridEntry<Attachment>> {

        private String _cid;

        private AttachmentDataSource(String cid) {

            _cid = cid;
        }

        @Override
        public boolean isRemote() {

            return true;
        }

        @Override
        public boolean supportCursor() {

            return false;
        }

        @Override
        public void load(final Filter f, final long start, final long end,
                final DataLoadHandler<ListGridEntry<Attachment>> lh) {

            new ObjectAttachmentList(_cid).send(new ObjectMessageResponse<List<Attachment>>() {

                @Override
                public void responded(List<Attachment> as) {

                    if (as != null && !as.isEmpty()) {
                        List<ListGridEntry<Attachment>> es = new Vector<ListGridEntry<Attachment>>(as.size());
                        for (Attachment a : as) {
                            ListGridEntry<Attachment> e = new ListGridEntry<Attachment>(a);
                            e.set("assetId", a.assetId());
                            e.set("name", a.name());
                            e.set("extension", a.extension());
                            e.set("mimeType", a.mimeType());
                            e.set("size", a.humanReadableSize());
                            es.add(e);
                        }
                        int total = es.size();
                        int start1 = start < 0 ? 0 : (start > total ? total : (int) start);
                        int end1 = end > total ? total : (int) end;
                        if (start1 < 0 || end1 > total || start1 > end) {
                            lh.loaded(start, end, total, null, null);
                        } else {
                            es = es.subList(start1, end1);
                            lh.loaded(start1, end1, total, es, DataLoadAction.REPLACE);
                        }
                        return;
                    }
                    lh.loaded(0, 0, 0, null, null);
                }
            });
        }
    }

    private String _cid;

    private Menu _actionMenu;
    private ActionEntry _aeAdd;
    private ActionEntry _aeRemove;
    private ActionEntry _aeClear;
    private ActionEntry _aeDownload;
    private ActionEntry _aeRefresh;

    public AttachmentGrid(String cid) {

        super(new AttachmentDataSource(cid), ScrollPolicy.AUTO);
        _cid = cid;
        addColumnDefn("assetId", "Asset ID").setWidth(80);
        addColumnDefn("name", "Name").setWidth(120);
        addColumnDefn("extension", "Extension").setWidth(90);
        addColumnDefn("mimeType", "MIME Type").setWidth(120);
        addColumnDefn("size", "Size").setWidth(120);

        fitToParent();
        setShowHeader(true);
        setShowRowSeparators(true);
        setMultiSelect(true);
        setFontSize(10);
        setCellSpacing(0);
        setCellPadding(1);
        setEmptyMessage("");
        setLoadingMessage("Loading object attachments...");
        setCursorSize(Integer.MAX_VALUE);

        setRowToolTip(new ToolTip<Attachment>() {

            @Override
            public void generate(Attachment a, ToolTipHandler th) {

                th.setTip(new HTML(a.toHTML()));
            }
        });

        /*
         * initialize action menu
         */
        _actionMenu = new Menu("Action") {
            public void preShow() {
            }
        };
        _aeAdd = new ActionEntry("Add attachment", new Action() {

            @Override
            public void execute() {
                LocalFileSelector dlg = new LocalFileSelector(LocalFile.Filter.FILES,
                        new LocalFileSelector.FileSelectionHandler() {

                            @Override
                            public void selected(LocalFile file) {
                                new ObjectAttach(_cid, file).send(new ObjectMessageResponse<Attachment>() {

                                    @Override
                                    public void responded(Attachment r) {
                                        AttachmentGrid.this.refresh(true);
                                    }
                                });
                            }
                        });
                dlg.show(window());
            }
        });
        _actionMenu.add(_aeAdd);
        _aeRemove = new ActionEntry("Remove attchment", new Action() {

            @Override
            public void execute() {
                List<Attachment> selections = AttachmentGrid.this.selections();
                if (selections != null && !selections.isEmpty()) {
                    new ObjectDetach(_cid, selections, false).send(new ObjectMessageResponse<Null>() {
                        @Override
                        public void responded(Null r) {

                            AttachmentGrid.this.refresh();
                        }
                    });
                }
            }
        });
        _aeRemove.disable();
        _actionMenu.add(_aeRemove);
        _aeClear = new ActionEntry("Clear all attachments", new Action() {

            @Override
            public void execute() {
                new ObjectDetach(_cid).send(new ObjectMessageResponse<Null>() {

                    @Override
                    public void responded(Null r) {
                        AttachmentGrid.this.refresh();
                    }
                });
            }
        });
        _actionMenu.add(_aeClear);
        _aeDownload = new ActionEntry("Download attachment", new Action() {

            @Override
            public void execute() {

                List<Attachment> selections = AttachmentGrid.this.selections();
                if (selections != null && !selections.isEmpty()) {

                    new ObjectAttachmentGet(_cid, selections).send(null);
                }
            }
        });
        _aeDownload.disable();
        _actionMenu.add(_aeDownload);
        _aeRefresh = new ActionEntry("Refresh", new Action() {

            @Override
            public void execute() {
                AttachmentGrid.this.refresh();
            }
        });
        _actionMenu.add(_aeRefresh);

        setRowContextMenuHandler(new ListGridRowContextMenuHandler<Attachment>() {
            @Override
            public void show(Attachment attachment, ContextMenuEvent event) {
                new ActionMenu(_actionMenu).showAt(event.getNativeEvent());
            }
        });

        setSelectionHandler(new SelectionHandler<Attachment>() {

            @Override
            public void selected(Attachment a) {

                List<Attachment> selections = selections();
                _aeRemove.setEnabled(selections != null && !selections.isEmpty());
                _aeDownload.setEnabled(selections != null && !selections.isEmpty());
            }

            @Override
            public void deselected(Attachment o) {

                List<Attachment> selections = selections();
                _aeRemove.setEnabled(selections != null && !selections.isEmpty());
                _aeDownload.setEnabled(selections != null && !selections.isEmpty());
            }
        });

        // enableRowDrag();

        /*
         * enable drop to the grid.
         */
        enableDropTarget(false);
        setDropHandler(this);

        refresh();
    }

    @Override
    public DropCheck checkCanDrop(Object object) {

        if (object != null && object instanceof LocalFile) {
            if (((LocalFile) object).isFile()) {
                return DropCheck.CAN;
            }
        }
        return DropCheck.CANNOT;
    }

    @Override
    public void drop(BaseWidget target, List<Object> objects, DropListener dl) {

        if (objects == null || objects.isEmpty()) {
            dl.dropped(DropCheck.CANNOT);
            return;
        }
        dl.dropped(DropCheck.CAN);
        for (Object object : objects) {
            new ObjectAttach(_cid, (LocalFile) object).send(new ObjectMessageResponse<Attachment>() {
                @Override
                public void responded(Attachment r) {

                    if (r != null) {
                        AttachmentGrid.this.refresh();
                    }
                }
            });
        }
    }

    public Menu actionMenu() {
        return _actionMenu;
    }

}
