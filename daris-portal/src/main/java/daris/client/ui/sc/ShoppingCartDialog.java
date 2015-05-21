package daris.client.ui.sc;

import java.util.List;

import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.ListUtil;
import arc.mf.event.Filter;
import arc.mf.event.Subscriber;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventChannel;
import arc.mf.model.shopping.events.ShoppingCartEvent;
import arc.mf.model.shopping.events.ShoppingEvent;
import arc.mf.object.ObjectResolveHandler;

import daris.client.model.sc.ActiveShoppingCart;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;
import daris.client.ui.widget.LoadingMessage;

public class ShoppingCartDialog implements Subscriber, ActiveShoppingCart.Listener {

    private arc.gui.gwt.widget.window.Window _win;

    // root container
    private SimplePanel _sp;

    private int _ascTabId = 0;

    private int _sclTabId = 0;

    private int _activeTabId = 0;

    /*
     * active shopping cart
     */
    private ActiveShoppingCartPanel _ascPanel;

    /*
     * shopping cart list
     */
    private ShoppingCartListPanel _sclPanel;

    public ShoppingCartDialog() {

        _sp = new SimplePanel();
        _sp.fitToParent();

        /*
         * active shopping cart tab
         */
        _ascPanel = new ActiveShoppingCartPanel() {
            protected void preOrderSubmit() {

            }

            protected void postOrderSubmit() {
                activateSCLTab();
            }

            protected void postOrder() {

            }

            protected void preLoad(ShoppingCartRef cart) {
            }

            protected void postLoad(ShoppingCart cart) {
            }
        };

        /*
         * shopping cart list tab
         */
        _sclPanel = new ShoppingCartListPanel(false);

        /*
         * activate active shopping cart tab
         */
        activateASCTab();

        SystemEventChannel.add(this);
        ActiveShoppingCart.addListener(this);

    }

    private void activateTab(final boolean asc) {
        _sp.setContent(new LoadingMessage("Loading " + (asc ? "active shopping cart" : "shopping cart list") + "..."));
        ActiveShoppingCart.resolve(new ObjectResolveHandler<ShoppingCart>() {

            @Override
            public void resolved(ShoppingCart cart) {
                if (_win != null) {
                    _win.setTitle(asc?"Active shopping cart " + cart.id():"Shopping cart list");
                }
                TabPanel tp = new TabPanel() {
                    @Override
                    protected void activated(int id) {
                        _activeTabId = id;
                        updateWindowTitle();
                    }
                };
                tp.fitToParent();
                _ascTabId = tp.addTab("Active shopping cart: " + cart.id(), "Active shopping cart " + cart.id(),
                        _ascPanel.gui());
                _sclTabId = tp.addTab("Shopping cart list", "All shopping carts.", _sclPanel.gui());
                tp.setActiveTabById(asc ? _ascTabId : _sclTabId);
                _sp.setContent(tp);
            }
        });
    }

    /**
     * Activate the active shopping cart tab
     */
    private void activateASCTab() {
        activateTab(true);
    }

    private void activateSCLTab() {
        activateTab(false);
    }

    private boolean isASCTabActive() {
        return _activeTabId > 0 && _activeTabId == _ascTabId;
    }

    private boolean isSCLTabActive() {
        return _activeTabId > 0 && _activeTabId == _sclTabId;
    }

    private void updateWindowTitle() {
        if (isSCLTabActive()) {
            if (_win != null) {
                _win.setTitle("Shopping cart list");
            }
        } else if (isASCTabActive()) {
            ActiveShoppingCart.get(new ObjectResolveHandler<ShoppingCartRef>() {

                @Override
                public void resolved(ShoppingCartRef asc) {
                    if (_win != null) {
                        _win.setTitle("Active shopping cart: " + asc.id());
                    }
                }
            });
        }
    }

    public void showDialog(Window owner, boolean active) {
        if (_win == null) {
            WindowProperties wp = new WindowProperties();
            wp.setOwnerWindow(owner);
            wp.setTitle("Shopping cart");
            wp.setCanBeClosed(true);
            wp.setCenterInPage(true);
            wp.setSize(0.7, 0.7);

            _win = arc.gui.gwt.widget.window.Window.create(wp);
            _win.addCloseListener(new WindowCloseListener() {

                @Override
                public void closed(arc.gui.gwt.widget.window.Window w) {
                    _win = null;
                }
            });
            _win.setContent(_sp);
        }
        updateWindowTitle();
        if (active) {
            activateASCTab();
        } else {
            activateSCLTab();
        }
        _win.show();
    }

    @Override
    public void activeCartChanged(ShoppingCartRef asc) {
        _ascPanel.refresh(true);
        _sclPanel.refresh(false);
        if (isASCTabActive()) {
            activateASCTab();
        }
        if (isSCLTabActive()) {
            activateSCLTab();
        }
    }

    @Override
    public void contentChanged(ShoppingCartRef asc) {
        _ascPanel.refresh(false);
        if (_sclPanel.isSelectedActive()) {
            _sclPanel.refresh(false);
        }
    }

    @Override
    public List<Filter> systemEventFilters() {
        return ListUtil.list(new Filter(ShoppingCartEvent.SYSTEM_EVENT_NAME), new Filter(
                ShoppingEvent.SYSTEM_EVENT_NAME));
    }

    @Override
    public void process(SystemEvent e) {
        _sclPanel.refresh(true);
        if (e instanceof ShoppingEvent) {
            ShoppingEvent se = (ShoppingEvent) e;
            switch (se.action()) {
            case DESTROY:
                if (ActiveShoppingCart.isActive(se.cartId())) {
                    ActiveShoppingCart.reset();
                }
                break;
            default:
                break;
            }
        } else if (e instanceof ShoppingCartEvent) {
            ShoppingCartEvent sce = (ShoppingCartEvent) e;
            switch (sce.action()) {
            case MODIFY:
                if (ActiveShoppingCart.isActive(sce.cartId())) {
                    new ShoppingCartRef(sce.cartId()).resolve(new ObjectResolveHandler<ShoppingCart>() {

                        @Override
                        public void resolved(ShoppingCart cart) {
                            if (cart.status() != Status.editable) {
                                ActiveShoppingCart.reset();
                            }
                        }
                    });
                }
                break;
            default:
                break;
            }
        }
    }

    private static ShoppingCartDialog _instance;

    public static ShoppingCartDialog get() {
        if (_instance == null) {
            _instance = new ShoppingCartDialog();
        }
        return _instance;
    }

    public static void reset() {
        if (_instance != null) {
            SystemEventChannel.remove(_instance);
            ActiveShoppingCart.removeListener(_instance);
            _instance = null;
        }
    }
}
