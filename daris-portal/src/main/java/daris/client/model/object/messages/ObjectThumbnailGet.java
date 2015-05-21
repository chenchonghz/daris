package daris.client.model.object.messages;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.Thumbnail;

public class ObjectThumbnailGet extends ObjectMessage<Thumbnail> {
	private String _cid;
	private boolean _download = false;

	public ObjectThumbnailGet(String cid, boolean download) {
		_cid = cid;
		_download = download;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _cid);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.thumbnail.get";
	}

	@Override
	protected Thumbnail instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			XmlElement te = xe.element("thumbnail");
			if (te != null) {
				return new Thumbnail(te);
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "Thumbnail";
	}

	@Override
	protected String idToString() {

		return _cid;
	}

	@Override
	protected int numberOfOutputs() {

		return _download ? 1 : 0;
	}

	@Override
	protected void process(Thumbnail t, List<Output> outputs) {

		if (outputs == null || outputs.isEmpty() || !_download) {
			return;
		}

		for (Output output : outputs) {
			String filename = _cid + "_thumbnails." + Thumbnail.EXTENSION;
			output.download(filename);
		}
	}

}
