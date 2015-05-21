package daris.client.ui.query.options;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.IntegerType;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.options.QueryOptions;
import daris.client.model.query.options.QueryOptions.Action;

public class QueryOptionsForm extends ValidatedInterfaceComponent {

    private QueryOptions _opts;

    private ScrollPanel _sp;
    private VerticalPanel _vp;

    private SimplePanel _mainFormSP;
    private Form _mainForm;

    private SimplePanel _xpathSP;
    private XPathForm _xpathForm;

    private SimplePanel _sortSP;
    private SortForm _sortForm;

    public QueryOptionsForm(QueryOptions opts) {
        _opts = opts;

        _vp = new VerticalPanel();
        _vp.setPaddingLeft(20);
        _vp.setMarginTop(15);

        _mainFormSP = new SimplePanel();
        _mainFormSP.setHeight(70);
        updateMainForm();
        _vp.add(_mainFormSP);

        _xpathSP = new SimplePanel();
        _xpathSP.setHeight(220);
        updateXPathForm();
        _vp.add(_xpathSP);

        _sortSP = new SimplePanel();
        _sortSP.setHeight(220);
        updateSortForm();
        _vp.add(_sortSP);

        _sp = new ScrollPanel(_vp, ScrollPolicy.VERTICAL);
    }

    private void updateMainForm() {
        if (_mainForm != null) {
            removeMustBeValid(_mainForm);
        }
        _mainForm = new Form();
        _mainForm.setShowHelp(false);
        _mainForm.setShowDescriptions(false);
        // Field<QueryOptions.Entity> entity = new Field<QueryOptions.Entity>(new FieldDefinition("entity",
        // ConstantType.DEFAULT, "entity", null, 1, 1));
        // entity.setValue(_opts.entity(), false);
        // _mainForm.add(entity);

        Field<QueryOptions.Action> action = new Field<QueryOptions.Action>(new FieldDefinition("action",
                new EnumerationType<QueryOptions.Action>(new QueryOptions.Action[] { QueryOptions.Action.get_value,
                        QueryOptions.Action.count }), null, null, 1, 1));
        action.setInitialValue(_opts.action(), false);
        action.addListener(new FormItemListener<QueryOptions.Action>() {

            @Override
            public void itemValueChanged(FormItem<Action> f) {
                _opts.setAction(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Action> f, Property property) {

            }
        });
        _mainForm.add(action);

        Field<Integer> size = new Field<Integer>(new FieldDefinition("size", IntegerType.POSITIVE_ONE,
                "The maximum size of the result set", null, 1, 1));
        size.setValue(_opts.size());
        size.addListener(new FormItemListener<Integer>() {

            @Override
            public void itemValueChanged(FormItem<Integer> f) {
                _opts.setSize(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<Integer> f, Property property) {

            }
        });
        _mainForm.add(size);
        addMustBeValid(_mainForm);
        _mainForm.render();
        _mainFormSP.setContent(_mainForm);
    }

    private void updateXPathForm() {
        if (_xpathForm != null) {
            removeMustBeValid(_xpathForm);
        }
        _xpathForm = new XPathForm(_opts);
        addMustBeValid(_xpathForm);
        _xpathSP.setContent(_xpathForm.gui());
    }

    private void updateSortForm() {
        if (_sortForm != null) {
            removeMustBeValid(_sortForm);
        }
        _sortForm = new SortForm(_opts);
        addMustBeValid(_sortForm);
        _sortSP.setContent(_sortForm.gui());
    }

    public QueryOptions options() {
        return _opts;
    }

    @Override
    public Widget gui() {
        return _sp;
    }

    public void refresh() {
        updateMainForm();
        updateXPathForm();
        updateSortForm();
    }
}
