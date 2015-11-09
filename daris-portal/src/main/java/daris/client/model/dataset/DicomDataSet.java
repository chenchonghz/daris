package daris.client.model.dataset;

import arc.mf.client.RemoteServer;
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

    public String viewerUrl() {
        if (data() == null && !RemoteServer.haveSession()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(com.google.gwt.user.client.Window.Location.getProtocol());
        sb.append("//");
        sb.append(com.google.gwt.user.client.Window.Location.getHost());
        sb.append("/daris/dicom.mfjp?_skey=");
        sb.append(RemoteServer.sessionId());
        sb.append("&module=view&id=");
        sb.append(assetId());
        return sb.toString();
    }

}
