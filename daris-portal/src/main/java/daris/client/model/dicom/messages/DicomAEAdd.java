package daris.client.model.dicom.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.dicom.DicomAE;

public class DicomAEAdd extends ObjectMessage<Null> {

	private DicomAE _ae;

	public DicomAEAdd(DicomAE ae) {
		_ae = ae;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("access", _ae.access().toString());
		if (_ae.name() != null) {
			w.push("ae", new String[] { "name", _ae.name() });
		} else {
			w.push("ae");
		}
		w.add("aet", _ae.aet());
		w.add("host", _ae.host());
		w.add("port", _ae.port());
		w.pop();
	}

	@Override
	protected String messageServiceName() {
		return "om.pssd.dicom.ae.add";
	}

	@Override
	protected Null instantiate(XmlElement xe) throws Throwable {
		return null;
	}

	@Override
	protected String objectTypeName() {
		return null;
	}

	@Override
	protected String idToString() {
		return null;
	}


}
