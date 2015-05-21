package daris.client.ui.sc;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.colour.RGB;
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
import arc.gui.gwt.widget.menu.ActionContextMenu;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tip.ToolTip;
import arc.gui.gwt.widget.tip.ToolTipHandler;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.Validity;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.object.DObjectRef;
import daris.client.model.sc.ActiveShoppingCart;
import daris.client.model.sc.ContentItem;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;
import daris.client.model.sc.messages.ShoppingCartContentList;
import daris.client.ui.DObjectGUIRegistry;
import daris.client.util.ByteUtil;

public class ShoppingCartContentsForm extends ValidatedInterfaceComponent {
    public static interface ContentSelectionListener {
        void contentSelectionChanged(List<ContentItem> selections);
    }

    private static class ShoppingCartContentDataSource implements DataSource<ListGridEntry<ContentItem>> {

        private ShoppingCartRef _cart;

        public ShoppingCartContentDataSource(ShoppingCart cart) {
            if (cart == null) {
                _cart = null;
            } else {
                _cart = new ShoppingCartRef(cart);
            }
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
                final DataLoadHandler<ListGridEntry<ContentItem>> lh) {

            if (_cart == null) {
                lh.loaded(0, 0, 0, null, null);
                return;
            }
            new ShoppingCartContentList(_cart).send(new ObjectMessageResponse<List<ContentItem>>() {

                @Override
                public void responded(List<ContentItem> items) {
                    if (items != null) {
                        doLoad(f, start, end, items, lh);
                    } else {
                        lh.loaded(0, 0, 0, null, null);
                    }

                }
            });

        }

        private void doLoad(Filter f, long start, long end, List<ContentItem> items,
                DataLoadHandler<ListGridEntry<ContentItem>> lh) {

            List<ListGridEntry<ContentItem>> es = new Vector<ListGridEntry<ContentItem>>();
            for (ContentItem item : items) {
                ListGridEntry<ContentItem> e = new ListGridEntry<ContentItem>(item);
                e.set("id", item.cid());
                e.set("name", item.objectName());
                e.set("description", item.objectDescription());
                e.set("size", ByteUtil.humanReadableByteCount(item.size(), true));
                e.set("mimeType", item.mimeType());
                e.set("type", item.objectType());
                e.set("assetId", item.assetId());
                e.set("status", item.status());
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
        }
    }

    private ShoppingCart _cart;
    private FormEditMode _mode;
    private VerticalPanel _vp;
    private ListGrid<ContentItem> _grid;
    private boolean _hasContentItems = false;
    private HTML _summary;

    private List<ContentSelectionListener> _csls;

