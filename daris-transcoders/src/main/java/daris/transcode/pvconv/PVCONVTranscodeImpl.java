package daris.transcode.pvconv;

import java.io.File;
import java.util.Map;

import arc.mime.MimeType;
import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public abstract class PVCONVTranscodeImpl extends DarisTranscodeImpl {

    @Override
    protected boolean resourceManaged() {
        return false;
    }

    @Override
    public final DarisTranscodeProvider provider() {
        return PVCONVTranscodeProvider.INSTANCE;
    }

    protected abstract PVCONV.Target target();

    @Override
    protected void transcode(File inputDir, MimeType fromType, MimeType toType,
            Map<String, String> params) throws Throwable {
        PVCONV.execute(inputDir, target());
    }

}
