package daris.client.ui.object.action;

import java.util.ArrayList;
import java.util.List;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.window.Window;
import arc.mf.client.file.LocalFile;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.ui.util.WindowUtil;

public class DerivedDataSetCreateAction extends ActionInterface<DObject> {

	private List<LocalFile> _files;
	private DObjectRef _input;

	public DerivedDataSetCreateAction(List<LocalFile> files, DObjectRef po,
			DObjectRef input, Window owner) {

		this(files, po, input, owner, WindowUtil.windowWidth(owner, 0.8),
				WindowUtil.windowHeight(owner, 0.8));
	}

	public DerivedDataSetCreateAction(List<LocalFile> files, DObjectRef po,
			DObjectRef input, Window owner, int width, int height) {

		super(po.referentTypeName(), po, new ArrayList<ActionPrecondition>(),
				owner, width, height);
		_input = input;
		_files = files;
		preconditions().add(new CanCreatePrecondition(po));
	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {
		ch.created(new DerivedDataSetCreateForm((DObjectRef) object(), _input,
				_files));
	}

	@Override
	public String actionName() {
		return "Create/Import derived data set";
	}

	@Override
	public String actionButtonName() {
		return "Import";
	}

	@Override
	public String title() {
		DObjectRef o = (DObjectRef) object();
		return "Create/Import derived data set for " + o.referentTypeName()
				+ " " + o.id();
	}
}