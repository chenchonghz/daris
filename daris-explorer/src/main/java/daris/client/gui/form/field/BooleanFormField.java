package daris.client.gui.form.field;

import daris.client.gui.form.FormItem;
import javafx.scene.control.CheckBox;

public class BooleanFormField extends CheckBox {

    private FormItem<Boolean> _formItem;

    public BooleanFormField(FormItem<Boolean> formItem) {
        _formItem = formItem;
        setSelected(_formItem.value());
        selectedProperty().addListener((obs, ov, nv) -> {
            _formItem.setValue(nv);
        });
        _formItem.addResetListener((item) -> {
            setSelected(item.value());
        });
    }

}
