package daris.client.ui.dicom.action;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.gui.window.Window;
import daris.client.model.dicom.DicomAE;
import daris.client.ui.dicom.DicomAEForm;

public class DicomAEAddAction extends ActionInterface<DicomAE> {

	public DicomAEAddAction(Window owner) {
		this(owner, 380, 300);
	}

	public DicomAEAddAction(Window owner, int width, int height) {
		super("DICOM AE", null, owner, width, height);
	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {
		ch.created(new DicomAEForm() {
			public void added(){
				DicomAEAddAction.this.added();
			}
		});
	}

	@Override
	public String actionName() {
		return "Add";
	}

	public void added() {

	}

}
