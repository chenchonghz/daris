package daris.client.model.query.filter.mf;

import java.util.Date;

import arc.mf.client.util.DateTime;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.CompareOperator;
import daris.client.util.DateUtil;

public abstract class TimeFilter extends Filter {



    private CompareOperator _op;
    private Date _time;
    private boolean _dateOnly;

    protected TimeFilter(CompareOperator op, Date time, boolean dateOnly) {
        _op = op;
        _time = time;
        _dateOnly = dateOnly;
    }

    protected TimeFilter(XmlElement xe) throws Throwable {
        _op = CompareOperator.parse(xe.value("operator"));
        String time = xe.value(type().toString());
        if (time == null) {
            _dateOnly = false;
            _time = null;
        } else {
            _dateOnly = time.indexOf(':') == -1;
            _time = xe.dateValue("ctime");
        }
    }

    public abstract TimeType type();

    public CompareOperator operator() {
        return _op;
    }

    public void setOperator(CompareOperator op) {
        _op = op;
    }

    public Date time() {
        return _time;
    }

    public void setTime(Date time) {
        _time = DateUtil.copyDate(time, _dateOnly);
    }

    public boolean dateOnly() {
        return _dateOnly;
    }

    public void setDateOnly(boolean dateOnly) {
        _dateOnly = dateOnly;
        if (_dateOnly) {
            setTime(_time);
        }
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append(type());
        sb.append(" ");
        sb.append(_op);
        sb.append(" '");
        sb.append(timeAsString());
        sb.append("'");
    }

    protected String timeAsString() {
        return (_dateOnly ? DateTime.SERVER_DATE_FORMAT : DateTime.SERVER_DATE_TIME_FORMAT).format(_time);
    }

    @Override
    protected void saveXml(XmlWriter w) {
        w.add("operator", _op);
        w.add(type().toString(), timeAsString());
    }

    @Override
    public Validity valid() {
        if (_op == null) {
            return new IsNotValid("operator is not set.");
        }
        if (_time == null) {
            return new IsNotValid(type().toString() + " is not set.");
        }
        return IsValid.INSTANCE;
    }

}
