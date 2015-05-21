package daris.client.model.study;

import arc.mf.client.xml.XmlElement;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectBuilder;
import daris.client.model.object.DObjectRef;

public class StudyBuilder extends DObjectBuilder<Study> {

    private XmlElement _methodMetadata;

    public StudyBuilder(DObjectRef parent) {
        super(parent);
        assert parent.referentType() == DObject.Type.ex_method;
    }

    public XmlElement methodMetadata() {
        return _methodMetadata;
    }

    public void setMethodMetadata(XmlElement methodMetadata) {
        _methodMetadata = methodMetadata;
    }
}
