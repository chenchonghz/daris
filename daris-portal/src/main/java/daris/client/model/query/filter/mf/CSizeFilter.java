package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.CompareOperator;

public class CSizeFilter extends Filter {

    private CompareOperator _op;
    private long _size;

    public CSizeFilter(CompareOperator op, long size) {
        _op = op;
        _size = size;
    }

    protected CompareOperator operator() {
        return _op;
    }

    protected long size() {
        return _size;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("csize" + _op.value() + _size + "");
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
