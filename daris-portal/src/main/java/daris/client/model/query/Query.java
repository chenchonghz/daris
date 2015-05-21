package daris.client.model.query;

import arc.mf.client.util.ObjectUtil;
import arc.mf.client.xml.XmlElement;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.pssd.ObjectQuery;
import daris.client.model.query.options.QueryOptions;

public class Query {

    private Filter _filter;
    private QueryOptions _options;

    protected Query(XmlElement xe) throws Throwable {
        this(new QueryAsset(xe));
    }

    protected Query(QueryAsset asset) {
        this(asset.filter(), asset.options());
    }

    protected Query(Filter filter, QueryOptions options) {
        _filter = filter;
        _options = options;
    }

    public Filter filter() {
        return _filter;
    }

    public void setFilter(Filter filter) {
        _filter = filter;
    }

    public QueryOptions options() {
        return _options;
    }

    public void setOptions(QueryOptions options) {
        _options = options;
    }

    public boolean equals(Object o) {
        if (o instanceof Query) {
            Query qo = (Query) o;
            return ObjectUtil.equals(filter(), qo.filter()) && ObjectUtil.equals(qo.options(), options());
        } else {
            return false;
        }
    }

    public static Query create(QueryAsset qa) {
        if (qa.options().entity() == QueryOptions.Entity.object) {
            return new ObjectQuery(qa);
        } else if (qa.options().entity() == QueryOptions.Entity.asset) {
            return new AssetQuery(qa);
        } else {
            throw new AssertionError("Invalid query option: entity=" + qa.options().entity());
        }
    }
}
