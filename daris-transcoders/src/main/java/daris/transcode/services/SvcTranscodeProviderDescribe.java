package daris.transcode.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import daris.transcode.DarisTranscodeImpl;
import daris.transcode.DarisTranscodeProvider;
import daris.transcode.DarisTranscodeRegistry;

public class SvcTranscodeProviderDescribe extends PluginService {

    public static final String SERVICE_NAME = "daris.transcode.provider.describe";

    public static final String SERVICE_DESCRIPTION = "Descirbe DaRIS transcode providers.";

    private Interface _defn;

    public SvcTranscodeProviderDescribe() {

        _defn = new Interface();
        _defn.add(new Interface.Element("provider", StringType.DEFAULT,
                "The name of the provider.", 0, 1));

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
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w)
            throws Throwable {
        String name = args.value("provider");
        Collection<DarisTranscodeProvider> providers = DarisTranscodeRegistry
                .providers();
        if (providers != null) {
            for (DarisTranscodeProvider provider : providers) {
                if (name == null || name.equals(provider.name())) {
                    describe(provider, w);
                    if (name != null) {
                        break;
                    }
                }
            }
        }
    }

    private static void describe(DarisTranscodeProvider provider, XmlWriter w)
            throws Throwable {
        w.push("provider", new String[] { "name", provider.name() });
        w.add("description", provider.description());
        Collection<DarisTranscodeImpl> impls = provider.transcodeImpls();
        if (impls != null) {
            for (DarisTranscodeImpl impl : impls) {
                boolean active = impl.equals(DarisTranscodeRegistry
                        .getActiveTranscoderImpl(impl.from(), impl.to()));
                w.push("transcode",
                        new String[] { "active", Boolean.toString(active) });
                w.add("from", impl.from());
                w.add("to", impl.to());
                w.pop();
            }
        }
        w.pop();
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
