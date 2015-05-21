package daris.client.model.query.filter.dicom;

import java.util.Date;

import arc.mf.client.util.DateTime;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.DateOperator;

public abstract class DateTimeFilter extends Filter {
    private DateOperator _op;
    private Date _value;
    private boolean _time;

    protected DateTimeFilter(DateOperator op, Date value, boolean time) {
        _op = op;
        _value = new Date(value.getTime());
        _time = time;
    }

    protected DateTimeFilter(XmlElement xe) throws Throwable {
        _op = DateOperator.parse(xe.value("operator"));
        if (_op != null && _op.numberOfValues() > 0) {
            _value = xe.dateValue("value");
            _time = xe.booleanValue("value/@time", false);
        }
    }

    protected abstract String xpath();

    @Override
    public void save(StringBuilder sb) {
        sb.append("xpath(" + xpath() + ") ");
        sb.append(_op.value());
        if (_op.numberOfValues() > 0) {
            sb.append(" '");
            sb.append(stringValue());
            sb.append("'");
        }
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("operator", _op.value());
        w.add("value", new String[] { "time", Boolean.toString(_time) }, stringValue());
    }

    public Date value() {
        return _value;
    }

    public void setValue(Date value) {
        if (value == null) {
            _value = value;
        } else {
            _value = new Date(value.getTime());
        }
    }

    public String stringValue() {
        if (_value == null) {
            return null;
        } else {
            return _time ? DateTime.clientDateTimeFormat().format(_value) : DateTime
                    .clientDateFormat().format(_value);
        }
    }

    public DateOperator operator() {
        return _op;
    }

    public void setOperator(DateOperator op) {
        _op = op;
    }

    public boolean includeTime() {
        if (!canIncludeTime()) {
            return false;
        } else {
            return _time;
        }
    }

    public void setIncludeTime(boolean includeTime) {
        if (!canIncludeTime()) {
            return;
        } else {
            _time = includeTime;
        }
    }

    public abstract boolean canIncludeTime();

    @Override
    public Validity valid() {
        if (_op == null) {
            return new IsNotValid("date operator is not set.");
        }
        if (_value == null) {
            return new IsNotValid("date value is not set.");
        }
        return IsValid.INSTANCE;
    }

}
