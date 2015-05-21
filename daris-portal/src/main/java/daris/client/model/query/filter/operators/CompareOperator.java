package daris.client.model.query.filter.operators;


public class CompareOperator extends arc.mf.expr.Operator {

    public static final CompareOperator EQ = new CompareOperator("=", "equal to", 1);
    public static final CompareOperator NE = new CompareOperator("!=", "not equal to", 1);
    public static final CompareOperator GT = new CompareOperator(">", "greater than", 1);
    public static final CompareOperator GE = new CompareOperator(">=", "greater than or equal to", 1);
    public static final CompareOperator LT = new CompareOperator("<", "less than", 1);
    public static final CompareOperator LE = new CompareOperator("<=", "less than or equal to", 1);

    public static final CompareOperator[] VALUES = new CompareOperator[] { EQ, NE, GT, GE, LT, LE };

    private CompareOperator(String value, String description, int nbValues) {
        super(value, value, description, nbValues);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof CompareOperator) {
            CompareOperator mo = (CompareOperator) o;
            return matches(mo.value());
        }
        return false;
    }

    public static CompareOperator parse(String s) {
        return OperatorUtil.parse(VALUES, s);
    }

}