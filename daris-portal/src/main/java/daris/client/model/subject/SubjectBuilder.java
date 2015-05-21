package daris.client.model.subject;

import arc.mf.client.xml.XmlElement;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectBuilder;
import daris.client.model.object.DObjectRef;

public class SubjectBuilder extends DObjectBuilder<Subject> {

    private XmlElement _privateMetadata;
    private XmlElement _publicMetadata;

    public SubjectBuilder(DObjectRef parent) {
        super(parent);
        assert parent.referentType() == DObject.Type.project;
    }

    public XmlElement privateMetadata() {
        return _privateMetadata;
    }

    public void setPrivateMetadata(XmlElement privateMetadata) {
        _privateMetadata = privateMetadata;
    }

    public XmlElement publicMetadata() {
        return _publicMetadata;
    }

    public void setPublicMetadata(XmlElement publicMetadata) {
        _publicMetadata = publicMetadata;
    }
}
