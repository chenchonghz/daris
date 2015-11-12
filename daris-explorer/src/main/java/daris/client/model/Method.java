package daris.client.model;

import arc.xml.XmlDoc.Element;

public class Method extends DObject {

    protected Method(Element oe) throws Throwable {
        super(oe);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Type type() {
        // TODO Auto-generated method stub
        return DObject.Type.METHOD;
    }

}
