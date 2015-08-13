package daris.transcode;

import java.util.Collection;

public interface DarisTranscodeProvider {

    String name();

    String description();

    Collection<DarisTranscodeImpl> transcodeImpls();
}
