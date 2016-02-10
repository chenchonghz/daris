package daris.client.model.exmethod;

import arc.mf.client.xml.XmlDoc;
import arc.mf.client.xml.XmlElement;

public class ExMethodSubjectStep extends ExMethodStep {
    private XmlElement _psMetaEditable;
    private XmlElement _rsMetaEditable;
    private XmlElement _psMeta;
    private XmlElement _rsMeta;

    public ExMethodSubjectStep(String exmId, String exmProute, String step,
            String name, State state, String notes, XmlElement psMetaEditable,
            XmlElement rsMetaEditable, boolean editable) {

        super(exmId, exmProute, step, name, state, notes, editable);

        _psMetaEditable = psMetaEditable;
        _rsMetaEditable = rsMetaEditable;
        _psMeta = null;
        _rsMeta = null;
    }

    /**
     * Project subject metadata.
     * 
     * @return
     */
    public XmlElement psPublicMetadata() {

        return _psMeta;
    }

    public XmlElement psPublicMetadataEditable() {

        return _psMetaEditable;
    }

    /**
     * R-Subject metadata.
     * 
     * @return
     */
    public XmlElement rsPublicMetadata() {

        return _rsMeta;
    }

    public XmlElement rsPublicMetadataEditable() {

        return _rsMetaEditable;
    }

    public void setPSPublicMetadata(String psMeta) {
        if (psMeta == null || psMeta.trim().equals("")) {
            _psMeta = null;
            return;
        }
        try {
            _psMeta = XmlDoc.parse("<ps-meta>" + psMeta + "</ps-meta>");
        } catch (Throwable e) {

        }
    }

    public void setRSPublicMetadata(String rsMeta) {
        if (rsMeta == null || rsMeta.trim().equals("")) {
            _rsMeta = null;
            return;
        }
        try {
            _rsMeta = XmlDoc.parse("<rs-meta>" + rsMeta + "</rs-meta>");
        } catch (Throwable e) {

        }
    }
}