package daris.transcode.debabeler;

public class DebabelerDicom2AnalyzeRL extends DebabelerTranscodeImpl {
    public static final String TARGET = "analyze";
    public static final String MAPPING_FILE_NAME = "DicomToAnalyze_RL_Wilson_05Jan2007.xml";

    DebabelerDicom2AnalyzeRL() {

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
        return nig.mf.MimeTypes.ANALYZE_SERIES_RL;
    }

}
