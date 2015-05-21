package daris.client.model.archive;

import arc.mf.client.xml.XmlElement;

public class ArchiveEntry {

	private String _name;
	private int _index;
	private long _size;

	public ArchiveEntry(XmlElement e) throws Throwable {
		_name = e.value();
		_index = e.intValue("@index", -1);
		_size = e.longValue("@size", -1);
	}

	public String name() {
		return _name;
	}

	public int index() {
		return _index;
	}

	public long size() {
		return _size;
	}
}
