package daris.client.model.transform;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.OrderedCollectionRef;
import daris.client.model.exmethod.messages.ExMethodStepTransformFind;
import daris.client.model.transform.messages.TransformFind;

public class RelatedTransformCollectionRef extends OrderedCollectionRef<Transform> {

    public static final int DEFAULT_PAGING_SIZE = 100;

    private int _defaultPagingSize = DEFAULT_PAGING_SIZE;

    private long _defnId;
    private int _defnVersion;
    private String _scopeId;
    private String _stepPath;

    public RelatedTransformCollectionRef(long defnId, int defnVersion, String scopeId) {
        _defnId = defnId;
        _defnVersion = defnVersion;
        _scopeId = scopeId;
        _stepPath = null;
    }

    public RelatedTransformCollectionRef(String exMethodId, String stepPath) {
        _defnId = 0;
        _defnVersion = 0;
        _scopeId = exMethodId;
        _stepPath = stepPath;

    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w, long start, int size, boolean count) {
        if (_stepPath != null) {
            w.add("id", _scopeId);
            w.add("step", _stepPath);
            w.add("idx", start + 1);
            w.add("size", size);
        } else {
            w.add("definition", new String[] { "version", _defnVersion > 0 ? Integer.toString(_defnVersion) : null },
                    _defnId);
            w.add("scope", _scopeId);
        }
    }

    @Override
    protected String resolveServiceName() {
        return _stepPath == null ? TransformFind.SERVICE_NAME : ExMethodStepTransformFind.SERVICE_NAME;
    }

    @Override
    protected Transform instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            return new Transform(xe);
        }
        return null;
    }

    @Override
    protected String referentTypeName() {
        return Transform.TYPE_NAME;
    }

    @Override
    protected String[] objectElementNames() {
        return new String[] { "transform" };
    }

    @Override
    public final int defaultPagingSize() {
        return _defaultPagingSize;
    }

    public void setDefaultPagingSize(int defaultPagingSize) {
        _defaultPagingSize = defaultPagingSize;
    }

}
