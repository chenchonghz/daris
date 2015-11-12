package daris.client.model;

import arc.xml.XmlDoc.Element;

public class DataSet extends DObject {

    protected DataSet(Element oe) throws Throwable {
        super(oe);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Type type() {
        return DObject.Type.DATASET;
    }

}
