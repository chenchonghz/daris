package daris.transcode.pvconv;

import daris.transcode.pvconv.PVCONV.Target;

public class Bruker2AnalyzeNL extends PVCONVTranscodeImpl {

    Bruker2AnalyzeNL() {

    }

    @Override
    protected Target target() {
        return PVCONV.Target.ANALYZE_NL;
    }

    @Override
    public String from() {
        return nig.mf.MimeTypes.BRUKER_SERIES;
    }

    @Override
    public String to() {
        return nig.mf.MimeTypes.ANALYZE_SERIES_NL;
    }
}
