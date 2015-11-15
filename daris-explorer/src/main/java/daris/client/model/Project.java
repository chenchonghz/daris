package daris.client.model;

import arc.xml.XmlDoc.Element;

public class Project extends DObject {

    private DataUse _dataUse;

    protected Project(Element oe) throws Throwable {
        super(oe);
        _dataUse = DataUse.fromString(oe.value("data-use"));
    }

    public DataUse dataUse() {
        return _dataUse;
    }

    @Override
    public final Type type() {
        return DObject.Type.PROJECT;
    }

}
