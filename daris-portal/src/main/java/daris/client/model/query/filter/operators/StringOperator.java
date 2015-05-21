package daris.client.model.query.filter.operators;


public class StringOperator extends arc.mf.expr.Operator {
    public static final StringOperator HAS_VALUE = new StringOperator("has value", "has value", 0);
    public static final StringOperator HASNO_VALUE = new StringOperator("hasno value",
            "hasno value", 0);
    public static final StringOperator EQ = new StringOperator("=", "equal to", 1);
    public static final StringOperator CONTAINS = new StringOperator("contains", "contains", 1);
    public static final StringOperator CONTAINS_ALL = new StringOperator("contains all",
            "contains all", 1);
    public static final StringOperator CONTAINS_ANY = new StringOperator("contains any",
            "contains any", 1);
    public static final StringOperator CONTAINS_NO = new StringOperator("contains no",
            "contains no", 1);

    public static final StringOperator LIKE = new StringOperator("like", "like", 1);

    public static final StringOperator STARTS_WITH = new StringOperator("starts with",
            "starts with", 1);
    public static final StringOperator ENDS_WITH = new StringOperator("ends with", "ends with", 1);

    public static final StringOperator[] VALUES = new StringOperator[] { HAS_VALUE, HASNO_VALUE,
            EQ, CONTAINS, CONTAINS_ALL, CONTAINS_ANY, CONTAINS_NO, LIKE, STARTS_WITH, ENDS_WITH };

    private StringOperator(String value, String description, int nbValues) {
        super(value, value, description, nbValues);
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
