package daris.transcode.pvconv;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public class PVCONVTranscodeProvider implements DarisTranscodeProvider {
    public static final String NAME = "pvconv";
    public static final String DESC = "pvconv.pl - a Bruker data converter";

    public static final PVCONVTranscodeProvider INSTANCE = new PVCONVTranscodeProvider();

    private List<DarisTranscodeImpl> _impls;

    private PVCONVTranscodeProvider() {
        _impls = new Vector<DarisTranscodeImpl>();
        _impls.add(new Bruker2Minc());
        _impls.add(new Bruker2AnalyzeRL());
        _impls.add(new Bruker2AnalyzeNL());
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
