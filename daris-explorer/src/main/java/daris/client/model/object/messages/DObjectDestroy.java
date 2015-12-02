package daris.client.model.object.messages;

import arc.mf.client.util.UnhandledException;
import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import arc.xml.XmlDoc.Element;
import daris.client.model.CiteableIdUtils;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class DObjectDestroy extends ObjectMessage<Null> {

    private String _cid;

    public DObjectDestroy(String citeableId) {
        _cid = citeableId;
    }

    public DObjectDestroy(DObjectRef o) {
        this(o.citeableId());
    }

    public DObjectDestroy(DObject o) {
        this(o.citeableId());
    }

    @Override
    protected String idToString() {
        return _cid;
    }

    @Override
    protected Null instantiate(Element re) throws Throwable {
        return new Null();
    }

    @Override
    protected void messageServiceArgs(XmlWriterNe w) {
        w.add("cid", _cid);
    }

    @Override
    protected String messageServiceName() {
        return "om.pssd.object.destroy";
    }

    @Override
    protected String objectTypeName() {
        return CiteableIdUtils.getTypeNameFromCID(_cid);
    }

    public static void destroy(DObject o) {
        try {
            new DObjectDestroy(o).send();
        } catch (Throwable ex) {
            UnhandledException.report(
                    "Destroying " + o.type().typeName() + " " + o.citeableId(),
                    ex);
        }
    }
}
