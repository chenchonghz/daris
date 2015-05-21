package daris.client.ui.query.filter.item;

import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.panel.HorizontalPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.model.query.filter.CompositeFilter;
import daris.client.ui.query.filter.action.CompositeOpenAction;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem.HasComposite;

public class CompositeFilterItem extends FilterItem<CompositeFilter> implements HasComposite {

    public static final arc.gui.image.Image ICON_EDIT = new arc.gui.image.Image(Resource.INSTANCE.edit10().getSafeUri()
            .asString(), 12, 12);
    public static final arc.gui.image.Image ICON_VIEW = new arc.gui.image.Image(Resource.INSTANCE.viewList16()
            .getSafeUri().asString(), 12, 12);

    private HorizontalPanel _hp;

    public CompositeFilterItem(CompositeFilterForm form, CompositeFilter filter, boolean editable) {
        super(form, filter, editable);
        _hp = new HorizontalPanel();
        _hp.setHeight(22);
        HTML label = new HTML("composite: ");
        label.setFontSize(11);
        label.setMarginTop(3);
        _hp.add(label);
        _hp.setSpacing(3);
        _hp.add(createActionButton());
    }

    private Button createActionButton() {
        String label = editable() ? "Edit" : "View";
        arc.gui.image.Image icon = editable() ? ICON_EDIT : ICON_VIEW;
        Button actionButton = new Button(new Image(icon), label);

        actionButton.setHeight(22);
        actionButton.setWidth(70);
        actionButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                CompositeOpenAction action = new CompositeOpenAction(CompositeFilterItem.this, window(), 0.9, 0.9);
                action.execute();
            }
        });
        return actionButton;
    }

    @Override
    public Widget gui() {
        return _hp;
    }

    @Override
    public CompositeFilter composite() {
        return filter();
    }

    @Override
    public void setComposite(CompositeFilter filter) {
        setFilter(filter, true);
    }

    @Override
    public FilterItem<?> hadBy() {
        return this;
    }

}
