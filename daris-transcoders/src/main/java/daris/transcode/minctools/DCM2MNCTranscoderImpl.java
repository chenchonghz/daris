package daris.transcode.minctools;

import java.io.File;
import java.util.Map;

import arc.mime.MimeType;
import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public class DCM2MNCTranscoderImpl extends DarisTranscodeImpl {

    DCM2MNCTranscoderImpl() {

    }

    @Override
    protected void transcode(File dir, MimeType fromType, MimeType toType,
            Map<String, String> params) throws Throwable {
        DCM2MNC.execute(dir);
    }

    @Override
    protected boolean resourceManaged() {
        return false;
    }

    @Override
    public String from() {
        return nig.mf.MimeTypes.DICOM_SERIES;
    }

    @Override
    public String to() {
        return nig.mf.MimeTypes.MINC_SERIES;
    }

    @Override
    public DarisTranscodeProvider provider() {
        return MincToolsTranscodeProvider.INSTANCE;
    }

}
