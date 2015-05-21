package daris.client.ui.query.filter.item.dicom;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.mf.dtype.EnumerationType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.dicom.PatientSexFilter;
import daris.client.model.query.filter.dicom.PatientSexFilter.Sex;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class PatientSexFilterItem extends FilterItem<PatientSexFilter> {

    private HorizontalPanel _hp;

    public PatientSexFilterItem(CompositeFilterForm containerForm, PatientSexFilter filter,
            boolean editable) {
        super(containerForm, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("dicom patient sex:");
        label.setFontSize(11);
        label.setMarginTop(8);
        _hp.add(label);
        _hp.setSpacing(3);

        Form form = new Form(editable ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        form.setShowLabels(false);
        form.setShowHelp(false);
        form.setShowDescriptions(false);

        Field<Sex> valueField = new Field<Sex>(new FieldDefinition("value",
                new EnumerationType<Sex>(Sex.values()), null, null, 1, 1));
        valueField.setInitialValue(filter().value(), false);
        valueField.addListener(new FormItemListener<Sex>() {

            @Override
            public void itemValueChanged(FormItem<Sex> f) {
                filter().setValue(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Sex> f, Property property) {

            }
        });
        form.add(valueField);

        addMustBeValid(form);

        form.render();

        _hp.add(form);
    }

    @Override
    public Widget gui() {
        return _hp;
    }

}
