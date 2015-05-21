package daris.client.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.IDUtil;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class DObjectDestroy extends ObjectMessage<Null> {

	public static final String SERVICE_NAME = "om.pssd.object.destroy";

	private String _cid;

	public DObjectDestroy(DObjectRef o) {
		assert o.id() != null;
		_cid = o.id();
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
	    w.add("cid", _cid);
		if (IDUtil.isProjectId(_cid)) {
			w.add("destroy", true);
		}
	}

	@Override
	protected String messageServiceName() {
		return SERVICE_NAME;
	}

	@Override
	protected Null instantiate(XmlElement xe) throws Throwable {
		return new Null();
	}

	@Override
	protected String objectTypeName() {
		DObject.Type type = IDUtil.typeFromId(_cid);
		return type == null ? null : type.toString();
	}

	@Override
	protected String idToString() {
		return _cid;
	}

}
