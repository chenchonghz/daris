package daris.client.ui.exmethod;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.gui.window.Window;
import daris.client.model.exmethod.ExMethodStep;

public class StepEditAction extends ActionInterface<ExMethodStep> {

    private MethodAndStep _mas;
    private ExMethodStep _step;

    public StepEditAction(MethodAndStep mas, ExMethodStep step, Window owner) {
        super("ex-method step", null, owner, (int) (com.google.gwt.user.client.Window.getClientWidth() * 0.6),
                (int) (com.google.gwt.user.client.Window.getClientHeight() * 0.6));
        _mas = mas;
        _step = step;
    }

    @Override
    public void createInterface(InterfaceCreateHandler ch) {
        ch.created(new StepEditForm(_mas, _step));
    }

    @Override
    public String actionName() {
        return "Edit step " + _step.stepPath() + " of ex-method " + _mas.exMethod().id();
    }

    public String title() {
        return "Edit step " + _step.stepPath() + " of ex-method " + _mas.exMethod().id();
    }

    public String actionButtonName() {
        return "Save";
    }

}
