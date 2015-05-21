package daris.client.ui.dicom.action;

import arc.gui.InterfaceComponent;
import arc.gui.InterfaceCreateHandler;
import arc.gui.gwt.widget.label.Label;
import arc.gui.object.action.ActionInterface;
import arc.gui.window.Window;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.dicom.DicomAE;
import daris.client.model.dicom.messages.DicomAERemove;

public class DicomAERemoveAction extends ActionInterface<DicomAE> {

	private DicomAE _ae;

	public DicomAERemoveAction(DicomAE ae, Window owner, int width, int height) {
		super("DICOM AE", null, owner, width, height);
		_ae = ae;
	}

	@Override
	public void createInterface(InterfaceCreateHandler ch) {
		ch.created(new IC(_ae));
	}

	@Override
	public String actionName() {
		return "Remove";
	}

	private static class IC implements InterfaceComponent, AsynchronousAction {

		private DicomAE _ae;

		public IC(DicomAE ae) {
			_ae = ae;
		}

		@Override
		public void execute(final ActionListener l) {
			new DicomAERemove(_ae).send(new ObjectMessageResponse<Null>() {

				@Override
				public void responded(Null r) {
					l.executed(true);
				}
			});
		}

		@Override
		public Widget gui() {
			return new Label("Are you sure you want to remove DICOM AE: " + _ae.toString());
		}

	}

}
