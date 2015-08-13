package daris.transcode.minctools;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public class DCM2MNCTranscodeProvider implements DarisTranscodeProvider {

    public static final String NAME = "dcm2mnc";
    public static final String DESC = "minc-tools dcm2mnc dicom to minc trancoder.";
    public static final DCM2MNCTranscodeProvider INSTANCE = new DCM2MNCTranscodeProvider();

    private List<DarisTranscodeImpl> _impls;

    private DCM2MNCTranscodeProvider() {
        _impls = new Vector<DarisTranscodeImpl>();
        _impls.add(new DCM2MNCTranscodeImpl());
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
