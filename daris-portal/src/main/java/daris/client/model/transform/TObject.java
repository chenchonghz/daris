package daris.client.model.transform;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.transform.Transform.Type;

public abstract class TObject {

    private String _assetId;
    private long _uid;
    private String _name;
    private String _description;
    private Transform.Type _type;

    protected TObject(XmlElement te) throws Throwable {
        _assetId = te.value("@asset");
        _uid = te.longValue("uid");
        _name = te.value("name");
        _description = te.value("description");
        _type = Type.fromString(te.value("type"));
        if (_type == null) {
            throw new Exception("Invalid type: " + _type);
        }
    }

    public String assetId() {
        return _assetId;
    }

    public long uid() {
        return _uid;
    }

    public String name() {
        return _name;
    }

    public String description() {
        return _description;
    }

    public Type type() {
        return _type;
    }

    protected void save(XmlWriter w) throws Throwable {
        w.add("uid", uid());
        if (name() != null) {
            w.add("name", name());
        }
        if (description() != null) {
            w.add("description", description());
        }
        w.add("type", type().toString());
    }

}
