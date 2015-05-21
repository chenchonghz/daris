package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class VersionFilter extends Filter {

    public static class VersionOperator extends arc.mf.expr.Operator {

        public static final VersionOperator EVERY_VERSION_HAS = new VersionOperator("every version has",
                "every version has", 1);
        public static final VersionOperator ANY_VERSION_HAS = new VersionOperator("any version has",
                "every version has", 1);

        public static final VersionOperator[] VALUES = new VersionOperator[] { EVERY_VERSION_HAS, ANY_VERSION_HAS };

        private VersionOperator(String value, String description, int nbValues) {
            super(value, value, description, nbValues);
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof VersionOperator) {
                VersionOperator mo = (VersionOperator) o;
                return matches(mo.value());
            }
            return false;
        }

        public static VersionOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }

    }

    private VersionOperator _op;
    private Filter _filter;

    protected VersionFilter(VersionOperator op, Filter filter) {
        _op = op;
        _filter = filter;
    }

    protected Filter filter() {
        return _filter;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append(_op.value() + " (");
        _filter.save(sb);
        sb.append(")");
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
