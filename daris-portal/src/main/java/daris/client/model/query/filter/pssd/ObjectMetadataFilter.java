package daris.client.model.query.filter.pssd;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.mf.MetadataFilter;
import daris.client.model.query.filter.mf.MetadataPath;

public class ObjectMetadataFilter extends MetadataFilter implements ProjectAware {

    private DObjectRef _project;
    private DObject.Type _type;

    protected ObjectMetadataFilter(DObjectRef project, DObject.Type type, MetadataPath path,
            MetadataFilter.MetadataOperator op, String value, boolean ignoreCase) {
        super(path, op, value, ignoreCase);
        _project = project;
        _type = type;
    }

    public ObjectMetadataFilter(XmlElement e) throws Throwable {
        super(e);
        _project = new DObjectRef(e.value("project"));
        _type = DObject.Type.parse(e.value("type"));
    }

    public DObject.Type objectType() {
        return _type;
    }

    public void setObjectType(DObject.Type type) {
        _type = type;
    }

    public DObjectRef project() {
        return _project;
    }

    public void setProject(DObjectRef parent) {
        _project = parent;
    }

    @Override
    public void save(StringBuilder sb) {
        if (!valid().valid()) {
            return;
        }
        sb.append("model='om.pssd." + _type + "' and ");
        super.save(sb);
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("type", _type.toString());
        w.add("project", _project.id());
        super.saveXml(w);
    }

    @Override
    public Validity valid() {
        if (_type == null) {
            return new IsNotValid("Object type is not set.");
        }
        if (_project == null) {
            return new IsNotValid("The object parent project is not set.");
        }
        return super.valid();
    }

    @Override
    public Filter copy() {
        return new ObjectMetadataFilter(project(), objectType(), path(), operator(), value(), ignoreCase());
    }
}
