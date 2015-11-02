package daris.client.model.archive;

import arc.mf.client.xml.XmlElement;

public class ArchiveEntry {

    private String _name;
    private long _idx;
    private long _size;

    public ArchiveEntry(XmlElement e) throws Throwable {
        _name = e.value();
        _idx = e.longValue("@idx", 1);
        _size = e.longValue("@size", -1);
    }

    public String name() {
        return _name;
    }

    public long ordinal() {
        return _idx;
    }

    public long size() {
        return _size;
    }
}
