package daris.transcode.pvconv;

import daris.transcode.pvconv.PVCONV.Target;

public class Bruker2Minc extends PVCONVTranscodeImpl {

    Bruker2Minc() {

    }

    @Override
    protected Target target() {
        return PVCONV.Target.MINC;
    }

    @Override
    public String from() {
        return nig.mf.MimeTypes.BRUKER_SERIES;
    }

    @Override
    public String to() {
        return nig.mf.MimeTypes.MINC_SERIES;
    }

}
