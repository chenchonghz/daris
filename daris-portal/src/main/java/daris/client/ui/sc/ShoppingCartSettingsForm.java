package daris.client.ui.sc;

import java.util.HashMap;
import java.util.Map;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.sc.Layout;
import daris.client.model.sc.Layout.Pattern;
import daris.client.model.sc.Layout.Type;
import daris.client.model.sc.LayoutPatternEnum;
import daris.client.model.sc.ShoppingCart;
import daris.client.model.sc.ShoppingCartRef;
import daris.client.model.sc.Status;
import daris.client.model.sc.messages.ShoppingCartModify;
import daris.client.model.sc.messages.ShoppingCartUserSelfSettingsSet;
import daris.client.model.transcode.Transcode;
import daris.client.model.transcode.TranscodeEnum;

public class ShoppingCartSettingsForm extends ValidatedInterfaceComponent {

    private final String TAB_NAME_GENERAL = "General";
    private final String TAB_NAME_DESTINATION = "Destination";
    // private final String TAB_NAME_TRANSFORMATION = "Tranformation";

    public static int LAST_ACTIVE_TAB_ID = 0;

    private VerticalPanel _vp;
    private TabPanel _tp;
    private Map<String, Integer> _tabIds;
    private HTML _sb;

    private Field<Layout.Type> _layoutTypeField;
    private Field<Layout.Pattern> _layoutPatternField;

    private ShoppingCart _cart;
    private FormEditMode _mode;

    public ShoppingCartSettingsForm(ShoppingCart cart, FormEditMode mode) {

        _mode = mode;
        _vp = new VerticalPanel();
        _vp.fitToParent();

        _tabIds = new HashMap<String, Integer>();
        _tp = new TabPanel() {
            protected void activated(int id) {
                LAST_ACTIVE_TAB_ID = id;
            }
        };
        _tp.fitToParent();
        _vp.add(_tp);

        SimplePanel sp = new SimplePanel();
        sp.setHeight(20);
        sp.setWidth100();
        _sb = new HTML();
        _sb.setColour(RGB.RED);
        _sb.setFontFamily("Helvetica");
        _sb.setFontWeight(FontWeight.BOLD);
        _sb.setFontSize(12);
        sp.setContent(_sb);
        _vp.add(sp);

        if (_mode != FormEditMode.READ_ONLY) {
            addChangeListener(new StateChangeListener() {

                @Override
                public void notifyOfChangeInState() {
                    Validity v = valid();
                    if (!v.valid()) {
                        _sb.setHTML(v.reasonForIssue());
                    } else {
                        _sb.clear();
                    }
                }
            });
        }

        setCart(cart);

    }

    public void setCart(ShoppingCart cart) {
        if (cart == null) {
            return;
        }
        _cart = cart;
        updateTabs(_cart);
    }

    protected void addTab(String name, String description, BaseWidget w) {
        if (_tabIds.containsKey(name)) {
            _tp.setTabContent(_tabIds.get(name), w);
        } else {
            int tabId = _tp.addTab(name, description, w);
            _tabIds.put(name, tabId);
        }
    }

    protected void removeTab(String name) {
        if (_tabIds.containsKey(name)) {
            _tp.removeTabById(_tabIds.get(name));
            _tabIds.remove(name);
        }
    }

    protected void setTab(String name, String description, BaseWidget w) {
        addTab(name, description, w);
    }

