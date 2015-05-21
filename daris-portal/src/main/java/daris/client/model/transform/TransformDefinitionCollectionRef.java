package daris.client.model.transform;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.OrderedCollectionRef;

public class TransformDefinitionCollectionRef extends OrderedCollectionRef<TransformDefinitionRef> {

    public static final int DEFAULT_PAGE_SIZE = 100;

    private int _pagingSize = DEFAULT_PAGE_SIZE;

    private String _type;

    public TransformDefinitionCollectionRef(String type, int pageSize) {
        _type = type;
        _pagingSize = pageSize;
    }

    public TransformDefinitionCollectionRef(String type) {
        this(type, DEFAULT_PAGE_SIZE);
    }

    public TransformDefinitionCollectionRef() {
        this(null, DEFAULT_PAGE_SIZE);
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
        if (_type != null) {
            w.add("type", _type);
        }
        w.add("idx", start + 1);
        w.add("size", size);
    }

    @Override
    protected String resolveServiceName() {
        return "transform.definition.list";
    }

    @Override
    protected TransformDefinitionRef instantiate(XmlElement xe) throws Throwable {
        return new TransformDefinitionRef(xe.longValue("@uid"), xe.value("@name"), xe.value("@type"));
    }

    @Override
    protected String referentTypeName() {
        return "transform-definition";
    }

    @Override
    protected String[] objectElementNames() {
        return new String[] { "transform-definition" };
    }

}
