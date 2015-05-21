package daris.client.model.object.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.Attachment;

public class ObjectAttachmentGet extends ObjectMessage<Boolean> {

	private String _cid;
	private List<Attachment> _attachments;

	public ObjectAttachmentGet(String cid) {
		this(cid, (List<Attachment>) null);
	}

	public ObjectAttachmentGet(String cid, Attachment attachment) {
		this(cid, new ArrayList<Attachment>(Arrays.asList(attachment)));
	}

	public ObjectAttachmentGet(String cid, List<Attachment> attachments) {

		_cid = cid;
		_attachments = attachments;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _cid);
		if (_attachments != null) {
			for (Attachment a : _attachments) {
				w.add("aid", a.assetId());
			}
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.attachment.get";
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

	@Override
	protected int numberOfOutputs() {

		return 1;
	}

	@Override
	protected void process(Boolean o, List<Output> outputs) {

		if (o) {
			if (outputs != null) {
				for (Output output : outputs) {
					String filename = _cid;
					if (_attachments == null) {
						filename += "_attachments.zip";
					} else {
						if (_attachments.size() == 1) {
							Attachment a = _attachments.get(0);
							filename += "_" + a.name();
							if (a.extension() != null) {
								if (!filename.endsWith("." + a.extension())) {
									filename += "." + a.extension();
								}
							}
						} else {
							filename += "_attachments.zip";
						}
					}
					output.download(filename);
				}
			}
		}
	}

}
