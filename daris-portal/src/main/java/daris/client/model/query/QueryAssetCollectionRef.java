package daris.client.model.query;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.OrderedCollectionRef;

public class QueryAssetCollectionRef extends OrderedCollectionRef<QueryAssetRef> {
    
    

    private int _pagingSize = 100;
    
    public QueryAssetCollectionRef(){
        
    }

    public void setDefaultPagingSize(int size) {
        _pagingSize = size;
    }

    @Override
    public int defaultPagingSize() {
        return _pagingSize;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w, long start, int size, boolean count) {
        w.add("where", "type='" + QueryAsset.MIME_TYPE + "'");
        w.add("idx", start + 1);
        w.add("size", size);
        w.add("count", true);
        w.add("action", "get-value");
        w.add("xpath", new String[] { "ename", "name" }, "name");
        w.add("xpath", new String[] { "ename", "description" }, "description");
    }

    @Override
    protected String resolveServiceName() {
        return "asset.query";
    }

    @Override
    protected QueryAssetRef instantiate(XmlElement xe) throws Throwable {
        return new QueryAssetRef(xe.value("@id"), xe.value("name"), xe.value("description"));
    }

    @Override
    protected String referentTypeName() {
        return "daris-query";
    }

    @Override
    protected String[] objectElementNames() {
        return new String[] { "asset" };
    }

}
