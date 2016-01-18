package daris.client.gui.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public abstract class NumberField<T extends Number> extends TextField
        implements EventHandler<KeyEvent> {
    protected T _min;
    protected T _max;
    private ObjectProperty<T> _valueProperty;

    public NumberField(T min, T max, T value) {
        _min = min;
        _max = max;
        _valueProperty = new SimpleObjectProperty<T>(value);
        setOnKeyTyped(this);
        textProperty().addListener((obs, oldValue, newValue) -> {
            if (isEmptyOrNull(newValue)) {
                _valueProperty.setValue(null);
            } else {
                try {
                    updateValue(newValue);
                } catch (Throwable t) {
                    _valueProperty.setValue(null);
                }
            }

        });
    }

    protected abstract void updateValue(String text);

    protected static boolean isEmptyOrNull(String v) {
        return v != null && !v.trim().equals("");
    }

    public T value() {
        return _valueProperty.get();
    }

    public void setValue(T v) {
        _valueProperty.setValue(v);
        setText(v.toString());
    }

    public ObjectProperty<T> valueProperty() {
        return _valueProperty;
    }

}
