package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class TextFilter extends Filter {

    public static enum TextTarget {
        text, mtext, ctext, atext, ntext;
        public static TextTarget parse(String s) {
            if (s != null) {
                return valueOf(s);
            }
            return null;
        }
    }

    public static class TextOperator extends arc.mf.expr.Operator {
        public static final TextOperator LIKE = new TextOperator("like", "like", 1);
        public static final TextOperator CONTAINS = new TextOperator("contains", "contains", 1);
        public static final TextOperator CONTAINS_ANY = new TextOperator("contains-any", "contains any", 1);
        public static final TextOperator CONTAINS_ALL = new TextOperator("contains-all", "contains all", 1);
        public static final TextOperator CONTAINS_NO = new TextOperator("contains-no", "contains no", 1);
        public static final TextOperator CONTAINS_LITERAL = new TextOperator("contains literal", "contains literal", 1);
        public static final TextOperator CONTAINS_PATTERN = new TextOperator("contains pattern", "contains pattern", 1);
        public static final TextOperator CONTAINS_NO_LITERAL = new TextOperator("contains-no literal",
                "contains-no literal", 1);
        public static final TextOperator CONTAINS_NO_PATTERN = new TextOperator("contains-no pattern",
                "contains-no pattern", 1);

        public static final TextOperator[] VALUES = new TextOperator[] { CONTAINS, CONTAINS_ANY, CONTAINS_ALL,
                CONTAINS_LITERAL, CONTAINS_PATTERN, CONTAINS_NO, CONTAINS_NO_LITERAL, CONTAINS_NO_PATTERN, LIKE };

        private TextOperator(String value, String description, int nbValues) {
            super(value, value, description, nbValues);
        }

        public static TextOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }

    }

    private TextTarget _target;
    private TextOperator _op;
    private String _value;

    public TextFilter(TextTarget target, TextOperator op, String value) {
        _target = target;
        _op = op;
        _value = value;
    }

    public TextFilter(XmlElement xe) {
        _target = TextTarget.parse(xe.value("target"));
        _op = TextOperator.parse(xe.value("operator"));
        _value = xe.value("value");
    }

    public String value() {
        return _value;
    }

    public TextOperator operator() {
        return _op;
    }

    public TextTarget target() {
        return _target;
    }

    @Override
    public void save(StringBuilder sb) {
        if (!valid().valid()) {
            return;
        }
        sb.append(_target.toString() + " ");
        sb.append(_op.value());
        if (_op.value().endsWith("pattern") || _op.value().endsWith("literal")) {
            sb.append("('");
        } else {
            sb.append(" '");
        }
        sb.append(_value);
        if (_op.toString().endsWith("pattern") || _op.toString().endsWith("literal")) {
            sb.append("')");
        } else {
            sb.append("'");
        }
    }

    @Override
    protected void saveXml(XmlWriter w) {
        if (valid().valid()) {
            w.add("target", _target);
            w.add("operator", _op.value());
            w.add("value", _value);
        }
    }

    @Override
    public Validity valid() {
        if (_target == null) {
            return new IsNotValid("Text target is not set.");
        }
        if (_op == null) {
            return new IsNotValid("Operator is not set.");
        }
        if (_value == null) {
            return new IsNotValid("Value is not set.");
        }
        return IsValid.INSTANCE;
    }

    public void setTarget(TextTarget target) {
        _target = target;
    }

    public void setOperator(TextOperator op) {
        _op = op;
    }

    public void setValue(String value) {
        _value = value;
    }

    @Override
    public Filter copy() {
        return new TextFilter(target(), operator(), value());
    }

}
