package daris.transcode.dcmtools;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public class DCMToolsTranscodeProvider implements DarisTranscodeProvider {

    public static final String NAME = "dcmtools";
    public static final String DESC = "DaRIS dcmtools Siemens DICOM to Siemens RDA transcoder.";
    public static final DCMToolsTranscodeProvider INSTANCE = new DCMToolsTranscodeProvider();

    private List<DarisTranscodeImpl> _impls;

    private DCMToolsTranscodeProvider() {
        _impls = new Vector<DarisTranscodeImpl>();
        _impls.add(new Dicom2SiemensRDA());
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
        return null;
    }
}
