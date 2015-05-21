package daris.client.model.query.filter.pssd;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.query.filter.Filter;

public class ObjectTagFilter extends Filter implements ProjectAware {

    private DObjectRef _project;
    private DObject.Type _type;
    private String _tag;

    protected ObjectTagFilter(DObjectRef project, DObject.Type type, String tag) {

        _project = project;
        _type = type;
        _tag = tag;
    }

    public ObjectTagFilter(XmlElement e) throws Throwable {
        _project = new DObjectRef(e.value("project"));
        _type = DObject.Type.parse(e.value("type"));
        _tag = e.value("tag");
    }

    public DObject.Type objectType() {
        return _type;
    }

    public void setObjectType(DObject.Type type) {
        if (ObjectUtil.equals(_type, type)) {
            return;
        }
        _type = type;
        _tag = null;        
    }

    public DObjectRef project() {
        return _project;
    }

    public void setProject(DObjectRef parent) {
        _project = parent;
    }

    public String tag() {
        return _tag;
    }

    public void setTag(String tag) {
        _tag = tag;
    }

    @Override
    public void save(StringBuilder sb) {
        if (!valid().valid()) {
            return;
        }
        sb.append("tag='" + _tag + "'");
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("project", _project.id());
        w.add("type", _type.toString());
        w.add("tag", _tag);
    }

    @Override
    public Validity valid() {
        if (_type == null) {
            return new IsNotValid("Object type is not set.");
        }
        if (_project == null) {
            return new IsNotValid("The project is not set.");
        }
        if (_tag == null) {
            return new IsNotValid("Tag is not set.");
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new ObjectTagFilter(project(), objectType(), tag());
    }
}
