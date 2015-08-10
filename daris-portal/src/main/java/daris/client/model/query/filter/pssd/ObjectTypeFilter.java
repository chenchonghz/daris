package daris.client.model.query.filter.pssd;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.query.filter.Filter;

public class ObjectTypeFilter extends Filter implements ProjectAware {

    private DObject.Type _type;
    private DObjectRef _project;

    public ObjectTypeFilter(DObject.Type type, DObjectRef project) {
        _type = type;
        _project = project;
    }

    public ObjectTypeFilter(XmlElement xe) {
        _type = DObject.Type.parse(xe.value("type"));
        _project = new DObjectRef(xe.value("project"));
    }

    @Override
    public void save(StringBuilder sb) {
        if (!valid().valid()) {
            return;
        }
        sb.append("model='" + _type.model() + "'");
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("type", _type.toString());
        w.add("project", _project.id());
    }

    @Override
    public Validity valid() {
        if (_type == null) {
            return new IsNotValid("Object type is not set.");
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new ObjectTypeFilter(_type, _project);
    }

    @Override
    public DObjectRef project() {
        return _project;
    }

    @Override
    public void setProject(DObjectRef project) {
        _project = project;
    }

    public DObject.Type type() {
        return _type;
    }

    public void setType(DObject.Type type) {
        _type = type;
    }

}
