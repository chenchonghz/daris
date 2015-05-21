package daris.client.ui.sc;

import java.util.List;

import arc.gui.gwt.dnd.DragWidget;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
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
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.ListUtil;
import arc.mf.object.ObjectResolveHandler;
import daris.client.Resource;
import daris.client.model.object.DObjectRef;
import daris.client.model.sc.ActiveShoppingCart;
import daris.client.model.sc.DeliveryMethod;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartDownloadManager;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;
import daris.client.model.sc.messages.ShoppingCartDestroy;
import daris.client.model.sc.messages.ShoppingCartProcessingAbort;
import daris.client.ui.widget.MessageBox;

public class ShoppingCartGUI implements ObjectGUI {

    public static final ShoppingCartGUI INSANCE = new ShoppingCartGUI();

    public static final Image ICON_ORDER = new Image(Resource.INSTANCE.submit16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_DESTROY = new Image(Resource.INSTANCE.delete16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_DOWNLOAD = new Image(Resource.INSTANCE.download16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_CLEAR = new Image(Resource.INSTANCE.clear16().getSafeUri().asString(), 16, 16);
    public static final Image ICON_ABORT = new Image(Resource.INSTANCE.abort24().getSafeUri().asString(), 16, 16);
    public static final Image ICON_REFRESH = new arc.gui.image.Image(Resource.INSTANCE.refreshGreen16().getSafeUri()
            .asString(), 16, 16);
    public static final Image ICON_DETAIL = new arc.gui.image.Image(Resource.INSTANCE.detail24().getSafeUri()
            .asString(), 16, 16);

    public static final String ICON_ACTIVE = Resource.INSTANCE.active24().getSafeUri().asString();
    public static final String ICON_DEPOSIT = Resource.INSTANCE.deposit24().getSafeUri().asString();
    public static final String ICON_EDITABLE = Resource.INSTANCE.editable24().getSafeUri().asString();
    public static final String ICON_AWAIT_PROCESSING = Resource.INSTANCE.awaitProcessing24().getSafeUri().asString();
    public static final String ICON_ASSIGNED = Resource.INSTANCE.assigned24().getSafeUri().asString();
    public static final String ICON_PROCESSING = Resource.INSTANCE.processing24().getSafeUri().asString();
    public static final String ICON_DATA_READY = Resource.INSTANCE.download24().getSafeUri().asString();
    public static final String ICON_FULFILLED = Resource.INSTANCE.fulfilled24().getSafeUri().asString();
    public static final String ICON_REJECTED = Resource.INSTANCE.rejected24().getSafeUri().asString();
    public static final String ICON_ERROR = Resource.INSTANCE.error24().getSafeUri().asString();
    public static final String ICON_WITHDRAWN = Resource.INSTANCE.withdrawn24().getSafeUri().asString();
    public static final String ICON_ABORTED = Resource.INSTANCE.abort24().getSafeUri().asString();

    private ShoppingCartGUI() {

    }

    @Override
    public String idToString(Object o) {

        if (o instanceof ShoppingCartRef) {
            return Long.toString(((ShoppingCartRef) o).id());
        } else if (o instanceof ShoppingCart) {
            return Long.toString(((ShoppingCart) o).id());
        }
        return null;
    }

    @Override
    public String icon(Object o, int size) {
        long id = 0;
        Status status = null;
        if (o instanceof ShoppingCartRef) {
            id = ((ShoppingCartRef) o).id();
            status = ((ShoppingCartRef) o).status();
        } else if (o instanceof ShoppingCart) {
            id = ((ShoppingCart) o).id();
            status = ((ShoppingCart) o).status();
        }
        if (id > 0 && ActiveShoppingCart.isActive(id)) {
            return ICON_ACTIVE;
        } else {
            return iconFromStatus(status);
        }
    }

    @Override
    public Object reference(Object o) {

        return null;
    }

    @Override
    public boolean needToResolve(Object o) {

        if (o instanceof ShoppingCartRef) {
            return ((ShoppingCartRef) o).needToResolve();
        }
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
        if (o != null) {
            if (o instanceof ShoppingCartRef) {
                final ShoppingCartRef sc = (ShoppingCartRef) o;
                return new DropHandler() {

                    @Override
                    public DropCheck checkCanDrop(Object data) {
                        if (sc.isActive() && sc.status() == Status.editable) {
                            return DropCheck.CAN;
                        }
                        return DropCheck.CANNOT;
                    }

                    @Override
                    public void drop(BaseWidget target, List<Object> data, DropListener dl) {
                        if (data != null && Status.editable.equals(sc.status())) {
                            if (!data.isEmpty()) {
                                final DObjectRef o = (DObjectRef) data.get(0);
                                ActiveShoppingCart.addContents(o, true, new Action() {

                                    @Override
                                    public void execute() {
                                        MessageBox.info("Active shopping cart: " + sc.id(), o.idToString()
                                                + " has been added to shopping cart.", 3);
                                    }
                                });
                                dl.dropped(DropCheck.CAN);
                                return;
                            }
                        }
                        dl.dropped(DropCheck.CANNOT);
                    }
                };
            }
        }
        return null;
    }

    @Override
    public DragWidget dragWidget(Object o) {

        return null;
    }

    @Override
    public Menu actionMenu(Window w, Object o, SelectedObjectSet selected, boolean readOnly) {
        final ShoppingCartRef cart = (ShoppingCartRef) o;

        final ActionEntry downloadAE = new ActionEntry(ICON_DOWNLOAD, "Download shopping cart " + cart.id(),
                "Download the shopping cart", new Action() {

                    @Override
                    public void execute() {
                        ShoppingCartDownloadManager.download(cart, new ActionListener() {

                            @Override
                            public void executed(boolean succeeded) {
                                // TODO:
                            }
                        });
                    }
                });
        downloadAE.disable();
        final ActionEntry destroyAE = new ActionEntry(ICON_DESTROY, "Destroy shopping cart " + cart.id(),
                "Destroy the shopping cart", new Action() {

                    @Override
                    public void execute() {
                        new ShoppingCartDestroy(cart).send();
                    }
                });
        destroyAE.disable();
        final ActionEntry clearAE = new ActionEntry(ICON_CLEAR, "Remove finished shopping carts", new Action() {

            @Override
            public void execute() {
                new ShoppingCartDestroy(ListUtil.list(Status.data_ready, Status.aborted, Status.rejected,
                        Status.withdrawn, Status.error)).send();
            }
        });
        Menu menu = new Menu(cart.referentTypeName() + " - " + cart.idToString()) {
            @Override
            public void preShow() {

                cart.reset();
                cart.resolve(new ObjectResolveHandler<ShoppingCart>() {
                    @Override
                    public void resolved(ShoppingCart co) {
                        if (!(co.destination().method() == DeliveryMethod.download && co.status() == Status.data_ready)) {
                            downloadAE
                                    .softDisable("You can only download the content to your browser when the shopping cart destination is set to browser and the status of the shopping cart become data ready.");
                        } else {
                            downloadAE.enable();
                        }
                    }
                });
                if (ActiveShoppingCart.isActive(cart.id())) {
                    destroyAE.softDisable("Cannot destroy the active shopping cart " + cart.id() + ". It is in use.");
                } else {
                    destroyAE.enable();
                }
            }
        };
        menu.setShowTitle(false);
        if (!cart.isActive() && cart.status() == Status.editable) {
            ActionEntry activateAE = new ActionEntry(new Image(ICON_ACTIVE), "Set shopping cart " + cart.id()
                    + " as active", null, new Action() {

                @Override
                public void execute() {
                    ActiveShoppingCart.set(cart);
                }
            });
            menu.add(activateAE);
        }
        if (cart.status() == Status.processing) {
            menu.add(new ActionEntry(ICON_ABORT, "Abort shopping cart " + cart.id(), new Action() {

                @Override
                public void execute() {
                    new ShoppingCartProcessingAbort(cart).send();
                }
            }));
        }
        menu.add(downloadAE);
        if (cart.status() != Status.processing) {
            menu.add(destroyAE);
        }
        menu.add(clearAE);
        return menu;
    }

    @Override
    public Menu memberActionMenu(Window w, Object o, SelectedObjectSet selected, boolean readOnly) {
        return null;
    }

    @Override
    public ObjectUpdateHandle createUpdateMonitor(Object o, ObjectUpdateListener ul) {
        // TODO Auto-generated method stub
        return null;
    }

    public static String iconFromStatus(Status status) {
        if (status != null) {
            switch (status) {
            case editable:
                return ICON_EDITABLE;
            case await_processing:
                return ICON_AWAIT_PROCESSING;
            case assigned:
                return ICON_ASSIGNED;
            case processing:
                return ICON_PROCESSING;
            case data_ready:
                return ICON_DATA_READY;
            case fulfilled:
                return ICON_FULFILLED;
            case rejected:
                return ICON_REJECTED;
            case error:
                return ICON_ERROR;
            case withdrawn:
                return ICON_WITHDRAWN;
            case aborted:
                return ICON_ABORTED;
            default:
                break;
            }
        }
        return null;
    }

}
