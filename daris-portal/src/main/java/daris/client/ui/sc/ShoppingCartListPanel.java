package daris.client.ui.sc;

import arc.gui.InterfaceComponent;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.panel.VerticalSplitPanel;
import arc.gui.image.Image;
import arc.mf.client.util.Action;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.ListUtil;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.sc.ActiveShoppingCart;
import daris.client.model.sc.DeliveryMethod;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartDownloadManager;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;
import daris.client.model.sc.messages.ShoppingCartDestroy;
import daris.client.model.sc.messages.ShoppingCartOrder;
import daris.client.model.sc.messages.ShoppingCartProcessingAbort;
import daris.client.ui.util.ButtonUtil;
import daris.client.ui.widget.MessageBox;

public class ShoppingCartListPanel implements InterfaceComponent {

    private VerticalPanel _vp;
    private SimplePanel _sp;
    private ShoppingCartGrid _grid;
    private ShoppingCartForm _form;
    private SimplePanel _bbSP;
    private Button _detailButton;
    private Button _activateButton;
    private Button _orderButton;
    private Button _abortButton;
    private Button _downloadButton;
    private Button _destroyButton;
    private Button _clearButton;
    private Button _refreshButton;

    private ShoppingCartRef _selected;
    private boolean _showDetail;

    public ShoppingCartListPanel(boolean showDetail) {

        _vp = new VerticalPanel();
        _vp.fitToParent();

        _sp = new SimplePanel();
        _sp.fitToParent();
        _vp.add(_sp);

        _form = new ShoppingCartForm(null, FormEditMode.READ_ONLY, true);

        _grid = new ShoppingCartGrid(null);
        _grid.fitToParent();

        _grid.setSelectionHandler(new SelectionHandler<ShoppingCartRef>() {

            @Override
            public void selected(ShoppingCartRef sc) {
                _selected = sc;
                if (_selected != null) {
                    _selected.resolve(new ObjectResolveHandler<ShoppingCart>() {

                        @Override
                        public void resolved(ShoppingCart cart) {
                            _form.setCart(cart);
                        }
                    });
                }
                updateButtons(sc);
            }

            @Override
            public void deselected(ShoppingCartRef o) {

            }
        });
        _sp.setContent(_grid);

        _bbSP = new SimplePanel();
        _bbSP.setWidth100();
        _bbSP.setHeight(28);
        _vp.add(_bbSP);

        _detailButton = ButtonUtil.createButton(ShoppingCartGUI.ICON_DETAIL, "Show Detail",
                "Display the details of the selected cart.", false, new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        _detailButton.disable();
                        setShowDetail(!_showDetail);
                        ButtonUtil.setButtonLabel(_detailButton, ShoppingCartGUI.ICON_DETAIL,
                                _showDetail ? "Hide Detail" : "Show Detail");
                        _detailButton.enable();
                    }
                });

        _activateButton = ButtonUtil.createButton(new Image(ShoppingCartGUI.ICON_ACTIVE), "Set Active",
                "Set the selected cart as the active shopping cart.", false, new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        _activateButton.disable();
                        if (!_selected.isActive() && _selected.status() == Status.editable) {
                            ActiveShoppingCart.set(_selected);
                        }
                    }
                });

        _orderButton = ButtonUtil.createButton(ShoppingCartGUI.ICON_ORDER, "Order", "Submit the cart for processing.",
                false, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (_selected == null || _selected.status() != Status.editable) {
                            return;
                        }
                        final long cartId = _selected.id();
                        _orderButton.disable();
                        if (_selected.isActive()) {
                            ActiveShoppingCart.order(new Action() {

                                @Override
                                public void execute() {
                                    MessageBox.info("Ordered", "Shopping cart " + cartId + " has been ordered.", 2);
                                }
                            });
                        } else {
                            new ShoppingCartOrder(_selected).send(new ObjectMessageResponse<Null>() {

                                @Override
                                public void responded(Null r) {
                                    MessageBox.info("Ordered", "Shopping cart " + cartId + " has been ordered.", 2);
                                }
                            });
                        }
                    }
                });

        _abortButton = ButtonUtil.createButton(ShoppingCartGUI.ICON_ORDER, "Abort", "Abort the cart.", false,
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (_selected != null && _selected.status() == Status.processing) {
                            _abortButton.disable();
                            final long cartId = _selected.id();
                            new ShoppingCartProcessingAbort(_selected).send(new ObjectMessageResponse<Null>() {

                                @Override
                                public void responded(Null r) {
                                    MessageBox.info("Aborted", "Shopping cart " + cartId + " has been aborted.", 2);
                                }
                            });
                        }
                    }
                });

        _downloadButton = ButtonUtil.createButton(ShoppingCartGUI.ICON_DOWNLOAD, "Download", "Download the cart.",
                false, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (_selected == null || _selected.status() != Status.data_ready) {
                            return;
                        }
                        _downloadButton.disable();
                        final ShoppingCartRef sc = _selected;
                        sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

                            @Override
                            public void resolved(ShoppingCart cart) {
                                if (cart.destination().method() == DeliveryMethod.download
                                        && cart.status() == Status.data_ready) {
                                    ShoppingCartDownloadManager.download(sc, new ActionListener() {

                                        @Override
                                        public void executed(boolean succeeded) {
                                            _downloadButton.enable();
                                        }
                                    });
                                }
                            }
                        });
                    }
                });

        _destroyButton = ButtonUtil.createButton(ShoppingCartGUI.ICON_DESTROY, "Delete", "Delete the cart.", false,
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (_selected == null || _selected.isActive()) {
                            return;
                        }
                        _destroyButton.disable();
                        final ShoppingCartRef sc = _selected;
                        sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

                            @Override
                            public void resolved(ShoppingCart cart) {
                                if (cart.canDestroy()) {
                                    new ShoppingCartDestroy(sc).send(new ObjectMessageResponse<Null>() {

                                        @Override
                                        public void responded(Null r) {
                                            MessageBox.info("Deleted",
                                                    "Shopping cart " + sc.id() + "has been deleted.", 2);

                                        }
                                    });
                                }
                            }
                        });
                    }
                });

        _clearButton = ButtonUtil.createButton(ShoppingCartGUI.ICON_CLEAR, "Clear",
                "Delete all the shopping carts in the state of '" + Status.data_ready + "', '" + Status.aborted
                        + "', '" + Status.rejected + "', '" + Status.withdrawn + "' or '" + Status.error + "'.", false,
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        _clearButton.disable();
                        new ShoppingCartDestroy(ListUtil.list(Status.data_ready, Status.aborted, Status.rejected,
                                Status.withdrawn, Status.error)).send();
                    }
                });

        _refreshButton = ButtonUtil.createButton(ShoppingCartGUI.ICON_REFRESH, "Refresh", "Refresh the cart list.",
                false, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        _grid.update(true);
                    }
                });

        setShowDetail(showDetail);
        updateButtons(null);
    }

    public void setShowDetail(boolean showDetail) {

        if (_showDetail == showDetail) {
            return;
        }
        _showDetail = showDetail;
        if (_showDetail) {
            _sp.clear();
            VerticalSplitPanel vsp = new VerticalSplitPanel(5);
            vsp.fitToParent();
            _grid.setWidth100();
            _grid.setPreferredHeight(0.5);
            vsp.add(_grid);
            _form.widget().fitToParent();
            vsp.add(_form.widget());
            _sp.setContent(vsp);
        } else {
            _grid.fitToParent();
            _sp.setContent(_grid);
        }
    }

    private void updateButtons(final ShoppingCartRef sc) {
        _bbSP.setContent(new CenteringPanel(new HTML("Updating...")));
        final ButtonBar bb = ButtonUtil.createButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.RIGHT, 28);
        if (sc == null) {
            _bbSP.setContent(bb);
        } else {
            _detailButton.enable();
            bb.add(_detailButton);
            if (!sc.isActive() && sc.status() == Status.editable) {
                _activateButton.enable();
                bb.add(_activateButton);
            }
            sc.resolve(new ObjectResolveHandler<ShoppingCart>() {

                @Override
                public void resolved(ShoppingCart cart) {
                    if (cart.status() == Status.editable && cart.numberOfContentItems() > 0) {
                        _orderButton.enable();
                        bb.add(_orderButton);
                    }
                    if (cart.status() == Status.processing) {
                        _abortButton.enable();
                        bb.add(_abortButton);
                    }
                    if (cart.status() == Status.data_ready && cart.destination().method() == DeliveryMethod.download) {
                        _downloadButton.enable();
                        bb.add(_downloadButton);
                    }
                    _destroyButton.setEnabled(!cart.isActive() && cart.canDestroy());
                    bb.add(_destroyButton);
                    _clearButton.setEnabled(_grid.hasCarts());
                    bb.add(_clearButton);
                    _refreshButton.enable();
                    bb.add(_refreshButton);
                    _bbSP.setContent(bb);
                }
            });
        }
    }

    public void refresh(boolean reload) {
        _grid.update(reload);
    }

    public ShoppingCartRef selected() {
        return _selected;
    }

    public boolean isSelectedActive() {
        return _selected != null && _selected.isActive();
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    public BaseWidget widget() {
        return _vp;
    }
}
