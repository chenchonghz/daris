package daris.client.model.archive;

import arc.mf.client.xml.XmlElement;

public class ArchiveEntry {

    private String _name;
    private int _idx;
    private long _size;

    public ArchiveEntry(XmlElement e) throws Throwable {
        _name = e.value();
        _idx = e.intValue("@idx", 1);
        _size = e.longValue("@size", -1);
    }

    public String name() {
        return _name;
    }

    public int ordinal() {
        return _idx;
    }

    public long size() {
        return _size;
    }

    public String fileName() {
        if (_name == null) {
            return null;
        }
        int idx = _name.lastIndexOf('/');
        if (idx == -1) {
            return _name;
        }
        return _name.substring(idx + 1);
    }

    public String fileExtension() {
        if (_name == null) {
            return null;
        }
        int idx = _name.lastIndexOf('.');
        if (idx == -1) {
            return null;
        }
        return _name.substring(idx + 1);
    }
}
