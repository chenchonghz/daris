package nig.mf.plugin.pssd.servlets;

public enum Disposition {
    attachment, inline;
    public static Disposition parse(String value) {
        if (value != null) {
            if (attachment.name().equalsIgnoreCase(value)) {
                return attachment;
            } else if (inline.name().equalsIgnoreCase(value)) {
                return inline;
            }
        }
        return null;
    }

    public static Disposition parse(String value, Disposition defaultValue) {
        Disposition v = parse(value);
        if (v == null) {
            return defaultValue;
        } else {
            return v;
        }
    }
}
