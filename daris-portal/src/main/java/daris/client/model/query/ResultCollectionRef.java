package daris.client.model.query;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.util.ActionListener;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.object.ObjectRef;
import arc.mf.object.OrderedCollectionRef;
import daris.client.model.query.filter.pssd.ObjectQuery;
import daris.client.model.query.options.QueryOptions.Purpose;
import daris.client.model.query.options.XPath;

public abstract class ResultCollectionRef<T extends ObjectRef<?>> extends OrderedCollectionRef<T> {

    private Query _query;
    private int _pagingSize = 100;

    protected ResultCollectionRef(Query query) {
        _query = query;
        setCountMembers(true);
        setDefaultPagingSize(_query.options().size());
    }

    public void setDefaultPagingSize(int size) {
        _pagingSize = size;
    }

    @Override
    public int defaultPagingSize() {
        return _pagingSize;
    }

    public int totalNumberOfPages() {
        long total = totalNumberOfMembers();
        if (total == -1) {
            return -1;
        }
        if (total == 0) {
            return 0;
        }
        int totalPages = (int) (total / pagingSize());
        totalPages = total % pagingSize() == 0 ? totalPages : (totalPages + 1);
        return totalPages;
    }

    @Override
    public boolean supportsPaging() {
        return true;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w, long start, int size, boolean count) {
        StringBuilder where = new StringBuilder();
        where.append(_query.filter());
        if (_query instanceof ObjectQuery) {
            where.append(" and (daris:pssd-object has value)");
        }
        w.add("where", where);
        _query.options().save(w, Purpose.QUERY);
        w.add("idx", start + 1);
        w.add("size", size);
        w.add("count", true);
    }

    @Override
    protected String resolveServiceName() {
        return "asset.query";
    }

    @Override
    public String referentTypeName() {
        return _query.options().entity().toString();
    }

    @Override
    protected String[] objectElementNames() {

        return new String[] { _query.options().entity().toString() };
    }

    @SuppressWarnings("unchecked")
    public static <T extends ObjectRef<?>> ResultCollectionRef<T> create(Query query) {
        if (query instanceof AssetQuery) {
            return (ResultCollectionRef<T>) new AssetResultCollectionRef((AssetQuery) query);
        } else if (query instanceof ObjectQuery) {
            return (ResultCollectionRef<T>) new DObjectResultCollectionRef((ObjectQuery) query);
        }
        throw new AssertionError(ResultCollectionRef.class.getName() + " does not support entity type: "
                + query.options().entity());
    }

    public void resolve(final ActionListener al) {
        resolve(0, defaultPagingSize(), new CollectionResolveHandler<T>() {

            @Override
            public void resolved(List<T> os) throws Throwable {
                if (al != null) {
                    al.executed(os != null && !os.isEmpty());
                }
            }
        });
    }

    public Query query() {
        return _query;
    }

    protected void addXPathValues(HasXPathValues o, XmlElement xe) {
        List<XPath> xpaths = new ArrayList<XPath>();
        List<XPath> defaultXPaths = query().options().defaultXPaths(Purpose.QUERY);
        if (defaultXPaths != null && !defaultXPaths.isEmpty()) {
            xpaths.addAll(defaultXPaths);
        }
        List<XPath> userDefinedXPaths = query().options().xpaths();
        if (userDefinedXPaths != null && !userDefinedXPaths.isEmpty()) {
            xpaths.addAll(userDefinedXPaths);
        }
        for (XPath xpath : xpaths) {
            String value = xe.value(xpath.name());
            if (value != null) {
                o.addXpathValue(new XPathValue(xpath.value(), xpath.name(), value));
            }
        }
    }

}
