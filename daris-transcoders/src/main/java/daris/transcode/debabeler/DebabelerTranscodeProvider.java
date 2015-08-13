package daris.transcode.debabeler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;

public class DebabelerTranscodeProvider implements DarisTranscodeProvider {

    public static final String NAME = "debabeler";
    public static final String DESC = "LONI Debabeler";

    public static final DebabelerTranscodeProvider INSTANCE = new DebabelerTranscodeProvider();

    private List<DarisTranscodeImpl> _impls;

    private DebabelerTranscodeProvider() {
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

    @Override
    public String executableFileName() {
        return Debabeler.JAR;
    }

}
