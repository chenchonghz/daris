package daris.client.gui.form.field;

import arc.mf.dtype.FloatType;
import daris.client.gui.control.FloatField;
import daris.client.gui.form.FormItem;

public class FloatFormField extends FloatField {

    private FormItem<Float> _formItem;
    public FloatFormField(FormItem<Float> formItem) {
        super(((FloatType) formItem.dataType()).minimum(),
                ((FloatType) formItem.dataType()).maximum(),
                formItem.value());
        _formItem = formItem;
        valueProperty().addListener((obs, ov, nv) -> {
            _formItem.setValue(nv);
        });
        _formItem.addResetListener((item) -> {
            valueProperty().setValue(item.value());
        });
    }
}
