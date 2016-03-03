package daris.client.gui.form.field;

import arc.mf.dtype.IntegerType;
import daris.client.gui.control.IntegerField;
import daris.client.gui.form.FormItem;

public class IntegerFormField extends IntegerField {

    private FormItem<Integer> _formItem;
    public IntegerFormField(FormItem<Integer> formItem) {
        super(((IntegerType) formItem.dataType()).minimum(),
                ((IntegerType) formItem.dataType()).maximum(),
                formItem.value());
        _formItem = formItem;
        setValue(_formItem.value());
        valueProperty().addListener((obs, ov, nv) -> {
            _formItem.setValue(nv);
        });
        _formItem.addResetListener((item) -> {
            valueProperty().setValue(item.value());
        });
    }
}
