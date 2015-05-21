package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;

public class RelatedFilter extends Filter {

    private String _relationship;
    private Filter _target;

    public RelatedFilter(String relationship, Filter target) {
        _relationship = relationship;
        _target = target;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("related to");
        sb.append(_relationship != null ? "{" + _relationship + "}" : "");
        sb.append(" ");
        sb.append(_target != null ? "(" + _target.toString() + ")" : "any");
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
