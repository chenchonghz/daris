package daris.client.ui.query.filter.item.pssd;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.dtype.EnumerationType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.object.DObject;
import daris.client.model.object.DObject.Type;
import daris.client.model.object.TagEnumerationDataSource;
import daris.client.model.query.filter.pssd.ObjectTagFilter;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class ObjectTagFilterItem extends FilterItem<ObjectTagFilter> {

    private HorizontalPanel _hp;
    private Form _form;
    private SimplePanel _formSP;

    public ObjectTagFilterItem(CompositeFilterForm cform, ObjectTagFilter filter, boolean editable) {
        super(cform, filter, editable);

        _hp = new HorizontalPanel();
        _hp.setHeight(22);

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
        _form.setNumberOfColumns(2);
        _form.setShowLabels(true);
        _form.setShowDescriptions(false);
        _form.setShowHelp(false);

        Field<DObject.Type> typeField = new Field<DObject.Type>(new FieldDefinition("type",
                new EnumerationType<DObject.Type>(new DObject.Type[] { DObject.Type.project, DObject.Type.subject,
                        DObject.Type.ex_method, DObject.Type.study, DObject.Type.dataset }), null, null, 1, 1));
        typeField.setInitialValue(filter().objectType(), false);
        typeField.addListener(new FormItemListener<DObject.Type>() {

            @Override
            public void itemValueChanged(FormItem<Type> f) {
                filter().setObjectType(f.value());
                updateForm();
            }

            @Override
            public void itemPropertyChanged(FormItem<Type> f, Property property) {

            }
        });
        _form.add(typeField);

        if (filter().objectType() != null) {

            Field<String> tagField = new Field<String>(new FieldDefinition("tag", new EnumerationType<String>(
                    new TagEnumerationDataSource(filter().project(), filter().objectType())), null, null, 1, 1));
            tagField.setInitialValue(filter().tag(), false);
            tagField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {
                    filter().setTag(f.value());
                    updateForm();
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f, Property property) {

                }
            });
            _form.add(tagField);
        }
        _form.render();
        addMustBeValid(_form);
        _formSP.setContent(_form);
    }

    @Override
    public Widget gui() {
        return _hp;
    }

}
