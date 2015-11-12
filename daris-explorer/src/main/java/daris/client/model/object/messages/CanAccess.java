package daris.client.model.object.messages;

import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.ObjectMessage;
import arc.xml.XmlDoc.Element;
import daris.client.model.CiteableIdUtils;
import daris.client.model.DObject;
import daris.client.model.object.DObjectRef;

public class CanAccess extends ObjectMessage<Boolean> {

    private String _cid;

    public CanAccess(String cid) {

        _cid = cid;
    }

    public CanAccess(DObject o) {

        this(o.citeableId());
    }

    public CanAccess(DObjectRef o) {

        this(o.citeableId());
    }

    @Override
    protected String messageServiceName() {

        return "om.pssd.user.can.access";
    }

    @Override
    protected String objectTypeName() {

        DObject.Type type = CiteableIdUtils.getTypeFromCID(_cid);
        return type == null ? null : type.toString();
    }

    @Override
    protected String idToString() {

        return _cid;
    }

    @Override
    protected Boolean instantiate(Element xe) throws Throwable {
        if (xe != null) {
            return xe.booleanValue("can", false);
        }
        return false;
    }

    @Override
    protected void messageServiceArgs(XmlWriterNe w) {
        w.add("cid", _cid);
    }

}