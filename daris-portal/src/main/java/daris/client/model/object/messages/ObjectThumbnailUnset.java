package daris.client.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;

public class ObjectThumbnailUnset extends ObjectMessage<Null> {

	private String _cid;

	public ObjectThumbnailUnset(String cid) {

		_cid = cid;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _cid);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.thumbnail.unset";
	}

	@Override
	protected Null instantiate(XmlElement xe) throws Throwable {

		return new Null();
	}

	@Override
	protected String objectTypeName() {

		return "thumbnail";
	}

	@Override
	protected String idToString() {

		return _cid;
	}

}