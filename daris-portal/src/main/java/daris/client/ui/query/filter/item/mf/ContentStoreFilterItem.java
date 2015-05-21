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
import arc.mf.model.asset.store.DataStoreEnumDataSource;
import arc.mf.model.asset.store.DataStoreRef;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.mf.ContentStoreFilter;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class ContentStoreFilterItem extends FilterItem<ContentStoreFilter> {

    private HorizontalPanel _hp;

    public ContentStoreFilterItem(CompositeFilterForm cform, ContentStoreFilter filter, boolean editable) {
        super(cform, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("content store: ");
        label.setFontSize(11);
        label.setMarginTop(8);
        _hp.add(label);
        _hp.setSpacing(3);

        Form form = new Form(editable ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        form.setShowDescriptions(false);
        form.setShowLabels(false);
        form.setShowHelp(false);

        Field<DataStoreRef> stateField = new Field<DataStoreRef>(new FieldDefinition("store",
                new EnumerationType<DataStoreRef>(new DataStoreEnumDataSource()), null, null, 1, 1));
        stateField.setInitialValue(filter.store(), false);
        stateField.addListener(new FormItemListener<DataStoreRef>() {

            @Override
            public void itemValueChanged(FormItem<DataStoreRef> f) {
                filter().setStore(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<DataStoreRef> f, Property property) {

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