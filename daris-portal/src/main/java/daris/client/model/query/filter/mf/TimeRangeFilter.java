package daris.client.model.query.filter.mf;

import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.Validity;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import daris.client.model.query.filter.Filter;

public abstract class TimeRangeFilter extends Filter {

    private DateRange _range;

    protected TimeRangeFilter(DateRange range) {
        _range = range;
    }

    protected TimeRangeFilter(XmlElement xe) throws Throwable {
        XmlElement re = xe.element("range");
        _range = re == null ? null : new DateRange(re);
    }

    public abstract TimeType type();

    @Override
    public void save(StringBuilder sb) {
        sb.append(type());
        sb.append(" in range " + _range);
    }

    @Override
    protected void saveXml(XmlWriter w) {
        _range.save(w);
    }

    @Override
    public Validity valid() {
        if (_range.from() == null) {
            return new IsNotValid("lower " + type() + " value is not set.");
        }
        if (_range.to() == null) {
            return new IsNotValid("upper " + type() + " value is not set.");
        }
        return IsValid.INSTANCE;
    }

    public DateRange range() {
        return _range;
    }
}
