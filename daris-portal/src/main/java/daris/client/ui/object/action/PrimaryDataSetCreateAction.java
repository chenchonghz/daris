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

public class PrimaryDataSetCreateAction extends ActionInterface<DObject> {

	private List<LocalFile> _files;

	public PrimaryDataSetCreateAction(List<LocalFile> files, DObjectRef po,
			Window owner) {

		this(files, po, owner, WindowUtil.windowWidth(owner, 0.8), WindowUtil
				.windowHeight(owner, 0.8));
	}

	public PrimaryDataSetCreateAction(List<LocalFile> files, DObjectRef po,
			Window owner, int width, int height) {

		super(po.referentTypeName(), po, new ArrayList<ActionPrecondition>(),
				owner, width, height);
		_files = files;
		preconditions().add(new CanCreatePrecondition(po));

	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {
		ch.created(new PrimaryDataSetCreateForm((DObjectRef) object(), _files));
	}

	@Override
	public String actionName() {
		return "Create/Import primary data set";
	}

	@Override
	public String actionButtonName() {
		return "Create";
	}

	@Override
	public String title() {
		DObjectRef o = (DObjectRef) object();
		return "Create/Import primary data set for " + o.referentTypeName()
				+ " " + o.id();
	}
}