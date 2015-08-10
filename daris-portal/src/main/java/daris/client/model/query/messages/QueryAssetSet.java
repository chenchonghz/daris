package daris.client.model.query.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.query.QueryAsset;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.options.QueryOptions;
import daris.client.model.query.options.QueryOptions.Purpose;

public class QueryAssetSet extends ObjectMessage<Null> {

    private String _id;
    private String _name;
    private String _description;
    private Filter _filter;
    private QueryOptions _opts;

    private QueryAssetSet(String id, String name, String description,
            Filter filter, QueryOptions opts) {
        _id = id;
        _name = name;
        _description = description;
        _filter = filter;
        _opts = opts;
    }

    public QueryAssetSet(QueryAssetCreate qac) {
        this("path=" + qac.path(), qac.name(), qac.description(), qac.filter(),
                qac.options());
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("id", _id);
        w.add("type", QueryAsset.MIME_TYPE);
        if (_name != null) {
            w.add("name", _name);
        }
        if (_description != null) {
            w.add("description", _description);
        }
        w.push("xml-content");
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
        w.pop();
    }

    @Override
    protected String messageServiceName() {
        return "asset.set";
    }

    @Override
    protected String objectTypeName() {
        return QueryAsset.class.getName();
    }

    @Override
    protected String idToString() {
        return _id;
    }

    @Override
    protected Null instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            return new Null();
        }
        return null;
    }
}
