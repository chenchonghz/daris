package daris.client.model.object;

import arc.mf.client.xml.XmlElement;

public class DataContent {

    private String _atime;
    private long _atimeMillisec;
    private String _ctime;
    private long _ctimeMillisec;
    private String _ltype;
    private String _type;
    private String _ext;
    private long _size;
    private String _sizeUnits;
    private String _csum;
    private int _csumBase;
    private String _store;
    private String _url;

    public DataContent(XmlElement de) throws Throwable {

        if (de.element("atime") != null) {
            _atime = de.value("atime");
            _atimeMillisec = de.longValue("atime/@millisec");
        }
        if (de.element("ctime") != null) {
            _ctime = de.value("ctime");
            _ctimeMillisec = de.longValue("ctime/@millisec");
        }
        _size = de.longValue("size", 0);
        _csumBase = de.intValue("csum/@base", 16);

        _type = de.value("type");
        _ltype = de.value("ltype");
        _ext = de.value("type/@ext");
        _sizeUnits = de.value("size/@units");
        _csum = de.value("csum");
        _store = de.value("store");
        _url = de.value("url");

    }

    public String atime() {

        return _atime;
    }

    public long atimeMillisecs() {

        return _atimeMillisec;
    }

    public String ctime() {

        return _ctime;
    }

    public long ctimeMillisecs() {

        return _ctimeMillisec;
    }

    public String mimeType() {

        return _type;
    }

    public String logicalMimeType() {
        return _ltype;
    }

    public String extension() {

        return _ext;
    }

    public long size() {

        return _size;
    }

    public String sizeUnits() {

        return _sizeUnits;
    }

    public String checksum() {

        return _csum;
    }

    public int checksumBase() {

        return _csumBase;
    }

    public String store() {

        return _store;
    }

    public String url() {

        return _url;
    }

    public boolean isSupportedArchive() {
        return daris.client.model.archive.ArchiveRegistry.isSupportedArchive(mimeType());
    }
}
