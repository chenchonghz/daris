package nig.mf.plugin.pssd.sc;

public enum DeliveryMethod {

    download, deposit;

    public static DeliveryMethod fromString(String method) {
        if (method != null) {
            DeliveryMethod[] vs = values();
            for (DeliveryMethod v : vs) {
                if (v.name().equalsIgnoreCase(method)) {
                    return v;
                }
            }
        }
        return null;
    }
}
