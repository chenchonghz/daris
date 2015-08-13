package daris.transcode.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import daris.transcode.DarisTranscodePluginModule;
import daris.transcode.DarisTranscodeRegistry;

public class SvcTranscodeActivate extends PluginService {

    public static final String SERVICE_NAME = "daris.transcode.activate";
    public static final String SERVICE_DESCRIPTION = "Activate a specific transcode from the given provider.";

    private Interface _defn;

    public SvcTranscodeActivate() {
        _defn = new Interface();
        _defn.add(new Interface.Element("from", StringType.DEFAULT,
                "The mime type to transcode from.", 1, 1));
        _defn.add(new Interface.Element("to", StringType.DEFAULT,
                "The mime type to transcode to.", 1, 1));
        _defn.add(new Interface.Element("provider", StringType.DEFAULT,
                "The mime type to transcode to.", 1, 1));

    }

    @Override
    public Access access() {
        return ACCESS_ADMINISTER;
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
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter arg3) throws Throwable {
        String provider = args.value("provider");
        String from = args.value("from");
        String to = args.value("to");
        if (!DarisTranscodeRegistry.hasProvider(provider)) {
            throw new Exception("Transcoder provider: " + provider
                    + " is not found.");
        }
        if (!DarisTranscodeRegistry.hasTranscoder(from, to, provider)) {
            throw new Exception("Transcoder provider: " + provider
                    + " does not have transcoder to transcode from " + from
                    + " to " + to + ".");
        }

        DarisTranscodeRegistry.activate(provider, from, to);
        DarisTranscodePluginModule.setTranscoderConfigValue(executor(), from,
                to, provider);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
