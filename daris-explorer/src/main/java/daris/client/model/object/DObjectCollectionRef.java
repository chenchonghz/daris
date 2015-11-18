package daris.client.model.object;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.object.OrderedCollectionRef;
import arc.xml.XmlDoc;

public class DObjectCollectionRef extends OrderedCollectionRef<DObjectRef> {

    public static final int PAGE_SIZE_INFINITY = -1;

    private DObjectRef _parent;
    private Comparator<DObjectRef> _comparator;
    private boolean _sort;
    private int _pageSize = PAGE_SIZE_INFINITY;

    protected DObjectCollectionRef(DObjectRef parent, boolean sort,
            Comparator<DObjectRef> comparator) {
        _parent = parent;
        _sort = sort;
        _comparator = comparator;
        _pageSize = PAGE_SIZE_INFINITY;
    }

    public DObjectCollectionRef(DObjectRef parent,
            Comparator<DObjectRef> comparator) {
        this(parent, true, comparator);
    }

    public DObjectCollectionRef(DObjectRef parent, boolean sort) {
        this(parent, sort, null);
    }

    public DObjectCollectionRef(DObjectRef parent) {
        this(parent, true, null);
    }

    public DObjectCollectionRef() {
        this(null, true, null);
    }

    public boolean sort() {
        return _sort;
    }

    @Override
    public int defaultPagingSize() {
        return _pageSize;
    }

    public void setPageSize(int pageSize) {
        _pageSize = pageSize;
    }

    @Override
    protected void resolveServiceArgs(XmlWriterNe w, long start, int size,
            boolean count) {
        if (_parent != null && _parent.citeableId() != null) {
            w.add("id", _parent.citeableId());
        }
        w.add("idx", start + 1);
        w.add("size", size);
        w.add("isleaf", true);
        w.add("sort", true);
    }

    @Override
    protected String resolveServiceName() {
        return "om.pssd.collection.member.list";
    }

    @Override
    protected DObjectRef instantiate(XmlDoc.Element xe) throws Throwable {
        return new DObjectRef(DObject.create(xe));
    }

    @Override
    protected String referentTypeName() {
        return "pssd-object";
    }

    @Override
    protected String[] objectElementNames() {
        return new String[] { "object" };
    }

    @Override
    public void resolve(long start, long end,
            final CollectionResolveHandler<DObjectRef> rh) {
        if (_sort) {
            super.resolve(start, end,
                    new CollectionResolveHandler<DObjectRef>() {
                        @Override
                        public void resolved(List<DObjectRef> os)
                                throws Throwable {
                            if (os != null && !os.isEmpty()) {
                                if (_comparator != null) {
                                    Collections.sort(os, _comparator);
                                } else {
                                    Collections.sort(os);
                                }
                            }
                            if (rh != null) {
                                rh.resolved(os);
                            }
                        }
                    });
        } else {
            super.resolve(start, end, rh);
        }
    }

}