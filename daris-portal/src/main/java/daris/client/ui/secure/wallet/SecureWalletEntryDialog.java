package daris.client.ui.secure.wallet;

import arc.gui.dialog.DialogProperties;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.window.Window;
import arc.mf.client.util.ActionListener;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.secure.wallet.SecureWallet;
import daris.client.model.secure.wallet.SecureWallet.Availability;
import daris.client.model.secure.wallet.SecureWalletEntry.Type;
import daris.client.model.secure.wallet.SecureWalletEntry;
import daris.client.model.secure.wallet.SecureWalletEntryRef;
import daris.client.model.secure.wallet.Usage;

public class SecureWalletEntryDialog {

    public static interface Listener {
        void process(SecureWalletEntryRef entry);
    }

    private FormEditMode _mode;
    private String _key;
    private String _value;
    private boolean _maskValue;
    private boolean _forShoppingCart;
    private Listener _listener;

    SecureWalletEntryDialog(FormEditMode mode, String key, String value, boolean maskValue, boolean forShoppingCart,
            Listener listener) {
        _mode = mode;
        _key = key;
        _value = value;
        _maskValue = maskValue;
        _forShoppingCart = forShoppingCart;
        _listener = listener;
    }

    public static void show(final FormEditMode mode, final String key, final String value, final boolean maskValue,
            final boolean forShoppingCart, final Listener listener, final Window owner) {
        SecureWallet.canBeUsed(new ObjectMessageResponse<Availability>() {

            @Override
            public void responded(Availability a) {
                ActionListener al = new ActionListener() {

                    @Override
                    public void executed(boolean succeeded) {
                        if (succeeded) {
                            new SecureWalletEntryDialog(mode, key, value, maskValue, forShoppingCart, listener)
                                    .showDialog(owner);
                        } else {
                            Dialog.warn(owner, "Error", "Failed to access seccure wallet.", new ActionListener() {

                                @Override
                                public void executed(boolean succeeded) {
                                    if (listener != null) {
                                        listener.process(null);
                                    }
                                }
                            });
                        }
                    }
                };
                if (a.canBeUsed()) {
                    al.executed(true);
                } else {
                    if (!a.exists()) {
                        new SecureWalletRecreateForm().showDialog(owner, al);
                    } else {
                        new SecureWalletPasswordSetForm().showDialog(owner, al);
                    }
                }
            }
        });

    }

    public static void show(final SecureWalletEntryRef entry, final Listener listener, final Window owner) {
        SecureWallet.canBeUsed(new ObjectMessageResponse<Availability>() {

            @Override
            public void responded(Availability a) {
                ActionListener al = new ActionListener() {

                    @Override
                    public void executed(boolean succeeded) {
                        if (succeeded) {
                            if (entry.usage() == null && entry.type() == Type.string) {
                                entry.resolve(new ObjectResolveHandler<SecureWalletEntry>() {

                                    @Override
                                    public void resolved(SecureWalletEntry eo) {
                                        new SecureWalletEntryDialog(FormEditMode.UPDATE, entry.key(), eo.value(),
                                                false, false, listener).showDialog(owner);
                                    }
                                });
                            } else {
                                new SecureWalletEntryDialog(FormEditMode.UPDATE, entry.key(), null, false, entry
                                        .usage() != null && entry.usage().equals(Usage.SHOPPING_CART_DELIVERY),
                                        listener).showDialog(owner);
                            }
                        } else {
                            Dialog.warn(owner, "Error", "Failed to access seccure wallet.", new ActionListener() {

                                @Override
                                public void executed(boolean succeeded) {
                                    if (listener != null) {
                                        listener.process(null);
                                    }
                                }
                            });
                        }
                    }
                };
                if (a.canBeUsed()) {
                    al.executed(true);
                } else {
                    if (!a.exists()) {
                        new SecureWalletRecreateForm().showDialog(owner, al);
                    } else {
                        new SecureWalletPasswordSetForm().showDialog(owner, al);
                    }
                }
            }
        });
    }

    public void showDialog(Window owner) {
        final SecureWalletEntryForm form = new SecureWalletEntryForm(_mode, _key, _value, _maskValue, _forShoppingCart);
        String action = _mode == FormEditMode.CREATE ? "Add" : "Update";
        DialogProperties dp = new DialogProperties(action + " secure wallet entry", form);
        dp.setActionEnabled(false);
        dp.setButtonAction(form);
        dp.setButtonLabel(action);
        dp.setModal(true);
        dp.setOwner(owner);
        dp.setSize(480, 240);
        dp.setCancelLabel("Cancel");
        Dialog dlg = Dialog.postDialog(dp, new ActionListener() {

            @Override
            public void executed(boolean succeeded) {
                if (_listener != null) {
                    if (succeeded) {
                        _listener.process(new SecureWalletEntryRef(_key, SecureWalletEntry.Type.string,
                                _forShoppingCart ? Usage.SHOPPING_CART_DELIVERY : null));
                    } else {
                        _listener.process(null);
                    }
                }
            }
        });
        dlg.show();
    }
}
