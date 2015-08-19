package daris.client.model.query.filter.operators;

public class StringOperator extends arc.mf.expr.Operator {
    public static final StringOperator HAS_VALUE = new StringOperator(
            "has value", 0);
    public static final StringOperator HASNO_VALUE = new StringOperator(
            "hasno value", 0);
    public static final StringOperator EQ = new StringOperator("=", 1);
    public static final StringOperator CONTAINS = new StringOperator(
            "contains", 1);
    public static final StringOperator CONTAINS_ALL = new StringOperator(
            "contains-all", 1);
    public static final StringOperator CONTAINS_ANY = new StringOperator(
            "contains-any", 1);
    public static final StringOperator CONTAINS_NO = new StringOperator(
            "contains-no", 1);

    public static final StringOperator LIKE = new StringOperator("like", 1);

    public static final StringOperator STARTS_WITH = new StringOperator(
            "starts with", 1);
    public static final StringOperator ENDS_WITH = new StringOperator(
            "ends with", 1);

    public static final StringOperator[] VALUES = new StringOperator[] {
            HAS_VALUE, HASNO_VALUE, EQ, CONTAINS, CONTAINS_ALL, CONTAINS_ANY,
            CONTAINS_NO, LIKE, STARTS_WITH, ENDS_WITH };

    private StringOperator(String value, int nbValues) {
        super(value, value, value, nbValues);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof StringOperator) {
            StringOperator mo = (StringOperator) o;
            return matches(mo.value());
        }
        return false;
    }

    public static StringOperator parse(String s) {
        return OperatorUtil.parse(VALUES, s);
    }

}
