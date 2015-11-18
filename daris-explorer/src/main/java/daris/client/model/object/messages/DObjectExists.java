package daris.client.model.object.messages;

import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.ObjectMessage;
import arc.xml.XmlDoc;
import daris.client.model.CiteableIdUtils;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class DObjectExists extends ObjectMessage<Boolean> {

    private String _cid;
    private String _route;

    public DObjectExists(String cid, String route) {

        _cid = cid;
        _route = route;
    }

    public DObjectExists(DObjectRef o) {
        this(o.citeableId(), o.route());
    }

    @Override
    protected void messageServiceArgs(XmlWriterNe w) {

        if (_route != null) {
            w.add("id", new String[] { "proute", _route }, _cid);
        } else {
            w.add("id", _cid);
        }
    }

    @Override
    protected String messageServiceName() {

        return "om.pssd.object.exists";
    }

    @Override
    protected Boolean instantiate(XmlDoc.Element xe) throws Throwable {

        if (xe != null) {
            return xe.booleanValue("exists", false);
        }
        return false;
    }

    @Override
    protected String objectTypeName() {

        DObject.Type type = CiteableIdUtils.getTypeFromCID(_cid);
        return type == null ? null : type.typeName();
    }

    @Override
    protected String idToString() {

        return _cid;
    }

}