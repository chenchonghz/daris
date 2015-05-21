package daris.client.ui.query.filter.item.dicom;

import daris.client.model.query.filter.dicom.StudyScanDateFilter;
import daris.client.ui.query.filter.form.CompositeFilterForm;

public class StudyScanDateFilterItem extends DateTimeFilterItem<StudyScanDateFilter> {

    public StudyScanDateFilterItem(CompositeFilterForm containerForm,
            StudyScanDateFilter filter, boolean editable) {
        super(containerForm, filter, editable);
    }

    @Override
    protected String label() {
        return "dicom study scan " + (filter().includeTime() ? "time" : "date");
    }

}
