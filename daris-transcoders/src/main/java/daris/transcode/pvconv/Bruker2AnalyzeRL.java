package daris.transcode.pvconv;

import daris.transcode.pvconv.PVCONV.Target;

public class Bruker2AnalyzeRL extends PVCONVTranscodeImpl {

    Bruker2AnalyzeRL() {

    }

    @Override
    protected Target target() {
        return PVCONV.Target.ANALYZE_RL;
    }

    @Override
    public String from() {
        return nig.mf.MimeTypes.BRUKER_SERIES;
    }

    @Override
    public String to() {
        return nig.mf.MimeTypes.ANALYZE_SERIES_RL;
    }

}
