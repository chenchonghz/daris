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

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.mf.DateRange;
import daris.client.model.query.filter.mf.TimeRangeFilter;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class TimeRangeFilterItem extends FilterItem<TimeRangeFilter> {

    private SimplePanel _sp;
    private Form _lowerValueForm;
    private Form _upperValueForm;

    public TimeRangeFilterItem(CompositeFilterForm form, TimeRangeFilter filter, boolean editable) {
        super(form, filter, editable);
        _sp = new SimplePanel();
        _sp.setHeight(22);
        updateForm();
    }

    private void updateForm() {
        if (_lowerValueForm != null) {
            removeMustBeValid(_lowerValueForm);
        }
        if (_upperValueForm != null) {
            removeMustBeValid(_upperValueForm);
        }
        _sp.clear();

        HorizontalPanel hp = new HorizontalPanel();

        HTML label = new HTML(filter().type().toString() + " in range: ");
        label.setFontSize(11);
        label.setMarginTop(8);
        hp.add(label);
        hp.setSpacing(3);

        DateRange range = filter().range();

        _lowerValueForm = new Form(editable() ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        _lowerValueForm.setNumberOfColumns(1);
        _lowerValueForm.setShowDescriptions(false);
        _lowerValueForm.setShowLabels(false);
        _lowerValueForm.setShowHelp(false);

        Field<Date> lowerValueField = new Field<Date>(new FieldDefinition("from", range.dateOnly() ? DateType.DATE_ONLY
                : DateType.DATE_AND_TIME, null, null, 1, 1));
        lowerValueField.setInitialValue(range.from(), false);
        lowerValueField.addListener(new FormItemListener<Date>() {

            @Override
            public void itemValueChanged(FormItem<Date> f) {
                filter().range().setFrom(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Date> f, Property property) {

            }
        });
        _lowerValueForm.add(lowerValueField);
        _lowerValueForm.render();
        addMustBeValid(_lowerValueForm);
        hp.add(_lowerValueForm);

        hp.setSpacing(5);

        CheckBox lowerValueInclusiveCB = new CheckBox();
        lowerValueInclusiveCB.setMarginTop(7);
        lowerValueInclusiveCB.setChecked(range.fromInclusive(), false);
        lowerValueInclusiveCB.addChangeListener(new Listener() {

            @Override
            public void changed(CheckBox cb) {
                filter().range().setFromInclusive(cb.checked());
                // TimeRangeFilterItem.this.notifyOfChangeInState();
                updateForm();
            }
        });
        hp.add(lowerValueInclusiveCB);
        hp.setSpacing(3);

        HTML lowerValueInclusiveLabel = new HTML("inclusive");
        lowerValueInclusiveLabel.setFontSize(11);
        lowerValueInclusiveLabel.setMarginTop(7);
        hp.add(lowerValueInclusiveLabel);

        hp.setSpacing(8);

        _upperValueForm = new Form(editable() ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        _upperValueForm.setNumberOfColumns(1);
        _upperValueForm.setShowDescriptions(false);
        _upperValueForm.setShowLabels(false);
        _upperValueForm.setShowHelp(false);

        Field<Date> upperValueField = new Field<Date>(new FieldDefinition("to", range.dateOnly() ? DateType.DATE_ONLY
                : DateType.DATE_AND_TIME, null, null, 1, 1));
        upperValueField.setInitialValue(range.from(), false);
        upperValueField.addListener(new FormItemListener<Date>() {

            @Override
            public void itemValueChanged(FormItem<Date> f) {
                filter().range().setFrom(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Date> f, Property property) {

            }
        });
        _upperValueForm.add(upperValueField);
        _upperValueForm.render();
        addMustBeValid(_upperValueForm);
        hp.add(_upperValueForm);

        hp.setSpacing(5);

        CheckBox upperValueInclusiveCB = new CheckBox();
        upperValueInclusiveCB.setMarginTop(7);
        upperValueInclusiveCB.setChecked(range.fromInclusive(), false);
        upperValueInclusiveCB.addChangeListener(new Listener() {

            @Override
            public void changed(CheckBox cb) {
                filter().range().setFromInclusive(cb.checked());
                // TimeRangeFilterItem.this.notifyOfChangeInState();
                updateForm();
            }
        });
        hp.add(upperValueInclusiveCB);
        hp.setSpacing(3);

        HTML upperValueInclusiveLabel = new HTML("inclusive");
        upperValueInclusiveLabel.setFontSize(11);
        upperValueInclusiveLabel.setMarginTop(7);
        hp.add(upperValueInclusiveLabel);

        hp.setSpacing(5);

        CheckBox dateOnlyCB = new CheckBox();
        dateOnlyCB.setMarginTop(7);
        dateOnlyCB.setChecked(filter().range().dateOnly(), false);
        dateOnlyCB.addChangeListener(new Listener() {

            @Override
            public void changed(CheckBox cb) {
                filter().range().setDateOnly(cb.checked());
                // TimeRangeFilterItem.this.notifyOfChangeInState();
                updateForm();
            }
        });
        hp.add(dateOnlyCB);
        hp.setSpacing(3);

        HTML dateOnlyLabel = new HTML("date only");
        dateOnlyLabel.setFontSize(11);
        dateOnlyLabel.setMarginTop(7);
        hp.add(dateOnlyLabel);

        _sp.setContent(hp);
    }

    @Override
    public Widget gui() {
        return _sp;
    }

}
