package daris.transcode.mrtrix;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public class MRTrixTranscodeProvider implements DarisTranscodeProvider {

    public static final String NAME = "mrtrix";
    public static final String DESC = "MRTRIX mrconvert transcoders.";
    public static final MRTrixTranscodeProvider INSTANCE = new MRTrixTranscodeProvider();

    private List<DarisTranscodeImpl> _impls;

    private MRTrixTranscodeProvider() {
        _impls = new Vector<DarisTranscodeImpl>();
        _impls.add(new MRConvertDicom2Nifti());
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
