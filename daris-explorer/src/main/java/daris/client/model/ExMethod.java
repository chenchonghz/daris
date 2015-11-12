package daris.client.model;

import arc.xml.XmlDoc.Element;

public class ExMethod extends DObject {

    protected ExMethod(Element oe) throws Throwable {
        super(oe);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Type type() {
        // TODO Auto-generated method stub
        return DObject.Type.EX_METHOD;
    }

}
