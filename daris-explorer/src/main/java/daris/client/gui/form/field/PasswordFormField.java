package daris.client.gui.form.field;

import daris.client.gui.form.FormItem;
import javafx.scene.control.PasswordField;

public class PasswordFormField extends PasswordField {
    private FormItem<String> _formItem;

    public PasswordFormField(FormItem<String> formItem) {
        super();
        _formItem = formItem;
        if (_formItem.value() != null) {
            setText(_formItem.value());
        }
        textProperty().addListener((obs, ov, nv) -> {
            _formItem.setValue(nv);
        });
        _formItem.addResetListener((item) -> {
            textProperty().setValue(item.value());
        });
    }
}
