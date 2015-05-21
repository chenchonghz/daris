package daris.client.model.transform;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.OrderedCollectionRef;
import daris.client.model.transform.Transform.Status;
import daris.client.util.ClassUtil;

public class TransformCollectionRef extends OrderedCollectionRef<TransformRef> {

    public static final int DEFAULT_PAGE_SIZE = 100;

    private int _pagingSize = DEFAULT_PAGE_SIZE;

    private Set<Transform.Status.State> _states;
    private String _type;

    public TransformCollectionRef(Collection<Status.State> states, String type, int pageSize) {
        if (states != null && !states.isEmpty()) {
            _states = new HashSet<Status.State>();
            for (Status.State state : states) {
                _states.add(state);
            }
        }
        _type = type;
        _pagingSize = pageSize;
        setCountMembers(true);
    }

    public TransformCollectionRef(Collection<Status.State> states, String type) {
        this(states, type, DEFAULT_PAGE_SIZE);
    }

    public TransformCollectionRef() {
        this(null, null, DEFAULT_PAGE_SIZE);
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
        if (_states != null && !_states.isEmpty()) {
            for (Transform.Status.State state : _states) {
                w.add("status", state);
            }
        }
        w.add("idx", start + 1);
        w.add("size", size);

    }

    @Override
    protected String resolveServiceName() {
        return "transform.list";
    }

    @Override
    protected TransformRef instantiate(XmlElement xe) throws Throwable {
        return new TransformRef(xe.longValue("@uid"), xe.value("@name"), xe.value("@type"), Status.State.fromString(xe
                .value("@status")));
    }

    @Override
    protected String referentTypeName() {
        return ClassUtil.simpleClassNameOf(Transform.class).toLowerCase();
    }

    @Override
    protected String[] objectElementNames() {
        return new String[] { "transform" };
    }

}
