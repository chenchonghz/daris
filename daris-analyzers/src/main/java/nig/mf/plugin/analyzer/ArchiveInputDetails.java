package nig.mf.plugin.analyzer;

import java.io.File;

public class ArchiveInputDetails {

    private String _type;
    private String _ltype;
    private File[] _files;

    public ArchiveInputDetails(File[] files, String ctype, String lctype) {
        _files = files;
        _type = ctype;
        _ltype = lctype;
    }

    public File[] files() {
        return _files;
    }

    public void setFiles(File[] files) {
        _files = files;
    }

    public String type() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public String logicalType() {
        return _ltype;
    }

    public void setLogicalType(String ltype) {
        _ltype = ltype;
    }
}
