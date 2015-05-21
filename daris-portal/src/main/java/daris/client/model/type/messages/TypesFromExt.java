package daris.client.model.type.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class TypesFromExt extends ObjectMessage<List<String>> {

    private String _ext;

    public TypesFromExt(String ext) {
        _ext = ext;
    }

    @Override
    protected void messageServiceArgs(XmlWriter w) {
        w.add("extension", _ext);
    }

    @Override
    protected String messageServiceName() {
        return "type.ext.types";
    }

    @Override
    protected List<String> instantiate(XmlElement xe) throws Throwable {
        return xe.values("extension/type");
    }

    @Override
    protected String objectTypeName() {
        return "extension";
    }

    @Override
    protected String idToString() {
        return _ext;
    }

}
