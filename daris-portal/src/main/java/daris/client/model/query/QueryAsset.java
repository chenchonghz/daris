package daris.client.model.query;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.model.asset.Asset;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.options.QueryOptions;
import daris.client.model.query.options.QueryOptions.Purpose;

public class QueryAsset extends Asset {

    public static final String MIME_TYPE = "application/daris-query";

    private Filter _filter;
    private QueryOptions _opts;

    public QueryAsset(XmlElement xe) throws Throwable {
        super(xe);
        XmlElement qe = xe.element("content/xml/query");
        if (qe != null) {
            XmlElement fe = qe.element("filter");
            if (fe != null) {
                _filter = Filter.instantiate(fe);
            }
            XmlElement oe = qe.element("options");
            if (oe != null) {
                _opts = QueryOptions.instantiate(oe);
            }
        }
    }

    public QueryAsset(Filter filter, QueryOptions options) throws Throwable {
        super(null);
        _filter = filter;
        _opts = options;
    }

    public QueryOptions options() {
        return _opts;
    }

    public Filter filter() {
        return _filter;
    }

    // TODO: this method is not used atm. Should think about use it in QueryAssetSet.java.
    public void save(XmlWriter w) {
        w.push("query");
        if (_filter != null) {
            _filter.save(w);
        }
        if (_opts != null) {
            w.push("options");
            _opts.save(w, Purpose.SERIALIZE);
            w.pop();
        }
        w.pop();
    }

}
