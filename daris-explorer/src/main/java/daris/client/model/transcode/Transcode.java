package daris.client.model.transcode;

public class Transcode {

    public final String fromMimeType;
    public final String toMimeType;

    public Transcode(String fromMimeType, String toMimeType) {
        this.fromMimeType = fromMimeType;
        this.toMimeType = toMimeType;
    }
}
