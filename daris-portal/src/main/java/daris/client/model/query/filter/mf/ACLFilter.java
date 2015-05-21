package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.OperatorUtil;

public class ACLFilter extends Filter {

    public static class ACLOperator extends arc.mf.expr.Operator {
        public static final ACLOperator ACTOR_INVALID = new ACLOperator("actor invalid", "acl actor invalid", 0);
        public static final ACLOperator FOR_ROLE = new ACLOperator("for role", "acl for role", 1);
        public static final ACLOperator[] VALUES = new ACLOperator[] { ACTOR_INVALID, FOR_ROLE };

        private ACLOperator(String value, String description, int nbValues) {
            super(value, value, description, nbValues);
        }

        public static ACLOperator parse(String s) {
            return OperatorUtil.parse(VALUES, s);
        }
    }

    private ACLFilter.ACLOperator _op;
    private String _role;

    public ACLFilter(XmlElement xe) {
        _op = ACLOperator.parse(xe.value("operator"));
        _role = xe.value("role");
    }

    public ACLFilter(ACLFilter.ACLOperator op, String role) {
        _op = op;
        _role = role;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("acl ");
        sb.append(_op.value());
        if (_role != null) {
            sb.append(" '" + _role + "'");
        }
    }

    public void setForRole(String role) {
        _op = ACLFilter.ACLOperator.FOR_ROLE;
        _role = role;
    }

    public void setActorInvalid() {
        _op = ACLFilter.ACLOperator.ACTOR_INVALID;
        _role = null;
    }

    public ACLOperator operator() {
        return _op;
    }

    public String role() {
        return _role;
    }

    @Override
    public void saveXml(XmlWriter w) {
        w.add("operator", _op.value());
        if (role() != null) {
            w.add("role", role());
        }
    }

    @Override
    public Validity valid() {
        if (_op == null) {
            return new IsNotValid("Operator is not set.");
        }
        if (_op.matches(ACLOperator.FOR_ROLE.value()) && _role == null) {
            return new IsNotValid("Actor role is not set.");
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new ACLFilter(operator(), role());
    }

}
