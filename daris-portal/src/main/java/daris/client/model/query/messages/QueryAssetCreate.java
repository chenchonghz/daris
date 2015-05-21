package daris.client.model.query.messages;

import arc.mf.client.RemoteServer;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import arc.mf.session.Session;
import daris.client.model.query.QueryAsset;
import daris.client.model.query.QueryAssetRef;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.options.QueryOptions;
import daris.client.model.query.options.QueryOptions.Purpose;

public class QueryAssetCreate extends ObjectMessage<QueryAssetRef> {

    // TODO: use application.property to manage the root namespace for the saved-queries.
    // at the moment, it is hard coded to /pssd-users/$domain/$user for private queries,
    // /pssd-users/saved-queries for public queries.
    public static final String NS_PUBLIC = "pssd-users/public/saved-queries";
    public static final String NS_PRIVATE_ROOT = "pssd-users/private";

    public static enum Access {
        Public_Read_Only, Public_Read_Write, Private;
        public String toString() {
            return super.toString().replace('_', ' ');
        }
    }

    private String _name;
    private String _description;
    private Filter _filter;
    private QueryOptions _opts;
    private Access _access;

    private QueryAssetCreate(String name, String description, Filter filter, QueryOptions opts,
            Access access) {
        _name = name;
        _description = description;
        _filter = filter;
        _opts = opts;
        _access = access;
    }

    public QueryAssetCreate(Filter filter, QueryOptions opts) {
        this(null, null, filter, opts, Access.Private);
    }

    public String namespace() {
        switch (_access) {
        case Private:
            return NS_PRIVATE_ROOT + "/" + Session.domainName() + "/" + Session.userName();
        default:
            return NS_PUBLIC;
        }
    }

    public void setName(String name) {
        _name = name;
    }

    public String name() {
        return _name;
    }

    public String path() {
        if (_name == null) {
            return null;
        }
        return namespace() + "/" + _name;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public String description() {
        return _description;
    }

    public Filter filter() {
        return _filter;
    }

    public QueryOptions options() {
        return _opts;
    }

    public void setOptions(QueryOptions opts) {
        _opts = opts;
    }

//    private void setAccess(Access access) {
//        _access = access;
//    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("type", QueryAsset.MIME_TYPE);
        w.add("namespace",new String[]{"create", "true"}, namespace());
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

        /*
         * access/acl
         */
        w.push("acl");
        w.add("actor", new String[] { "type", "user" }, RemoteServer.domain() + ":" + RemoteServer.user());
        w.add("access", "read-write");
        w.pop();
        if (_access == Access.Public_Read_Write || _access == Access.Public_Read_Only) {
            w.push("acl");
            w.add("actor", new String[] { "type", "role" }, "user");
            w.add("access", _access == Access.Public_Read_Write ? "read-write" : "read");
            w.pop();
        }
    }

    @Override
    protected String messageServiceName() {
        return "asset.create";
    }

    @Override
    protected QueryAssetRef instantiate(XmlElement xe) throws Throwable {
        String id = xe.value("id");
        return new QueryAssetRef(id, _name, _description);
    }

    @Override
    protected String objectTypeName() {
        return QueryAssetRef.class.getName();
    }

    @Override
    protected String idToString() {
        return path();
    }

    public Access access() {
        return _access;
    }

}
