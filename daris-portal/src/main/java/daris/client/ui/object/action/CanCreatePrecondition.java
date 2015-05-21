package daris.client.ui.object.action;

import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.CanCreate;

public class CanCreatePrecondition implements ActionPrecondition {

    private DObjectRef _parent;

    public CanCreatePrecondition(DObjectRef parent) {

        _parent = parent;
    }

    @Override
    public EvaluatePrecondition evaluate() {

        return EvaluatePrecondition.BEFORE_INTERACTION;
    }

    @Override
    public String description() {

        return "Check if the user can create a child object for the parent object"
                + (_parent.id() == null ? "." : (" " + _parent.id() + "."));
    }

    @Override
    public void execute(final ActionPreconditionListener l) {

        new CanCreate(_parent.id()).send(new ObjectMessageResponse<Boolean>() {

            @Override
            public void responded(Boolean canCreate) {

                if (canCreate) {
                    l.executed(ActionPreconditionOutcome.PASS, "You have privilege to create child object for "
                            + _parent);
                } else {
                    l.executed(ActionPreconditionOutcome.FAIL, "You do not have privilege to create child object for "
                            + _parent);
                }
            }
        });
    }

}
