package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class ModelFilter extends Filter {

    public static class ModelOperator extends arc.mf.expr.Operator {

        public static final ModelOperator EQUALS = new ModelOperator("=", "Has the same value as");
        public static final ModelOperator NOT_EQUALS = new ModelOperator("!=", "Does not have the same value as");
        public static final ModelOperator STARTS_WITH = new ModelOperator("starts with", "starts with");
        public static final ModelOperator CONTAINS = new ModelOperator("contains", "contains");

        public static final ModelOperator[] VALUES = new ModelOperator[] { EQUALS, NOT_EQUALS, STARTS_WITH, CONTAINS };

        private ModelOperator(String value, String description) {
            super(value, value, description, 1);
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof ModelOperator) {
                ModelOperator mo = (ModelOperator) o;
                return matches(mo.value());
            }
            return false;
        }

        public static ModelOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }

    }

    private ModelOperator _op;
    private String _name;

    public ModelFilter(ModelOperator op, String modelName) {
        _op = op;
        _name = modelName;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("model " + _op.toString() + " '" + _name + "'");
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
