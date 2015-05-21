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

public class DicomIngestAction extends ActionInterface<DObject> {

	private List<LocalFile> _files;

	public DicomIngestAction(List<LocalFile> files, DObjectRef po, Window owner) {

		this(files, po, owner, WindowUtil.windowWidth(owner, 0.8), WindowUtil.windowHeight(owner, 0.8));
	}

	public DicomIngestAction(List<LocalFile> files, DObjectRef po, Window owner, int width, int height) {

		super(po.referentTypeName(), po, null, owner, width, height);
		_files = files;
	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {
		ch.created(new DicomIngestForm((DObjectRef) object(), _files));
	}

	@Override
	public String actionName() {
		return "Create/Import DICOM data sets";
	}

	@Override
	public String actionButtonName() {
		return "Proceed";
	}

	@Override
	public String title() {
		DObjectRef o = (DObjectRef) object();
		return "Create/Import DICOM data sets (" + o.referentTypeName() + ": " + o.id() + ")";
	}
}
