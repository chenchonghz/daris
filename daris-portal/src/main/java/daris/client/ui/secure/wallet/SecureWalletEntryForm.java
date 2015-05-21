package daris.client.ui.secure.wallet;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldValidHandler;
import arc.gui.form.FieldValueValidator;
import arc.gui.form.Form;
import arc.gui.form.Form.BooleanAs;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.PasswordType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.secure.wallet.SecureWallet;
import daris.client.model.secure.wallet.Usage;

public class SecureWalletEntryForm extends ValidatedInterfaceComponent implements AsynchronousAction {

    private FormEditMode _mode;
    private String _key;
    private String _value;
    private boolean _maskValue;
    private boolean _forShoppingCart;
    private VerticalPanel _vp;
    private SimplePanel _formSP;
    private Form _form;
    private Field<String> _passwordField;
    private Field<String> _confirmPasswordField;
    private HTML _sb;

    public SecureWalletEntryForm(FormEditMode mode, String key, String value, boolean maskValue, boolean forShoppingCart) {

        _mode = mode;
        _key = key;
        _value = value;
        _maskValue = maskValue;
        _forShoppingCart = forShoppingCart;

        _vp = new VerticalPanel();
        _vp.fitToParent();

        _formSP = new SimplePanel();
        _formSP.fitToParent();
        _vp.add(_formSP);

        _sb = new HTML();
        _sb.setHeight(22);
        _sb.setFontSize(11);
        _sb.setColour(RGB.RED);
        _sb.setPaddingLeft(20);
        _vp.add(_sb);

        addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                Validity v = _form.valid();
                if (!v.valid()) {
                    _sb.setHTML(v.reasonForIssue());
                } else {
                    _sb.clear();
                }
            }
        });

        updateForm();

        notifyOfChangeInState();
    }

    private void updateForm() {

        if (_form != null) {
            _formSP.clear();
            removeMustBeValid(_form);
            _passwordField = null;
            _confirmPasswordField = null;
            _form = null;
        }
        _form = new Form(_mode) {
            public Validity valid() {
                Validity v = super.valid();
                if (v.valid()) {
                    if (_maskValue) {
                        if (_passwordField != null && _confirmPasswordField != null) {
                            if (!ObjectUtil.equals(_passwordField.value(), _confirmPasswordField.value())) {
                                return new IsNotValid("The values do not match.");
                            }
                        }
                    }
                }
                return v;
            }
        };
        _form.fitToParent();
        _form.setBooleanAs(BooleanAs.CHECKBOX);

        Field<String> keyField = new Field<String>(new FieldDefinition("key",
                (_mode != FormEditMode.CREATE && _key != null) ? ConstantType.DEFAULT : StringType.DEFAULT,
                "The key for the entry.", null, 1, 1));
        if (_mode == FormEditMode.CREATE) {
            keyField.addValueValidator(new FieldValueValidator<String>() {

                @Override
                public void validate(Field<String> f, final FieldValidHandler vh) {
                    final String key = f.value();
                    if (key == null) {
                        vh.setInvalid("Secure wallet key must be set.");
                    } else {
                        SecureWallet.contains(key, new ObjectMessageResponse<Boolean>() {

                            @Override
                            public void responded(Boolean exists) {
                                if (exists) {
                                    vh.setInvalid("Secure wallet key: " + key + " already exists.");
                                } else {
                                    vh.setValid();
                                }
                            }
                        });
                    }
                }
            });
        }
        if (_key != null) {
            keyField.setInitialValue(_key, false);
        }

        keyField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                _key = f.value();
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

            }
        });
        _form.add(keyField);

        if (_maskValue) {
            _passwordField = new Field<String>(new FieldDefinition("value", PasswordType.DEFAULT, "The entry value.",
                    null, 1, 1));
            if (_value != null) {
                _passwordField.setInitialValue(_value, false);
            }
            _passwordField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {
                    String v = f.value();
                    String cv = _confirmPasswordField.value();
                    if (v != null && v.equals(cv)) {
                        _value = v;
                    }
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f, Property property) {

                }
            });

            _form.add(_passwordField);
            _confirmPasswordField = new Field<String>(new FieldDefinition("confirm the value", PasswordType.DEFAULT,
                    "Confirm the value.", null, 1, 1));
            if (_value != null) {
                _confirmPasswordField.setInitialValue(_value, false);
            }
            _confirmPasswordField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {
                    String cv = f.value();
                    String v = _passwordField.value();
                    if (cv != null && cv.equals(v)) {
                        _value = cv;
                    }
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f, Property property) {

                }
            });
            _form.add(_confirmPasswordField);
        } else {
            Field<String> valueField = new Field<String>(new FieldDefinition("value", TextType.DEFAULT,
                    "The entry value", null, 1, 1));
            if (_value != null) {
                valueField.setInitialValue(_value, false);
            }
            valueField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {
                    _value = f.value();
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f, Property property) {

                }
            });
            _form.add(valueField);
        }

        Field<Boolean> maskValueField = new Field<Boolean>(new FieldDefinition("mask the value field",
                BooleanType.DEFAULT_TRUE_FALSE, "Mask the value field when entering so it cannot be seen.", null, 0, 1));
        maskValueField.setInitialValue(_maskValue, false);
        maskValueField.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                if (_maskValue != f.value()) {
                    _maskValue = f.value();
                    updateForm();
                }
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

            }
        });
        _form.add(maskValueField);

        Field<Boolean> forShoppingCartField = new Field<Boolean>(new FieldDefinition(
                "only accessible from shopping cart", BooleanType.DEFAULT_TRUE_FALSE,
                "Restrict access from only the shopping.cart.delivery context.", null, 0, 1));
        if (_forShoppingCart) {
            forShoppingCartField.setInitialValue(_forShoppingCart, false);
        }
        forShoppingCartField.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                _forShoppingCart = f.value();
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

            }
        });
        _form.add(forShoppingCartField);

        _form.render();
        _formSP.setContent(_form);
        addMustBeValid(_form);

    }

    public String key() {
        return _key;
    }

    public String value() {
        return _value;
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    @Override
    public void execute(final ActionListener l) {
        SecureWallet.setEntry(_key, _value, _forShoppingCart ? Usage.SHOPPING_CART_DELIVERY : null,
                new ObjectMessageResponse<Null>() {

                    @Override
                    public void responded(Null r) {
                        l.executed(true);
                    }
                });
    }

}
