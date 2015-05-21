package daris.client.ui.sc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.DateTime;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.user.client.Timer;

import daris.client.model.sc.ActiveShoppingCart;
import daris.client.model.sc.DeliveryMethod;
import daris.client.model.sc.Log;
import daris.client.model.sc.Progress;
import daris.client.model.sc.ProgressHandler;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;
import daris.client.model.sc.messages.ShoppingCartList;
import daris.client.ui.DObjectGUIRegistry;
import daris.client.ui.widget.ProgressBar;

public class ShoppingCartGrid extends ListGrid<ShoppingCartRef> {

    private List<ShoppingCartRef> _carts;
    private Map<ShoppingCartRef, Timer> _timers;

    public ShoppingCartGrid(List<ShoppingCartRef> scs) {
        super(ScrollPolicy.AUTO);
        _timers = new HashMap<ShoppingCartRef, Timer>();
        addColumnDefn("id", "Cart", "Shopping Cart", new WidgetFormatter<ShoppingCartRef, Long>() {
            @Override
            public BaseWidget format(ShoppingCartRef sc, Long id) {
                StringBuilder sb = new StringBuilder();
                sb.append(id);
                if (sc.name() != null) {
                    sb.append(": ");
                    sb.append(sc.name());
                }
                String title = sb.toString();
                String icon = ShoppingCartGUI.INSANCE.icon(sc, 16);
                return new HTML("<div><img src=\"" + icon
                        + "\" style=\"width:16px;height:16px;vertical-align:middle\"><span style=\"\">&nbsp;" + title
                        + "</span></div>");
            }
        }).setWidth(120);
        addColumnDefn("status", "Status", "Status", new WidgetFormatter<ShoppingCartRef, Status>() {
            @Override
            public BaseWidget format(final ShoppingCartRef sc, Status status) {
                final HTML w = new HTML();
                if (sc.status() == null) {
                    sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

                        @Override
                        public void resolved(ShoppingCart c) {
                            w.setHTML(c.status().toString());
                        }
                    });
                } else {
                    w.setHTML(sc.status().toString());
                }
                return w;
            }
        }).setWidth(80);
        addColumnDefn("progress", "Progress", "Progress", new WidgetFormatter<ShoppingCartRef, Status>() {
            @Override
            public BaseWidget format(final ShoppingCartRef sc, Status status) {
                final ProgressBar pw = new ProgressBar();
                pw.setWidth(200);
                if (Status.data_ready == status) {
                    pw.setProgress(1.0);
                } else if (Status.processing == status) {
                    Timer t = sc.monitorProgress(1000, new ProgressHandler() {

                        @Override
                        public void progress(Progress progress) {
                            if (progress != null) {
                                String msg = DateTime.durationAsString((long) (progress.duration() * 1000)) + " - "
                                        + progress.completed() + "/" + progress.total() + " processed.";
                                pw.setProgress((double) progress.completed() / (double) progress.total(), msg);
                            } else {
                                _timers.remove(sc);
                            }
                        }
                    });
                    _timers.put(sc, t);
                } else {
                    pw.setProgress(0.0);
                }
                return pw;
            }
        }).setWidth(220);
        addColumnDefn("log", "Log", "Log", new WidgetFormatter<ShoppingCartRef, Status>() {
            @Override
            public BaseWidget format(ShoppingCartRef sc, Status status) {
                final HTML w = new HTML();
                sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

                    @Override
                    public void resolved(ShoppingCart c) {
                        updateLog(w, c);
                    }
                });
                return w;
            }
        }).setWidth(300);
        setMultiSelect(false);
        setPreferredHeight(0.5);
        setEmptyMessage("");
        setLoadingMessage("");
        setObjectRegistry(DObjectGUIRegistry.get());
        // @formatter:off
//        setRowContextMenuHandler(new ListGridRowContextMenuHandler<ShoppingCartRef>() {
//
//            @Override
//            public void show(ShoppingCartRef cart, ContextMenuEvent event) {
//                Menu menu = ShoppingCartGUI.INSANCE.actionMenu(ShoppingCartGrid.this.window(), cart,
//                        new SelectedObjectSet() {
//
//                            @Override
//                            public List<ShoppingCartRef> selections() {
//                                return ShoppingCartGrid.this.selections();
//                            }
//                        }, true);
//                ActionContextMenu acm = new ActionContextMenu(menu);
//                NativeEvent ne = event.getNativeEvent();
//                acm.showAt(ne);
//            }
//        });
//        enableDropTarget(true);
        // @formatter:on

        update(true);
    }

    public void update(boolean reload) {
        if (reload) {
            new ShoppingCartList(null).send(new ObjectMessageResponse<List<ShoppingCartRef>>() {
                @Override
                public void responded(List<ShoppingCartRef> carts) {
                    setCarts(carts);
                }
            });
        } else {
            setCarts(_carts);
        }
    }

    private void setCarts(final List<ShoppingCartRef> carts) {
        _carts = carts;
        for (Timer t : _timers.values()) {
            t.cancel();
        }
        _timers.clear();

        if (carts == null) {
            setData(null);
            return;
        }
        List<ListGridEntry<ShoppingCartRef>> es = new Vector<ListGridEntry<ShoppingCartRef>>(carts.size());
        for (ShoppingCartRef sc : carts) {
            ListGridEntry<ShoppingCartRef> e = new ListGridEntry<ShoppingCartRef>(sc);
            e.set("id", sc.id());
            e.set("status", sc.status());
            e.set("progress", sc.status());
            e.set("log", sc.status());
            es.add(e);
        }
        if (!es.isEmpty()) {
            ShoppingCartRef selected = null;
            List<ShoppingCartRef> selections = selections();
            if (selections != null) {
                if (!selections.isEmpty()) {
                    selected = selections.get(0);
                }
            }
            setData(es);
            if (selected != null && carts.contains(selected)) {
                select(selected);
            } else {
                ActiveShoppingCart.get(new ObjectResolveHandler<ShoppingCartRef>() {

                    @Override
                    public void resolved(ShoppingCartRef asc) {
                        select(asc);
                    }
                });
            }
        } else {
            setData(null);
        }
    }

    private void updateLog(HTML w, ShoppingCart c) {

        Log log = null;
        if (c.logs() != null) {
            if (!c.logs().isEmpty()) {
                log = c.logs().get(0);
            }
        }
        switch (c.status()) {
        case editable:
            if (c.isActive()) {
                w.setHTML("Cart is active. " + (log != null ? log.message : ""));
            } else {
                w.setHTML(log != null ? log.message : "");
            }
            break;
        case data_ready:
            if (c.destination().method() == DeliveryMethod.deposit) {
                w.setHTML("Data has been transfered to " + c.destination().name());
            } else {
                w.setHTML("Data archive has been prepared. Ready to download");
            }
            break;
        default:
            w.setHTML(log != null ? log.message : "");
        }
    }

    public boolean hasCarts() {
        return _carts != null && !_carts.isEmpty();
    }
}
