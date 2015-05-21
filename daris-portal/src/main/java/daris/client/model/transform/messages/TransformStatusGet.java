package daris.client.model.transform.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.transform.TransformRef;

public class TransformStatusGet extends ObjectMessage<Null> {

    public static final String SERVICE_NAME = "transform.status.get";

    private long _uid;

    public TransformStatusGet() {
        _uid = -1;
    }

    public TransformStatusGet(long uid) {
        _uid = uid;
    }

    public TransformStatusGet(TransformRef transform) {
        this(transform.uid());
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        if (_uid > 0) {
            w.add("uid", _uid);
        }
    }

    @Override
    protected String messageServiceName() {
        return SERVICE_NAME;
    }

    @Override
    protected Null instantiate(XmlElement xe) throws Throwable {
        return new Null();
    }

    @Override
    protected String objectTypeName() {
        return "Transform";
    }

    @Override
    protected String idToString() {
        if (_uid > 0) {
            return Long.toString(_uid);
        } else {
            return null;
        }
    }
}
