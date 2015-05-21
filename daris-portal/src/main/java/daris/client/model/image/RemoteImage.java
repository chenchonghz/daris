package daris.client.model.image;

public abstract class RemoteImage {

    public static final String MIME_TYPE_PNG = "image/png";
    public static final String EXT_PNG = "png";
    public static final String MIME_TYPE_JPG = "image/jpeg";
    public static final String EXT_JPG = "jpg";

    private String _assetId;
    private int _index;
    private String _url;
    private boolean _lossless;

    protected RemoteImage(String assetId, int index, String url, boolean lossless) {
        _index = index;
        _url = url;
        _lossless = lossless;
    }

    public String assetId() {
        return _assetId;
    }

    public int index() {
        return _index;
    }

    public String url() {
        return _url;
    }

    public void setUrl(String url) {
        _url = url;
    }

    public boolean lossless() {
        return _lossless;
    }

    public final String mimeType() {
        return _lossless ? MIME_TYPE_PNG : MIME_TYPE_JPG;
    }

    public final String extension() {
        return _lossless ? EXT_PNG : EXT_JPG;
    }

}
