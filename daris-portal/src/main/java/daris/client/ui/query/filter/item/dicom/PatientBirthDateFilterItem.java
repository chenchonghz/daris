package daris.client.ui.query.filter.item.dicom;

import daris.client.model.query.filter.dicom.PatientBirthDateFilter;
import daris.client.ui.query.filter.form.CompositeFilterForm;

public class PatientBirthDateFilterItem extends DateTimeFilterItem<PatientBirthDateFilter> {

    public PatientBirthDateFilterItem(CompositeFilterForm form, PatientBirthDateFilter filter,
            boolean editable) {
        super(form, filter, editable);
    }

    @Override
    protected String label() {
        return "dicom patient birth date";
    }

}
