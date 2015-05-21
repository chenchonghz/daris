package daris.client.model.query.filter;

public enum LogicOperator {
    and, or;
    public static LogicOperator parse(String s) {
        if (s != null) {
            return valueOf(s);
        }
        return null;
    }
}