    public ShoppingCartContentsForm(ShoppingCart cart, FormEditMode mode) {

        _mode = mode;

        _vp = new VerticalPanel();
        _vp.fitToParent();

        /*
         * List grid
         */
        _grid = new ListGrid<ContentItem>(new ShoppingCartContentDataSource(null), ScrollPolicy.AUTO) {
            @Override
            protected void postLoad(long start, long end, long total, List<ListGridEntry<ContentItem>> entries) {
                _hasContentItems = entries != null && !entries.isEmpty();
                notifyOfChangeInState();
            }
        };
        _grid.addColumnDefn("id", "ID").setWidth(100);
        _grid.addColumnDefn("name", "Name").setWidth(120);
        _grid.addColumnDefn("mimeType", "MIME Type").setWidth(120);
        _grid.addColumnDefn("size", "Size").setWidth(120);
        _grid.addColumnDefn("status", "Status").setWidth(120);
        _grid.fitToParent();
        _grid.setShowHeader(true);
        _grid.setShowRowSeparators(true);
        _grid.setMultiSelect(false);
        _grid.setFontSize(10);
        _grid.setCellSpacing(0);
        _grid.setCellPadding(1);
        _grid.setEmptyMessage("");
        _grid.setLoadingMessage("Loading shopping-cart contents ...");
        _grid.setCursorSize(Integer.MAX_VALUE);
        _grid.setMultiSelect(true);
        _grid.setObjectRegistry(DObjectGUIRegistry.get());

        /*
         * Context Menu
         */
        if (_mode != FormEditMode.READ_ONLY) {
            _grid.setRowContextMenuHandler(new ListGridRowContextMenuHandler<ContentItem>() {

                @Override
                public void show(ContentItem data, ContextMenuEvent event) {
                    SelectedObjectSet selected = new SelectedObjectSet() {

                        @Override
                        public List<?> selections() {
                            return _grid.selections();
                        }
                    };
                    Menu menu = ContentItemGUI.INSANCE.actionMenu(_vp.window(), data, selected, true);
                    if (menu != null) {
                        ActionContextMenu acm = new ActionContextMenu(menu);
                        NativeEvent ne = event.getNativeEvent();
                        acm.showAt(ne);
                    }
                }
            });
        }

        /*
         * D & D
         */
        if (_mode != FormEditMode.READ_ONLY) {
            _grid.enableDropTarget(false);
            _grid.setDropHandler(new DropHandler() {

                @Override
                public DropCheck checkCanDrop(Object data) {
                    if (_cart == null) {
                        return DropCheck.CANNOT;
                    }
                    if (!Status.editable.equals(_cart.status())) {
                        return DropCheck.CANNOT;
                    }
                    if (data instanceof DObjectRef) {
                        return DropCheck.CAN;
                    } else {
                        return DropCheck.CANNOT;
                    }
                }

                @Override
                public void drop(BaseWidget target, final List<Object> data, final DropListener dl) {

                    if (_cart != null && data != null && Status.editable.equals(_cart.status())) {
                        if (!data.isEmpty()) {
                            final DObjectRef o = (DObjectRef) data.get(0);
                            ActiveShoppingCart.addContents(o, true, null);
                            dl.dropped(DropCheck.CAN);
                            return;
                        }
                    }
                    dl.dropped(DropCheck.CANNOT);
                }
            });
        }

        _grid.setRowToolTip(new ToolTip<ContentItem>() {

            @Override
            public void generate(ContentItem item, ToolTipHandler th) {

                th.setTip(new HTML(item.toHTML()));
            }
        });

        _grid.setSelectionHandler(new SelectionHandler<ContentItem>() {

            @Override
            public void selected(ContentItem o) {
                notifyOfSelectionChange(_grid.selections());
            }

            @Override
            public void deselected(ContentItem o) {
                notifyOfSelectionChange(_grid.selections());
            }
        });
        _vp.add(_grid);

        /*
         * Summary
         */
        CenteringPanel cp = new CenteringPanel(Axis.HORIZONTAL);
        cp.setHeight(20);
        cp.setWidth100();
        cp.setBorderTop(1, BorderStyle.DOTTED, new RGB(0xdd, 0xdd, 0xdd));
        _summary = new HTML("Shopping Cart");
        _summary.setFontFamily("Helvetica");
        _summary.setFontWeight(FontWeight.BOLD);
        _summary.setFontSize(12);
        _summary.setMarginTop(3);
        cp.add(_summary);
        _vp.add(cp);

        /*
         * set data source
         */
        setCart(cart);
    }

    private void updateSummary(ShoppingCart sc) {
        if (sc == null) {
            _summary.clear();
        } else {
            if (sc.numberOfContentItems() == 0 && _mode!=FormEditMode.READ_ONLY) {
                _summary.setColour(RGB.RED);
            } else {
                _summary.setColour(RGB.BLUE);
            }
            String title = " [Total number of datasets: " + sc.numberOfContentItems() + "; ";
            title += "Total size: " + ByteUtil.humanReadableByteCount(sc.sizeOfContentItems(), true) + "]";
            _summary.setHTML(title);
        }
    }

    public void setCart(ShoppingCart cart) {
        _cart = cart;
        _grid.setDataSource(new ShoppingCartContentDataSource(_cart));
        updateSummary(_cart);
    }

    public boolean hasSelections() {
        List<ContentItem> selections = _grid.selections();
        if (selections != null) {
            if (!selections.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    public void addContentSelectionListener(ContentSelectionListener csl) {
        if (csl == null) {
            return;
        }
        if (_csls == null) {
            _csls = new ArrayList<ContentSelectionListener>();
        }
        _csls.add(csl);
    }

    public void removeContentSelectionListener(ContentSelectionListener csl) {
        if (_csls == null) {
            return;
        }
        if (csl == null) {
            return;
        }
        _csls.remove(csl);
    }

    private void notifyOfSelectionChange(List<ContentItem> selections) {
        if (_csls != null) {
            for (ContentSelectionListener csl : _csls) {
                csl.contentSelectionChanged(selections);
            }
        }
    }

    public Validity valid() {
        if (_mode != FormEditMode.READ_ONLY) {
            if (!_hasContentItems) {
                return new IsNotValid("Shopping cart is empty.");
            }
        }
        return super.valid();
    }

    public List<ContentItem> selections() {
        return _grid.selections();
    }

    public boolean hasContentItems() {
        return _hasContentItems;
    }

    public void refresh() {
        if (_cart != null) {
            ShoppingCartRef sc = new ShoppingCartRef(_cart);
            sc.reset();
            sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

                @Override
                public void resolved(ShoppingCart cart) {
                    setCart(cart);
                }
            });
        }
    }
}
