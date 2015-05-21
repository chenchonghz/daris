package daris.client.ui.query.filter.item.mf;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.mf.TextFilter;
import daris.client.model.query.filter.mf.TextFilter.TextOperator;
import daris.client.model.query.filter.mf.TextFilter.TextTarget;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class TextFilterItem extends FilterItem<TextFilter> {
    private HorizontalPanel _hp;

    public TextFilterItem(CompositeFilterForm cform, TextFilter filter, boolean editable) {
        super(cform, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        Form form = new Form(editable ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        form.setNumberOfColumns(3);
        form.setShowDescriptions(false);
        form.setShowLabels(false);
        form.setShowHelp(false);

        Field<TextTarget> targetField = new Field<TextTarget>(new FieldDefinition("target",
                new EnumerationType<TextTarget>(TextTarget.values()), null, null, 1, 1));
        targetField.setInitialValue(filter.target(), false);
        targetField.addListener(new FormItemListener<TextTarget>() {

            @Override
            public void itemValueChanged(FormItem<TextTarget> f) {
                filter().setTarget(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<TextTarget> f, Property property) {

            }
        });
        FieldRenderOptions rops = new FieldRenderOptions();
        rops.setWidth(60);
        targetField.setRenderOptions(rops);
        form.add(targetField);

        Field<TextOperator> opField = new Field<TextOperator>(new FieldDefinition("operator",
                new EnumerationType<TextOperator>(TextOperator.VALUES), null, null, 1, 1));
        opField.setInitialValue(filter.operator(), false);
        opField.addListener(new FormItemListener<TextOperator>() {

            @Override
            public void itemValueChanged(FormItem<TextOperator> f) {
                filter().setOperator(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<TextOperator> f, Property property) {

            }
        });
        rops = new FieldRenderOptions();
        rops.setWidth(125);
        opField.setRenderOptions(rops);
        form.add(opField);

        Field<String> valueField = new Field<String>(new FieldDefinition("value", StringType.DEFAULT, null, null, 1, 1));
        valueField.setInitialValue(filter.value(), false);
        valueField.addListener(new FormItemListener<String>() {

            @Override
            public void itemValueChanged(FormItem<String> f) {
                filter().setValue(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {

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
