package daris.client.model.query.filter.mf;


public enum TimeType {
    ctime, mtime;
    public static TimeType parse(String s) {
        if (s != null) {
            return valueOf(s);
        }
        return null;
    }
}