    private void updateTabs(final ShoppingCart cart) {

        removeAllMustBeValid();

        Form mainForm = new Form(_mode) {
            public Validity valid() {
                Validity v = super.valid();
                if (v.valid() && cart.layout().type() == Layout.Type.custom && cart.layout().pattern() == null) {
                    return new IsNotValid("If layout type is custom, the layout pattern must be specified.");
                }
                return v;
            }
        };

        // @formatter:off
//        Field<String> statusField = new Field<String>(new FieldDefinition("Status", ConstantType.DEFAULT,
//                "Status of the shopping cart", null, 1, 1));
//        statusField.setValue(_cart.status().toString());
//        add(statusField);
//        if (_cart.totalNumberOfContentItems() > 0) {
//            Field<Integer> nbItemsField = new Field<Integer>(new FieldDefinition("Number of Datasets",
//                    ConstantType.DEFAULT, "Total number of datasets in the shopping cart", null, 1, 1));
//            nbItemsField.setValue(_cart.totalNumberOfContentItems());
//            add(nbItemsField);
//        }
//        if (_cart.totalSizeOfContentItems() > 0) {
//            Field<String> totalSizeField = new Field<String>(new FieldDefinition("Total Size", ConstantType.DEFAULT,
//                    "Total size of the datasets in the shopping cart", null, 1, 1));
//            totalSizeField.setValue(ByteUtil.humanReadableByteCount(_cart.totalSizeOfContentItems(), true));
//            add(totalSizeField);
//        }
        // @formatter:on

        Field<String> nameField = new Field<String>(new FieldDefinition("name", StringType.DEFAULT,
                "Name for the shopping-cart", null, 0, 1));
        nameField.setInitialValue(_cart.name(), false);
        nameField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                cart.setName(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        mainForm.add(nameField);

        FieldGroup layoutFieldGroup = new FieldGroup(new FieldDefinition("layout", ConstantType.DEFAULT,
                "The layout to use when producing the cart output content.", null, 1, 1));
        _layoutTypeField = new Field<Layout.Type>(new FieldDefinition("type", new EnumerationType<Layout.Type>(
                Layout.Type.values()), "Layout type", null, 1, 1));
        _layoutTypeField.setInitialValue(cart.layout().type(), false);
        _layoutTypeField.addListener(new FormItemListener<Layout.Type>() {

            @Override
            public void itemValueChanged(FormItem<Type> f) {
                Layout.Type type = f.value();
                Layout.Pattern pattern = _layoutPatternField.value();
                if (type == Layout.Type.custom) {
                    if (pattern != null) {
                        cart.setLayout(new Layout(type, pattern));
                    }
                } else {
                    if (pattern == null) {
                        cart.setLayout(new Layout(type, pattern));
                    } else {
                        _layoutPatternField.setValue(null);
                    }
                }
            }

            @Override
            public void itemPropertyChanged(FormItem<Type> f, Property property) {

            }
        });
        layoutFieldGroup.add(_layoutTypeField);
        _layoutPatternField = new Field<Layout.Pattern>(new FieldDefinition("pattern",
                new EnumerationType<Layout.Pattern>(new LayoutPatternEnum()),
                "An Asset Path Language (APL) expression. Only applicable if the layout type is custom.", null, 0, 1));
        if (cart.layout() != null && cart.layout().pattern() != null) {
            Layout.Pattern.resolve(cart.layout().pattern().pattern(), new ObjectResolveHandler<Layout.Pattern>() {

                @Override
                public void resolved(Pattern po) {
                    _layoutPatternField.setInitialValue(po);
                }
            });
        }
        _layoutPatternField.addListener(new FormItemListener<Layout.Pattern>() {

            @Override
            public void itemValueChanged(FormItem<Pattern> f) {
                Layout.Pattern pattern = f.value();
                Layout.Type type = _layoutTypeField.value();
                if (type == Layout.Type.custom) {
                    if (pattern != null) {
                        cart.setLayout(new Layout(type, pattern));
                    }
                } else {
                    if (pattern == null) {
                        cart.setLayout(new Layout(type, pattern));
                    }
                }
            }

            @Override
            public void itemPropertyChanged(FormItem<Pattern> f, Property property) {

            }
        });
        layoutFieldGroup.add(_layoutPatternField);
        mainForm.add(layoutFieldGroup);

        if (cart.transcodes() != null) {
            FieldGroup transcodesFieldGroup = new FieldGroup(new FieldDefinition("Data Transformation",
                    ConstantType.DEFAULT, null, null, 1, 1));
            for (Transcode t : cart.transcodes()) {
                FieldGroup transcodeFieldGroup = new FieldGroup(new FieldDefinition("transcode", ConstantType.DEFAULT,
                        null, null, 1, 1));
                Field<String> transcodeFromField = new Field<String>(new FieldDefinition("from", ConstantType.DEFAULT,
                        null, null, 1, 1));
                transcodeFromField.setValue(t.from());
                transcodeFieldGroup.add(transcodeFromField);
                Field<Transcode> transcodeToField = new Field<Transcode>(new FieldDefinition("to",
                        new EnumerationType<Transcode>(new TranscodeEnum(t.from())), null, null, 1, 1));
                transcodeToField.setInitialValue(t, false);
                transcodeToField.addListener(new FormItemListener<Transcode>() {

                    @Override
                    public void itemValueChanged(FormItem<Transcode> f) {

                        cart.setTranscode(f.value());
                    }

                    @Override
                    public void itemPropertyChanged(FormItem<Transcode> f, FormItem.Property p) {

                    }
                });
                transcodeFieldGroup.add(transcodeToField);
                transcodesFieldGroup.add(transcodeFieldGroup);
            }
            mainForm.add(transcodesFieldGroup);
        }
        addMustBeValid(mainForm);
        mainForm.render();

        setTab(TAB_NAME_GENERAL, null, new ScrollPanel(mainForm, ScrollPolicy.AUTO));

        DestinationForm destForm = new DestinationForm(cart, _mode);
        addMustBeValid(destForm);

        setTab(TAB_NAME_DESTINATION, null, (BaseWidget) destForm.gui());

        if (LAST_ACTIVE_TAB_ID > 0) {
            _tp.setActiveTabById(LAST_ACTIVE_TAB_ID);
        } else {
            _tp.setActiveTab(0);
        }

    }

    void applySettings(boolean saveToUserSelfSettings, final ObjectMessageResponse<Null> rh) {
        if (_cart.status() != Status.editable) {
            rh.responded(null);
            return;
        }
        if (saveToUserSelfSettings) {
            /*
             * save sink settings to user self settings
             */
            _cart.destination().saveSinkSettings(null);
            /*
             * save cart settings to user self settings
             */
            new ShoppingCartUserSelfSettingsSet(_cart).send(new ObjectMessageResponse<Null>() {

                @Override
                public void responded(Null r) {
                    new ShoppingCartModify(_cart).send(rh);
                }
            });
        } else {
            new ShoppingCartModify(_cart).send(rh);
        }
    }

    @Override
    public Widget gui() {
        return _vp;
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
