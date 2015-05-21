package nig.mf.plugin.sink.services;

import java.util.Collection;

import nig.mf.plugin.sink.settings.UserSelfSinkSettings;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcUserSelfSinkSettingsGet extends PluginService {

    public static final String SERVICE_NAME = "nig.user.self.sink.settings.get";

    private Interface _defn;

    public SvcUserSelfSinkSettingsGet() {
        _defn = new Interface();
        Interface.Element se = new Interface.Element("sink", XmlDocType.DEFAULT,
                "The sink. If not given, all the sink settings will be returned.", 0, Integer.MAX_VALUE);
        se.add(new Interface.Element("name", StringType.DEFAULT, "The sink name.", 1, 1));
        _defn.add(se);
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
        return "Gets the current user's sink settings.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        Collection<String> sinkNames = args.values("sink/name");
        if (sinkNames == null || sinkNames.isEmpty()) {
            sinkNames = executor().execute("sink.list").values("sink");
        }

        if (sinkNames == null || sinkNames.isEmpty()) {
            return;
        }

        for (String sinkName : sinkNames) {
            if (!sinkExists(executor(), sinkName)) {
                throw new IllegalArgumentException("Sink: " + sinkName + " does not exist.");
            }
            XmlDoc.Element se = UserSelfSinkSettings.get(executor(), sinkName);
            if (se != null) {
// @formatter:off
//            List<XmlDoc.Element> aes = se.elements();
//            if (aes != null) {
//                for (XmlDoc.Element ae : aes) {
//                    String swkey = ae.value("@swkey");
//                    if (swkey != null) {
//                        ae.setValue(SecureWallet.getValue(executor(), swkey));
//                    }
//                }
//            }
// @formatter:on
                w.push("sink", new String[] { "name", sinkName, "type", getSinkType(executor(), sinkName) });
                w.add(se, false);
                w.pop();
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    public static String getSinkType(ServiceExecutor executor, String sinkName) throws Throwable {
        return executor.execute("sink.describe", "<args><name>" + sinkName + "</name></args>", null, null).value(
                "sink/destination/type");
    }

    public static boolean sinkExists(ServiceExecutor executor, String sinkName) throws Throwable {
        Collection<String> sinkNames = executor.execute("sink.list").values("sink");
        if (sinkNames == null) {
            return false;
        }
        return sinkNames.contains(sinkName);
    }

}
