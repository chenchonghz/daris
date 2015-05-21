package daris.client.ui.query.filter.form;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.mf.dtype.EnumerationType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.CompositeFilter;
import daris.client.model.query.filter.LogicOperator;

public class OperatorItem extends ValidatedInterfaceComponent {

    private CompositeFilter.Member _cfm;
    private boolean _editable;

    private Form _form;

    public OperatorItem(CompositeFilter.Member cfm, boolean editable) {
        _cfm = cfm;
        _editable = editable;
        _form = new Form(_editable ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        _form.setShowDescriptions(false);
        _form.setShowHelp(false);
        _form.setShowLabels(false);

        Field<LogicOperator> operatorField = new Field<LogicOperator>(new FieldDefinition("operator",
                new EnumerationType<LogicOperator>(LogicOperator.values()), null, null,
                _editable ? 1 : 0, 1));
        operatorField.setInitialValue(_cfm.operator(), false);
        operatorField.addListener(new FormItemListener<LogicOperator>() {

            @Override
            public void itemValueChanged(FormItem<LogicOperator> f) {
                _cfm.setOperator(f.value());
                notifyOfChangeInState();
            }

            @Override
            public void itemPropertyChanged(FormItem<LogicOperator> f, Property property) {

            }
        });
        _form.add(operatorField);

        addMustBeValid(_form);

        _form.render();
    }

    @Override
    public Widget gui() {
        return _form;
    }

    public BaseWidget widget() {
        return _form;
    }

}
