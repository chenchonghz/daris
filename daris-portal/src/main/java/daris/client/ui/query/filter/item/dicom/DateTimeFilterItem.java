package daris.client.ui.query.filter.item.dicom;

import java.util.Date;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.label.Label;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.client.util.ObjectUtil;
import arc.mf.dtype.BooleanType;
import arc.mf.dtype.DateType;
import arc.mf.dtype.EnumerationType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.dicom.DateTimeFilter;
import daris.client.model.query.filter.operators.DateOperator;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public abstract class DateTimeFilterItem<T extends DateTimeFilter> extends FilterItem<T> {

    private HorizontalPanel _hp;
    private HTML _label;
    private SimplePanel _formSP;
    private Form _form;

    protected DateTimeFilterItem(CompositeFilterForm containerForm, T filter, boolean editable) {
        super(containerForm, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        _label = new HTML(label());
        _label.setFontSize(11);
        _label.setMarginTop(8);
        _hp.add(_label);
        _hp.setSpacing(3);

        _formSP = new SimplePanel();
        _formSP.setHeight100();
        _hp.add(_formSP);

        updateForm();
    }

    private void updateLabel() {
        _label.setHTML(label());
    }

    private void updateForm() {

        updateLabel();

        if (_form != null) {
            removeMustBeValid(_form);
        }

        _formSP.clear();
        _form = new Form(editable() ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        _form.setShowLabels(false);
        _form.setShowHelp(false);
        _form.setShowDescriptions(false);
        _form.setMarginRight(0);
        _form.setPaddingRight(0);
        if (filter().operator() == null || filter().operator().numberOfValues() < 1) {
            _form.setNumberOfColumns(1);
        } else {
            if (filter().canIncludeTime()) {
                _form.setNumberOfColumns(4);
            } else {
                _form.setNumberOfColumns(2);
            }
        }

        Field<DateOperator> opField = new Field<DateOperator>(new FieldDefinition("operator",
                new EnumerationType<DateOperator>(DateOperator.VALUES), null, null, 1, 1));
        opField.setInitialValue(filter().operator(), false);
        opField.addListener(new FormItemListener<DateOperator>() {

            @Override
            public void itemValueChanged(FormItem<DateOperator> f) {
                int nvs1 = filter().operator() == null ? 0 : filter().operator().numberOfValues();
                int nvs2 = f.value() == null ? 0 : f.value().numberOfValues();
                filter().setOperator(f.value());
                if (nvs1 != nvs2) {
                    updateForm();
                }
            }

            @Override
            public void itemPropertyChanged(FormItem<DateOperator> f, Property property) {

            }
        });
        _form.add(opField);

        if (filter().operator() != null && filter().operator().numberOfValues() > 0) {
            Field<Date> valueField = new Field<Date>(new FieldDefinition("value", filter()
                    .includeTime() ? DateType.DATE_AND_TIME : DateType.DATE_ONLY, null, null, 1, 1));
            valueField.setInitialValue(filter().value());
            valueField.addListener(new FormItemListener<Date>() {

                @Override
                public void itemValueChanged(FormItem<Date> f) {
                    filter().setValue(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<Date> f, Property property) {

                }
            });
            _form.add(valueField);

            if (filter().canIncludeTime()) {
                Field<Boolean> includeTimeField = new Field<Boolean>(new FieldDefinition(
                        "include time", BooleanType.DEFAULT_TRUE_FALSE, null, null, 1, 1));
                includeTimeField.setInitialValue(filter().includeTime());
                includeTimeField.addListener(new FormItemListener<Boolean>() {

                    @Override
                    public void itemValueChanged(FormItem<Boolean> f) {
                        if (!ObjectUtil.equals(f.value(), filter().includeTime())) {
                            filter().setIncludeTime(f.value());
                            updateForm();
                        }
                    }

                    @Override
                    public void itemPropertyChanged(FormItem<Boolean> f, Property property) {

                    }
                });
                _form.add(includeTimeField);
            }
        }

        addMustBeValid(_form);
        _form.render();

        if (filter().canIncludeTime() && filter().operator() != null
                && filter().operator().numberOfValues() > 0) {
            HorizontalPanel hp = new HorizontalPanel();
            hp.setHeight(22);
            hp.add(_form);
            Label includeTimeLabel = new Label("include time");
            includeTimeLabel.setMarginLeft(0);
            includeTimeLabel.setMarginTop(6);
            hp.add(includeTimeLabel);
            _formSP.setContent(hp);
        } else {
            _formSP.setContent(_form);
        }
    }

    @Override
    public Widget gui() {
        return _hp;
    }

    protected abstract String label();

}
