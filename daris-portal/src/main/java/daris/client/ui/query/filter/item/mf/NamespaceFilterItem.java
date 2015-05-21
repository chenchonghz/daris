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
import arc.mf.model.asset.namespace.NamespaceRef;
import arc.mf.model.asset.namespace.NamespaceTree;
import arc.mf.object.tree.Node;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.query.filter.mf.NamespaceFilter;
import daris.client.model.query.filter.mf.NamespaceFilter.NamespaceOperator;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;
import daris.client.ui.widget.TreeSelectComboBox;
import daris.client.ui.widget.TreeSelectComboBox.SelectionHandler;

public class NamespaceFilterItem extends FilterItem<NamespaceFilter> {

    private HorizontalPanel _hp;
    private TreeSelectComboBox<NamespaceRef> _nsCombo;

    public NamespaceFilterItem(CompositeFilterForm cform, NamespaceFilter filter, boolean editable) {
        super(cform, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("namespace");
        label.setFontSize(11);
        label.setMarginTop(8);
        _hp.add(label);
        _hp.setSpacing(3);

        Form form = new Form(editable ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        form.setNumberOfColumns(1);
        form.setShowDescriptions(false);
        form.setShowLabels(false);
        form.setShowHelp(false);

        Field<NamespaceOperator> opField = new Field<NamespaceOperator>(new FieldDefinition("operator",
                new EnumerationType<NamespaceOperator>(NamespaceOperator.VALUES), null, null, 1, 1));
        opField.setInitialValue(filter().operator());
        opField.addListener(new FormItemListener<NamespaceOperator>() {

            @Override
            public void itemValueChanged(FormItem<NamespaceOperator> f) {
                filter().setOperator(f.value());
            }

            @Override
            public void itemPropertyChanged(FormItem<NamespaceOperator> f, Property property) {

            }
        });
        form.add(opField);

        addMustBeValid(form);

        form.render();

        _hp.add(form);
        _nsCombo = new TreeSelectComboBox<NamespaceRef>(filter().namespace(), new NamespaceTree(), true) {

            @Override
            protected String toString(NamespaceRef ns) {
                return ns.path();
            }

            @Override
            protected boolean canSelect(Node n) {

                return n.object() != null && (n.object() instanceof NamespaceRef);
            }

            @Override
            protected NamespaceRef transform(Node n) {
                return (NamespaceRef) n.object();
            }
        };
        _nsCombo.setWidth(250);
        _nsCombo.addSelectionHandler(new SelectionHandler<NamespaceRef>() {

            @Override
            public void selected(NamespaceRef o) {
                filter().setNamespace(o);
                NamespaceFilterItem.this.notifyOfChangeInState();
            }
        });
        _hp.add(_nsCombo);
    }

    @Override
    public Widget gui() {
        return _hp;
    }

}
