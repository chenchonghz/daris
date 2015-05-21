package daris.client.model.query.filter.mf;

import java.util.Date;

import arc.mf.client.xml.XmlElement;
import daris.client.model.query.filter.Filter;
import daris.client.util.DateUtil;

public class CTimeRangeFilter extends TimeRangeFilter {

    public CTimeRangeFilter(DateRange range) {
        super(range);
    }

    public CTimeRangeFilter(XmlElement xe) throws Throwable {
        super(xe);
    }

    public CTimeRangeFilter() {
        this(new DateRange(DateUtil.clearTime(new Date()), true, new Date(), true, true));
    }

    @Override
    public Filter copy() {
        return new CTimeRangeFilter(new DateRange(range().from(), range().fromInclusive(), range().to(),
                range().toInclusive(), range().dateOnly()));
    }

    @Override
    public TimeType type() {
        return TimeType.ctime;
    }

}
