package daris.client.model.object.messages;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.Tag;

public class ObjectTagRemove extends ObjectMessage<Null> {
    private DObjectRef _o;
    private List<String> _tags;

    public ObjectTagRemove(DObjectRef o, List<String> tags) {
        _o = o;
        _tags = tags;
    }

    public ObjectTagRemove(DObjectRef o, Tag... tags) {
        _o = o;
        _tags = new ArrayList<String>();
        for (Tag tag : tags) {
            _tags.add(tag.name());
        }
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("cid", _o.id());
        for (String tag : _tags) {
            w.add("tag", tag);
        }
    }

    @Override
    protected String messageServiceName() {

        return "om.pssd.object.tag.remove";
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
