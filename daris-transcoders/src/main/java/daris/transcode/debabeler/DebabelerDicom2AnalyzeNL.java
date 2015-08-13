package daris.transcode.debabeler;


public class DebabelerDicom2AnalyzeNL extends DebabelerTranscodeImpl {

    public static final String TARGET = "analyze";
    public static final String MAPPING_FILE_NAME = "DicomToAnalyze_NL_Wilson_05Jan2007.xml";

    DebabelerDicom2AnalyzeNL(){
        
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
        return nig.mf.MimeTypes.ANALYZE_SERIES_NL;
    }
}
