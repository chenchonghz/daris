package daris.transcode.debabeler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public class DebabelerTranscoderProvider implements DarisTranscodeProvider {

    public static final String NAME = "debabeler";
    public static final String DESC = "LONI Debabeler";

    public static final DebabelerTranscoderProvider INSTANCE = new DebabelerTranscoderProvider();

    private List<DarisTranscodeImpl> _impls;

    private DebabelerTranscoderProvider() {
        _impls = new Vector<DarisTranscodeImpl>();
        _impls.add(new DebabelerDicom2AnalyzeNL());
        _impls.add(new DebabelerDicom2AnalyzeRL());
        _impls.add(new DebabelerDicom2Nifti());
    }

    @Override
    public final String name() {
        return NAME;
    }

    @Override
    public final String description() {
        return DESC;
    }

    @Override
    public Collection<DarisTranscodeImpl> transcodeImpls() {
        return Collections.unmodifiableList(_impls);
    }

}
