package daris.client.ui.query.filter.item.pssd;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.mf.dtype.EnumerationType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.object.DObject;
import daris.client.model.object.DObject.Type;
import daris.client.model.query.filter.pssd.ObjectTypeFilter;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class ObjectTypeFilterItem extends FilterItem<ObjectTypeFilter> {

    private HorizontalPanel _hp;

    public ObjectTypeFilterItem(CompositeFilterForm cform,
            ObjectTypeFilter filter, boolean editable) {
        super(cform, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        Form form = new Form(editable ? FormEditMode.UPDATE
                : FormEditMode.READ_ONLY);
        form.setNumberOfColumns(1);
        form.setShowDescriptions(false);
        form.setShowLabels(true);
        form.setShowHelp(false);

        Field<DObject.Type> typeField = new Field<DObject.Type>(
                new FieldDefinition("daris.object.type",
                        new EnumerationType<DObject.Type>(new DObject.Type[] {
                                DObject.Type.subject, DObject.Type.ex_method,
                                DObject.Type.study, DObject.Type.dataset }),
                        null, null, 1, 1));
        typeField.setInitialValue(filter().type(), false);
        typeField.addListener(new FormItemListener<DObject.Type>() {

            @Override
            public void itemValueChanged(FormItem<Type> f) {
                filter().setType(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Type> f, Property property) {

            }
        });
        form.add(typeField);
        addMustBeValid(form);
        form.render();
        _hp.add(form);
    }

    @Override
    public Widget gui() {
        return _hp;
    }

}
