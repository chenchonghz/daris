package daris.client.model.object;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.ObjectRef;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;

public class DObjectChildrenRef extends ObjectRef<List<DObjectRef>> {

    private String _pid;

    public DObjectChildrenRef(DObjectRef parent) {
        _pid = parent.citeableId();
    }

    @Override
    public String idToString() {
        return _pid;
    }

    @Override
    protected List<DObjectRef> instantiate(Element xe) throws Throwable {
        if (xe != null) {
            List<XmlDoc.Element> oes = xe.elements("object");
            if (oes != null && !oes.isEmpty()) {
                List<DObjectRef> os = new ArrayList<DObjectRef>();
                for (XmlDoc.Element oe : oes) {
                    os.add(new DObjectRef(DObject.create(oe)));
                }
                return os;
            }
        }
        return null;
    }

    @Override
    public String referentTypeName() {
        return null;
    }

    @Override
    protected void resolveServiceArgs(XmlWriterNe w) {
        if (_pid != null) {
            w.add("id", _pid);
        }
        w.add("isleaf", true);
        w.add("size", "infinity");
        w.add("sort", true);
    }

    @Override
    protected String resolveServiceName() {
        return "om.pssd.collection.member.list";
    }

}
