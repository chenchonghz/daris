package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class RIDFilter extends Filter {

    public static class RIDOperator extends arc.mf.expr.Operator {

        public static final RIDOperator EQUALS = new RIDOperator("=", "equals", 1);
        public static final RIDOperator IN = new RIDOperator("in", "in", 1);
        public static final RIDOperator STARTS_WITH = new RIDOperator("starts with", "rid starts with", 1);
        public static final RIDOperator HAS_VALUE = new RIDOperator("has value", "rid is set", 0);
        public static final RIDOperator HASNO_VALUE = new RIDOperator("hasno value", "rid is not set", 0);

        public static final RIDOperator[] VALUES = new RIDOperator[] { EQUALS, IN, STARTS_WITH, HAS_VALUE, HASNO_VALUE };

        private RIDOperator(String value, String description, int nbValues) {
            super(value, value, description, nbValues);
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof RIDOperator) {
                RIDOperator mo = (RIDOperator) o;
                return matches(mo.value());
            }
            return false;
        }

        public static RIDOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }

    }

    private RIDOperator _op;
    private String _rid;

    public RIDFilter(RIDOperator op, String rid) {
        _op = op;
        _rid = rid;
    }

    @Override
    public void save(StringBuilder sb) {
        if (_op.numberOfValues() < 1) {
            sb.append("rid " + _op.value());
        } else {
            sb.append("rid " + _op.value() + " '" + _rid + "'");
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
