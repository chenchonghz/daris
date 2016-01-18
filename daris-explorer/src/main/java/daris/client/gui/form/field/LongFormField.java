package daris.client.gui.form.field;

import arc.mf.dtype.LongType;
import daris.client.gui.control.LongField;
import daris.client.gui.form.FormItem;

public class LongFormField extends LongField {

    private FormItem<Long> _formItem;

    public LongFormField(FormItem<Long> formItem) {
        super(((LongType) formItem.dataType()).minimum(),
                ((LongType) formItem.dataType()).maximum(), formItem.value());
        _formItem = formItem;
        valueProperty().addListener((obs, ov, nv) -> {
            _formItem.setValue(nv);
        });
        _formItem.addResetListener((item) -> {
            valueProperty().setValue(item.value());
        });
    }
}
