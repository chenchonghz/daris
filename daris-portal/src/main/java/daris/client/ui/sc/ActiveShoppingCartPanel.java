package daris.client.ui.sc;

import java.util.List;

import arc.gui.InterfaceComponent;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.image.Image;
import arc.mf.client.util.Action;
import arc.mf.client.util.StateChangeListener;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.model.sc.ActiveShoppingCart;
import daris.client.model.sc.ContentItem;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.ui.sc.ShoppingCartContentsForm.ContentSelectionListener;
import daris.client.ui.util.ButtonUtil;
import daris.client.ui.widget.LoadingMessage;
import daris.client.ui.widget.MessageBox;

public class ActiveShoppingCartPanel implements InterfaceComponent {

    public static Image ICON_REMOVE = new arc.gui.image.Image(Resource.INSTANCE.remove16().getSafeUri().asString(), 16,
            16);
    public static Image ICON_CLEAR = new arc.gui.image.Image(Resource.INSTANCE.clear16().getSafeUri().asString(), 16,
            16);
    public static Image ICON_APPLY = new arc.gui.image.Image(Resource.INSTANCE.tickGreen16().getSafeUri().asString(),
            16, 16);
    public static Image ICON_ORDER = new arc.gui.image.Image(Resource.INSTANCE.submit16().getSafeUri().asString(), 16,
            16);
    public static Image ICON_REFRESH = new arc.gui.image.Image(Resource.INSTANCE.refreshGreen16().getSafeUri()
            .asString(), 16, 16);

    private SimplePanel _sp;
    private VerticalPanel _vp;
    private ShoppingCartForm _form;
    private SimplePanel _bbSP;
    private Button _contentsRemoveButton;
    private Button _contentsClearButton;
    private Button _settingsApplyButton;
    private Button _orderButton;
    private Button _refreshButton;

