package daris.client.ui.query.filter.item.mf;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.dtype.EnumerationType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.mf.TypeFilter;
import daris.client.model.type.TypeStringEnum;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class TypeFilterItem extends FilterItem<TypeFilter> {

    private SimplePanel _sp;
    private Form _form;

    public TypeFilterItem(CompositeFilterForm form, TypeFilter filter, boolean editable) {
        super(form, filter, editable);
        _sp = new SimplePanel();
        _sp.setHeight(22);

        _form = new Form();
        _form.setMarginTop(8);
        _form.setShowLabels(true);
        _form.setShowDescriptions(false);
        _form.setShowHelp(false);

        Field<String> typeField = new Field<String>(new FieldDefinition("type", new EnumerationType<String>(
                new TypeStringEnum()), "The asset MIME type.", null, 1, 1));
        typeField.setInitialValue(filter().type(), false);
        typeField.addListener(new FormItemListener<String>(){

            @Override
            public void itemValueChanged(FormItem<String> f) {
                filter().setType(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<String> f, Property property) {
                
            }});
        _form.add(typeField);
        _form.render();
        addMustBeValid(_form);
        
        _sp.setContent(_form);
    }

    @Override
    public Widget gui() {
        return _sp;
    }

}
