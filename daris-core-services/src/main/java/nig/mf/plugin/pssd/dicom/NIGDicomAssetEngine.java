package nig.mf.plugin.pssd.dicom;

import java.util.List;
import java.util.Vector;

import nig.mf.plugin.pssd.dicom.series.SeriesProxyFactory;
import nig.mf.plugin.pssd.dicom.study.StudyProxyFactory;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dicom.DataElementMap;
import arc.mf.plugin.dicom.DataElementTag;
import arc.mf.plugin.dicom.DicomAssetEngine;
import arc.mf.plugin.dicom.DicomEngineContext;
import arc.mf.plugin.dicom.SeriesProxy;
import arc.mf.plugin.dicom.StudyProxy;

public class NIGDicomAssetEngine implements DicomAssetEngine {

    public static final String TYPE_NAME = "nig.dicom";

    private DicomIngestControls _ic;
    private DicomEngineContext _ec;

    public NIGDicomAssetEngine() {
        _ic = null;
        _ec = null;
    }

    // This Engine is not thread safe in that if the received files are
    // processed in parallel, they may well end up in too many Studies
    public boolean isThreadSafe() {
        return false;
    }

    public StudyProxy createStudyProxy(ServiceExecutor executor,
            String studyUID, DataElementMap dem) throws Throwable {
        return StudyProxyFactory.createStudyProxy(executor, _ec, studyUID, dem,
                _ic);
    }

    public SeriesProxy createSeriesProxy(ServiceExecutor executor,
            StudyProxy study, int id, DataElementMap dem) throws Throwable {
        return SeriesProxyFactory.createSeriesProxy(executor, _ec, study, id,
                dem, _ic);
    }

    /**
     * DICOM elements must be explicitly requested to be put in the map or they
     * won't be available for subsequent access
     * 
     */
    public List<DataElementTag> requiredElements() {
        Vector<DataElementTag> res = new Vector<DataElementTag>(50);

        res.add(DicomElements.IMAGE_TYPE);
        res.add(DicomElements.PATIENT_ID);
        res.add(DicomElements.PATIENT_NAME);
        res.add(DicomElements.PATIENT_AGE);
        res.add(DicomElements.PATIENT_BIRTH_DATE);
        res.add(DicomElements.PATIENT_BIRTH_TIME);
        res.add(DicomElements.PATIENT_SEX);
        res.add(DicomElements.PATIENT_SIZE);
        res.add(DicomElements.PATIENT_WEIGHT);

        res.add(DicomElements.INSTITUTION_NAME);
        res.add(DicomElements.STATION_NAME);
        res.add(DicomElements.STUDY_UID);
        res.add(DicomElements.STUDY_ID);
        res.add(DicomElements.STUDY_DESCRIPTION);
        res.add(DicomElements.STUDY_DATE);
        res.add(DicomElements.STUDY_TIME);
        res.add(DicomElements.MODALITY);
        res.add(DicomElements.EQUIPMENT_MANUFACTURER);
        res.add(DicomElements.REFERRING_PHYSICIANS_NAME);
        res.add(DicomElements.REFERRING_PHYSICIANS_PHONE);
        res.add(DicomElements.PERFORMING_PHYSICIANS_NAME);
        res.add(DicomElements.MAGNETIC_FIELD_STRENGTH);

        res.add(DicomElements.SERIES_UID);
        res.add(DicomElements.SERIES_NUMBER);
        res.add(DicomElements.SERIES_SOP_INSTANCE_UID);
        res.add(DicomElements.SERIES_DESCRIPTION);
        res.add(DicomElements.SERIES_SDATE);
        res.add(DicomElements.SERIES_STIME);
        res.add(DicomElements.SERIES_ADATE);
        res.add(DicomElements.SERIES_ATIME);
        res.add(DicomElements.INSTANCE_NB);
        res.add(DicomElements.PROTOCOL_NAME);
        res.add(DicomElements.IMAGE_COMMENT);

        res.add(DicomElements.IMAGE_ORIENTATION_PATIENT);
        res.add(DicomElements.IMAGE_POSITION_PATIENT);

        return res;
    }

    public void setConfiguration(Object config, DicomEngineContext ec) {
        DicomIngestControls ic = (DicomIngestControls) config;
        _ic = ic;
        _ec = ec;
    }

}
