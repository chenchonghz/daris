package daris.client.gui.form.field;

import daris.client.gui.form.FormItem;
import javafx.scene.control.TextArea;

public class TextFormField extends TextArea {
    private FormItem<String> _formItem;

    public TextFormField(FormItem<String> formItem) {
        super(formItem.value());
        _formItem = formItem;
        textProperty().addListener((obs, ov, nv) -> {
            _formItem.setValue(nv);
        });
        _formItem.addResetListener((item) -> {
            textProperty().setValue(item.value());
        });
    }
}