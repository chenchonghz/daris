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

import daris.client.model.query.filter.mf.ContentStatusFilter;
import daris.client.model.query.filter.mf.ContentStatusFilter.ContentStatus;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class ContentStatusFilterItem extends FilterItem<ContentStatusFilter> {

    private HorizontalPanel _hp;

    public ContentStatusFilterItem(CompositeFilterForm cform, ContentStatusFilter filter, boolean editable) {
        super(cform, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("content status: ");
        label.setFontSize(11);
        label.setMarginTop(8);
        _hp.add(label);
        _hp.setSpacing(3);

        Form form = new Form(editable ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        form.setShowDescriptions(false);
        form.setShowLabels(false);
        form.setShowHelp(false);

        Field<ContentStatus> stateField = new Field<ContentStatus>(new FieldDefinition("state",
                new EnumerationType<ContentStatus>(ContentStatus.values()), null, null, 1, 1));
        stateField.setInitialValue(filter.status(), false);
        stateField.addListener(new FormItemListener<ContentStatus>() {

            @Override
            public void itemValueChanged(FormItem<ContentStatus> f) {
                filter().setStatus(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<ContentStatus> f, Property property) {

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
