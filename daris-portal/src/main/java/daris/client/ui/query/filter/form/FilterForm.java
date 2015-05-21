package daris.client.ui.query.filter.form;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.util.Action;
import daris.client.model.query.filter.Filter;
import daris.client.ui.query.filter.FilterInterfaceComponent;

public abstract class FilterForm<T extends Filter> extends FilterInterfaceComponent<T> implements Action {

    public static interface SubmitListener<T> {
        void submitted(T filter);
    }

    private List<FilterChangeListener<T>> _fcls;

    protected FilterForm(T filter, boolean editable) {
        super(Filter.copy(filter), editable);
    }

    @Override
    public void execute() {
        notifyOfFilterChange();
    }

    private void notifyOfFilterChange() {
        if (_fcls != null) {
            for (FilterChangeListener<T> fcl : _fcls) {
                fcl.filterChanged(filter());
            }
        }
    }

    public void addFilterChangeListener(FilterChangeListener<T> fcl) {
        if (_fcls == null) {
            _fcls = new ArrayList<FilterChangeListener<T>>();
        }
        _fcls.add(fcl);
    }

    public void removeFilterChangeListener(FilterChangeListener<T> fcl) {
        if (_fcls != null) {
            _fcls.remove(fcl);
        }
    }
}
