package daris.client.model.query.filter.dicom;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.StringOperator;

public class PatientNameFilter extends Filter implements DicomPatient {

    public static enum NameType {
        first, last, middle, prefix, suffix, other, full;
        public static NameType fromString(String s) {
            if (s != null) {
                NameType[] vs = values();
                for (NameType v : vs) {
                    if (v.name().equalsIgnoreCase(s)) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    private NameType _nt;
    private StringOperator _op;
    private String _value;

    public PatientNameFilter() {
        this(NameType.last, StringOperator.EQ, null);
    }

    private PatientNameFilter(NameType nt, StringOperator op, String value) {
        _nt = nt;
        _op = op;
        _value = value;
    }

    public PatientNameFilter(XmlElement xe) {
        _nt = NameType.fromString(xe.value("type"));
        _op = StringOperator.parse(xe.value("operator"));
        _value = xe.value("value");
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("xpath(" + DOC_TYPE + "/name[@type='");
        sb.append(_nt.name());
        sb.append("']) ");
        sb.append(_op.value());
        if (_op.numberOfValues() > 0) {
            sb.append(" '");
            sb.append(_value);
            sb.append("'");
        }
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("type", _nt);
        w.add("operator", _op.value());
        if (_op.numberOfValues() > 0) {
            w.add("value", _value);
        }
    }

    @Override
    public Validity valid() {
        if (_nt == null) {
            return new IsNotValid("name type is not set.");
        }
        if (_op == null) {
            return new IsNotValid("name operator is not set.");
        }
        if (_op.numberOfValues() > 0 && _value == null) {
            return new IsNotValid("name value is not set.");
        }
        return IsValid.INSTANCE;
    }

    public NameType nameType() {
        return _nt;
    }

    public void setNameType(NameType nameType) {
        _nt = nameType;
    }

    public StringOperator operator() {
        return _op;
    }

    public void setOperator(StringOperator operator) {
        _op = operator;
    }

    public String value() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    @Override
    public Filter copy() {
        return new PatientNameFilter(nameType(), operator(), value());
    }

}
