package daris.client.model.archive;

public class ImageEntry {

    private ArchiveEntry _entry;
    private boolean _lossless;
    private String _outputUrl;

    public ImageEntry(ArchiveEntry entry, boolean lossless) {
        _entry = entry;
        _lossless = lossless;
    }

    public String entryName() {
        return _entry.name();
    }

    public String entryFileName() {
        return _entry.fileName();
    }

    public String imageFileName() {
        String ext = _lossless ? ".png" : ".jpg";
        String fileName = entryFileName();
        if (!fileName.endsWith(ext) && !fileName.endsWith(ext.toUpperCase())) {
            fileName = fileName + ext;
        }
        return fileName;
    }

    public String outputUrl() {
        return _outputUrl;
    }

    public void setOutputUrl(String outputUrl) {
        _outputUrl = outputUrl;
    }
}
