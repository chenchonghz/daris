package daris.client.ui.query.filter.item.mf;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.dtype.EnumerationType;
import arc.mf.model.authorization.Role.Scope;
import arc.mf.model.authorization.RoleRef;

import com.google.gwt.user.client.ui.Widget;

import daris.client.mf.role.RoleEnumerationDataSource;
import daris.client.model.query.filter.mf.ACLFilter;
import daris.client.model.query.filter.mf.ACLFilter.ACLOperator;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;

public class ACLFilterItem extends FilterItem<ACLFilter> {

    private HorizontalPanel _hp;
    private SimplePanel _formSP;
    private Form _form;

    public ACLFilterItem(CompositeFilterForm form, ACLFilter filter, boolean editable) {
        super(form, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("acl");
        label.setFontSize(11);
        label.setMarginTop(8);
        _hp.add(label);
        _hp.setSpacing(3);

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
        int nbCols = 1;
        ACLOperator op = filter().operator();
        if (op != null) {
            if (ACLOperator.FOR_ROLE.equals(op)) {
                nbCols = 2;
            }
        }
        _form.setNumberOfColumns(nbCols);
        _form.setShowLabels(false);
        _form.setShowDescriptions(false);
        _form.setShowHelp(false);

        Field<ACLOperator> opField = new Field<ACLOperator>(new FieldDefinition("operator",
                new EnumerationType<ACLOperator>(ACLOperator.VALUES), null, null, 1, 1));
        opField.setInitialValue(filter().operator(), false);
        opField.addListener(new FormItemListener<ACLOperator>() {

            @Override
            public void itemValueChanged(FormItem<ACLOperator> f) {
                if (ACLOperator.ACTOR_INVALID.equals(f.value())) {
                    filter().setActorInvalid();
                } else {
                    filter().setForRole(filter().role());
                }
                updateForm();
            }

            @Override
            public void itemPropertyChanged(FormItem<ACLOperator> f, Property property) {

            }
        });
        _form.add(opField);

        if (ACLOperator.FOR_ROLE.equals(filter().operator())) {

            Field<RoleRef> roleField = new Field<RoleRef>(new FieldDefinition("role", new EnumerationType<RoleRef>(
                    new RoleEnumerationDataSource()), null, null, 1, 1));
            roleField.setInitialValue(new RoleRef(filter().role(), null, Scope.REPOSITORY), false);
            roleField.addListener(new FormItemListener<RoleRef>() {

                @Override
                public void itemValueChanged(FormItem<RoleRef> f) {
                    filter().setForRole(f.value() == null ? null : f.value().name());
                }

                @Override
                public void itemPropertyChanged(FormItem<RoleRef> f, Property property) {

                }
            });
            FieldRenderOptions fro = new FieldRenderOptions();
            fro.setWidth(300);
            roleField.setRenderOptions(fro);
            _form.add(roleField);
        }
        addMustBeValid(_form);
        _form.render();
        _formSP.setContent(_form);
    }

    @Override
    public Widget gui() {
        return _hp;
    }

}
