package daris.client.model.query.messages;

import java.util.HashSet;
import java.util.Set;

import arc.mf.client.RemoteServer;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.Project;
import daris.client.model.query.QueryAsset;
import daris.client.model.query.QueryAssetRef;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.options.ObjectQueryOptions;
import daris.client.model.query.options.QueryOptions;
import daris.client.model.query.options.QueryOptions.Purpose;

public class QueryAssetCreate extends ObjectMessage<QueryAssetRef> {

    private String _name;
    private String _description;
    private Filter _filter;
    private QueryOptions _opts;
    private Set<String> _rolesCanRead;
    private Set<String> _rolesCanWrite;
    private String _namespace;

    public QueryAssetCreate(String name, String description, String namespace,
            Filter filter, QueryOptions opts, String[] rolesCanRead,
            String[] rolesCanWrite) {
        _name = name;
        _description = description;
        _namespace = namespace;
        _filter = filter;
        _opts = opts;
        _rolesCanRead = new HashSet<String>();
        if (rolesCanRead != null) {
            for (String role : rolesCanRead) {
                _rolesCanRead.add(role);
            }
        }
        _rolesCanWrite = new HashSet<String>();
        if (rolesCanWrite != null) {
            for (String role : rolesCanWrite) {
                _rolesCanWrite.add(role);
            }
        }
    }

    public QueryAssetCreate(String namespace, Filter filter, QueryOptions opts,
            String[] rolesCanRead) {
        this(null, null, namespace, filter, opts, rolesCanRead, null);
    }

    public void setName(String name) {
        _name = name;
    }

    public String name() {
        return _name;
    }

    public DObjectRef project() {
        if (_opts != null && _opts instanceof ObjectQueryOptions) {
            return ((ObjectQueryOptions) _opts).project();
        }
        return null;
    }

    public String namespace() {
        return _namespace;
    }

    public String path() {
        if (_name == null) {
            return null;
        }
        return _namespace + "/" + _name;
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

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("type", QueryAsset.MIME_TYPE);
        w.add("namespace", new String[] { "create", "true" }, _namespace);
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
        w.add("actor", new String[] { "type", "user" }, RemoteServer.domain()
                + ":" + RemoteServer.user());
        w.add("access", "read-write");
        w.pop();
        if (_rolesCanRead != null) {
            for (String role : _rolesCanRead) {
                w.push("acl");
                w.add("actor", new String[] { "type", "role" }, role);
                w.add("access", "read");
                w.pop();
            }
        }
        if (_rolesCanWrite != null) {
            for (String role : _rolesCanWrite) {
                w.push("acl");
                w.add("actor", new String[] { "type", "role" }, role);
                w.add("access", "read-write");
                w.pop();
            }
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

    public void setAllowPublicAccess(Boolean allowPublicAccess) {
        if (project() == null) {
            _rolesCanRead.add("user");
        } else {
            _rolesCanRead.add(Project.memberRoleFromeId(project().id()));
        }
    }

}
