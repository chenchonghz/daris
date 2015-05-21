package daris.client.model.sc;


public enum DeliveryMethod {

    download, deposit;

    public static DeliveryMethod fromString(String method) {

        if (method != null) {
            DeliveryMethod[] vs = values();
            for (DeliveryMethod v : vs) {
                if (v.name().equals(method)) {
                    return v;
                }
            }
        }
        return null;
    }

}
