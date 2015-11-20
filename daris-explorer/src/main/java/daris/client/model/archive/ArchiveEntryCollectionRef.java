package daris.client.model.archive;

import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.OrderedCollectionRef;
import arc.xml.XmlDoc;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class ArchiveEntryCollectionRef
        extends OrderedCollectionRef<ArchiveEntry> {
    public static int PAGE_SIZE_INFINITY = -1;
    public static int PAGE_SIZE_DEFAULT = 500;

    private String _assetId;
    private String _cid;

    private int _pageSize = PAGE_SIZE_DEFAULT;

    public ArchiveEntryCollectionRef(DObject obj) {
        this(obj.citeableId(), true);
    }

    public ArchiveEntryCollectionRef(DObjectRef obj) {
        this(obj.citeableId(), true);
    }

    public ArchiveEntryCollectionRef(String id, boolean citeable) {
        if (citeable) {
            _cid = id;
        } else {
            _assetId = id;
        }
        setCountMembers(true);
    }
    
    @Override
    public boolean supportsPaging() {
        return true;
    }

    public String assetId() {
        return _assetId;
    }

    public String citeableId() {
        return _cid;
    }

    @Override
    public int defaultPagingSize() {
        return _pageSize;
    }

    public void setPageSize(int pageSize) {
        _pageSize = pageSize;
    }

    @Override
    protected String resolveServiceName() {
        return "daris.archive.content.list";
    }

    @Override
    protected ArchiveEntry instantiate(XmlDoc.Element ee) throws Throwable {
        if (ee != null) {
            return new ArchiveEntry(ee);
        }
        return null;
    }

    @Override
    protected String referentTypeName() {
        return "archive entry";
    }

    @Override
    protected String[] objectElementNames() {
        return new String[] { "entry" };
    }

    @Override
    protected void resolveServiceArgs(XmlWriterNe w, long start, int size,
            boolean count) {
        if (_cid != null) {
            w.add("cid", _cid);
        } else {
            w.add("id", _assetId);
        }
        w.add("idx", start + 1);
        w.add("size", size);
    }

}
