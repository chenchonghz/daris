package daris.client.model.query.filter.dicom;

import java.util.Date;

import arc.mf.client.xml.XmlElement;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.DateOperator;

public class SeriesScanDateFilter extends DateTimeFilter implements DicomSeries {

    protected SeriesScanDateFilter(DateOperator op, Date value, boolean time) {
        super(op, value, time);
    }

    public SeriesScanDateFilter() {
        this(DateOperator.EQ, new Date(0), true);
    }

    public SeriesScanDateFilter(XmlElement xe) throws Throwable {
        super(xe);
    }

    @Override
    public Filter copy() {
        return new SeriesScanDateFilter(operator(), value(), includeTime());
    }

    @Override
    protected String xpath() {
        return DOC_TYPE + "/sdate";
    }

    @Override
    public boolean canIncludeTime() {
        return true;
    }
}
