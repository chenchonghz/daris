package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.mf.aclass.AssetClassRef;
import daris.client.mf.aclass.AssetClassSchemeRef;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class ClassFilter extends Filter {

    public static class ClassOperator extends arc.mf.expr.Operator {

        public static final ClassOperator EQ = new ClassOperator("=", "equals", false);
        public static final ClassOperator NE = new ClassOperator("!=", "not equals", false);
        public static final ClassOperator GT = new ClassOperator(">", "greater than", false);
        public static final ClassOperator GE = new ClassOperator(">=", "greater than or equal to", false);
        public static final ClassOperator CONTAINS = new ClassOperator("contains", "contains", true);
        public static final ClassOperator CONTAINS_ALL = new ClassOperator("contains-all", "contains all", true);
        public static final ClassOperator CONTAINS_ANY = new ClassOperator("contains-any", "contains any", true);
        public static final ClassOperator[] VALUES = new ClassOperator[] { EQ, NE, GT, GE, CONTAINS, CONTAINS_ALL,
                CONTAINS_ANY };

        private boolean _textOp;

        private ClassOperator(String value, String description, boolean textOp) {
            super(value, value, description, 1);
            _textOp = textOp;
        }

        public boolean isTextOperator() {
            return _textOp;
        }

        public boolean isCompareOperator() {
            return !_textOp;
        }

        public static ClassOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }
    }

    private AssetClassSchemeRef _scheme;
    private AssetClassRef _class;
    private String _text;

    private String _value;

    private ClassFilter.ClassOperator _op;

    public ClassFilter(ClassFilter.ClassOperator op, String value) {
        _op = op;
        _value = value;
    }

    public ClassFilter(XmlElement xe) {
        _op = ClassOperator.parse(xe.value("operator"));
        _value = xe.value("value");
    }

    public ClassOperator operator() {
        return _op;
    }

    public String value() {
        return _value;
    }

    @Override
    public void save(StringBuilder sb) {
        if (valid().valid()) {
            sb.append("class " + _op.value() + " ");
            sb.append("'" + value() + "'");
        }
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("operator", _op.value());
        w.add("value", _value);
    }

    @Override
    public Validity valid() {
        if (_op == null) {
            return new IsNotValid("operator is not set.");
        }
        if (_op.isCompareOperator()) {
            if (_scheme == null) {
                return new IsNotValid("scheme is not set.");
            }
            if (_class == null) {
                return new IsNotValid("class is not set.");
            }
        } else {
            if (_text == null) {
                return new IsNotValid("text is not set.");
            }
        }

        if (_value == null) {
            return new IsNotValid("value is not set.");
        }
        return IsValid.INSTANCE;
    }

    public void setOperator(ClassOperator op) {
        _op = op;
    }

    public void setAssetClass(AssetClassRef cls) {
        _class = cls;
        if (cls != null) {
            _scheme = cls.scheme();
            _value = cls.path();
        } else {
            _scheme = null;
            _value = null;
        }
        _text = null;
    }

    public AssetClassRef assetClass() {
        return _class;
    }

    public void setAssetClassScheme(AssetClassSchemeRef scheme) {
        _scheme = scheme;
        if (_class != null && !_class.scheme().path().equals(scheme.path())) {
            _class = null;
            _value = null;
        }
        _text = null;
    }

    public AssetClassSchemeRef assetClassScheme() {
        return _scheme;
    }

    public void setText(String text) {
        _text = text;
        _value = text;
        _scheme = null;
        _class = null;
    }

    public String text() {
        return _text;
    }

    @Override
    public Filter copy() {
        return new ClassFilter(operator(), value());
    }

}
