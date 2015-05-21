package nig.mf.plugin.pssd.sc;

public enum MetadataOutput {
    none, mediaflux;

    public static MetadataOutput fromString(String metadataOutput) {
        if (metadataOutput != null) {
            MetadataOutput[] vs = values();
            for (MetadataOutput v : vs) {
                if (v.name().equalsIgnoreCase(metadataOutput)) {
                    return v;
                }
            }
        }
        return none;
    }

}
