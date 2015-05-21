package daris.client.ui.sc.sink;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.PasswordType;
import arc.mf.dtype.TextType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.sc.DeliveryArg;
import daris.client.model.sc.DeliveryDestination;
import daris.client.model.secure.wallet.SecureWalletEntryRef;
import daris.client.ui.secure.wallet.SecureWalletEntrySelectDialog;

public class SecureArgForm extends ValidatedInterfaceComponent {

    public static final String MASKED_VALUE = "********************************";

    private DeliveryDestination _destination;
    private FormEditMode _mode;
    private String _name;
    private String _title;
    private String _description;
    private boolean _mandatory;
    private boolean _maskValue;
    private boolean _inSecureWallet;

    private SimplePanel _sp;
    private Form _argForm;
    private Field<String> _argField;

    public SecureArgForm(DeliveryDestination destination, FormEditMode mode, String name, boolean mandatory,
            String title, String description, boolean maskValue) {
        _destination = destination;
        _mode = mode;
        _name = name;
        _mandatory = mandatory;
        _title = title;
        _description = description;
        _maskValue = maskValue;
        DeliveryArg arg = destination.arg(_name);
        if (arg == null) {
            _inSecureWallet = false;
        } else {
            _inSecureWallet = (arg.type() == DeliveryArg.Type.secure_wallet_delivery_arg);
        }

        _sp = new SimplePanel();
        if (_maskValue) {
            _sp.setHeight(30);
        } else {
            _sp.setHeight(120);
        }

        updateForm();

    }

    private void updateForm() {

        if (_argForm != null) {
            removeMustBeValid(_argForm);
            _sp.clear();
        }

        HorizontalPanel hp = new HorizontalPanel();
        hp.setHeight(22);

        DeliveryArg arg = _destination.arg(_name);

        _argForm = new Form(_mode) {
            @Override
            public Validity valid() {
                Validity v = super.valid();
                if (!v.valid()) {
                    DeliveryArg arg = _destination.arg(_name);
                    if (arg != null && arg.isSecureDeliveryArg() && arg.committed()) {
                        return IsValid.INSTANCE;
                    }
                }
                return v;
            }
        };
        _argField = new Field<String>(new FieldDefinition(_title, _inSecureWallet ? new EnumerationType<String>(
                DeliveryArg.getSecureWalletKeyEnum()) : (_maskValue ? PasswordType.DEFAULT : TextType.DEFAULT),
                _description, null, _mandatory ? 1 : 0, 1));
        if (arg != null) {
            if (_inSecureWallet) {
                if (arg.isSecureWalletDeliveryArg()) {
                    _argField.setInitialValue(arg.value(), false);
                }
            } else {
                if (arg.committed() && arg.isSecureDeliveryArg()) {
                    _argField.setInitialValue(MASKED_VALUE, false);
                }
            }
        }
        // if (_inSecureWallet) {
        // _argField.addValueValidator(new FieldValueValidator<String>() {
        //
        // @Override
        // public void validate(final Field<String> f, final FieldValidHandler
        // vh) {
        // SecureWallet.contains(f.value(), new ObjectMessageResponse<Boolean>()
        // {
        //
        // @Override
        // public void responded(Boolean exists) {
        // if (exists) {
        // vh.setValid();
        // } else {
        // vh.setInvalid("Secure wallet key: " + f.value() +
        // " does not exist.");
        // }
        // }
        // });
        //
        // }
        // });
        // }
        _argField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                _destination.setArg(_name, _inSecureWallet ? DeliveryArg.Type.secure_wallet_delivery_arg
                        : DeliveryArg.Type.secure_delivery_arg, f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {
            }
        });
        FieldRenderOptions fro = new FieldRenderOptions();
        fro.setWidth(200);
        _argField.setRenderOptions(fro);
        _argForm.add(_argField);
        _argForm.render();
        hp.add(_argForm);
        addMustBeValid(_argForm);
        hp.setSpacing(3);

        Form iswForm = new Form();
        iswForm.setShowDescriptions(false);
        iswForm.setShowHelp(false);
        Field<Boolean> iswField = new Field<Boolean>(new FieldDefinition("Use secure wallet",
                BooleanType.DEFAULT_TRUE_FALSE, null, null, 1, 1));
        iswField.setInitialValue(_inSecureWallet);
        iswField.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                _inSecureWallet = f.value();
                DeliveryArg arg = _destination.arg(_name);
                if (arg != null) {
                    // wipe the old value from the destination.
                    if (_inSecureWallet) {
                        if (arg.isSecureDeliveryArg() && !arg.committed()) {
                            _destination.setArg(_name, DeliveryArg.Type.secure_wallet_delivery_arg, null);
                        }
                    } else {
                        if (arg.isSecureWalletDeliveryArg()) {
                            _destination.setArg(_name, DeliveryArg.Type.secure_delivery_arg, null);
                        }
                    }
                }
                updateForm();
                if (_inSecureWallet) {
                    showSecureWalletEntrySelectDialog();
                }
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

            }
        });
        iswForm.add(iswField);
        iswForm.render();
        hp.add(iswForm);

        if (_inSecureWallet) {
            hp.setSpacing(3);

            Button openSecureWalletButton = new Button("Select from secure wallet");
            openSecureWalletButton.setWidth(160);
            openSecureWalletButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    showSecureWalletEntrySelectDialog();
                }
            });
            hp.add(openSecureWalletButton);
        }
        _sp.setContent(hp);
    }

    private void showSecureWalletEntrySelectDialog() {
        SecureWalletEntrySelectDialog.show(_sp.window(), DeliveryArg.SECURE_WALLET_ENTRY_FILTER,
                new SecureWalletEntrySelectDialog.SelectionHandler() {

                    @Override
                    public void selected(SecureWalletEntryRef entry) {
                        if (entry != null) {
                            _argField.setValue(entry.key());
                        }
                    }
                });
    }

    @Override
    public Widget gui() {

        return _sp;
    }

    protected String generateSecureWalletKey() {
        return _name;
    }

}
