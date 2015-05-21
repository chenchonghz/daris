package daris.client.model.transform;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class TransformDefinitionRef extends ObjectRef<TransformDefinition> {

    private long _uid;
    private int _version = TransformDefinition.VERSION_LATEST;
    private String _name;
    private String _type;

    protected TransformDefinitionRef(long uid, int version, String name, String type) {
        _uid = uid;
        _version = version;
        _name = name;
        _type = type;
    }

    public TransformDefinitionRef(long uid, int version) {
        this(uid, version, null, null);
    }

    public TransformDefinitionRef(long uid) {
        this(uid, TransformDefinition.VERSION_LATEST, null, null);
    }

    public TransformDefinitionRef(long uid, String name, String type) {
        this(uid, TransformDefinition.VERSION_LATEST, name, type);
    }

    public long uid() {
        return _uid;
    }

    public int version() {
        return _version;
    }

    public String name() {
        return _name;
    }

    public String type() {
        return _type;
    }

    @Override
    protected void resolveServiceArgs(XmlStringWriter w) {
        w.add("uid", new String[] { "version", Integer.toString(_version) }, _uid);
    }

    @Override
    protected String resolveServiceName() {
        return "transform.definition.describe";
    }

    @Override
    protected TransformDefinition instantiate(XmlElement xe) throws Throwable {

        if (xe != null) {
            XmlElement tde = xe.element("transform-definition");
            if (tde != null) {
                return new TransformDefinition(tde);
            }
        }
        return null;
    }

    @Override
    public String referentTypeName() {
        return TransformDefinition.TYPE_NAME;
    }

    @Override
    public String idToString() {
        return Long.toString(_uid);
    }

}
