package daris.client.model.transform.messages;

import daris.client.model.transform.TransformRef;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;

public class TransformDestroy extends ObjectMessage<Null>{
public static final String SERVICE_NAME = "transform.destroy";
    
    private long _uid;
    private boolean _ignoreStatus = false;
    private boolean _ignoreDependants = false;
    
    public TransformDestroy(long uid, boolean ignoreStatus, boolean ignoreDependants){
        _uid = uid;
        _ignoreStatus= ignoreStatus;
        _ignoreDependants = ignoreDependants;
    }
    
    public TransformDestroy(long uid){
        this(uid, false, false);
    }
    
    public TransformDestroy(TransformRef transform){
        this(transform.uid(),false, false);
    }
    
    public void setIgoreDependants(boolean ignoreDependants){
        _ignoreDependants = ignoreDependants;
    }
    
    public void setIgnoreStatus(boolean ignoreStatus){
        _ignoreStatus = ignoreStatus;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("uid", _uid);
        if(_ignoreDependants){
            w.add("ignore-dependants", _ignoreDependants);
        }
        if(_ignoreStatus){
            w.add("ignore_status", _ignoreStatus);
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
        return Long.toString(_uid);
    }

}
