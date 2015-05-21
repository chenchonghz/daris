package daris.client.model.query.filter.dicom;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;

public class PatientSexFilter extends Filter implements DicomPatient {

    public static enum Sex {
        male, female, other;
        public static Sex fromString(String s) {
            if (s != null) {
                Sex[] vs = values();
                for (Sex v : vs) {
                    if (v.name().equalsIgnoreCase(s)) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    private Sex _value;

    public PatientSexFilter() {
        this((Sex) null);
    }

    public PatientSexFilter(Sex value) {
        _value = value;
    }

    public PatientSexFilter(XmlElement xe) {
        _value = Sex.fromString(xe.value("value"));
    }

    public Sex value() {
        return _value;
    }

    public void setValue(Sex value) {
        _value = value;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("xpath(" + DOC_TYPE + "/sex)='");
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
            return new IsNotValid("sex value is not set.");
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new PatientSexFilter(value());
    }

}
