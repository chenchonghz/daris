package daris.client.model.dictionary;

import arc.mf.client.xml.XmlWriterNe;
import arc.mf.model.dictionary.DictionaryRef;
import arc.mf.model.dictionary.TermRef;
import arc.mf.object.OrderedCollectionRef;
import arc.xml.XmlDoc.Element;

public class DictionaryEntryCollectionRef
        extends OrderedCollectionRef<TermRef> {
    public static final String SERVICE_NAME = "dictionary.entries.list";
    public static int PAGE_SIZE_INFINITY = -1;
    public static int PAGE_SIZE_DEFAULT = 1000;

    private DictionaryRef _dict;
    private String _prefix;
    private int _pageSize = PAGE_SIZE_INFINITY;

    public DictionaryEntryCollectionRef(DictionaryRef dictionary) {
        _dict = dictionary;
    }

    public DictionaryEntryCollectionRef(String dictionary) {
        this(new DictionaryRef(dictionary));
    }

    public DictionaryEntryCollectionRef setPrefix(String prefix) {
        _prefix = prefix;
        return this;
    }

    @Override
    protected TermRef instantiate(Element xe) throws Throwable {
        return new TermRef(_dict, xe.value());
    }

    @Override
    protected String[] objectElementNames() {
        return new String[] { "term" };
    }

    @Override
    protected String referentTypeName() {
        return null;
    }

    @Override
    protected void resolveServiceArgs(XmlWriterNe w, long start, int size,
            boolean count) {
        if (_prefix != null) {
            w.add("starting-with", _prefix);
        }
        w.add("idx", start + 1);
        w.add("size", size);
    }

    @Override
    protected String resolveServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public int defaultPagingSize() {
        return _pageSize;
    }

    public void setPageSize(int pageSize) {
        _pageSize = pageSize;
    }
    
    public DictionaryRef dictionary(){
        return _dict;
    }
    
    public String dictionaryName(){
        return _dict.name();
    }
}
