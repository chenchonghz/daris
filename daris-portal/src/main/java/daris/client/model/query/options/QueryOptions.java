package daris.client.model.query.options;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.tree.Tree;

public abstract class QueryOptions {

    public static final int DEFAULT_SIZE = 100;

    public static enum Entity {
        asset, object;
        public static Entity parse(String s) {
            if (s != null) {
                return valueOf(s);
            }
            return null;
        }
    }

    public static enum Action {
        get_id, get_value, get_values, count;
        @Override
        public String toString() {
            return super.toString().replace('_', '-');
        }

        public static Action parse(String s) {
            if (s != null) {
                return valueOf(s.replace('-', '_'));
            }
            return null;
        }
    }

    public static enum Purpose {
        QUERY, EXPORT, SERIALIZE
    }

    private Entity _entity;

    private Action _action;

    private SortOptions _sort;

    private Map<String, XPath> _xpaths;

    private int _size;

    protected QueryOptions(Entity entity, Action action) {
        _entity = entity;
        _action = action;
        _sort = new SortOptions();
        _xpaths = null;
        _size = DEFAULT_SIZE;
    }

    public QueryOptions(XmlElement xe) throws Throwable {
        _action = Action.parse(xe.value("action"));
        _entity = Entity.parse(xe.value("entity"));
        XmlElement se = xe.element("sort");
        if (se != null) {
            _sort = new SortOptions(se);
        } else {
            _sort = new SortOptions();
        }
        _size = xe.intValue("size", DEFAULT_SIZE);
        List<XmlElement> xpes = xe.elements("xpath");
        if (xpes != null && !xpes.isEmpty()) {
            _xpaths = new LinkedHashMap<String, XPath>();
            for (XmlElement xpe : xpes) {
                XPath xpath = new XPath(xpe);
                _xpaths.put(xpath.value(), xpath);
            }
        }
    }

    public List<XPath> xpaths() {
        if (_xpaths == null) {
            return null;
        }
        return new ArrayList<XPath>(_xpaths.values());
    }

    public List<XPath> defaultXPaths(Purpose purpose) {
        return null;
    }

    public void addXPath(String name, String xpath) {
        addXPath(new XPath(name, xpath));
    }

    public void addXPath(XPath xpath) {
        if (_xpaths == null) {
            _xpaths = new LinkedHashMap<String, XPath>();
        }
        _xpaths.put(xpath.value(), xpath);
        _action = Action.get_value;
    }

    public void removeXPath(String path) {
        if (_xpaths != null) {
            _xpaths.remove(path);
        }
    }

    public void removeXPath(XPath xpath) {
        removeXPath(xpath.value());
    }

    public void clearXPaths() {
        if (_xpaths != null) {
            _xpaths.clear();
        }
    }

    public Action action() {
        return _action;
    }

    public void setAction(Action action) {
        _action = action;
    }

    public Entity entity() {
        return _entity;
    }

    public SortOptions sortOptions() {
        return _sort;
    }

    public void setSortOptions(SortOptions sortOptions) {
        _sort = sortOptions;
    }

    public int size() {
        return _size;
    }

    public void setSize(int size) {
        _size = size;
    }

    public void save(XmlWriter w, Purpose purpose) {
        if (purpose == Purpose.EXPORT) {
            // if it is exporting to csv or xml. override the action to get-values.
            w.add("action", Action.get_values);
        } else {
            w.add("action", _action);
        }

        w.add("entity", _entity);

        if (_action == Action.get_value) {
            // xpath only required when action is get-value or get-values

            // default xpaths got higher priority
            List<XPath> defaultXPaths = defaultXPaths(purpose);
            if (defaultXPaths != null && !defaultXPaths.isEmpty()) {
                for (XPath xpath : defaultXPaths) {
                    xpath.save(w);
                }
            }

            // user defined xpaths got lower priority
            if (_xpaths != null && !_xpaths.isEmpty()) {
                for (XPath xpath : _xpaths.values()) {
                    xpath.save(w);
                }
            }

        }

        if (_action != Action.count) {
            // sorting is meaningless when action is count
            if (_sort != null && _sort.hasKeys()) {
                _sort.save(w);
            }
        }
    }

    public abstract Tree metadataTree();

    public SortKeyTree sortKeyTree() {
        return new SortKeyTree(metadataTree());
    }

    public static QueryOptions instantiate(XmlElement xe) throws Throwable {
        Entity entity = Entity.parse(xe.value("entity"));
        if (entity == Entity.object) {
            return new ObjectQueryOptions(xe);
        } else if (entity == Entity.asset) {
            return new AssetQueryOptions(xe);
        } else {
            throw new IllegalArgumentException("Failed to parse query options from: " + xe);
        }
    }

}
