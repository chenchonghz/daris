package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class CTypeFilter extends Filter {

    public static class CTypeOperator extends arc.mf.expr.Operator {

        public static final CTypeOperator EQ = new CTypeOperator("=", "equal to", 1);
        public static final CTypeOperator NE = new CTypeOperator("!=", "not equal to", 1);
        public static final CTypeOperator GT = new CTypeOperator(">", "greater than", 1);
        public static final CTypeOperator GE = new CTypeOperator(">=", "greater than or equal to", 1);

        public static final CTypeOperator[] VALUES = new CTypeOperator[] { EQ, NE, GT, GE };

        private CTypeOperator(String value, String description, int nbValues) {
            super(value, value, description, nbValues);
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof CTypeOperator) {
                CTypeOperator mo = (CTypeOperator) o;
                return matches(mo.value());
            }
            return false;
        }

        public static CTypeOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }

    }

    private CTypeOperator _op;
    private String _contentMimeType;

    protected CTypeFilter(CTypeOperator op, String contentMimeType) {
        _op = op;
        _contentMimeType = contentMimeType;
    }

    public CTypeFilter() {
        this(CTypeOperator.EQ, null);
    }

    public void setCType(String contentMimeType) {
        _contentMimeType = contentMimeType;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("ctype" + _op + "'" + _contentMimeType + "'");
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("ctype", new String[] { "operator", _op.toString() }, _contentMimeType);
    }

    @Override
    public Validity valid() {
        if (_op == null) {
            return new IsNotValid("ctype operator is not set.");
        }
        if (_contentMimeType == null) {
            return new IsNotValid("ctype is not set.");
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new CTypeFilter(_op, _contentMimeType);
    }

    public String ctype() {
        return _contentMimeType;
    }

}
