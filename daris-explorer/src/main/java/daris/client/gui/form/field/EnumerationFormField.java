package daris.client.gui.form.field;

import java.util.List;

import arc.mf.client.util.UnhandledException;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.EnumerationType.Value;
import arc.utils.Transform;
import daris.client.gui.form.FormItem;
import javafx.scene.control.ComboBox;

@SuppressWarnings("unchecked")
public class EnumerationFormField<T> extends ComboBox<T> {

    private EnumerationType<T> _dataType;
    private FormItem<T> _formItem;

    public EnumerationFormField(FormItem<T> formItem) {
        _formItem = formItem;
        _dataType = (EnumerationType<T>) formItem.dataType();
        if (_dataType.dataSource() == null) {
            updateItems(_dataType.values());
        } else {
            updateItems(_dataType.dataSource(), null);
            setEditable(true);
            editorProperty().get().textProperty()
                    .addListener((ov, oldValue, newValue) -> {
                        updateItems(_dataType.dataSource(), newValue);
                    });
        }
        valueProperty().addListener((obs, ov, nv) -> {
            _formItem.setValue(nv);
        });
        formItem.addResetListener((item) -> {
            valueProperty().setValue(item.value());
        });

    }

    private void updateItems(List<EnumerationType.Value<T>> values) {
        getItems().clear();
        if (values != null) {
            List<T> vs = null;
            try {
                vs = Transform.transform(values, value -> {
                    return value.value();
                });
            } catch (Throwable e) {
                UnhandledException
                        .report("Transforming Enumeration.Value<T> to T", e);
            }
            if (vs != null && !vs.isEmpty()) {
                getItems().setAll(vs);
            }
        }
    }

    private void updateItems(DynamicEnumerationDataSource<T> ds,
            String prefix) {
        try {
            ds.retrieve(prefix, 0, Short.MAX_VALUE,
                    new DynamicEnumerationDataHandler<T>() {

                        @Override
                        public void process(long start, long end, long total,
                                List<Value<T>> values) {
                            updateItems(values);
                        }
                    });
        } catch (Throwable e) {
            UnhandledException
                    .report("Retrieving remote enumeration data source", e);
        }
    }

}
