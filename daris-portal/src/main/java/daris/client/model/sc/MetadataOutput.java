package daris.client.model.sc;

public enum MetadataOutput {
    none, mediaflux;
    public static MetadataOutput fromString(String metadataOutput) {
        MetadataOutput[] vs = values();
        for (MetadataOutput v : vs) {
            if (v.name().equalsIgnoreCase(metadataOutput)) {
                return v;
            }
        }
        return none;
    }
}
