package daris.client.model.exmethod;

import arc.xml.XmlDoc.Element;
import daris.client.model.object.DObject;

public class ExMethod extends DObject {

    public ExMethod(Element oe) throws Throwable {
        super(oe);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Type type() {
        // TODO Auto-generated method stub
        return DObject.Type.EX_METHOD;
    }

}
