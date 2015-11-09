package daris.client.model.archive;

import arc.mf.client.xml.XmlElement;

public class ImageEntry extends ArchiveEntry {

    private long _outputSize;
    private String _outputFormat;
    private String _outputUrl;

    public ImageEntry(XmlElement ee) throws Throwable {
        super(ee);
        _outputSize = ee.longValue("@output-size", size());
        _outputFormat = ee.stringValue("@output-format", fileExtension());
    }

    public long outputSize() {
        return _outputSize;
    }

    public String outputFormat() {
        return _outputFormat;
    }

    public String outputFileName() {
        return fileName() + "." + _outputFormat;
    }

    public String outputUrl() {
        return _outputUrl;
    }

    public void setOutputUrl(String outputUrl) {
        _outputUrl = outputUrl;
    }
}
