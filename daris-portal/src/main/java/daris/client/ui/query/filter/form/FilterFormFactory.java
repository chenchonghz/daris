package daris.client.ui.query.filter.form;

import daris.client.model.query.filter.CompositeFilter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.pssd.ObjectCompositeFilter;

public class FilterFormFactory {

    @SuppressWarnings("unchecked")
    public static <T extends Filter> FilterForm<T> createForm(T filter, boolean editable) {
        if (filter instanceof ObjectCompositeFilter) {
            return (FilterForm<T>) new ObjectCompositeFilterForm((ObjectCompositeFilter) filter, editable);
        } else if (filter instanceof CompositeFilter) {
            return (FilterForm<T>) new CompositeFilterForm((CompositeFilter) filter, editable);
        } else {
            throw new AssertionError("Failed create form for " + filter.simpleClassName());
        }
    }

}
