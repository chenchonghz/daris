package daris.client.model.query.filter.mf;

import java.util.Date;

import arc.mf.client.util.DateTime;
import arc.mf.client.xml.XmlElement;
import daris.client.util.DateUtil;

public class DateRange extends Range<Date> {

    private boolean _dateOnly;

    public DateRange(Date from, boolean fromInclusive, Date to, boolean toInclusive, boolean dateOnly) {
        super(DateUtil.copyDate(from, dateOnly), fromInclusive, DateUtil.copyDate(to, dateOnly), toInclusive);
        _dateOnly = dateOnly;
    }

    public DateRange(XmlElement xe) throws Throwable {
        super(xe);
    }

    public boolean dateOnly() {
        return _dateOnly;
    }

    public void setDateOnly(boolean dateOnly) {
        _dateOnly = dateOnly;
        if (_dateOnly == true) {
            if (from() != null) {
                setFrom(DateUtil.clearTime(from()));
            }
            if (to() != null) {
                setTo(DateUtil.clearTime(to()));
            }
        }
    }

    protected String format(Date date) {
        StringBuilder sb = new StringBuilder();
        sb.append("'");
        sb.append(_dateOnly ? DateTime.SERVER_DATE_FORMAT.format(date) : DateTime.SERVER_DATE_TIME_FORMAT.format(date));
        sb.append("'");
        return sb.toString();
    }

    @Override
    protected Date parse(String s) {
        if (s.indexOf(':') == -1) {
            return DateTime.SERVER_DATE_FORMAT.parse(s);
        } else {
            return DateTime.SERVER_DATE_TIME_FORMAT.parse(s);
        }
    }

}
