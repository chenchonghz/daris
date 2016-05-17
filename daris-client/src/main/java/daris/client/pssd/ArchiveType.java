package daris.client.pssd;

public enum ArchiveType {

    AAR, ZIP;

    public String fileExtension() {
        return name().toLowerCase();
    }

    public String toString() {
        return name().toLowerCase();
    }

    public String mimeType() {
        switch (this) {
        case AAR:
            return "application/arc-archive";
        case ZIP:
            return "application/zip";
        default:
            return "application/arc-archive";
        }
    }

    public static ArchiveType fromString(String s, ArchiveType defaultType) {
        if (s != null) {
            ArchiveType[] vs = values();
            for (ArchiveType v : vs) {
                if (s.equalsIgnoreCase(v.name())) {
                    return v;
                }
            }
        }
        return defaultType;
    }

}
