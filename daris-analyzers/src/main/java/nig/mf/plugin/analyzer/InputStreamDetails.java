package nig.mf.plugin.analyzer;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class InputStreamDetails {

    public static final int DEFAULT_BUFFER_SIZE = 1024;

    private InputStream _stream;
    private long _length;
    private String _type;
    private String _extension;
    private String _ltype;

    public InputStreamDetails(InputStream stream, long length, String type, String extension, String ltype,
            boolean enableMarkSupport) {
        _stream = stream;
        _length = length;
        _type = type;
        _extension = extension;
        _ltype = ltype;
        if (enableMarkSupport) {
            enableMarkSupport();
        }
    }

    public InputStream stream() {
        return _stream;
    }

    public void setStream(InputStream stream) {
        _stream = stream;
    }

    public long length() {
        return _length;
    }

    public void setLength(long length) {
        _length = length;
    }

    public String type() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public String extension() {
        return _extension;
    }

    public void setExtension(String extension) {
        _extension = extension;
    }

    public String logicalType() {
        return _ltype;
    }

    public void setLogicalType(String ltype) {
        _ltype = ltype;
    }

    public void enableMarkSupport(int bufferSize) {
        if (_stream != null && !_stream.markSupported()) {
            _stream = new BufferedInputStream(_stream, bufferSize);
        }
    }

    public void enableMarkSupport() {
        enableMarkSupport(DEFAULT_BUFFER_SIZE);
    }

}
