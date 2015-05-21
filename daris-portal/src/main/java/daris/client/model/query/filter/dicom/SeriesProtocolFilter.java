package daris.client.model.query.filter.dicom;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.StringOperator;

public class SeriesProtocolFilter extends Filter implements DicomSeries {

    private StringOperator _op;
    private String _value;

    public SeriesProtocolFilter() {
        this(StringOperator.CONTAINS, null);
    }

    private SeriesProtocolFilter(StringOperator op, String value) {
        _op = op;
        _value = value;
    }

    public SeriesProtocolFilter(XmlElement xe) {
        _op = StringOperator.parse(xe.value("operator"));
        _value = xe.value("value");
    }

    public StringOperator operator() {
        return _op;
    }

    public void setOperator(StringOperator op) {
        _op = op;
        if (_op == null || _op.numberOfValues() < 1) {
            _value = null;
        }
    }

    public String value() {
        return _value;
    }

    public void setValue(String value) {
        _value = value;
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("xpath(" + DOC_TYPE + "/protocol) ");
        sb.append(_op.value());
        if (_op.numberOfValues() > 0) {
            sb.append(" '");
            sb.append(_value);
            sb.append("'");
        }
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("operator", _op.value());
        if (_op.numberOfValues() > 0) {
            w.add("value", _value);
        }
    }

    @Override
    public Validity valid() {
        if (_op == null) {
            return new IsNotValid("id operator is not set.");
        }
        if (_op.numberOfValues() > 0 && _value == null) {
            return new IsNotValid("id value is not set.");
        }
        return IsValid.INSTANCE;
    }

    @Override
    public Filter copy() {
        return new SeriesProtocolFilter(operator(), value());
    }

}
