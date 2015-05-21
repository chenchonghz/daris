package daris.client.ui.query.filter.item.mf;

import java.util.Date;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.input.CheckBox;
import arc.gui.gwt.widget.input.CheckBox.Listener;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.dtype.DateType;
import arc.mf.dtype.EnumerationType;
import arc.mf.expr.Operator;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.mf.TimeFilter;
import daris.client.model.query.filter.operators.CompareOperator;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class TimeFilterItem extends FilterItem<TimeFilter> {

    private HorizontalPanel _hp;
    private SimplePanel _formSP;
    private Form _form;

    public TimeFilterItem(CompositeFilterForm cform, TimeFilter filter, boolean editable) {
        super(cform, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("ctime");
        label.setFontSize(11);
        label.setMarginTop(8);
        _hp.add(label);
        _hp.setSpacing(3);

        _formSP = new SimplePanel();
        _formSP.fitToParent();

        _hp.add(_formSP);

        updateForm();

    }

    private void updateForm() {

        _formSP.clear();

        if (_form != null) {
            removeMustBeValid(_form);
        }

        HorizontalPanel hp = new HorizontalPanel();
        hp.setMinWidth(320);

        _form = new Form(editable() ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        _form.setNumberOfColumns(2);
        _form.setShowDescriptions(false);
        _form.setShowLabels(false);
        _form.setShowHelp(false);

        Field<CompareOperator> opField = new Field<CompareOperator>(new FieldDefinition("operator",
                new EnumerationType<Operator>(CompareOperator.VALUES), null, null, 1, 1));
        opField.setInitialValue(filter().operator());
        opField.addListener(new FormItemListener<CompareOperator>() {

            @Override
            public void itemValueChanged(FormItem<CompareOperator> f) {
                filter().setOperator(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<CompareOperator> f, Property property) {

            }
        });
        _form.add(opField);

        Field<Date> timeField = new Field<Date>(new FieldDefinition(filter().type().toString(),
                filter().dateOnly() ? DateType.DATE_ONLY : DateType.DATE_AND_TIME, null, null, 1, 1));
        timeField.setInitialValue(filter().time());
        timeField.addListener(new FormItemListener<Date>() {

            @Override
            public void itemValueChanged(FormItem<Date> f) {
                filter().setTime(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Date> f, Property property) {

            }
        });
        _form.add(timeField);

        addMustBeValid(_form);

        _form.render();
        hp.add(_form);

        hp.setSpacing(5);

        CheckBox dateOnlyCB = new CheckBox();
        dateOnlyCB.setMarginTop(7);
        dateOnlyCB.setChecked(filter().dateOnly(), false);
        dateOnlyCB.addChangeListener(new Listener() {

            @Override
            public void changed(CheckBox cb) {
                filter().setDateOnly(cb.checked());
                // TimeFilterItem.this.notifyOfChangeInState();
                updateForm();
            }
        });
        hp.add(dateOnlyCB);
        hp.setSpacing(3);

        HTML dateOnlyLabel = new HTML("date only");
        dateOnlyLabel.setFontSize(11);
        dateOnlyLabel.setMarginTop(7);
        hp.add(dateOnlyLabel);

        _formSP.setContent(hp);

    }

    @Override
    public Widget gui() {
        return _hp;
    }

}
