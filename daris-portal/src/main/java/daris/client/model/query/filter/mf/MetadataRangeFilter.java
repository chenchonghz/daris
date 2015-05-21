package daris.client.model.query.filter.mf;

import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;

public class MetadataRangeFilter extends Filter {

    private MetadataPath _docPath;
    private Range<String> _range;

    protected MetadataRangeFilter(MetadataPath docPath, Range<String> range) {
        _docPath = docPath;
        _range = range;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append(_docPath.path() + " " + _range.toString());
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
