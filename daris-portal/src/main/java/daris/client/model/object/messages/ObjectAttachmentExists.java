package daris.client.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ObjectAttachmentExists extends ObjectMessage<Boolean> {

	private String _cid;
	private String _proute;
	private String _name;

	public ObjectAttachmentExists(String cid, String proute, String aName) {
		_cid = cid;
		_proute = proute;
		_name = aName;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		if (_proute != null) {
			w.add("id", new String[] { "proute", _proute }, _cid);
		} else {
			w.add("id", _cid);
		}
		w.add("name", _name);
	}

	@Override
	protected String messageServiceName() {
		return "om.pssd.object.attachment.exists";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {
		if (xe != null) {
			return xe.booleanValue("exists");
		}
		return null;
	}

	@Override
	protected String objectTypeName() {
		return "attachment";
	}

	@Override
	protected String idToString() {
		return _cid + " - " + _name;
	}

}
