package daris.transcode.debabeler;

public class DebabelerDicom2Nifti extends DebabelerTranscodeImpl {

    public static final String TARGET = "nifti";
    public static final String MAPPING_FILE_NAME = "DicomToNifti_Wilson_14Aug2012.xml";

    DebabelerDicom2Nifti() {
    }

    @Override
    protected final String target() {
        return TARGET;
    }

    @Override
    protected final String mappingFileName() {
        return MAPPING_FILE_NAME;
    }

    @Override
    public final String from() {
        return nig.mf.MimeTypes.DICOM_SERIES;
    }

    @Override
    public final String to() {
        return nig.mf.MimeTypes.NIFTI_SERIES;
    }

}
