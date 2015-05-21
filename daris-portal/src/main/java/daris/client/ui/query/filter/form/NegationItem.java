package daris.client.ui.query.filter.form;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.input.CheckBox;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.client.util.StateChangeListener;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.CompositeFilter;

public class NegationItem extends ValidatedInterfaceComponent {

    private CompositeFilter.Member _cfm;
    private boolean _editable;
    private SimplePanel _sp;

    public NegationItem(CompositeFilter.Member cfm, boolean editable) {
        _cfm = cfm;
        _editable = editable;

        _sp = new SimplePanel();

        addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                updateForm();
            }
        });

        updateForm();
    }

    private void updateForm() {

        _sp.clear();

        HorizontalPanel hp = new HorizontalPanel();

        CheckBox cb = new CheckBox(_cfm.negated());
        if (_editable) {
            cb.addChangeListener(new CheckBox.Listener() {

                @Override
                public void changed(CheckBox cb) {
                    if (cb.checked() != _cfm.negated()) {
                        _cfm.setNegated(cb.checked());
                        NegationItem.this.notifyOfChangeInState();
                    }
                }
            });
        } else {
            cb.setReadOnly(true);
        }
        hp.add(cb);

        hp.setSpacing(2);
        if (_cfm.negated()) {
            HTML label = new HTML("not");
            label.setFontSize(11);
            hp.add(label);
        }

        _sp.setContent(hp);
    }

    @Override
    public Widget gui() {
        return _sp;
    }

    public BaseWidget widget() {
        return _sp;
    }
}
