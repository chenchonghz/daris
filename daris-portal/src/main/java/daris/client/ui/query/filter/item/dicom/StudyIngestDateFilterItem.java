package daris.client.ui.query.filter.item.dicom;

import daris.client.model.query.filter.dicom.StudyIngestDateFilter;
import daris.client.ui.query.filter.form.CompositeFilterForm;

public class StudyIngestDateFilterItem extends DateTimeFilterItem<StudyIngestDateFilter> {

    public StudyIngestDateFilterItem(CompositeFilterForm containerForm,
            StudyIngestDateFilter filter, boolean editable) {
        super(containerForm, filter, editable);
    }

    @Override
    protected String label() {
        return "dicom study ingest " + (filter().includeTime() ? "time" : "date");
    }

}
