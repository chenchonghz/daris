package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.CompareOperator;

public class STimeFilter extends Filter {

    private CompareOperator _op;
    private long _n;

    protected STimeFilter(CompareOperator op, long number) {
        _op = op;
        _n = number;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("id" + _op.toString() + Long.toString(_n));
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
