package daris.client.model.object;

import arc.mf.client.xml.XmlElement;

public class DObjectBuilder<T extends DObject> {

    private DObjectRef _parent;
    private DObject.Type _type;
    private XmlElement _metadata;

    protected DObjectBuilder(DObjectRef parent) {
        _parent = parent;
        if (parent == null) {
            _type = DObject.Type.project;
        } else {
            DObject.Type ptype = parent.referentType();
            switch (ptype) {
            case repository:
                _type = DObject.Type.project;
                break;
            case project:
                _type = DObject.Type.subject;
                break;
            case subject:
                _type = DObject.Type.ex_method;
                break;
            case ex_method:
                _type = DObject.Type.study;
                break;
            case study:
                _type = DObject.Type.dataset;
                break;
            case dataset:
                _type = DObject.Type.data_object;
                break;
            default:
                break;
            }
        }
    }

    public void setMetadata(XmlElement metadata) {
        _metadata = metadata;
    }

    public XmlElement metadata() {
        return _metadata;
    }

    public DObject.Type type() {
        return _type;
    }

    public DObjectRef parent() {
        return _parent;
    }

}
