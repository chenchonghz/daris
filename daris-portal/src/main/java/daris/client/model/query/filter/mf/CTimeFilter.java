package daris.client.model.query.filter.mf;

import java.util.Date;

import arc.mf.client.xml.XmlElement;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.CompareOperator;
import daris.client.util.DateUtil;

public class CTimeFilter extends TimeFilter {

    public CTimeFilter(CompareOperator op, Date time, boolean dateOnly) {
        super(op, time, dateOnly);
    }

    public CTimeFilter() {
        this(null, DateUtil.clearTime(new Date()), true);
    }

    public CTimeFilter(XmlElement xe) throws Throwable {
        super(xe);
    }

    @Override
    public Filter copy() {
        return new CTimeFilter(operator(), time(), dateOnly());
    }

    @Override
    public TimeType type() {
        return TimeType.ctime;
    }

}
