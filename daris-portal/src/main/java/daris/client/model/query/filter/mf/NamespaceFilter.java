package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.model.asset.namespace.NamespaceRef;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class NamespaceFilter extends Filter {

    public static class NamespaceOperator extends arc.mf.expr.Operator {

        public static final NamespaceOperator EQ = new NamespaceOperator("=", "equal to", 1);
        public static final NamespaceOperator NE = new NamespaceOperator("!=", "not equal to", 1);
        public static final NamespaceOperator GT = new NamespaceOperator(">", "greater than", 1);
        public static final NamespaceOperator GE = new NamespaceOperator(">=", "greater than or equal to", 1);

        public static final NamespaceOperator[] VALUES = new NamespaceOperator[] { EQ, NE, GT, GE };

        private NamespaceOperator(String value, String description, int nbValues) {
            super(value, value, description, nbValues);
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof NamespaceOperator) {
                NamespaceOperator mo = (NamespaceOperator) o;
                return matches(mo.value());
            }
            return false;
        }

        public static NamespaceOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }

    }

    private NamespaceOperator _op;
    private NamespaceRef _namespace;

    protected NamespaceFilter(NamespaceOperator op, NamespaceRef namespace) {
        _op = op;
        _namespace = namespace;
    }

    public NamespaceFilter(XmlElement xe) {
        _op = NamespaceOperator.parse(xe.value("namespace/@operator"));
        String ns = xe.value("namespace");
        _namespace = ns == null ? null : new NamespaceRef(ns);
    }

    public NamespaceFilter() {
        this(NamespaceOperator.GE, null);
    }

    public NamespaceOperator operator() {
        return _op;
    }

    public void setOperator(NamespaceOperator op) {
        _op = op;
    }

    public NamespaceRef namespace() {
        return _namespace;
    }

    public void setNamespace(NamespaceRef namespace) {
        _namespace = namespace;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("namespace " + _op.value() + " '" + _namespace.path() + "'");
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("namespace", new String[] { "operator", _op.toString() }, _namespace);
    }

    @Override
    public Validity valid() {
        if (_op == null) {
            return new IsNotValid("namespace operator is not set.");
        }
        if (_namespace == null) {
            return new IsNotValid("namespace is not set.");
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new NamespaceFilter(_op, _namespace);
    }

}
