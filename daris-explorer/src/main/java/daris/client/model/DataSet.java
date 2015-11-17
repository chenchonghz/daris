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

    public boolean isDicomSeries() {
        // TODO
        return false;
    }

    public boolean isNiftiSeries() {
        // TODO
        return false;
    }

    public String dicomWebViewUrl() {
        // TODO
        if (!isDicomSeries()) {
            return null;
        }
        return null;
    }

    public String niftiWebViewUrl() {
        // TODO
        if (!isNiftiSeries()) {
            return null;
        }
        return null;
    }

}
