package daris.client.model.query.filter.dicom;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.CompareOperator;

public class SeriesSizeFilter extends Filter implements DicomSeries {

    private int _value;
    private CompareOperator _op;

    protected SeriesSizeFilter(CompareOperator op, int value) {
        _op = op;
        _value = value;
    }

    public SeriesSizeFilter() {
        this(CompareOperator.GT, 0);
    }

    public SeriesSizeFilter(XmlElement xe) {
        _op = CompareOperator.parse(xe.value("operator"));
        try {
            _value = xe.intValue("value");
        } catch (Throwable e) {
            _value = 0;
            e.printStackTrace();
        }
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append("xpath(" + DOC_TYPE + "/size)");
        sb.append(_op.value());
        sb.append(_value);
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("operator", _op.value());
        w.add("value", _value);
    }

    @Override
    public Validity valid() {
        if (_op == null) {
            return new IsNotValid("Operator is not specified.");
        }
        return IsValid.INSTANCE;
    }

    public CompareOperator operator() {
        return _op;
    }

    public void setOperator(CompareOperator op) {
        _op = op;
    }

    public int value() {
        return _value;
    }

    public void setValue(int value) {
        _value = value;
    }

    @Override
    public Filter copy() {
        return new SeriesSizeFilter(_op, _value);
    }

}
