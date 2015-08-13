package daris.transcode.minctools;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public class MincToolsTranscodeProvider implements DarisTranscodeProvider {

    public static final String NAME = "minc-tools";
    public static final String DESC = "minc-tools dicom to minc trancoder.";
    public static final MincToolsTranscodeProvider INSTANCE = new MincToolsTranscodeProvider();

    private List<DarisTranscodeImpl> _impls;

    private MincToolsTranscodeProvider() {
        _impls = new Vector<DarisTranscodeImpl>();
        _impls.add(new DCM2MNCTranscoderImpl());
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public String description() {
        return DESC;
    }

    @Override
    public Collection<DarisTranscodeImpl> transcodeImpls() {
        return _impls;
    }

}
