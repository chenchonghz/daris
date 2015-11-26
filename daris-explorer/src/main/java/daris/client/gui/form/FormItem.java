package daris.client.gui.form;

import java.util.ArrayList;
import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.Validity;
import arc.mf.dtype.DataType;
import arc.mf.dtype.DocType;
import arc.utils.ObjectUtil;
import javafx.scene.Node;

@SuppressWarnings("rawtypes")
public class FormItem<T> extends ValidatedInterfaceComponent {

    public static enum XmlNodeType {
        ELEMENT, ATTRIBUTE
    }

    private FormItem<?> _parent;
    private String _name;
    private String _displayName;
    private String _description;
    private int _minOccurs;
    private int _maxOccurs;
    private T _initialValue;
    private T _value;
    private DataType _dataType;
    private List<FormItem> _members;
    private Node _gui;

    protected FormItem(FormItem<?> parent, String name, String displayName,
            DataType dataType, String description, int minOccurs,
            int maxOccurs) {
        _parent = parent;
        _name = name;
        _displayName = displayName;
        _description = description;
        _minOccurs = minOccurs;
        _maxOccurs = maxOccurs;
        _initialValue = null;
        _value = null;
    }

    public void reset() {
        setValue(_initialValue);
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

    public int maxOccurs() {
        return _maxOccurs;
    }

    public FormItem parent() {
        return _parent;
    }

    public DataType dataType() {
        return _dataType;
    }

    public T value() {
        return _value;
    }

    public void setValue(T value, boolean fireEvent) {
        if (ObjectUtil.equals(_value, value)) {
            return;
        }
        _value = value;
        if (fireEvent) {
            notifyOfChangeInState();
        }
    }

    public void setValue(T value) {
        setValue(value, true);
    }

    public void setInitialValue(T initialValue) {
        _initialValue = initialValue;
        if (value() == null) {
            setValue(_initialValue);
        }
    }

    @Override
    public Validity valid() {
        if (minOccurs() > 0 && isValueRequired() && value() == null) {
            return new IsNotValid(path() + " is missing value.");
        } else {
            return super.valid();
        }
    }

    public XmlNodeType xmlNodeType() {
        return XmlNodeType.ELEMENT;
    }

    public boolean isValueRequired() {
        return !(dataType() instanceof DocType);
    }

    public String path() {
        StringBuilder sb = new StringBuilder();
        if (parent() != null) {
            sb.append(parent().path());
        }
        sb.append("/");
        if (xmlNodeType() == XmlNodeType.ELEMENT) {
            sb.append(name());
        } else {
            sb.append("@");
            sb.append(name());
        }
        return sb.toString();
    }

    public List<FormItem> members() {
        return _members;
    }

    public boolean hasMembers() {
        return (_members != null && !_members.isEmpty());
    }

    public void addMember(FormItem member, boolean fireEvent) {
        if (_members == null) {
            _members = new ArrayList<FormItem>();
        }
        boolean added = _members.add(member);
        if (added && fireEvent) {
            notifyOfChangeInState();
        }
    }

    public void removeMember(FormItem member, boolean fireEvent) {
        if (_members != null) {
            boolean removed = _members.remove(member);
            if (removed && fireEvent) {
                notifyOfChangeInState();
            }
        }
    }

    public void addMember(FormItem member) {
        addMember(member, true);
    }

    public void removeMember(FormItem member) {
        removeMember(member, true);
    }

    @Override
    public Node gui() {
        if (_gui == null) {
            _gui = FormItemGUIFactory.createFormItemGUI(this);
        }
        return _gui;
    }

}
