package daris.client.model.query.filter.mf;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.ListUtil;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.dtype.DataType;
import arc.mf.dtype.DocType;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.dtype.NumericDataType;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.xml.defn.Node;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class MetadataFilter extends Filter {

    public static class MetadataOperator extends arc.mf.expr.Operator {
        public static final MetadataOperator HAS_VALUE = new MetadataOperator("has value", "has value", 0);
        public static final MetadataOperator HASNO_VALUE = new MetadataOperator("hasno value", "hasno value", 0);
        public static final MetadataOperator IS_VALID = new MetadataOperator("is valid", "is valid", 0);
        public static final MetadataOperator IS_INVALID = new MetadataOperator("is invalid", "is invalid", 0);

        public static final MetadataOperator EQ = new MetadataOperator("=", "equal to", 1);
        public static final MetadataOperator NE = new MetadataOperator("!=", "not equal to", 1);
        public static final MetadataOperator GT = new MetadataOperator(">", "greater than", 1);
        public static final MetadataOperator GE = new MetadataOperator(">=", "greater than or equal to", 1);
        public static final MetadataOperator LT = new MetadataOperator("<", "less than", 1);
        public static final MetadataOperator LE = new MetadataOperator("<=", "less than or equal to", 1);

        public static final MetadataOperator CONTAINS = new MetadataOperator("contains", "contains", 1);
        public static final MetadataOperator CONTAINS_ALL = new MetadataOperator("contains all", "contains all", 1);
        public static final MetadataOperator CONTAINS_ANY = new MetadataOperator("contains any", "contains any", 1);
        public static final MetadataOperator CONTAINS_NO = new MetadataOperator("contains no", "contains no", 1);

        public static final MetadataOperator LIKE = new MetadataOperator("like", "like", 1);

        public static final MetadataOperator STARTS_WITH = new MetadataOperator("starts with", "starts with", 1);
        public static final MetadataOperator ENDS_WITH = new MetadataOperator("ends with", "ends with", 1);

        public static final MetadataOperator[] VALUES = new MetadataOperator[] { HAS_VALUE, HASNO_VALUE, IS_VALID,
                IS_INVALID, EQ, NE, GT, GE, LT, LE, CONTAINS, CONTAINS_ALL, CONTAINS_ANY, CONTAINS_NO, LIKE,
                STARTS_WITH, ENDS_WITH };
        public static final List<MetadataOperator> DOC_TYPE_OPERATORS = ListUtil.list(HAS_VALUE, HASNO_VALUE, IS_VALID,
                IS_INVALID);

        private MetadataOperator(String value, String description, int nbValues) {
            super(value, value, description, nbValues);
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof MetadataOperator) {
                MetadataOperator mo = (MetadataOperator) o;
                return matches(mo.value());
            }
            return false;
        }

        public boolean isOperatorFor(DataType type) {
            List<MetadataOperator> ops = operatorsFor(type);
            if (ops != null) {
                return ops.contains(this);
            }
            return false;
        }

        public static MetadataOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }

        public static List<MetadataOperator> operatorsFor(DataType type) {
            List<arc.mf.expr.Operator> ops = type.operators();
            if (ops == null) {
                // DocType.operators() returns null
                return DOC_TYPE_OPERATORS;
            }
            List<MetadataOperator> mops = new ArrayList<MetadataOperator>(DOC_TYPE_OPERATORS);
            for (arc.mf.expr.Operator op : ops) {
                MetadataOperator mop = parse(op.value());
                if (mop != null) {
                    mops.add(mop);
                }
            }
            return mops;
        }
    }

    public static enum CastTarget {
        STRING, INTEGER, LONG, FLOAT, DOUBLE, DATE, HIERARCHICAL_ID;
        @Override
        public String toString() {
            return super.toString().replace('_', '-').toLowerCase();
        }
    }

    public static class OperatorEnumDataSource implements DynamicEnumerationDataSource<MetadataOperator> {
        private MetadataPath _path;

        public OperatorEnumDataSource(MetadataPath path) {
            _path = path;
        }

        @Override
        public boolean supportPrefix() {
            return false;
        }

        @Override
        public void exists(final String value, final DynamicEnumerationExistsHandler handler) {
            if (_path == null) {
                handler.exists(value, false);
                return;
            }
            final MetadataOperator op = MetadataOperator.parse(value);
            if (_path.documentOnly()) {
                handler.exists(value, op.isOperatorFor(DocType.DEFAULT));
                return;
            } else {
                _path.resolveNode(new ObjectResolveHandler<arc.mf.xml.defn.Node>() {
                    @Override
                    public void resolved(Node n) {
                        if (n == null) {
                            handler.exists(value, false);
                            return;
                        }
                        handler.exists(value, op.isOperatorFor(n.type()));
                    }
                });
            }
        }

        @Override
        public void retrieve(String prefix, final long start, final long end,
                final DynamicEnumerationDataHandler<MetadataOperator> handler) {
            if (_path == null) {
                handler.process(0, 0, 0, null);
                return;
            }
            if (_path.documentOnly()) {
                List<Value<MetadataOperator>> opvs = new ArrayList<Value<MetadataOperator>>();
                for (MetadataOperator op : MetadataOperator.DOC_TYPE_OPERATORS) {
                    Value<MetadataOperator> opv = new Value<MetadataOperator>(op);
                    opvs.add(opv);
                }
                handler.process(start, end, opvs.size(), opvs);
                return;
            } else {
                _path.resolveNode(new ObjectResolveHandler<arc.mf.xml.defn.Node>() {
                    @Override
                    public void resolved(Node n) {
                        if (n == null) {
                            handler.process(0, 0, 0, null);
                            return;
                        }

                        List<Value<MetadataOperator>> opvs = new ArrayList<Value<MetadataOperator>>();
                        DataType type = n.type();
                        List<MetadataOperator> ops = MetadataOperator.operatorsFor(type);
                        if (ops != null) {
                            for (MetadataOperator op : ops) {
                                Value<MetadataOperator> opv = new Value<MetadataOperator>(op);
                                opvs.add(opv);
                            }
                        }
                        handler.process(start, end, opvs.size(), opvs);
                    }
                });
            }

        }

    }

    private MetadataPath _path;
    private MetadataFilter.MetadataOperator _op;
    private String _value;
    private boolean _ignoreCase;

    public MetadataFilter(MetadataPath path, MetadataFilter.MetadataOperator op, String value, boolean ignoreCase) {
        _path = path;
        _op = op;
        _value = value;
        _ignoreCase = ignoreCase;
    }

    public MetadataFilter(XmlElement e) throws Throwable {
        _path = new MetadataPath(e.value("path"));
        _op = MetadataOperator.parse(e.value("operator"));
        _value = e.value("value");
        _ignoreCase = e.booleanValue("value/@ignore-case", false);
    }

    public void setIgnoreCase(boolean ignoreCase) {
        _ignoreCase = ignoreCase;
    }

    public boolean ignoreCase() {
        return _ignoreCase;
    }

    public MetadataPath path() {
        return _path;
    }

    public void setPath(MetadataPath path) {
        if (!ObjectUtil.equals(_path, path)) {
            _path = path;
            _op = null;
            _value = null;
        }
    }

    public DynamicEnumerationDataSource<MetadataOperator> availableOperators() {
        return new OperatorEnumDataSource(_path);
    }

    public MetadataFilter.MetadataOperator operator() {
        return _op;
    }

    public void setOperator(MetadataFilter.MetadataOperator op) {
        if (!ObjectUtil.equals(_op, op)) {
            _op = op;
            if (_op.numberOfValues() < 1) {
                _value = null;
            }
        }
    }

    public String value() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    public boolean requiresValue() {
        if (_path != null && _op != null && _op.numberOfValues() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void save(StringBuilder sb) {
        if (_path == null || _op == null) {
            return;
        }
        if (!_path.documentOnly() && _op.equals(MetadataOperator.STARTS_WITH) && _ignoreCase) {
            sb.append("function(starts-with(lowercase(xvalue('meta/");
            sb.append(_path.path());
            sb.append("')),'");
            sb.append(_value);
            sb.append("'))");
        } else {
            sb.append(_path.documentOnly() ? _path.path() : "xpath(" + _path.path() + ")");
            sb.append(" ");
            sb.append(_op);
            if (_op.numberOfValues() > 0) {
                sb.append(" ");
                if (_path.resolved()) {
                    DataType type = _path.node().type();
                    if (type instanceof NumericDataType) {
                        sb.append(_value);
                        return;
                    }
                }
                if (_ignoreCase) {
                    sb.append("ignore-case('" + _value + "')");
                } else {
                    sb.append("'" + _value + "'");
                }
            }
        }
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("path", _path.path());
        w.add("operator", _op);
        if (_value != null) {
            if (_ignoreCase) {
                w.add("value", new String[] { "ignore-case", "true" }, _value);
            } else {
                w.add("value", _value);
            }
        }
    }

    @Override
    public Validity valid() {
        if (_path == null) {
            return new IsNotValid("Metadata path is not set.");
        }
        if (_op == null) {
            return new IsNotValid("Operator is not set.");
        }
        if (_op.numberOfValues() > 0) {
            if (_value == null) {
                return new IsNotValid("Value is not set.");
            }
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new MetadataFilter(path(), operator(), value(), ignoreCase());
    }

}
