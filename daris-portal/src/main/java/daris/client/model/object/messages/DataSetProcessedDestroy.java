package daris.client.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.IDUtil;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class DataSetProcessedDestroy extends ObjectMessage<XmlElement> {

	public static final String SERVICE_NAME = "om.pssd.dataset.processed.destroy";

	private String _cid;

	public DataSetProcessedDestroy(DObjectRef o) {
		_cid = o.id();
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("cid", _cid);
	}

	@Override
	protected String messageServiceName() {
		return SERVICE_NAME;
	}

	@Override
	protected XmlElement instantiate(XmlElement xe) throws Throwable {
		return xe == null ? null : xe.element("destroyed");
	}

	@Override
	protected String objectTypeName() {
		DObject.Type t = IDUtil.typeFromId(_cid);
		return t == null ? null : t.toString();
	}

	@Override
	protected String idToString() {
		return _cid;
	}

}
