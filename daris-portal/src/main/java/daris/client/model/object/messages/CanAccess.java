package daris.client.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.IDUtil;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class CanAccess extends ObjectMessage<Boolean> {

    private String _cid;

    public CanAccess(String cid) {

        _cid = cid;
    }

    public CanAccess(DObject o) {

        this(o.id());
    }

    public CanAccess(DObjectRef o) {

        this(o.id());
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        w.add("cid", _cid);
    }

    @Override
    protected String messageServiceName() {

        return "om.pssd.user.can.access";
    }

    @Override
    protected Boolean instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            return xe.booleanValue("can", false);
        }
        return false;
    }

    @Override
    protected String objectTypeName() {

        DObject.Type type = IDUtil.typeFromId(_cid);
        return type == null ? null : type.toString();
    }

    @Override
    protected String idToString() {

        return _cid;
    }

}