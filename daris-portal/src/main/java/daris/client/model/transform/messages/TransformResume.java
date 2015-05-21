package daris.client.model.transform.messages;

import daris.client.model.transform.Transform;
import daris.client.model.transform.TransformRef;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;

public class TransformResume extends ObjectMessage<Null> {
    
    public static final String SERVICE_NAME = "transform.resume";
    
    private long _uid;
    
    public TransformResume(long uid){
        _uid = uid;
    }
    
    public TransformResume(TransformRef transform){
        this(transform.uid());
    }

    public TransformResume(Transform transform) {
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
