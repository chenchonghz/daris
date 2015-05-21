package daris.client.ui.query.filter;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.window.Window;
import arc.mf.client.util.Validity;
import daris.client.model.query.filter.Filter;

public abstract class FilterInterfaceComponent<T extends Filter> extends ValidatedInterfaceComponent {

    private T _filter;
    private boolean _editable;
    private Window _win;

    protected FilterInterfaceComponent(T filter, boolean editable) {
        _filter = filter;
        _editable = editable;
    }

    public T filter() {
        return _filter;
    }

    public boolean editable() {
        return _editable;
    }

    public BaseWidget widget() {
        return (BaseWidget) gui();
    }

    public Window window() {
        Window win = widget().window();
        if (win != null) {
            _win = win;
        }
        return _win;
    }

    /**
     * FilterItem in ListGrid cell does not have its parent (ContainerWidget) set. Therefore, its window() can not be
     * resolved. This method is to manually set its window().
     * 
     * 
     * @param win
     * @return
     */
    public boolean setWindow(Window win) {
        _win = widget().window();
        if (_win == null) {
            _win = win;
            return true;
        } else {
            return false;
        }
    }

    protected void setFilter(T f, boolean fireEvents) {
        _filter = f;
        if (fireEvents) {
            notifyOfChangeInState();
        }
    }

    @Override
    public Validity valid() {
        Validity v = super.valid();
        if (!v.valid()) {
            return v;
        }
        return _filter.valid();
    }

}