    ActiveShoppingCartPanel() {

        _sp = new SimplePanel();
        _sp.fitToParent();

        _vp = new VerticalPanel();
        _vp.fitToParent();
        _sp.setContent(_vp);

        _bbSP = new SimplePanel();
        _bbSP.setHeight(28);
        _bbSP.setWidth100();

        /*
         * shopping cart form
         */
        _form = new ShoppingCartForm(null, FormEditMode.UPDATE, false) {
            @Override
            protected void contentsTabActivated() {
                ActiveShoppingCart.resolve(new ObjectResolveHandler<ShoppingCart>() {

                    @Override
                    public void resolved(ShoppingCart o) {
                        updateButtons();
                    }
                });
            }

            @Override
            protected void settingsTabActivated() {
                ActiveShoppingCart.resolve(new ObjectResolveHandler<ShoppingCart>() {

                    @Override
                    public void resolved(ShoppingCart o) {
                        updateButtons();
                    }
                });
            }
        };

        _form.addContentSelectionListener(new ContentSelectionListener() {

            @Override
            public void contentSelectionChanged(List<ContentItem> selections) {
                _contentsRemoveButton.setEnabled(_form.hasSelectedContentItems());
            }
        });

        _form.addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                _orderButton.setEnabled(_form.valid().valid());
            }
        });

        _form.addContentsChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                if (!_form.hasContentItems()) {
                    _contentsRemoveButton.disable();
                }
                _contentsClearButton.setEnabled(_form.hasContentItems());
            }
        });
        _form.addSettingsChangeLisenter(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                _settingsApplyButton.setEnabled(_form.settingsValid());
                // _settingsApplyButton.setEnabled(true);
            }
        });

        /*
         * buttons
         */
        _refreshButton = ButtonUtil.createButton(ICON_REFRESH, "Refresh",
                "Refresh the shopping cart contents and settings.", false, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        _refreshButton.disable();
                        refresh(false);
                    }
                });

        _contentsRemoveButton = ButtonUtil.createButton(ICON_REMOVE, "Remove",
                "Remove the selected items from the cart.", false, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        ActiveShoppingCart.removeContents(_form.selectedContentItems(), null);
                    }
                });

        _contentsClearButton = ButtonUtil.createButton(ICON_CLEAR, "Clear", "Remove all items from the cart.", false,
                new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        _contentsRemoveButton.disable();
                        _contentsClearButton.disable();
                        ActiveShoppingCart.clearContents(null);
                    }
                });

        _settingsApplyButton = ButtonUtil.createButton(ICON_APPLY, "Apply", "Apply the changes on the cart settings.",
                false, new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        _settingsApplyButton.disable();
                        _form.applySettings(true, null);
                    }
                });

        _orderButton = ButtonUtil.createButton(ICON_ORDER, "Order", "Submit the cart for processing.", false,
                new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        preOrderSubmit();
                        _orderButton.disable();
                        if (_form.settingsChanged()) {
                            _settingsApplyButton.disable();
                            _form.applySettings(true, new ObjectMessageResponse<Null>() {

                                @Override
                                public void responded(Null r) {
                                    ActiveShoppingCart.order(new Action() {
                                        @Override
                                        public void execute() {
                                            MessageBox.info("shopping cart",
                                                    "shopping cart has been submitted for processing", 3);
                                            postOrder();
                                        }
                                    });
                                    postOrderSubmit();
                                }
                            });
                        } else {
                            ActiveShoppingCart.order(new Action() {
                                @Override
                                public void execute() {
                                    MessageBox.info("shopping cart", "shopping cart has been submitted for processing",
                                            3);
                                    postOrder();
                                }
                            });
                            postOrderSubmit();
                        }
                    }
                });
        _vp.add(_form.gui());

        _vp.add(_bbSP);

        refresh(true);

    }

    private void updateButtons() {
        if (_form == null) {
            return;
        }
        _bbSP.setContent(new CenteringPanel(new HTML("Updating...")));
        ButtonBar bb = ButtonUtil.createButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.RIGHT, 28);
        if (_form.isContentsTabActivated()) {
            _contentsRemoveButton.setEnabled(_form.hasSelectedContentItems());
            bb.add(_contentsRemoveButton);
            _contentsClearButton.setEnabled(_form.hasContentItems());
            bb.add(_contentsClearButton);
        } else {
            _settingsApplyButton.setEnabled(_form.settingsChanged());
            bb.add(_settingsApplyButton);
        }
        /*
         * order button available in both tabs
         */
        bb.add(_orderButton);
        _orderButton.setEnabled(_form.valid().valid());

        /*
         * refresh button available in both tabs
         */
        bb.add(_refreshButton);
        _refreshButton.enable();
        _bbSP.setContent(bb);
    }

    void refresh(boolean reload) {
        if (reload) {
            _sp.setContent(new LoadingMessage("Loading active shopping cart..."));
            _contentsRemoveButton.disable();
            _contentsClearButton.disable();
            _settingsApplyButton.disable();
            _orderButton.disable();
            _refreshButton.disable();
            ActiveShoppingCart.get(new ObjectResolveHandler<ShoppingCartRef>() {

                @Override
                public void resolved(ShoppingCartRef asc) {
                    preLoad(asc);
                    _sp.setContent(new LoadingMessage("Loading active shopping cart " + asc.id() + "..."));
                    asc.reset();
                    asc.resolve(new ObjectResolveHandler<ShoppingCart>() {

                        @Override
                        public void resolved(ShoppingCart cart) {
                            _form.setCart(cart);
                            _sp.setContent(_vp);
                            updateButtons();
                            postLoad(cart);
                        }
                    });
                }
            });
        } else {
            _form.refreshContents();
            _form.refreshSettings();
            updateButtons();
        }
    }

    protected void preOrderSubmit() {

    }

    protected void postOrderSubmit() {

    }

    protected void postOrder() {

    }

    protected void preLoad(ShoppingCartRef cart) {
    }

    protected void postLoad(ShoppingCart cart) {
    }

    @Override
    public Widget gui() {
        return _sp;
    }

    public BaseWidget widget() {
        return _sp;
    }
}
