package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.CompareOperator;

public class VersionCountFilter extends Filter {


    private CompareOperator _op;
    private long _count;

    public VersionCountFilter(CompareOperator op, long count) {
        _op = op;
        _count = count;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("version count " + _op.value() + " " + Long.toString(_count));
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
