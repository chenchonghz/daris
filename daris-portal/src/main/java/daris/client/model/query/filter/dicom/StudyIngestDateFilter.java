package daris.client.model.query.filter.dicom;

import java.util.Date;

import arc.mf.client.xml.XmlElement;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.DateOperator;

public class StudyIngestDateFilter extends DateTimeFilter implements DicomStudy {

    protected StudyIngestDateFilter(DateOperator op, Date value, boolean time) {
        super(op, value, time);
    }

    public StudyIngestDateFilter() {
        this(DateOperator.EQ, new Date(0), false);
    }

    public StudyIngestDateFilter(XmlElement xe) throws Throwable {
        super(xe);
    }

    @Override
    public Filter copy() {
        return new StudyIngestDateFilter(operator(), value(), includeTime());
    }

    @Override
    protected String xpath() {
        return DOC_TYPE + "/ingest/date";
    }

    @Override
    public boolean canIncludeTime() {
        return true;
    }

}
