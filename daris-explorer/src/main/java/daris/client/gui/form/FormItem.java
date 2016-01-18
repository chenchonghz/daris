package daris.client.gui.form;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.MustBeValid;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;
import arc.mf.dtype.DataType;
import arc.mf.dtype.DocType;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class FormItem<T> implements MustBeValid, StateChangeListener {

    public static interface ResetListener<T> {
        void reset(FormItem<T> item);
    }

    public static enum XmlNodeType {
        ELEMENT, ATTRIBUTE
    }

    private Form _form;
    private FormItem<?> _parent;

    private DataType _dataType;
    private String _name;
    private String _displayName;
    private String _description;
    private int _minOccurs;
    private int _maxOccurs;
    private XmlNodeType _xmlNodeType;
    private T _initialValue;
    private ObjectProperty<T> _valueProperty;
    private ListProperty<FormItem<?>> _itemsProperty;

    private ObjectProperty<Validity> _validityProperty;
    private boolean _changed = false;
    private List<StateChangeListener> _scls;
    private List<ResetListener<T>> _rls;

    public FormItem(Form form, FormItem<?> parent, DataType dataType,
            String name, String displayName, String description, int minOccurs,
            int maxOccurs, XmlNodeType xmlNodeType, T initialValue) {
        _form = form;
        _parent = parent;
        _dataType = dataType;
        _name = name;
        _displayName = displayName;
        _description = description;
        _minOccurs = minOccurs;
        _maxOccurs = maxOccurs;
        _xmlNodeType = xmlNodeType;
        _initialValue = initialValue;
        _valueProperty = new SimpleObjectProperty<T>(initialValue);
        _valueProperty.addListener((ov, oldValue, newValue) -> {
            if (!ObjectUtils.equals(newValue, _initialValue)) {
                _changed = true;
            }
            updateValidity();
            notifyOfChangeInState();
        });
        _itemsProperty = new SimpleListProperty<FormItem<?>>(
                FXCollections.observableArrayList());
        _itemsProperty.addListener(new ListChangeListener<FormItem<?>>() {
            @Override
            public void onChanged(Change<? extends FormItem<?>> c) {
                if (c.getAddedSize() > 0 || c.getRemovedSize() > 0) {
                    _changed = true;
                }
                updateValidity();
                notifyOfChangeInState();
            }
        });
    }

    public FormItem(Form form, FormItem<?> parent, DataType dataType,
            String name, String displayName, String description, int minOccurs,
            int maxOccurs) {
        this(form, parent, dataType, name, displayName, description, minOccurs,
                maxOccurs, XmlNodeType.ELEMENT, null);
    }

    public Form form() {
        return _form;
    }

    void setForm(Form form) {
        _form = form;
    }

    public FormItem<?> parent() {
        return _parent;
    }

    void setParent(FormItem<?> parent) {
        _parent = parent;
    }

    public DataType dataType() {
        return _dataType;
    }

    public String name() {
        return _name;
    }

    public String displayName() {
        return _displayName;
    }

    public String description() {
        return _description;
    }

    public int minOccurs() {
        return _minOccurs;
    }

    public int maxOccrus() {
        return _maxOccurs;
    }

    public XmlNodeType xmlNodeType() {
        return _xmlNodeType;
    }

    public T initialValue() {
        return _initialValue;
    }

    public void setInitialValue(T initialValue) {
        _initialValue = initialValue;
    }

    public T value() {
        return _valueProperty.get();
    }

    public void setValue(T value) {
        _valueProperty.set(value);
    }

    @Override
    public void addChangeListener(StateChangeListener scl) {
        if (_scls == null) {
            _scls = new ArrayList<StateChangeListener>();
        }
        _scls.add(scl);
    }

    public void addValueChangeListener(ChangeListener<? super T> cl) {
        _valueProperty.addListener(cl);
    }

    @Override
    public boolean changed() {
        return _changed;
    }

    @Override
    public void removeChangeListener(StateChangeListener scl) {
        if (_scls != null) {
            _scls.remove(scl);
        }
    }

    public void addResetListener(ResetListener<T> rl) {
        if (_rls == null) {
            _rls = new ArrayList<ResetListener<T>>();
        }
        _rls.add(rl);
    }

    @Override
    public void notifyOfChangeInState() {
        if (_scls != null) {
            for (StateChangeListener scl : _scls) {
                scl.notifyOfChangeInState();
            }
        }
    }

    @Override
    public Validity valid() {
        return _validityProperty.get();
    }

    private void updateValidity() {
        if (_minOccurs == 0) {
            if (!changed()) {
                _validityProperty.set(IsValid.INSTANCE);
                return;
            }
        }
        ObservableList<FormItem<?>> items = _itemsProperty.get();
        if (items != null) {
            for (FormItem<?> item : items) {
                Validity validity = item.valid();
                if (!validity.valid()) {
                    _validityProperty.set(validity);
                    return;
                }
            }
        }
        if (value() == null && !(_dataType instanceof DocType)) {
            _validityProperty
                    .set(new IsNotValid("Missing value for " + path()));
            return;
        }
        _validityProperty.set(IsValid.INSTANCE);
    }

    public String path() {
        StringBuilder sb = new StringBuilder();
        if (_parent != null) {
            sb.append(_parent.path());
        }
        sb.append("/");
        if (_xmlNodeType == XmlNodeType.ATTRIBUTE) {
            sb.append("@");
        }
        sb.append(_name);
        return sb.toString();
    }

    public void add(FormItem<?> formItem) {
        _itemsProperty.add(formItem);
    }

    public void remove(FormItem<?> formItem) {
        _itemsProperty.remove(formItem);
    }
}
