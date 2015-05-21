package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class LabelFilter extends Filter {

    public static class LabelOperator extends arc.mf.expr.Operator {

        public static final LabelOperator EQ = new LabelOperator("=", "equal to", 1);
        public static final LabelOperator NE = new LabelOperator("!=", "not equal to", 1);

        public static final LabelOperator[] VALUES = new LabelOperator[] { EQ, NE };

        private LabelOperator(String value, String description, int nbValues) {
            super(value, value, description, nbValues);
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof LabelOperator) {
                LabelOperator mo = (LabelOperator) o;
                return matches(mo.value());
            }
            return false;
        }

        public static LabelOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }

    }

    private LabelOperator _op;
    private String _label;

    public LabelFilter(LabelOperator op, String label) {
        _op = op;
        _label = label;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("label" + _op.toString() + "'" + _label + "'");
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
