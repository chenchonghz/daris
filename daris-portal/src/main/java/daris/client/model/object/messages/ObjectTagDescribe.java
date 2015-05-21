package daris.client.model.object.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.Tag;

public class ObjectTagDescribe extends ObjectMessage<List<Tag>> {

    private DObjectRef _o;

    public ObjectTagDescribe(DObjectRef o) {
        _o = o;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("cid", _o.id());
    }

    @Override
    protected String messageServiceName() {
        return "om.pssd.object.tag.describe";
    }

    @Override
    protected List<Tag> instantiate(XmlElement xe) throws Throwable {
        if (xe != null) {
            List<XmlElement> tes = xe.elements("tag");
            if (tes != null && !tes.isEmpty()) {
                List<Tag> tags = new ArrayList<Tag>(tes.size());
                for (XmlElement te : tes) {
                    tags.add(new Tag(_o.id(), te.intValue("@id"), te.value("name"), te.value("description")));
                }
                return tags;
            }
        }
        return null;
    }

    @Override
    protected String objectTypeName() {
        return "list of tags";
    }

    @Override
    protected String idToString() {
        return _o.id();
    }

}
