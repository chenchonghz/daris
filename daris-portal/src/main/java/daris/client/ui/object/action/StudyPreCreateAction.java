package daris.client.ui.object.action;

import java.util.ArrayList;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.window.Window;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.ui.util.WindowUtil;

/**
 * Class to manage the pre-creation of Studies. Pre-created Studies are made
 * with just the meta-data that the Method pre-specifies as given
 * 
 * 
 * @author nebk
 * 
 */
public class StudyPreCreateAction extends ActionInterface<DObject> {

	public StudyPreCreateAction(DObjectRef po, Window owner) {

		this(po, owner, WindowUtil.windowWidth(owner, 0.8), WindowUtil
				.windowHeight(owner, 0.8));
	}

	public StudyPreCreateAction(DObjectRef po, Window owner, int width,
			int height) {

		super(po.referentTypeName(), po, new ArrayList<ActionPrecondition>(),
				owner, width, height);
		preconditions().add(new CanCreatePrecondition(po));
		preconditions().add(new HasStudyStepsPrecondition(po));
	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {

		// Create the form. This form is purely meta-data, no
		// data upload, but has some specialised value checking
		ch.created(new StudyPreCreateForm((DObjectRef) object()));
	}

	@Override
	public String actionName() {
		return "Pre-create Studies";
	}

	@Override
	public String actionButtonName() {
		return "Pre-create";
	}

	@Override
	public String title() {
		DObjectRef o = (DObjectRef) object();
		return "Pre-create Studies for " + o.referentTypeName() + " " + o.id();
	}
}
