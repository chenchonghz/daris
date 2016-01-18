package daris.client.gui.form.field;

import daris.client.gui.form.FormItem;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

@SuppressWarnings({ "rawtypes" })
public class UneditableStringFormField extends Label {

    private FormItem<?> _formItem;
    private StringConverter _sc;

    public UneditableStringFormField(FormItem<?> formItem, StringConverter sc) {
        _formItem = formItem;
        _sc = sc;
        setText(_formItem.value(), _sc);
        _formItem.addValueChangeListener((ov, oldValue, newValue) -> {
            setText(newValue, _sc);
        });
    }

    private void setText(Object value, StringConverter sc) {
        if (sc != null) {
            setText(String.valueOf(value));
        } else {
            setText(value.toString());
        }
    }
}
