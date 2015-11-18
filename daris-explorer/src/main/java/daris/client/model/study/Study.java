package daris.client.model.study;

import arc.xml.XmlDoc.Element;
import daris.client.model.object.DObject;

public class Study extends DObject {

    public Study(Element oe) throws Throwable {
        super(oe);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Type type() {
        return DObject.Type.STUDY;
    }

}
