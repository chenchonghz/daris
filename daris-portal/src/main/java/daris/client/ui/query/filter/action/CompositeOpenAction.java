package daris.client.ui.query.filter.action;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.gui.window.Window;
import daris.client.model.query.filter.CompositeFilter;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.item.FilterItem.HasComposite;

public class CompositeOpenAction extends ActionInterface<CompositeFilter> {

    private HasComposite _hc;

    public CompositeOpenAction(HasComposite hasComposite, Window owner, int width, int height) {
        super("composite filter", null, owner, width, height);
        _hc = hasComposite;
    }

    public CompositeOpenAction(HasComposite hasComposite, Window owner, double width, double height) {
        this(hasComposite, owner, owner == null ? (int) (com.google.gwt.user.client.Window.getClientWidth() * width)
                : (int) (((arc.gui.gwt.widget.window.Window) owner).width() * width),
                owner == null ? (int) (com.google.gwt.user.client.Window.getClientHeight() * height)
                        : (int) (((arc.gui.gwt.widget.window.Window) owner).height() * height));
    }

    @Override
    public void createInterface(InterfaceCreateHandler ch) {

        ch.created(CompositeFilterForm.create(_hc));
    }

    @Override
    public String actionName() {
        return _hc.hadBy().editable() ? "Edit" : "View";
    }

    
    @Override
    public String actionButtonName() {
        return "OK";
    }
}
