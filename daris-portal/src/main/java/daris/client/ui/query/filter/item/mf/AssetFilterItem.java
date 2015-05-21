package daris.client.ui.query.filter.item.mf;

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
import arc.mf.expr.Operator;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.mf.AssetFilter;
import daris.client.model.query.filter.mf.AssetFilter.AssetProperty;
import daris.client.model.query.filter.mf.AssetFilter.AssetPropertyOperator;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class AssetFilterItem extends FilterItem<AssetFilter> {

    private HorizontalPanel _hp;

    public AssetFilterItem(CompositeFilterForm cform, AssetFilter filter, boolean editable) {
        super(cform, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("asset");
        label.setFontSize(11);
        label.setMarginTop(8);
        _hp.add(label);
        _hp.setSpacing(3);

        Form form = new Form(editable ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        form.setNumberOfColumns(2);
        form.setShowDescriptions(false);
        form.setShowLabels(false);
        form.setShowHelp(false);

        Field<AssetPropertyOperator> opField = new Field<AssetPropertyOperator>(new FieldDefinition("operator",
                new EnumerationType<Operator>(AssetPropertyOperator.VALUES), null, null, 1, 1));
        opField.setInitialValue(filter().operator());
        opField.addListener(new FormItemListener<AssetPropertyOperator>() {

            @Override
            public void itemValueChanged(FormItem<AssetPropertyOperator> f) {
                filter().setOperator(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<AssetPropertyOperator> f, Property property) {

            }
        });
        form.add(opField);

        Field<AssetFilter.AssetProperty> propField = new Field<AssetFilter.AssetProperty>(new FieldDefinition(
                "property", new EnumerationType<AssetFilter.AssetProperty>(AssetFilter.AssetProperty.values()), null,
                null, 1, 1));
        propField.setInitialValue(filter().property());
        propField.addListener(new FormItemListener<AssetFilter.AssetProperty>() {

            @Override
            public void itemValueChanged(FormItem<AssetProperty> f) {
                filter().setAssetProperty(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<AssetProperty> f, Property property) {

            }
        });
        form.add(propField);

        addMustBeValid(form);

        form.render();

        _hp.add(form);

    }

    @Override
    public Widget gui() {
        return _hp;
    }

}
