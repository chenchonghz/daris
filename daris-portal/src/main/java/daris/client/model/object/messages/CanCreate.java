package daris.client.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.IDUtil;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class CanCreate extends ObjectMessage<Boolean> {

    private String _parentId;

    public CanCreate( String parentId) {

        _parentId = parentId;
    }

    public CanCreate(DObject parent) {

        this(parent.id());
    }

    public CanCreate(DObjectRef parent) {

        this(parent.id());
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {

        if (_parentId != null) {
            w.add("pid", _parentId);
        }
    }

    @Override
    protected String messageServiceName() {

        return "om.pssd.user.can.create";
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

        DObject.Type type =  IDUtil.childTypeFromId(_parentId);
        return type == null ? null: type.toString();
    }

    @Override
    protected String idToString() {

        return _parentId + ".x";
    }

}