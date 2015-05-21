package daris.client.ui.query.filter.item.mf;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.mf.dtype.DataType;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.StringType;
import arc.mf.expr.Operator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.mf.citeable.CiteableNameEnum;
import daris.client.model.query.filter.CompositeFilter;
import daris.client.model.query.filter.mf.CIDFilter;
import daris.client.model.query.filter.mf.CIDFilter.CIDOperator;
import daris.client.ui.query.filter.action.CompositeOpenAction;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem;
import daris.client.ui.query.filter.item.FilterItem.HasComposite;

public class CIDFilterItem extends FilterItem<CIDFilter> implements HasComposite {

    public static final arc.gui.image.Image ICON_EDIT = new arc.gui.image.Image(Resource.INSTANCE
            .edit10().getSafeUri().asString(), 12, 12);
    public static final arc.gui.image.Image ICON_VIEW = new arc.gui.image.Image(Resource.INSTANCE
            .viewList16().getSafeUri().asString(), 12, 12);

    private HorizontalPanel _hp;
    private SimplePanel _formSP;
    private Form _form;

    public CIDFilterItem(CompositeFilterForm cform, CIDFilter filter, boolean editable) {
        super(cform, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);

        HTML label = new HTML("cid");
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

        CIDOperator op = filter().operator();

        _form = new Form(editable() ? FormEditMode.UPDATE : FormEditMode.READ_ONLY);
        _form.setNumberOfColumns(op == null ? 2 : op.numberOfValues() + 2);
        _form.setShowLabels(false);
        _form.setShowHelp(false);
        _form.setShowDescriptions(false);

        Field<CIDOperator> opField = new Field<CIDOperator>(new FieldDefinition("operator",
                new EnumerationType<Operator>(CIDOperator.VALUES), null, null, 1, 1));
        opField.setInitialValue(op);
        opField.addListener(new FormItemListener<CIDOperator>() {

            @Override
            public void itemValueChanged(FormItem<CIDOperator> f) {
                filter().setOperator(f.value());
                updateForm();
            }

            @Override
            public void itemPropertyChanged(FormItem<CIDOperator> f, Property property) {

            }
        });
        _form.add(opField);
        if (op != null && op.numberOfValues() == 1 && !CIDOperator.CONTAINED_BY.equals(op)
                && !CIDOperator.CONTAINS.equals(op)) {
            // TODO: CiteableIdType: not form item factory for it.
            DataType dt = op.isNamedIdOperator() ? new EnumerationType<String>(
                    new CiteableNameEnum()) : StringType.DEFAULT;
            Field<String> valueField = new Field<String>(new FieldDefinition("value", dt, null,
                    null, 1, 1));
            valueField.setInitialValue(filter().value());
            valueField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {
                    filter().setValue(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f, Property property) {

                }
            });
            _form.add(valueField);
        }
        addMustBeValid(_form);
        _form.render();

        if (CIDOperator.CONTAINS.equals(op)) {
            HorizontalPanel hp = new HorizontalPanel();
            hp.add(_form);

            String label = editable() ? "Edit" : "View";
            arc.gui.image.Image icon = editable() ? ICON_EDIT : ICON_VIEW;
            Button containsButton = new Button(new Image(icon), label);
            containsButton.setHeight(22);
            containsButton.setWidth(70);
            containsButton.setMarginTop(3);
            containsButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    if (filter().contains() == null) {
                        filter().setContains(createCompositeFilter());
                    }
                    CompositeOpenAction action = new CompositeOpenAction(CIDFilterItem.this,
                            window(), 0.9, 0.9);
                    action.execute();
                }
            });
            hp.add(containsButton);
            _formSP.setContent(hp);
        } else if (CIDOperator.CONTAINED_BY.equals(op)) {
            HorizontalPanel hp = new HorizontalPanel();
            hp.add(_form);

            String label = editable() ? "Edit" : "View";
            arc.gui.image.Image icon = editable() ? ICON_EDIT : ICON_VIEW;
            Button containedByButton = new Button(new Image(icon), label);
            containedByButton.setHeight(22);
            containedByButton.setWidth(70);
            containedByButton.setMarginTop(3);
            containedByButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    if (filter().containedBy() == null) {
                        filter().setContainedBy(createCompositeFilter());
                    }
                    CompositeOpenAction action = new CompositeOpenAction(CIDFilterItem.this,
                            window(), 0.9, 0.9);
                    action.execute();
                }
            });
            hp.add(containedByButton);
            _formSP.setContent(hp);
        } else {
            _formSP.setContent(_form);
        }
    }

    @Override
    public Widget gui() {
        return _hp;
    }

    @Override
    public FilterItem<?> hadBy() {
        return this;
    }

    @Override
    public CompositeFilter composite() {
        if (CIDOperator.CONTAINS.equals(filter().operator())) {
            return filter().contains();
        } else if (CIDOperator.CONTAINED_BY.equals(filter().operator())) {
            return filter().containedBy();
        } else {
            return null;
        }
    }

    @Override
    public void setComposite(CompositeFilter filter) {
        if (CIDOperator.CONTAINS.equals(filter().operator())) {
            filter().setContains(filter);
            notifyOfChangeInState();
        }
        if (CIDOperator.CONTAINED_BY.equals(filter().operator())) {
            filter().setContainedBy(filter);
            notifyOfChangeInState();
        }
    }
}
