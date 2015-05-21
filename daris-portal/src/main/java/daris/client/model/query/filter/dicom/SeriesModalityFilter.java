package daris.client.model.query.filter.dicom;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.dicom.DicomModality;
import daris.client.model.query.filter.Filter;

public class SeriesModalityFilter extends Filter implements DicomSeries {

    private String _value;

    private SeriesModalityFilter(String value) {
        _value = value;
    }

    public SeriesModalityFilter() {
        this((String) null);
    }

    public SeriesModalityFilter(XmlElement xe) {
        _value = xe.value("value");
    }

    public DicomModality value() {
        if (_value == null) {
            return null;
        } else {
            return new DicomModality(_value, null);
        }
    }

    public void setValue(DicomModality value) {
        _value = value.name;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("xpath(" + DOC_TYPE + "/modality)='");
        sb.append(_value);
        sb.append("'");
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("value", _value);
    }

    @Override
    public Validity valid() {
        if (_value == null) {
            return new IsNotValid("Modality value is not set.");
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new SeriesModalityFilter(_value);
    }

}
