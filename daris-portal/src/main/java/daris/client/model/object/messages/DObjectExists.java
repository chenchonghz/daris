package daris.client.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectLock;
import arc.mf.object.ObjectMessage;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.IDUtil;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class DObjectExists extends ObjectMessage<Boolean> {

    private String _id;
    private String _proute;

    public DObjectExists(String id, String proute) {

        _id = id;
        _proute = proute;
    }
    
    public DObjectExists(DObjectRef o){
        this(o.id(), o.proute());
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        if (_proute != null) {
            w.add("id", new String[] { "proute", _proute }, _id);
        } else {
            w.add("id", _id);
        }
    }

    @Override
    protected String messageServiceName() {

        return "om.pssd.object.exists";
    }

    @Override
    protected Boolean instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            return xe.booleanValue("exists", false);
        }
        return false;
    }

    @Override
    public ObjectMessage<Boolean> send(final ObjectLock lock, final ObjectMessageResponse<Boolean> rh) {

        if (_id == null) {
            rh.responded(true);
            return this;
        }
        return super.send(lock, rh);
    }

    @Override
    protected String objectTypeName() {

        DObject.Type type = IDUtil.typeFromId(_id);
        return type == null ? null : type.toString();
    }

    @Override
    protected String idToString() {

        return _id;
    }

}
