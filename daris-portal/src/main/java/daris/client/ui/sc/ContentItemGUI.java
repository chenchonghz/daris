package daris.client.ui.sc;

import java.util.List;

import arc.gui.gwt.dnd.DragWidget;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.image.Image;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.gui.object.SelectedObjectSet;
import arc.gui.object.display.ObjectDetailsDisplay;
import arc.gui.object.register.ObjectGUI;
import arc.gui.object.register.ObjectUpdateHandle;
import arc.gui.object.register.ObjectUpdateListener;
import arc.gui.window.Window;
import arc.mf.client.util.Action;
import daris.client.Resource;
import daris.client.model.sc.ActiveShoppingCart;
import daris.client.model.sc.ContentItem;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;
import daris.client.ui.widget.MessageBox;

public class ContentItemGUI implements ObjectGUI {
    public static final ContentItemGUI INSANCE = new ContentItemGUI();

    public static final Image ICON_REFRESH = new Image(Resource.INSTANCE.refreshBlue16().getSafeUri().asString(), 16,
            16);
    public static final Image ICON_REMOVE = new Image(Resource.INSTANCE.delete16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_CLEAR = new Image(Resource.INSTANCE.clear16().getSafeUri().asString(), 16, 16);

    private ContentItemGUI() {

    }

    @Override
    public String idToString(Object o) {
        if (o instanceof ContentItem) {
            return ((ContentItem) o).assetId();
        }
        return null;
    }

    @Override
    public String icon(Object o, int size) {
        return null;
    }

    @Override
    public Menu actionMenu(Window w, Object o, final SelectedObjectSet selected, boolean readOnly) {

        final ContentItem item = (ContentItem) o;
        ShoppingCartRef cart = item.cart();

        if (!cart.isActive() || cart.status() != Status.editable) {
            return null;
        }

        Menu menu = new Menu();
        menu.setShowTitle(true);
        ActionEntry refreshAE = new ActionEntry(ICON_REFRESH, "Refresh content items", new Action() {

            @Override
            public void execute() {
                ActiveShoppingCart.refreshContents(null);
            }
        });
        menu.add(refreshAE);
        if (selected != null) {
            @SuppressWarnings("unchecked")
            final List<ContentItem> selections = (List<ContentItem>) selected.selections();
            if (!selections.isEmpty()) {

                ActionEntry removeAE = new ActionEntry(ICON_REMOVE, "Remove selected content items",
                        "Remove selected content items", new Action() {

                            @Override
                            public void execute() {
                                ActiveShoppingCart.removeContents(selections, new Action() {

                                    @Override
                                    public void execute() {
                                        MessageBox.info("Shopping cart",
                                                "The selected items have removed from the active shopping cart.", 2);
                                    }
                                });
                            }
                        });
                menu.add(removeAE);
            }
        }
        ActionEntry clearAE = new ActionEntry(ICON_CLEAR, "Clear all content items", new Action() {

            @Override
            public void execute() {
                ActiveShoppingCart.clearContents(new Action() {

                    @Override
                    public void execute() {
                        MessageBox.info("Shopping cart", "The active shopping cart has been emptied.", 2);
                    }
                });
            }
        });
        menu.add(clearAE);
        return menu;
    }

    @Override
    public Menu memberActionMenu(Window w, Object o, SelectedObjectSet selected, boolean readOnly) {
        return null;
    }

    @Override
    public Object reference(Object o) {
        return null;
    }

    @Override
    public boolean needToResolve(Object o) {
        return false;
    }

    @Override
    public void displayDetails(Object o, ObjectDetailsDisplay dd, boolean forEdit) {

    }

    @Override
    public void open(Window w, Object o) {

    }

    @Override
    public DropHandler dropHandler(Object o) {
        return null;
    }

    @Override
    public DragWidget dragWidget(Object o) {
        return null;
    }

    @Override
    public ObjectUpdateHandle createUpdateMonitor(Object o, ObjectUpdateListener ul) {
        // TODO Auto-generated method stub
        return null;
    }

}
