package daris.client.model.dataset;

public enum SourceType {
    PRIMARY, DERIVATION;
    public String typeName() {
        return name().toLowerCase();
    }

    public final String toString() {
        return name().toLowerCase();
    }

    public static SourceType fromString(String s,
            SourceType defaultSourceType) {
        if (PRIMARY.typeName().equalsIgnoreCase(s)) {
            return PRIMARY;
        } else if (DERIVATION.typeName().equalsIgnoreCase(s)) {
            return DERIVATION;
        } else {
            return defaultSourceType;
        }
    }
}
