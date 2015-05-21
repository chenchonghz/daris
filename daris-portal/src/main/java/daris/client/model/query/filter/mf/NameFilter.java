package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class NameFilter extends Filter {

    public static class NameOperator extends arc.mf.expr.Operator {

        public static final NameOperator EQ = new NameOperator("=", "equal to", 1);
        public static final NameOperator NE = new NameOperator("!=", "not equal to", 1);
        public static final NameOperator GT = new NameOperator(">", "greater than", 1);
        public static final NameOperator GE = new NameOperator(">=", "greater than or equal to", 1);
        public static final NameOperator LT = new NameOperator("<", "less than", 1);
        public static final NameOperator LE = new NameOperator("<=", "less than or equal to", 1);
        public static final NameOperator LIKE = new NameOperator(" like ", "like", 1);

        public static final NameOperator[] VALUES = new NameOperator[] { EQ, NE, GT, GE, LT, LE };

        private NameOperator(String value, String description, int nbValues) {
            super(value, value, description, nbValues);
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof NameOperator) {
                NameOperator mo = (NameOperator) o;
                return matches(mo.value());
            }
            return false;
        }

        public static NameOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }

    }

    private NameOperator _op;
    private String _name;
    private boolean _ignoreCase;

    protected NameFilter(NameOperator op, String name, boolean ignoreCase) {
        _op = op;
        _name = name;
        _ignoreCase = ignoreCase;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("name ");
        sb.append(_op.value() + " ");
        if (_ignoreCase && !NameOperator.LIKE.equals(_op)) {
            sb.append("ignore-case('" + _name + "')");
        } else {
            sb.append("'" + _name + "'");
        }
    }

    @Override
    protected void saveXml(XmlWriter w) {
        // TODO Auto-generated method stub

    }

    @Override
    public Validity valid() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Filter copy() {
        // TODO Auto-generated method stub
        return null;
    }

}
