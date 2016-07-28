package daris.client.ui.dicom.action;

import arc.gui.dialog.DialogProperties;
import arc.gui.gwt.widget.dialog.Dialog;
import arc.mf.client.util.Action;
import daris.client.model.dicom.sr.StructuredReportRef;

public class DicomSRExportAction implements Action {

	private StructuredReportRef _o;

	private arc.gui.window.Window _owner;

	public DicomSRExportAction(StructuredReportRef o, arc.gui.window.Window owner) {
		_o = o;
		_owner = owner;
	}

	@Override
	public void execute() {
		DicomSRExportForm form = new DicomSRExportForm(_o);
		DialogProperties dp = new DialogProperties("Export DICOM Structured Report", form);
		dp.setButtonAction(form);
		dp.setCancelLabel("Cancel");
		dp.setButtonLabel("Export");
		dp.setOwner(_owner);
		dp.setSize(480, 320);
		Dialog.postDialog(dp).show();
	}

}
