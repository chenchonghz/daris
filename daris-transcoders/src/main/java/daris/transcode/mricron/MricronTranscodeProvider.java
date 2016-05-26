package daris.transcode.mricron;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;
import daris.transcode.minctools.DCM2MNC;

public class MricronTranscodeProvider implements DarisTranscodeProvider {
    public static final String NAME = "mricron";
    public static final String DESC = "mricron dcm2nii, a dicom to nifti trancoder.";
    public static final MricronTranscodeProvider INSTANCE = new MricronTranscodeProvider();

    private List<DarisTranscodeImpl> _impls;

    private MricronTranscodeProvider() {
        _impls = new ArrayList<DarisTranscodeImpl>();
        _impls.add(new DCM2NIITranscodeImpl());
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
        return DCM2MNC.CMD;
    }
}
