package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.CompareOperator;

public class IDFilter extends Filter {

    private CompareOperator _op;
    private long _id;

    protected IDFilter(CompareOperator op, long number) {
        _op = op;
        _id = number;
    }

    public CompareOperator operator() {
        return _op;
    }

    public long id() {
        return _id;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("id" + _op.toString() + Long.toString(_id));
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
