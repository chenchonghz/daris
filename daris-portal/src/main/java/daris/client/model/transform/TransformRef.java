package daris.client.model.transform;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;
import daris.client.model.transform.Transform.Status;

public class TransformRef extends ObjectRef<Transform> {

    private long _uid;
    private String _name;
    private Status.State _state;
    private String _type;

    public TransformRef(long uid) {
        this(uid, null, null, null);
    }

    public TransformRef(long uid, String name, String type, Status.State state) {
        _uid = uid;
        _name = name;
        _type = type;
        _state = state;
    }

    public long uid() {
        return _uid;
    }

    public String name() {
        return _name;
    }

    public String type() {
        return _type;
    }

    public Status.State state() {
        return _state;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        w.add("uid", _uid);

    }

    @Override
    protected String resolveServiceName() {
        return "transform.describe";
    }

    @Override
    protected Transform instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            XmlElement te = xe.element("transform");
            if (te != null) {
                Transform t = new Transform(te);
                _name = t.name();
                _state = t.status().state();
                return t;
            }
        }
        return null;
    }

    @Override
    public String referentTypeName() {
        return Transform.TYPE_NAME;
    }

    @Override
    public String idToString() {
        return Long.toString(_uid);
    }

}
