package daris.client.model.dataobject;

import arc.mf.client.xml.XmlElement;
import daris.client.model.dataobject.messages.DataObjectCreate;
import daris.client.model.dataobject.messages.DataObjectUpdate;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;

public class DataObject extends DObject {

    public DataObject(XmlElement xe) throws Throwable {

        super(xe);
        // TODO Auto-generated constructor stub
    }

    public DataObject(String id, String proute, String name, String description) {
        super(id, proute, name, description, false, 0, false);
    }

    @Override
    public DObject.Type type() {

        return DObject.Type.data_object;
    }

    @Override
    protected DObjectCreate objectCreateMessage(DObjectRef po) {

        return new DataObjectCreate(po, this);
    }

    @Override
    protected DObjectUpdate objectUpdateMessage() {

        return new DataObjectUpdate(this);
    }

}
