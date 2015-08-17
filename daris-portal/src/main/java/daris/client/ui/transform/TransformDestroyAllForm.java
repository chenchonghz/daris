package daris.client.ui.transform;

import java.util.Set;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.Form.BooleanAs;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.dtype.BooleanType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.transform.Transform;
import daris.client.model.transform.Transform.Status;

public class TransformDestroyAllForm extends ValidatedInterfaceComponent {

    private VerticalPanel _vp;
    private Set<Transform.Status.State> _states;

    public TransformDestroyAllForm(Set<Transform.Status.State> states) {
        _states = states;
        _vp = new VerticalPanel();
        _vp.setPaddingTop(10);
        _vp.setPaddingLeft(25);
        _vp.fitToParent();
        
        HTML label = new HTML("Delete the transforms in the following states:");
        _vp.add(label);

        Form form = new Form(FormEditMode.UPDATE);
        form.setNumberOfColumns(1);
        form.setShowHelp(false);
        form.setShowDescriptions(false);
        form.setShowLabels(true);
        form.setBooleanAs(BooleanAs.CHECKBOX);
        form.setMarginLeft(30);

        Field<Boolean> includeTerminated = new Field<Boolean>(
                new FieldDefinition("terminated",
                        BooleanType.DEFAULT_TRUE_FALSE, null, null, 0, 1));
        includeTerminated.setInitialValue(_states
                .contains(Status.State.terminated));
        includeTerminated.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                if (f.value()) {
                    _states.add(Status.State.terminated);
                } else {
                    if (_states.contains(Status.State.terminated)) {
                        _states.remove(Status.State.terminated);
                    }
                }
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f,
                    Property property) {

            }
        });
        form.add(includeTerminated);
        Field<Boolean> includeFailed = new Field<Boolean>(new FieldDefinition(
                "failed", BooleanType.DEFAULT_TRUE_FALSE, null, null, 0, 1));
        includeFailed.setInitialValue(_states.contains(Status.State.failed));
        includeFailed.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                if (f.value()) {
                    _states.add(Status.State.failed);
                } else {
                    if (_states.contains(Status.State.failed)) {
                        _states.remove(Status.State.failed);
                    }
                }
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f,
                    Property property) {

            }
        });
        form.add(includeFailed);
        Field<Boolean> includeUnknown = new Field<Boolean>(new FieldDefinition(
                "unknown", BooleanType.DEFAULT_TRUE_FALSE, null, null, 0, 1));
        includeUnknown.setInitialValue(_states.contains(Status.State.unknown));
        includeUnknown.addListener(new FormItemListener<Boolean>() {

            @Override
            public void itemValueChanged(FormItem<Boolean> f) {
                if (f.value()) {
                    _states.add(Status.State.unknown);
                } else {
                    if (_states.contains(Status.State.unknown)) {
                        _states.remove(Status.State.unknown);
                    }
                }
            }

            @Override
            public void itemPropertyChanged(FormItem<Boolean> f,
                    Property property) {

            }
        });
        form.add(includeUnknown);
        addMustBeValid(form);
        form.render();

        _vp.add(form);
    }

    @Override
    public Widget gui() {
        return _vp;
    }

}
