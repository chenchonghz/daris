package daris.client.model.archive;

public class ImageEntry {

    private ArchiveEntry _entry;
    private boolean _lossless;
    private String _outputUrl;

    public ImageEntry(ArchiveEntry entry, boolean lossless) {
        _entry = entry;
        _lossless = lossless;
        _outputUrl = null;
    }

    public int ordinal() {
        return _entry.ordinal();
    }

    public String name() {
        return _entry.name();
    }

    public String imageFileName() {
        String fileName = _entry.fileName();
        String fileExt = _entry.fileExtension();
        String imgExt = _lossless ? "png" : "jpg";
        if (!imgExt.equalsIgnoreCase(fileExt)) {
            return fileName.replaceAll("\\." + fileExt + "$", "." + imgExt);
        } else {
            return fileName;
        }
    }

    public String outputUrl() {
        return _outputUrl;
    }

    public void setOutputUrl(String outputUrl) {
        _outputUrl = outputUrl;
    }

}
