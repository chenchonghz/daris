package daris.client.model;

import arc.xml.XmlDoc.Element;

public class Project extends DObject {

    protected Project(Element oe) throws Throwable {
        super(oe);
        // TODO
    }

    @Override
    public Type type() {
        return DObject.Type.PROJECT;
    }

}
