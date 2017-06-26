package daris.client.ui.collection.download;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.ui.Widget;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.dialog.DialogProperties;
import arc.gui.dialog.DialogProperties.Type;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.Validity;
import arc.mf.dtype.PasswordType;

public class TokenPasswordSetForm extends ValidatedInterfaceComponent {

    private VerticalPanel _vp;

    private Field<String> _passwordField1;
    private Field<String> _passwordField2;

    private HTML _status;

    public TokenPasswordSetForm() {
        _vp = new VerticalPanel();
        _vp.fitToParent();

        Form form = new Form(FormEditMode.UPDATE);
        form.setMarginTop(20);
        form.setMarginLeft(20);

        _passwordField1 = new Field<String>(new FieldDefinition("Token Password", "token password",
                new PasswordType(6, 20, PasswordType.DEFAULT_DISPLAY_LENGTH), "Token password", null, 1, 1));
        form.add(_passwordField1);
        _passwordField2 = new Field<String>(new FieldDefinition("Confirm Password", "confirm password",
                new PasswordType(6, 20, PasswordType.DEFAULT_DISPLAY_LENGTH), "Confirm password", null, 1, 1));
        form.add(_passwordField2);
        form.render();

        addMustBeValid(form);

        _status = new HTML();
        _status.setColour(RGB.RED);
        _status.setFontWeight(FontWeight.BOLD);
        _status.setPaddingLeft(20);
        _status.setHeight(22);

        _vp.add(form);
        _vp.add(_status);
    }

    public String password() {
        if (valid().valid()) {
            return _passwordField1.value();
        }
        return null;
    }

    @Override
    public Validity valid() {
        Validity v = super.valid();
        if (v.valid()) {
            if (!ObjectUtil.equals(_passwordField1.value(), _passwordField2.value())) {
                v = new IsNotValid("Passwords do not match.");
            }
        }
        if (v.valid()) {
            _status.setHTML(null);
        } else {
            _status.setHTML(v.reasonForIssue());
        }
        return v;
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    public void showDialog(arc.gui.window.Window owner, ActionListener al) {
        DialogProperties dp = new DialogProperties(Type.ACTION, "Set token password", this);
        dp.setOwner(owner);
        dp.setModal(true);
        dp.setSize(380, 200);
        dp.setActionEnabled(false);
        dp.setButtonLabel("Set");
        Dialog dlg = Dialog.postDialog(dp, al);
        dlg.show();
    }

}
