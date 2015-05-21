package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class TypeFilter extends Filter {

    public static class TypeOperator extends arc.mf.expr.Operator {

        public static final TypeOperator EQ = new TypeOperator("=", "equal to", 1);
        public static final TypeOperator NE = new TypeOperator("!=", "not equal to", 1);
        public static final TypeOperator GT = new TypeOperator(">", "greater than", 1);
        public static final TypeOperator GE = new TypeOperator(">=", "greater than or equal to", 1);

        public static final TypeOperator[] VALUES = new TypeOperator[] { EQ, NE, GT, GE };

        private TypeOperator(String value, String description, int nbValues) {
            super(value, value, description, nbValues);
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof TypeOperator) {
                TypeOperator mo = (TypeOperator) o;
                return matches(mo.value());
            }
            return false;
        }

        public static TypeOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }

    }

    private TypeOperator _op;
    private String _mimeType;

    protected TypeFilter(TypeOperator op, String mimeType) {
        _op = op;
        _mimeType = mimeType;
    }

    public TypeFilter() {
        this(TypeOperator.EQ, null);
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("type" + _op.value() + " '" + _mimeType + "'");
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("type", new String[] { "operator", _op.toString() }, _mimeType);
    }

    @Override
    public Validity valid() {
        if (_op == null) {
            return new IsNotValid("type operator is not set.");
        }
        if (_mimeType == null) {
            return new IsNotValid("type is not set.");
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new TypeFilter(_op, _mimeType);
    }

    public String type() {
        return _mimeType;
    }

    public void setType(String mimeType) {
        _mimeType = mimeType;
    }

}
