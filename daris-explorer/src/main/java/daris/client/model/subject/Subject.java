package daris.client.model.subject;

import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.model.DataUse;
import daris.client.model.method.MethodRef;
import daris.client.model.object.DObject;

public class Subject extends DObject {

    private MethodRef _method;
    private DataUse _dataUse;
    private boolean _virtual;
    private XmlDoc.Element _privateMetadata;
    private XmlDoc.Element _publicMetadata;
    private XmlDoc.Element _privateMetadataForEdit;
    private XmlDoc.Element _publicMetadataForEdit;

    public Subject(Element oe) throws Throwable {
        super(oe);
        if (oe.elementExists("private/metadata")
                || oe.elementExists("public/metadata")) {
            _privateMetadataForEdit = oe.element("private");
            _publicMetadataForEdit = oe.element("public");
        } else {
            _privateMetadata = oe.element("private");
            _publicMetadata = oe.element("public");
        }
        if (oe.elementExists("method")) {
            _method = new MethodRef(oe.value("method/id"),
                    oe.value("method/name"), oe.value("method/description"));
        }
        _dataUse = DataUse.fromString(oe.value("data-use"));
        _virtual = oe.booleanValue("virtual", false);
    }

    @Override
    public Type type() {
        return DObject.Type.SUBJECT;
    }

    public boolean isVirtual() {
        return _virtual;
    }

    public MethodRef method() {
        return _method;
    }

    public DataUse dataUse() {
        return _dataUse;
    }

    public XmlDoc.Element privateMetadata() {
        return _privateMetadata;
    }

    public XmlDoc.Element publicMetadata() {
        return _publicMetadata;
    }

    public XmlDoc.Element privateMetadataForEdit() {
        return _privateMetadataForEdit;
    }

    public XmlDoc.Element publicMetadataForEdit() {
        return _publicMetadataForEdit;
    }

}
