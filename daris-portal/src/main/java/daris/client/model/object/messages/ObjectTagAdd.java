package daris.client.model.object.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.model.dictionary.Term;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.DObjectRef;

public class ObjectTagAdd extends ObjectMessage<Null> {

    private DObjectRef _o;
    private List<Term> _terms;

    public ObjectTagAdd(DObjectRef o, List<Term> terms) {
        _o = o;
        _terms = terms;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("cid", _o.id());
        for (Term term : _terms) {
            w.push("tag");
            w.add("name", term.term());
            String description = (term.definitions() != null && !term.definitions().isEmpty()) ? term.definitions()
                    .get(0).value() : null;
            if (description != null) {
                w.add("description", description);
            }
        }
    }

    @Override
    protected String messageServiceName() {

        return "om.pssd.object.tag.add";
    }

    @Override
    protected Null instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            return new Null();
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return null;
    }

    @Override
    protected String idToString() {
        return _o.id();
    }

}
