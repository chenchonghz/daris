package daris.client.gui.form.field;

import arc.mf.dtype.DoubleType;
import daris.client.gui.control.DoubleField;
import daris.client.gui.form.FormItem;

public class DoubleFormField extends DoubleField {

    private FormItem<Double> _formItem;

    public DoubleFormField(FormItem<Double> formItem) {
        super(((DoubleType) formItem.dataType()).minimum(),
                ((DoubleType) formItem.dataType()).maximum(), formItem.value());
        _formItem = formItem;
        valueProperty().addListener((obs, ov, nv) -> {
            _formItem.setValue(nv);
        });
        _formItem.addResetListener((item) -> {
            valueProperty().setValue(item.value());
        });
    }
}
