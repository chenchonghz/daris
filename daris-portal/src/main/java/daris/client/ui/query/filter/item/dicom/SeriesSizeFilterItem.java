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
import arc.mf.dtype.IntegerType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.dicom.SeriesSizeFilter;
import daris.client.model.query.filter.operators.CompareOperator;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class SeriesSizeFilterItem extends FilterItem<SeriesSizeFilter> {

    private HorizontalPanel _hp;

    public SeriesSizeFilterItem(CompositeFilterForm containerForm, SeriesSizeFilter filter,
            boolean editable) {
        super(containerForm, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("dicom series size");
        label.setFontSize(11);
        label.setMarginTop(8);
        _hp.add(label);
        _hp.setSpacing(3);

        Form form = new Form(editable ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        form.setShowLabels(false);
        form.setShowHelp(false);
        form.setShowDescriptions(false);
        form.setNumberOfColumns(2);
        
        Field<CompareOperator> opField = new Field<CompareOperator>(new FieldDefinition("operator",
                new EnumerationType<CompareOperator>(CompareOperator.VALUES), null, null, 1, 1));
        opField.setInitialValue(filter().operator(), false);
        opField.addListener(new FormItemListener<CompareOperator>() {

            @Override
            public void itemValueChanged(FormItem<CompareOperator> f) {
                filter().setOperator(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<CompareOperator> f, Property property) {

            }
        });
        form.add(opField);

        Field<Integer> valueField = new Field<Integer>(new FieldDefinition("value",
                IntegerType.POSITIVE, null, null, 1, 1));
        valueField.setInitialValue(filter().value(), false);
        valueField.addListener(new FormItemListener<Integer>() {

            @Override
            public void itemValueChanged(FormItem<Integer> f) {
                filter().setValue(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Integer> f, Property property) {

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
