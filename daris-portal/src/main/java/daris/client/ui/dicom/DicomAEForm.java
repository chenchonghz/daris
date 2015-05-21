package daris.client.ui.dicom;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.dtype.EnumerationType;
import arc.mf.dtype.IntegerType;
import arc.mf.dtype.StringType;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.user.client.ui.Widget;

import daris.client.model.dicom.DicomAE;
import daris.client.model.dicom.DicomAEAccessEnum;
import daris.client.model.dicom.messages.DicomAEAdd;

public class DicomAEForm extends ValidatedInterfaceComponent implements AsynchronousAction {

	private Form _form;
	private Field<DicomAE.Access> _accessField;
	private Field<String> _nameField;
	private Field<String> _aetField;
	private Field<String> _hostField;
	private Field<Integer> _portField;

	public DicomAEForm() {
		_form = new Form(FormEditMode.CREATE);
		_accessField = new Field<DicomAE.Access>(new FieldDefinition("Access", new EnumerationType<DicomAE.Access>(
				new DicomAEAccessEnum()), "The access type of the AE.", "The access type of the AE", 1, 1));
		_accessField.setValue(DicomAE.Access.PRIVATE);
		_form.add(_accessField);

		_nameField = new Field<String>(new FieldDefinition("Name", StringType.DEFAULT, "The name of the AE.",
				"The name of the AE", 1, 1));
		_form.add(_nameField);

		_aetField = new Field<String>(new FieldDefinition("AE Title", StringType.DEFAULT,
				"The title of the application entity", "The AE title", 1, 1));
		_form.add(_aetField);
		_hostField = new Field<String>(new FieldDefinition("Host", StringType.DEFAULT,
				"The host address of the application entity.", "The host", 1, 1));
		_form.add(_hostField);
		_portField = new Field<Integer>(new FieldDefinition("Port", new IntegerType(0, 65535), "The port number.",
				"The port number.", 1, 1));
		_portField.setValue(DicomAE.DEFAULT_DICOM_PORT);
		_form.add(_portField);

		addMustBeValid(_form);
	}

	public DicomAE getAE() {
		return new DicomAE(name(), aet(), host(), port(), access());
	}

	public String aet() {
		return _aetField.value();
	}

	public String host() {
		return _hostField.value();
	}

	public int port() {
		return _portField.value();
	}

	public DicomAE.Access access() {
		return _accessField.value();
	}
	
	public String name(){
		return _nameField.value();
	}

	@Override
	public Widget gui() {
		_form.render();
		return _form;
	}

	@Override
	public void execute(final ActionListener l) {
		new DicomAEAdd(getAE()).send(new ObjectMessageResponse<Null>() {
			public void responded(Null r) {
				l.executed(true);
				added();
			}
		});
	}
	
	public void added(){
		
	}
}
