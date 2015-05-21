package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.CompositeFilter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class CIDFilter extends Filter {

    public static class CIDOperator extends arc.mf.expr.Operator {
        public static final CIDOperator HAS_VALUE = new CIDOperator("has value", "cid has value", 0);
        public static final CIDOperator HASNO_VALUE = new CIDOperator("hasno value",
                "cid has no value", 0);
        public static final CIDOperator EQ = new CIDOperator("=", "cid equals to", 1);
        public static final CIDOperator NE = new CIDOperator("!=", "cid not equals to", 1);
        public static final CIDOperator GT = new CIDOperator(">", "cid is greater than", 1);
        public static final CIDOperator GE = new CIDOperator(">=",
                "cid is greater than or equal to", 1);
        public static final CIDOperator IN = new CIDOperator("in", "cid in", 1);
        public static final CIDOperator STARTS_WITH = new CIDOperator("starts with",
                "cid starts with", 1);
        public static final CIDOperator IN_NAMED_ID = new CIDOperator("in named id",
                "cid in named id", 1);
        public static final CIDOperator STARTS_WITH_NAMED_ID = new CIDOperator(
                "starts with named id", "cid starts with named id", 1);
        public static final CIDOperator CONTAINS = new CIDOperator("contains", "cid contains", 1);
        public static final CIDOperator CONTAINED_BY = new CIDOperator("contained by",
                "cid contained by", 1);
        public static final CIDOperator[] VALUES = new CIDOperator[] { HAS_VALUE, HASNO_VALUE, EQ,
                NE, GT, GE, IN, STARTS_WITH, IN_NAMED_ID, STARTS_WITH_NAMED_ID, CONTAINS, CONTAINED_BY };

        private CIDOperator(String value, String description, int nbValues) {
            super(value, value, description, nbValues);
        }

        public boolean isNamedIdOperator() {
            return value().endsWith("named id");
        }

        public static CIDOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }
    }

    private CIDFilter.CIDOperator _op;
    private String _value;
    private CompositeFilter _contains;
    private CompositeFilter _containedBy;

    public CIDFilter() {
        _op = CIDFilter.CIDOperator.HAS_VALUE;
        _value = null;
        _contains = null;
        _containedBy = null;
    }

    private CIDFilter(CIDFilter.CIDOperator op, String value, CompositeFilter contains,
            CompositeFilter containedBy) {
        _op = op;
        _value = value;
        _contains = contains;
        _containedBy = containedBy;
    }

    public CIDFilter(XmlElement xe) throws Throwable {
        _op = CIDFilter.CIDOperator.parse(xe.value("operator"));
        _value = xe.value("value");
        XmlElement cfe = xe.element("contains/filter");
        if (cfe != null) {
            Filter filter = Filter.instantiate(cfe);
            if (filter instanceof CompositeFilter) {
                _contains = (CompositeFilter) filter;
            } else {
                _contains = new CompositeFilter();
                _contains.addMember(null, false, filter);
            }
        }
        XmlElement cbfe = xe.element("contained-by/filter");
        if (cbfe != null) {
            Filter filter = Filter.instantiate(cbfe);
            if (filter instanceof CompositeFilter) {
                _containedBy = (CompositeFilter) filter;
            } else {
                _containedBy = new CompositeFilter();
                _containedBy.addMember(null, false, filter);
            }
        }
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("cid ");
        sb.append(_op);
        if (_op.numberOfValues() < 1) {
            return;
        } else {
            sb.append(" ");
            if (CIDFilter.CIDOperator.CONTAINS.matches(_op == null ? null : _op.value())) {
                if (_contains != null) {
                    sb.append("(");
                    _contains.save(sb);
                    sb.append(")");
                }
            } else if (CIDFilter.CIDOperator.CONTAINED_BY.matches(_op == null ? null : _op.value())) {
                if (_containedBy != null) {
                    sb.append("(");
                    _containedBy.save(sb);
                    sb.append(")");
                }
            } else {
                if (_value != null) {
                    sb.append("'");
                    sb.append(_value);
                    sb.append("'");
                }
            }
        }
    }

    public CompositeFilter contains() {
        return _contains;
    }

    public void setContains(CompositeFilter filter) {
        _contains = filter;
    }

    public CompositeFilter containedBy() {
        return _containedBy;
    }

    public void setContainedBy(CompositeFilter filter) {
        _containedBy = filter;
    }

    public String value() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    public CIDOperator operator() {
        return _op;
    }

    public void setOperator(CIDFilter.CIDOperator op) {
        if (_op != op) {
            if (op != null) {
                _value = null;
                _contains = null;
                _containedBy = null;
            }
            _op = op;
        }
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("operator", _op);
        if (_op.numberOfValues() < 1) {
            return;
        }
        if (CIDFilter.CIDOperator.CONTAINS.matches(_op != null ? _op.value() : null)) {
            w.push("contains");
            _contains.save(w);
            w.pop();
            return;
        }
        if (CIDFilter.CIDOperator.CONTAINED_BY.matches(_op != null ? _op.value() : null)) {
            w.push("contained-by");
            _containedBy.save(w);
            w.pop();
            return;
        }
        w.add("value", _value);
    }

    @Override
    public Validity valid() {
        if (_op == null) {
            return new IsNotValid("Operator is not set.");
        }
        if (_op.numberOfValues() > 0) {
            if (_op.matches(CIDOperator.CONTAINS.value())) {
                if (_contains == null) {
                    return new IsNotValid("Contains filter is not set.");
                } else {
                    return _contains.valid();
                }
            } else if (_op.matches(CIDOperator.CONTAINED_BY.value())) {
                if (_containedBy == null) {
                    return new IsNotValid("Contained-by filter is not set.");
                } else {
                    return _containedBy.valid();
                }
            } else {
                if (_value == null) {
                    return new IsNotValid("Value is not set.");
                }
            }
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new CIDFilter(operator(), value(), contains() != null ? (CompositeFilter) contains()
                .copy() : null, containedBy() != null ? (CompositeFilter) containedBy().copy()
                : null);
    }
}
