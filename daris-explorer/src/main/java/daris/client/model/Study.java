package daris.client.model;

import arc.xml.XmlDoc.Element;

public class Study extends DObject {

    protected Study(Element oe) throws Throwable {
        super(oe);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Type type() {
        return DObject.Type.STUDY;
    }

}
