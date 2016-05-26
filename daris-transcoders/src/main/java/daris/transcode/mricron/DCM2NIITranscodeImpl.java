package daris.transcode.mricron;

import java.io.File;
import java.util.Map;

import arc.mime.MimeType;
import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public class DCM2NIITranscodeImpl extends DarisTranscodeImpl {

    DCM2NIITranscodeImpl() {

    }

    @Override
    protected void transcode(File dir, MimeType fromType, MimeType toType,
            Map<String, String> params) throws Throwable {
        DCM2NII.execute(dir);
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
        return nig.mf.MimeTypes.NIFTI_SERIES;
    }

    @Override
    public DarisTranscodeProvider provider() {
        return MricronTranscodeProvider.INSTANCE;
    }

}
