package daris.client.model.archive;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.OrderedCollectionRef;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class ArchiveEntryCollectionRef
        extends OrderedCollectionRef<ArchiveEntry> {
    public static int PAGE_SIZE_INFINITY = -1;
    public static int PAGE_SIZE_DEFAULT = 50;

    private String _assetId;
    private String _cid;
    private String _assetMimeType;

    private int _pageSize = PAGE_SIZE_DEFAULT;
    
    public ArchiveEntryCollectionRef(DObject obj) {
        this(obj.id(), true, null);
    }

    public ArchiveEntryCollectionRef(DObjectRef obj) {
        this(obj.id(), true, null);
    }

    public ArchiveEntryCollectionRef(DObjectRef obj, String assetMimeType) {
        this(obj.id(), true, assetMimeType);
    }

    public ArchiveEntryCollectionRef(String id, boolean citeable,
            String assetMimeType) {
        if (citeable) {
            _cid = id;
        } else {
            _assetId = id;
        }
        _assetMimeType = assetMimeType;
        setCountMembers(true);
    }

    public String assetId() {
        return _assetId;
    }

    public String assetMimeType() {
        return _assetMimeType;
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
    protected void resolveServiceArgs(XmlStringWriter w, long start, int size,
            boolean count) {
        if (_cid != null) {
            w.add("cid", _cid);
        } else {
            w.add("id", _assetId);
        }
        w.add("idx", start + 1);
        w.add("size", size);
    }

    @Override
    protected String resolveServiceName() {
        return "daris.archive.content.list";
    }

    @Override
    protected ArchiveEntry instantiate(XmlElement ee) throws Throwable {
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
    public boolean supportsPaging() {
        return true;
    }
}
