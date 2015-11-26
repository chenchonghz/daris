package daris.client.model.transcode;

public class Transcode {

    public final String fromMimeType;
    public final String toMimeType;

    public Transcode(String fromMimeType, String toMimeType) {
        this.fromMimeType = fromMimeType;
        this.toMimeType = toMimeType;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && (o instanceof Transcode)) {
            Transcode t = (Transcode) o;
            return t.fromMimeType == fromMimeType && t.toMimeType == toMimeType;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fromMimeType.concat(toMimeType).hashCode();
    }
}
