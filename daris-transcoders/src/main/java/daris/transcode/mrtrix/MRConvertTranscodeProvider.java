package daris.transcode.mrtrix;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public class MRConvertTranscodeProvider implements DarisTranscodeProvider {

    public static final String NAME = "mrconvert";
    public static final String DESC = "MRTRIX mrconvert transcoders.";
    public static final MRConvertTranscodeProvider INSTANCE = new MRConvertTranscodeProvider();

    private List<DarisTranscodeImpl> _impls;

    private MRConvertTranscodeProvider() {
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

    @Override
    public String executableFileName() {
        return MRConvert.CMD;
    }

}
