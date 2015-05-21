package daris.client.model.object.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.Attachment;

public class ObjectAttachmentRemove extends ObjectMessage<Boolean> {

	private String _cid;
	private List<Attachment> _attachments = null;

	public ObjectAttachmentRemove(String cid, List<Attachment> attachments) {

		_cid = cid;
		_attachments = attachments;
	}

	public ObjectAttachmentRemove(String id, boolean detachAll) {

		assert detachAll == true;
		_cid = id;
		_attachments = null;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _cid);
		for (Attachment attachment : _attachments) {
			w.add("aid", attachment.assetId());
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.attachment.remove";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return xe != null;
	}

	@Override
	protected String objectTypeName() {

		return "attachment";
	}

	@Override
	protected String idToString() {

		return _cid;
	}

}
