package daris.client.ui.object.action;

import java.util.List;
import java.util.Vector;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.window.Window;
import arc.mf.client.file.LocalFile;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.ui.util.WindowUtil;

public class AttachmentAddAction extends ActionInterface<DObject> {

	private List<LocalFile> _files;

	public AttachmentAddAction(List<LocalFile> files, DObjectRef o, Window owner) {

		this(files, o, owner, WindowUtil.windowWidth(owner, 0.8), WindowUtil.windowHeight(owner, 0.8));
	}

	public AttachmentAddAction(List<LocalFile> files, DObjectRef o, Window owner, int width, int height) {

		super(o.referentTypeName(), o, null, owner, width, height);
		_files = files;
	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {
		ch.created(new AttachmentAddForm((DObjectRef) object(), _files));
	}

	@Override
	public String actionName() {
		return "Add attachment";
	}

	@Override
	public String actionButtonName() {
		return "Add";
	}

	@Override
	public String title() {
		DObjectRef o = (DObjectRef) object();
		return "Add attachment to " + o.referentTypeName() + " " + o.id();
	}
}