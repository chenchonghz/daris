package daris.client.model.archive.messages;

import java.util.List;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.model.archive.ImageEntry;

public class ArchiveContentImageGet extends ObjectMessage<ImageEntry> {

	private ArchiveEntryCollectionRef _arc;
	private ArchiveEntry _entry;
	private boolean _lossless;
	private Integer _size;

	public ArchiveContentImageGet(ArchiveEntryCollectionRef arc, ArchiveEntry entry) {
		this(arc, entry, false, null);
	}

	public ArchiveContentImageGet(ArchiveEntryCollectionRef arc, ArchiveEntry entry, boolean lossless, Integer size) {
		_arc = arc;
		_entry = entry;
		_lossless = lossless;
		_size = size;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		if (_arc.assetId() != null) {
			w.add("id", _arc.assetId());
		} else {
			w.add("cid", _arc.citeableId());
		}
		// _idx starts from 1.
		w.add("idx", _entry.ordinal());
		if (_entry.name() != null) {
			w.add("name", _entry.name());
		}
		w.add("lossless", _lossless);
		if (_size != null) {
			w.add("size", _size);
		}
	}

	@Override
	protected String messageServiceName() {
		return "daris.archive.content.image.get";
	}

	@Override
	protected ImageEntry instantiate(XmlElement xe) throws Throwable {
		return new ImageEntry(_entry, _lossless);
	}

	@Override
	protected String objectTypeName() {
		return null;
	}

	@Override
	protected String idToString() {
		return String.valueOf(_entry.ordinal());
	}

	@Override
	protected int numberOfOutputs() {
		return 1;
	}

	@Override
	protected void process(ImageEntry ae, List<Output> outputs) throws Throwable {
		Output output = outputs.get(0);
		ae.setOutputUrl(output.url());
	}

}
