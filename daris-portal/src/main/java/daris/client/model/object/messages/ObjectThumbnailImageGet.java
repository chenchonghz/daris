package daris.client.model.object.messages;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.Thumbnail;

public class ObjectThumbnailImageGet extends ObjectMessage<Null> {
	private Thumbnail _thumbnail;
	private int _index;

	public ObjectThumbnailImageGet(Thumbnail thumbnail, int index) {
		_thumbnail = thumbnail;
		_index = index;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", new String[] { "idx", Integer.toString(_index) }, _thumbnail.assetId());
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.thumbnail.image.get";
	}

	@Override
	protected Null instantiate(XmlElement xe) throws Throwable {

		return new Null();
	}

	@Override
	protected String objectTypeName() {

		return "Thumbnail image";
	}

	@Override
	protected String idToString() {

		return _thumbnail.assetId() + "_" + _index;
	}

	@Override
	protected int numberOfOutputs() {

		return 1;
	}

	@Override
	protected void process(Null o, List<Output> outputs) {

		if (outputs == null || outputs.isEmpty()) {
			return;
		}

		for (Output output : outputs) {
			_thumbnail.images().get(_index).setUrl(output.url());
		}
	}

}
