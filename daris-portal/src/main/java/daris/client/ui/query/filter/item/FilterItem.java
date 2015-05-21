package daris.client.ui.query.filter.item;

import daris.client.model.object.DObjectRef;
import daris.client.model.query.filter.CompositeFilter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.pssd.ObjectCompositeFilter;
import daris.client.ui.query.filter.FilterInterfaceComponent;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.form.ObjectCompositeFilterForm;

public abstract class FilterItem<T extends Filter> extends FilterInterfaceComponent<T> {

    public interface HasComposite {

        FilterItem<?> hadBy();

        CompositeFilter composite();

        void setComposite(CompositeFilter filter);

    }

    private CompositeFilterForm _form;

    protected FilterItem(CompositeFilterForm form, T filter, boolean editable) {
        super(filter, editable);
        _form = form;
    }

    public CompositeFilterForm form() {
        return _form;
    }

    public void setFilter(T filter, boolean fireEvents) {
        if (_form != null) {
            int index = _form.indexOf(this);
            CompositeFilter.Member m = _form.filter().memberAt(index);
            m.setFilter(filter);
        }
        super.setFilter(filter, fireEvents);
    }

    // TODO: improve
    public CompositeFilter createCompositeFilter() {
        if (_form != null) {
            if (_form instanceof ObjectCompositeFilterForm) {
                ObjectCompositeFilterForm oForm = (ObjectCompositeFilterForm) _form;
                DObjectRef project = ((ObjectCompositeFilter) oForm.filter()).project();
                return new ObjectCompositeFilter(project);
            }
        }
        return new CompositeFilter();
    }

}
