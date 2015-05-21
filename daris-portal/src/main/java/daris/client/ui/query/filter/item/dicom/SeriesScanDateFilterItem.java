package daris.client.ui.query.filter.item.dicom;

import daris.client.model.query.filter.dicom.SeriesScanDateFilter;
import daris.client.ui.query.filter.form.CompositeFilterForm;

public class SeriesScanDateFilterItem extends DateTimeFilterItem<SeriesScanDateFilter> {

    public SeriesScanDateFilterItem(CompositeFilterForm containerForm,
            SeriesScanDateFilter filter, boolean editable) {
        super(containerForm, filter, editable);
    }

    @Override
    protected String label() {
        return "dicom series scan " + (filter().includeTime() ? "time" : "date");
    }

}
