package daris.transcode.services;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;
import daris.transcode.DarisTranscodeRegistry;

public class SvcTranscodeList extends PluginService {

    public static final String SERVICE_NAME = "daris.transcode.list";

    public static final String SERVICE_DESCRIPTION = "List DaRIS transcodes.";

    private Interface _defn;

    public SvcTranscodeList() {
        _defn = new Interface();
        _defn.add(new Interface.Element("from", StringType.DEFAULT,
                "The source mime type.", 0, 1));
        _defn.add(new Interface.Element("active-only", BooleanType.DEFAULT,
                "List only the active transcodes. Defaults to true.", 0, 1));
    }

    @Override
    public Access access() {
        return ACCESS_ACCESS;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return SERVICE_DESCRIPTION;
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w)
            throws Throwable {
        String from = args.value("from");
        boolean activeOnly = args.booleanValue("active-only", true);
        if (activeOnly) {
            Collection<DarisTranscodeImpl> impls = DarisTranscodeRegistry
                    .getActiveTranscoderImpls();
            if (impls != null) {
                for (DarisTranscodeImpl impl : impls) {
                    if (from == null || from.equalsIgnoreCase(impl.from())) {
                        w.push("trancode", new String[] { "provider",
                                impl.provider().name(), "active", "true" });
                        w.add("from", impl.from());
                        w.add("to", impl.to());
                        w.pop();
                        if (from != null) {
                            break;
                        }
                    }
                }
            }
        } else {
            Collection<DarisTranscodeProvider> providers = DarisTranscodeRegistry
                    .providers();
            if (providers != null) {
                List<DarisTranscodeImpl> results = new java.util.ArrayList<DarisTranscodeImpl>();
                for (DarisTranscodeProvider provider : providers) {
                    Collection<DarisTranscodeImpl> impls = provider
                            .transcodeImpls();
                    if (impls != null) {
                        for (DarisTranscodeImpl impl : impls) {
                            if (from == null
                                    || from.equalsIgnoreCase(impl.from())) {
                                results.add(impl);
                            }
                        }
                    }
                }
                Collections.sort(results, new Comparator<DarisTranscodeImpl>() {

                    @Override
                    public int compare(DarisTranscodeImpl o1,
                            DarisTranscodeImpl o2) {
                        int f = o1.from().compareTo(o2.from());
                        if (f == 0) {
                            return o1.to().compareTo(o2.to());
                        } else {
                            return f;
                        }
                    }
                });
                if (!results.isEmpty()) {
                    for (DarisTranscodeImpl r : results) {
                        String provider = r.provider().name();
                        boolean active = r.equals(DarisTranscodeRegistry
                                .getActiveTranscoderImpl(r.from(), r.to()));
                        w.push("transcode", new String[] { "provider",
                                provider, "active", Boolean.toString(active) });
                        w.add("from", r.from());
                        w.add("to", r.to());
                        w.pop();
                    }
                }
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
