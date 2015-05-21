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

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.mf.AssetModifiedFilter;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class AssetModifiedFilterItem extends FilterItem<AssetModifiedFilter> {

    private HorizontalPanel _hp;

    public AssetModifiedFilterItem(CompositeFilterForm cform, AssetModifiedFilter filter, boolean editable) {
        super(cform, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("asset");
        label.setFontSize(11);
        label.setMarginTop(8);
        _hp.add(label);
        _hp.setSpacing(3);

        Form form = new Form(editable ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        form.setShowDescriptions(false);
        form.setShowHelp(false);
        form.setShowLabels(false);

        Field<AssetModifiedFilter.State> stateField = new Field<AssetModifiedFilter.State>(new FieldDefinition("state",
                new EnumerationType<AssetModifiedFilter.State>(AssetModifiedFilter.State.values()), null, null, 1, 1));
        stateField.setInitialValue(filter().state());
        stateField.addListener(new FormItemListener<AssetModifiedFilter.State>() {

            @Override
            public void itemValueChanged(FormItem<AssetModifiedFilter.State> f) {
                filter().setState(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<AssetModifiedFilter.State> f, Property property) {

            }
        });
        form.add(stateField);

        addMustBeValid(form);

        form.render();

        _hp.add(form);

    }

    @Override
    public Widget gui() {
        return _hp;
    }

}
