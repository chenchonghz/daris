package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;

public class MetaFilter extends Filter {

    public static enum MetaValidity {
        valid, invalid
    }

    private MetaValidity _validity;

    protected MetaFilter(MetaValidity validity) {
        _validity = validity;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("meta is " + _validity);
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
