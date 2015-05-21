package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class AssetFilter extends Filter {

    public static class AssetPropertyOperator extends arc.mf.expr.Operator {
        public static final AssetPropertyOperator HAS = new AssetPropertyOperator("has", "asset has the property of");
        public static final AssetPropertyOperator HASNO = new AssetPropertyOperator("hasno",
                "asset does not have the property of");
        public static final AssetPropertyOperator[] VALUES = { HAS, HASNO };

        private AssetPropertyOperator(String value, String description) {
            super(value, value, description, 1);
        }

        public static AssetPropertyOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }
    }

    public static enum AssetProperty {
        content, annotation, acl, related, trigger, name, cid, rid, model, lock, geoshape;
        public static AssetProperty parse(String s) {
            return s == null ? null : valueOf(s);
        }
    }

    private AssetPropertyOperator _op;
    private AssetProperty _prop;

    public AssetFilter(AssetPropertyOperator op, AssetProperty prop) {
        _op = op;
        _prop = prop;
    }

    public AssetFilter(XmlElement xe) {
        _op = AssetFilter.AssetPropertyOperator.parse(xe.value("operator"));
        _prop = AssetProperty.parse(xe.value("property"));
    }

    public AssetPropertyOperator operator() {
        return _op;
    }

    public void setOperator(AssetPropertyOperator op) {
        _op = op;
    }

    public AssetProperty property() {
        return _prop;
    }

    public void setAssetProperty(AssetProperty prop) {
        _prop = prop;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("asset ");
        sb.append(_op.value());
        sb.append(" ");
        sb.append(_prop);
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("operator", _op);
        w.add("property", _prop.toString());
    }

    @Override
    public Validity valid() {
        if (_op == null) {
            return new IsNotValid("Operator is not set.");
        }
        if (_prop == null) {
            return new IsNotValid("Asset property is not set.");
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new AssetFilter(operator(), property());
    }

}
