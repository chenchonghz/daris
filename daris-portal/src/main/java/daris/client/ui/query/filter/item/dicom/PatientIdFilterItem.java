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
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.dicom.PatientIdFilter;
import daris.client.model.query.filter.operators.StringOperator;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class PatientIdFilterItem extends FilterItem<PatientIdFilter> {

    private HorizontalPanel _hp;
    private SimplePanel _formSP;
    private Form _form;

    public PatientIdFilterItem(CompositeFilterForm containerForm, PatientIdFilter filter,
            boolean editable) {
        super(containerForm, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("dicom patient id");
        label.setFontSize(11);
        label.setMarginTop(8);
        _hp.add(label);
        _hp.setSpacing(3);

        _formSP = new SimplePanel();
        _formSP.setHeight100();
        _hp.add(_formSP);

        updateForm();

    }

    private void updateForm() {
        if (_form != null) {
            removeMustBeValid(_form);
        }

        _formSP.clear();
        _form = new Form(editable() ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        _form.setShowLabels(false);
        _form.setShowHelp(false);
        _form.setShowDescriptions(false);
        _form.setNumberOfColumns(filter().operator() == null
                || filter().operator().numberOfValues() < 1 ? 1 : 2);

        Field<StringOperator> opField = new Field<StringOperator>(new FieldDefinition("operator",
                new EnumerationType<StringOperator>(StringOperator.VALUES), null, null, 1, 1));
        opField.setInitialValue(filter().operator(), false);
        opField.addListener(new FormItemListener<StringOperator>() {

            @Override
            public void itemValueChanged(FormItem<StringOperator> f) {
                int nvs1 = filter().operator() == null ? 0 : filter().operator().numberOfValues();
                int nvs2 = f.value() == null ? 0 : f.value().numberOfValues();
                filter().setOperator(f.value());
                if (nvs1 != nvs2) {
                    updateForm();
                }
            }

            @Override
            public void itemPropertyChanged(FormItem<StringOperator> f, Property property) {

            }
        });
        _form.add(opField);

        if (filter().operator() != null && filter().operator().numberOfValues() > 0) {
            Field<String> valueField = new Field<String>(new FieldDefinition("value",
                    StringType.DEFAULT, null, null, 1, 1));
            valueField.setInitialValue(filter().value());
            valueField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {
                    filter().setValue(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f, Property property) {

                }
            });
            _form.add(valueField);
        }

        addMustBeValid(_form);
        _form.render();
        
        _formSP.setContent(_form);
    }

    @Override
    public Widget gui() {
        return _hp;
    }

}
