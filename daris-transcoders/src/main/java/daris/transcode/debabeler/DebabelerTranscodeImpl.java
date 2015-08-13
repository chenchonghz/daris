package daris.transcode.debabeler;

import java.io.File;
import java.util.Map;

import arc.mime.MimeType;
import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public abstract class DebabelerTranscodeImpl extends DarisTranscodeImpl {

    @Override
    protected boolean resourceManaged() {
        return true;
    }

    @Override
    public final DarisTranscodeProvider provider() {
        return DebabelerTranscodeProvider.INSTANCE;
    }

    protected abstract String target();

    protected abstract String mappingFileName();

    @Override
    protected void transcode(File inputDir, MimeType fromType, MimeType toType,
            Map<String, String> params) throws Throwable {
        Debabeler.execute(inputDir.getAbsolutePath(), target(),
                mappingFileName());
    }

}
