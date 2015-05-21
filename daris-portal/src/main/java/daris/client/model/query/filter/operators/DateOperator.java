package daris.client.model.query.filter.operators;

public class DateOperator extends arc.mf.expr.Operator {

    public static final DateOperator HAS_VALUE = new DateOperator("has value", "has value", 0);
    public static final DateOperator HASNO_VALUE = new DateOperator("hasno value", "hasno value", 0);
    public static final DateOperator EQ = new DateOperator("=", "equal to", 1);
    public static final DateOperator GT = new DateOperator(">", "greater than", 1);
    public static final DateOperator GE = new DateOperator(">=", "greater than or equal to", 1);
    public static final DateOperator LT = new DateOperator("<", "less than", 1);
    public static final DateOperator LE = new DateOperator("<=", "less than or equal to", 1);
    public static final DateOperator[] VALUES = new DateOperator[] { HAS_VALUE, HASNO_VALUE, EQ,
            GT, GE, LT, LE };

    private DateOperator(String value, String description, int nbValues) {
        super(value, value, description, nbValues);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof DateOperator) {
            DateOperator mo = (DateOperator) o;
            return matches(mo.value());
        }
        return false;
    }

    public static DateOperator parse(String s) {
        return OperatorUtil.parse(VALUES, s);
    }

}