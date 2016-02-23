package daris.client.model.dataset;

import arc.mf.client.xml.XmlElement;

public class DicomDataSet extends DerivedDataSet {

    private int _size;

    public DicomDataSet(XmlElement ddse) throws Throwable {

        super(ddse);
        try {
            _size = ddse.intValue("meta/mf-dicom-series/size", 0);
        } catch (Throwable e) {
            _size = 0;
        }
    }

    public DicomDataSet(String id, String proute, String name,
            String description) {
        super(id, proute, name, description);
    }

    public int size() {

        return _size;
    }

    public String papayaViewerUrl() {
        return DataSet.papayaDicomViewerUrl(this);
    }

    public String simpleViewerUrl() {
        return DataSet.simpleDicomViewerUrl(this);
    }

}
