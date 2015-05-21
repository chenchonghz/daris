package daris.client.ui.object.action;

import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.exmethod.StepItem;
import daris.client.model.exmethod.messages.ExMethodStudyStepFind;
import daris.client.model.object.DObjectRef;

import java.util.List;

/**
 * Can this ExMethod pre-create Studies ? Only if it has a Study making step
 * 
 * @author nebk
 * 
 */
public class HasStudyStepsPrecondition implements ActionPrecondition {

    private DObjectRef _root;

    public HasStudyStepsPrecondition(DObjectRef root) {

        _root = root;
    }

    @Override
    public EvaluatePrecondition evaluate() {

        return EvaluatePrecondition.BEFORE_INTERACTION;
    }

    @Override
    public String description() {

        return "Checking if object " + _root.id() == null ? "" : _root.id() + " contains steps that make Studies.";
    }

    @Override
    public void execute(final ActionPreconditionListener l) {

        new ExMethodStudyStepFind(_root.id(), _root.proute()).send(new ObjectMessageResponse<List<StepItem>>() {

            @Override
            public void responded(List<StepItem> steps) {
                if (steps != null) {
                    l.executed(ActionPreconditionOutcome.PASS, "The object " + (_root.id() == null ? "" : _root.id())
                            + " contains Study making steps.");
                } else {
                    l.executed(ActionPreconditionOutcome.FAIL, "The object " + (_root.id() == null ? "" : _root.id())
                            + " contains no Study making steps.");
                }
            }
        });

    }

}