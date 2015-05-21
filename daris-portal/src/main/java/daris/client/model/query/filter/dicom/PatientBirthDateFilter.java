package daris.client.model.query.filter.dicom;

import java.util.Date;

import arc.mf.client.xml.XmlElement;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.operators.DateOperator;

public class PatientBirthDateFilter extends DateTimeFilter implements DicomPatient {

    public PatientBirthDateFilter() {
        this(DateOperator.EQ, new Date(0));
    }

    private PatientBirthDateFilter(DateOperator op, Date value) {
        super(op, new Date(value.getTime()), false);
    }

    public PatientBirthDateFilter(XmlElement xe) throws Throwable {
        super(xe);
    }

    @Override
    public Filter copy() {
        return new PatientBirthDateFilter(operator(), value());
    }

    @Override
    protected String xpath() {
        return DOC_TYPE + "/dob";
    }

    @Override
    public boolean canIncludeTime() {
        return false;
    }

}
