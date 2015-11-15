package daris.client.model;

public enum DataUse {

    SPECIFIC, EXTENDED, UNSPECIFIED;

    @Override
    public final String toString() {

        return super.toString().toLowerCase();
    }

    public static DataUse fromString(String str) {
        if (str != null) {
            DataUse[] vs = values();
            for (DataUse v : vs) {
                if (v.name().equalsIgnoreCase(str)) {
                    return v;
                }
            }
        }
        return null;
    }

}