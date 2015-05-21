package daris.client.ui.secure.wallet;

import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.ActionListener;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.model.secure.wallet.SecureWallet;
import daris.client.model.secure.wallet.SecureWallet.Availability;
import daris.client.model.secure.wallet.SecureWalletEntryRef;

public class SecureWalletExplorer {

    private VerticalPanel _vp;
    private SecureWalletEntryGrid _grid;
    private Button _addButton;
    private Button _editButton;
    private Button _removeButton;

    private SecureWalletExplorer() {
        _vp = new VerticalPanel();
        _vp.fitToParent();

        _grid = new SecureWalletEntryGrid() {
            @Override
            public void selected(SecureWalletEntryRef o) {
                super.selected(o);
                _removeButton.enable();
                _editButton.enable();
            }

            @Override
            public void deselected(SecureWalletEntryRef o) {
                super.deselected(o);
                _removeButton.disable();
                _editButton.disable();
            }
        };
        _grid.fitToParent();
        _vp.add(_grid);

        ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.CENTER);
        bb.setHeight(30);

        _addButton = bb.addButton("Add");
        _addButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                SecureWalletEntryDialog.show(FormEditMode.CREATE, null, null, false, false,
                        new SecureWalletEntryDialog.Listener() {

                            @Override
                            public void process(SecureWalletEntryRef entry) {
                                _grid.refresh();
                            }
                        }, _vp.window());
            }
        });

        _editButton = bb.addButton("Edit");
        _editButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                SecureWalletEntryRef entry = _grid.selected();
                if (entry != null) {
                    SecureWalletEntryDialog.show(entry, new SecureWalletEntryDialog.Listener() {

                        @Override
                        public void process(SecureWalletEntryRef entry) {
                            _grid.refresh();
                        }
                    }, _vp.window());
                }
            }
        });
        _editButton.setEnabled(_grid != null && _grid.selected() != null);

        _removeButton = bb.addButton("Remove");
        _removeButton.disable();
        _removeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                SecureWalletEntryRef entry = _grid.selected();
                if (entry == null) {
                    return;
                }
                _removeButton.disable();
                Dialog.confirm("Remove secure wallet entry", "Are you sure you want to remove entry '"
                        + _grid.selected().key() + "'?", new ActionListener() {

                    @Override
                    public void executed(boolean succeeded) {
                        SecureWallet.removeEntry(_grid.selected().key(), new ObjectMessageResponse<Null>() {

                            @Override
                            public void responded(Null r) {
                                _grid.refresh();
                            }
                        });

                    }
                });
            }
        });
        _removeButton.setEnabled(_grid != null && _grid.selected() != null);
        _vp.add(bb);
    }

    private void showDialog(arc.gui.window.Window owner) {
        WindowProperties wp = new WindowProperties();
        wp.setCanBeClosed(true);
        wp.setModal(false);
        wp.setCenterInPage(true);
        wp.setSize(0.5, 0.5);
        wp.setOwnerWindow(owner);
        wp.setTitle("Secure Wallet");

        Window win = Window.create(wp);
        win.setContent(_vp);
        win.centerInPage();
        win.show();
    }

    public static void show(final arc.gui.window.Window owner) {
        SecureWallet.canBeUsed(new ObjectMessageResponse<Availability>() {

            @Override
            public void responded(Availability a) {
                ActionListener al = new ActionListener() {

                    @Override
                    public void executed(boolean succeeded) {
                        if (succeeded) {
                            new SecureWalletExplorer().showDialog(owner);
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
}
