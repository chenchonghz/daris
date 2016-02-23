package nig.mf.plugin.pssd.dicom.series;

import java.util.Date;

import nig.mf.plugin.pssd.dicom.DicomElements;
import arc.mf.plugin.dicom.DataElementMap;
import arc.mf.plugin.dicom.DicomDateTime;

public class SeriesMetadata {

    private String _id;
    private String _uid;
    private String _protocol;
    private String _description;
    private String _modality;
    private Date _sdate;
    private Date _adate;
    private double[] _image_position;
    private double[] _image_orientation;
    private String _imageType;

    /**
     * Create the metadata to be stored with a series
     * 
     * @param dem
     * @return
     * @throws Throwable
     */
    public static SeriesMetadata createFrom(DataElementMap dem)
            throws Throwable {
        SeriesMetadata sm = new SeriesMetadata();
        sm.restore(dem);
        return sm;
    }

    public String UID() {
        return _uid;
    }

    public String id() {
        return _id;
    }

    public String protocol() {
        return _protocol;
    }

    public String description() {
        return _description;
    }

    public Date seriesDateTime() {
        return _sdate;
    }

    public Date acquisitionDateTime() {
        return _adate;
    }

    public String modality() {
        return _modality;
    }

    public double[] imagePosition() {
        return _image_position;
    }

    public double[] imageOrientation() {
        return _image_orientation;
    }

    public String imageType() {
        return _imageType;
    }

    private void restore(DataElementMap dem) throws Throwable {
        _uid = dem.stringValue(DicomElements.SERIES_UID);
        _id = dem.stringValue(DicomElements.SERIES_NUMBER);

        Date seriesScanDate = dem.dateValue(DicomElements.SERIES_SDATE);
        String seriesScanTime = dem.stringValue(DicomElements.SERIES_STIME);
        _sdate = DicomDateTime.dateTime(seriesScanDate, seriesScanTime);
        if (_sdate == null) {
            Date studyDate = dem.dateValue(DicomElements.STUDY_DATE);
            String studyTime = dem.stringValue(DicomElements.STUDY_TIME);
            _sdate = DicomDateTime.dateTime(studyDate, studyTime);
        }

        Date seriesAcquisitionDate = dem.dateValue(DicomElements.SERIES_ADATE);
        String seriesAcquisitionTime = dem.stringValue(DicomElements.SERIES_ATIME);
        _adate = DicomDateTime.dateTime(seriesAcquisitionDate, seriesAcquisitionTime);

        _description = dem.stringValue(DicomElements.SERIES_DESCRIPTION);
        // Give ourselves something to hang onto if there was no other
        // description.
        if (_description != null) {
            _description = _description.replaceAll("\\^", " - ");
        }

        _protocol = dem.stringValue(DicomElements.PROTOCOL_NAME);
        _modality = dem.stringValue(DicomElements.MODALITY);

        // Image orientation parameters. We assume these don't change
        // from slice to slice.
        _image_position = dem
                .doubleValues(DicomElements.IMAGE_POSITION_PATIENT);
        _image_orientation = dem
                .doubleValues(DicomElements.IMAGE_ORIENTATION_PATIENT);

        // Image Type
        _imageType = dem.stringValue(DicomElements.IMAGE_TYPE);
    }
}
