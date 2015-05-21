package daris.client.model.transform.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.transform.Transform;
import daris.client.model.transform.TransformRef;

public class TransformTerminate extends ObjectMessage<Null> {

public static final String SERVICE_NAME = "transform.terminate";
    
    private long _uid;
    
    public TransformTerminate(long uid){
        _uid = uid;
    }
    
    public TransformTerminate(TransformRef transform){
        this(transform.uid());
    }
    
    public TransformTerminate(Transform transform) {
        this(transform.uid());
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("uid", _uid);
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
        return Long.toString(_uid);
    }

}
