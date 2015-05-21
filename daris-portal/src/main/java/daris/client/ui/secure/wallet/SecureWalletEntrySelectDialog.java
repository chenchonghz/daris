package daris.client.ui.secure.wallet;

import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.list.ListGridRowDoubleClickHandler;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.ActionListener;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;

import daris.client.model.secure.wallet.SecureWallet;
import daris.client.model.secure.wallet.SecureWallet.Availability;
import daris.client.model.secure.wallet.SecureWalletEntryRef;

public class SecureWalletEntrySelectDialog {

    public static interface SelectionHandler {
        void selected(SecureWalletEntryRef entry);
    }

    private VerticalPanel _vp;
    private SecureWalletEntryGrid _grid;
    private Button _addButton;
    private Button _editButton;
    private Button _confirmButton;
    private Button _cancelButton;

    private SelectionHandler _sh;
    private SecureWalletEntryRef.Filter _filter;

    private Window _win;

    private SecureWalletEntrySelectDialog(SecureWalletEntryRef.Filter filter, SelectionHandler sh) {

        _filter = filter;
        _sh = sh;

        _vp = new VerticalPanel();
        _vp.fitToParent();

        _grid = new SecureWalletEntryGrid() {
            @Override
            public void selected(SecureWalletEntryRef o) {
                super.selected(o);
                _editButton.enable();
                if (_filter == null) {
                    _confirmButton.enable();
                } else {
                    _confirmButton.setEnabled(_filter.matches(o));
                }
            }

            @Override
            public void deselected(SecureWalletEntryRef o) {
                super.deselected(o);
                _editButton.disable();
            }
        };
        _grid.setRowDoubleClickHandler(new ListGridRowDoubleClickHandler<SecureWalletEntryRef>() {

            @Override
            public void doubleClicked(SecureWalletEntryRef entry, DoubleClickEvent event) {
                if (_sh != null) {
                    _sh.selected(entry);
                }
                if (_win != null) {
                    _win.closeIfOK();
                }
            }
        });
        _grid.fitToParent();
        _vp.add(_grid);

        ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.RIGHT);
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

        _confirmButton = bb.addButton("Select");
        _confirmButton.disable();
        _confirmButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (_sh != null) {
                    if (_grid.selected() != null) {
                        _sh.selected(_grid.selected());
                    } else {
                        _sh.selected(null);
                    }
                }
                if (_win != null) {
                    _win.closeIfOK();
                }
            }
        });

        _cancelButton = bb.addButton("Cancel");
        _cancelButton.enable();
        _cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (_sh != null) {
                    _sh.selected(null);
                }
                if (_win != null) {
                    _win.closeIfOK();
                }
            }
        });
        _cancelButton.setMarginRight(25);
        _vp.add(bb);
    }

    public void setSelectionHandler(SelectionHandler sh) {
        _sh = sh;
    }

    public void showDialog(arc.gui.window.Window owner) {

        WindowProperties wp = new WindowProperties();
        wp.setOwnerWindow(owner);
        wp.setSize(0.6, 0.6);
        wp.setCanBeResized(true);
        wp.setModal(true);
        wp.setCenterInPage(true);

        if (_win == null) {
            _win = Window.create(wp);
            _win.setTitle("Select a secure wallet entry");
            _win.addCloseListener(new WindowCloseListener() {

                @Override
                public void closed(Window w) {
                    _win = null;
                }
            });
        }
        _win.setContent(_vp);
        _win.show();
        _win.centerInPage();

    }

    public static void show(final arc.gui.window.Window owner, final SecureWalletEntryRef.Filter filter,
            final SelectionHandler sh) {
        SecureWallet.canBeUsed(new ObjectMessageResponse<Availability>() {

            @Override
            public void responded(Availability a) {
                ActionListener al = new ActionListener() {

                    @Override
                    public void executed(boolean succeeded) {
                        if (succeeded) {
                            new SecureWalletEntrySelectDialog(filter, sh).showDialog(owner);
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
