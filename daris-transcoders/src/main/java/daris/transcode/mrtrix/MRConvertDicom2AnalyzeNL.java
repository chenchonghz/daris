package daris.transcode.mrtrix;

import java.io.File;
import java.util.Map;

import arc.mime.MimeType;
import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public class MRConvertDicom2AnalyzeNL extends DarisTranscodeImpl {

    MRConvertDicom2AnalyzeNL() {

    }

    @Override
    protected void transcode(File dir, MimeType fromType, MimeType toType,
            Map<String, String> params) throws Throwable {
        MRConvert.convertToAnalyzeNL(dir);
    }

    @Override
    protected boolean resourceManaged() {
        return true;
    }

    @Override
    public String from() {
        return nig.mf.MimeTypes.DICOM_SERIES;
    }

    @Override
    public String to() {
        return nig.mf.MimeTypes.ANALYZE_SERIES_NL;
    }

    @Override
    public DarisTranscodeProvider provider() {
        return MRConvertTranscodeProvider.INSTANCE;
    }

}
