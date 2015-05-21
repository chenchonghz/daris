package nig.mf.plugin.sink.services;

import java.util.Collection;
import java.util.List;

import nig.mf.plugin.sink.OwnCloudSink;
import nig.mf.plugin.sink.ScpSink;
import nig.mf.plugin.sink.WebDAVSink;
import nig.mf.plugin.sink.settings.UserSelfSinkSettings;
import nig.ssh.client.Ssh;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcSinkDescribe extends PluginService {

    public static final String SERVICE_NAME = "nig.sink.describe";

    private Interface _defn;

    public SvcSinkDescribe() {
        _defn = new Interface();
        _defn.add(new Interface.Element("name", StringType.DEFAULT,
                "The name of the sink. If not specified, all the sinks will be described.", 0, Integer.MAX_VALUE));
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
        return "Describes the given data sink.  It fills in values already set from when the sink was created (sink.describe).  Parameters with attribute 'constant' were set when the sink was created and cannot be over-ridden at run time.  Parameters with the attribute ''securable'' can be fecthed via a reference to the secure wallet.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        Collection<String> sinkNames = args.values("name");
        if (sinkNames == null || sinkNames.isEmpty()) {
            sinkNames = executor().execute("sink.list").values("sink");
        }

        if (sinkNames == null || sinkNames.isEmpty()) {
            return;
        }

        for (String sinkName : sinkNames) {
            if (!sinkExists(executor(), sinkName)) {
                throw new IllegalArgumentException("Sink " + sinkName + " does not exist.");
            }
            XmlDoc.Element se = executor().execute("sink.describe", "<args><name>" + sinkName + "</name></args>", null,
                    null).element("sink");

            String sinkType = se.value("destination/type");
            XmlDoc.Element de = executor().execute("sink.type.describe", "<args><name>" + sinkType + "</name></args>",
                    null, null).element("sink");

            describeSink(sinkName, sinkType, de, se, w);
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    public static boolean sinkExists(ServiceExecutor executor, String sinkName) throws Throwable {
        Collection<String> sinkNames = executor.execute("sink.list").values("sink");
        if (sinkNames == null || sinkNames.isEmpty()) {
            return false;
        }
        return sinkNames.contains(sinkName);
    }

    private static void describeSink(String sinkName, String sinkType, XmlDoc.Element de, XmlDoc.Element se, XmlWriter w)
            throws Throwable {
        w.push("sink", new String[] { "name", sinkName, "type", sinkType });
        if ("file-system".equals(sinkType)) {
            describeFileSystemSinkArgs(sinkName, de, se, w);
        } else if (ScpSink.SINK_TYPE.equals(sinkType)) {
            describeScpSinkArgs(sinkName, de, se, w);
        } else if (WebDAVSink.SINK_TYPE.equals(sinkType)) {
            describeWebdavSinkArgs(sinkName, de, se, w);
        } else if (OwnCloudSink.SINK_TYPE.equals(sinkType)) {
            describeOwncloudSinkArgs(sinkName, de, se, w);
        } else {
            describeUnknownSinkArgs(sinkName, de, se, w);
        }
        w.pop();

    }

    private static void addDescription(XmlWriter w, String desc) throws Throwable {
        if (desc == null) {
            return;
        }
        int idx = desc.indexOf("]:");
        if (idx == -1) {
            return;
        } else {
            desc = desc.substring(idx + 2).trim();
            if (desc != null) {
                w.add("description", desc);
            }
        }
    }

    private static String parseType(String desc) {
        if (desc == null) {
            return null;
        }
        return desc.substring(1, desc.indexOf(']')).toLowerCase();
    }

    private static void describeFileSystemSinkArgs(String sinkName, XmlDoc.Element de, XmlDoc.Element se, XmlWriter w)
            throws Throwable {

        String directory = se.value("destination/arg[@name='directory']");
        w.push("arg", new String[] { "name", "directory", "type", directory == null ? "string" : "constant",
                "min-occurs", "1", "max-occurs", "1" });
        addDescription(w, de.value("arg[@name='directory']"));
        if (directory != null) {
            w.add("value", directory);
        }
        w.pop();

        String path = se.value("destination/arg[@name='path']");
        w.push("arg", new String[] { "name", "path", "type", "string", "min-occurs", "0", "max-occurs", "1",
                "max-occurs-in-user-settings", Integer.toString(UserSelfSinkSettings.ARG_MAX_OCCURS) });
        addDescription(w, de.value("arg[@name='path']"));
        if (path != null) {
            w.add("value", path);
        }
        w.pop();

        String save = se.value("destination/arg[@name='save']");
        w.push("arg", new String[] { "name", "save", "type", "enumeration", "enumerated-values", "content,meta,both",
                "min-occurs", "0", "max-occurs", "1" });
        addDescription(w, de.value("arg[@name='save']"));
        if (save != null) {
            w.add("value", save);
        } else {
            // Defaults to content
            w.add("value", "content");
        }
        w.pop();

        int decompress = se.intValue("destination/arg[@name='decompress']", 0);
        w.push("arg", new String[] { "name", "decompress", "type", "integer", "min-occurs", "0", "max-occurs", "1" });
        addDescription(w, de.value("arg[@name='decompress']"));
        w.add("value", decompress);
        w.pop();

    }

    private static void describeScpSinkArgs(String sinkName, XmlDoc.Element de, XmlDoc.Element se, XmlWriter w)
            throws Throwable {

        String host = se.value("destination/arg[@name='host']");
        w.push("arg",
                new String[] { "name", "host", "type", host == null ? "string" : "constant", "min-occurs", "1",
                        "max-occurs", "1", "max-occurs-in-user-settings",
                        Integer.toString(UserSelfSinkSettings.ARG_MAX_OCCURS) });
        addDescription(w, de.value("arg[@name='host']"));
        if (host != null) {
            w.add("value", host);
        }
        w.pop();

        String port = se.value("destination/arg[@name='port']");
        w.push("arg", new String[] { "name", "port", "type", host == null ? "integer" : "constant", "min-occurs", "1",
                "max-occurs", "1" });
        addDescription(w, de.value("arg[@name='port']"));
        if (port != null) {
            w.add("value", port);
        } else {
            // Defaults to 22
            w.add("value", Ssh.DEFAULT_PORT);
        }
        w.pop();

        String hostKey = se.value("destination/arg[@name='host-key']");
        w.push("arg", new String[] { "name", "host-key", "type", hostKey == null ? "text" : "constant", "min-occurs",
                "0", "max-occurs", "1" });
        addDescription(w, de.value("arg[@name='host-key']"));
        if (hostKey != null) {
            w.add("value", hostKey);
        }
        w.pop();

        String user = se.value("destination/arg[@name='user']");
        w.push("arg", new String[] { "name", "user", "type", "string", "min-occurs", "1", "max-occurs", "1" });
        addDescription(w, de.value("arg[@name='user']"));
        if (user != null) {
            w.add("value", user);
        }
        w.pop();

        String password = se.value("destination/arg[@name='password']");
        w.push("arg", new String[] { "name", "password", "type", "password", "min-occurs", "0", "max-occurs", "1",
                "securable", "true" });
        addDescription(w, de.value("arg[@name='password']"));
        if (password != null) {
            w.add("value", password);
        }
        w.pop();

        String privateKey = se.value("destination/arg[@name='private-key']");
        w.push("arg", new String[] { "name", "private-key", "type", "text", "min-occurs", "0", "max-occurs", "1",
                "securable", "true" });
        addDescription(w, de.value("arg[@name='private-key']"));
        if (privateKey != null) {
            w.add("value", privateKey);
        }
        w.pop();

        String passphrase = se.value("destination/arg[@name='passphrase']");
        w.push("arg", new String[] { "name", "passphrase", "type", "password", "min-occurs", "0", "max-occurs", "1",
                "securable", "true" });
        addDescription(w, de.value("arg[@name='passphrase']"));
        if (passphrase != null) {
            w.add("value", passphrase);
        }
        w.pop();

        String directory = se.value("destination/arg[@name='directory']");
        w.push("arg", new String[] { "name", "directory", "type", "string", "min-occurs", "0", "max-occurs", "1",
                "max-occurs-in-user-settings", Integer.toString(UserSelfSinkSettings.ARG_MAX_OCCURS) });
        addDescription(w, de.value("arg[@name='directory']"));
        if (directory != null) {
            w.add("value", directory);
        }
        w.pop();

        String decompress = se.value("destination/arg[@name='decompress']");
        w.push("arg", new String[] { "name", "decompress", "type", "boolean", "min-occurs", "0", "max-occurs", "1" });
        addDescription(w, de.value("arg[@name='decompress']"));
        if (decompress != null) {
            w.add("value", decompress);
        } else {
            // Defaults to false
            w.add("value", "false");
        }
        w.pop();

        String fileMode = se.value("destination/arg[@name='file-mode']");
        w.push("arg", new String[] { "name", "file-mode", "type", "string", "min-occurs", "0", "max-occurs", "1" });
        addDescription(w, de.value("arg[@name='file-mode']"));
        if (fileMode != null) {
            w.add("value", fileMode);
        } else {
            // Defaults to 0660
            w.add("value", ScpSink.DEFAULT_FILE_MODE);
        }
        w.pop();
    }

    private static void describeWebdavSinkArgs(String sinkName, XmlDoc.Element de, XmlDoc.Element se, XmlWriter w)
            throws Throwable {

        String url = se.value("destination/arg[@name='url']");
        w.push("arg", new String[] { "name", "url", "type", url == null ? "url" : "constant", "min-occurs", "1",
                "max-occurs", "1" });
        addDescription(w, de.value("arg[@name='url']"));
        if (url != null) {
            w.add("value", url);
        }
        w.pop();

        String directory = se.value("destination/arg[@name='directory']");
        w.push("arg", new String[] { "name", "directory", "type", "string", "min-occurs", "0", "max-occurs", "1",
                "max-occurs-in-user-settings", Integer.toString(UserSelfSinkSettings.ARG_MAX_OCCURS) });
        addDescription(w, de.value("arg[@name='directory']"));
        if (directory != null) {
            w.add("value", directory);
        } else {
            // Defaults to "/"
            w.add("default", "/");
        }
        w.pop();

        String user = se.value("destination/arg[@name='user']");
        w.push("arg", new String[] { "name", "user", "type", "string", "min-occurs", "1", "max-occurs", "1" });
        addDescription(w, de.value("arg[@name='user']"));
        if (user != null) {
            w.add("value", user);
        }
        w.pop();

        String password = se.value("destination/arg[@name='password']");
        w.push("arg", new String[] { "name", "password", "type", "password", "min-occurs", "1", "max-occurs", "1",
                "securable", "true" });
        addDescription(w, de.value("arg[@name='password']"));
        if (password != null) {
            w.add("value", password);
        }
        w.pop();

        String decompress = se.value("destination/arg[@name='decompress']");
        w.push("arg", new String[] { "name", "decompress", "type", "boolean", "min-occurs", "0", "max-occurs", "1" });
        addDescription(w, de.value("arg[@name='decompress']"));
        if (decompress != null) {
            w.add("value", decompress);
        } else {
            // Defaults to false
            w.add("value", "false");
        }
        w.pop();

    }

    private static void describeOwncloudSinkArgs(String sinkName, XmlDoc.Element de, XmlDoc.Element se, XmlWriter w)
            throws Throwable {

        describeWebdavSinkArgs(sinkName, de, se, w);

        String chunked = se.value("destination/arg[@name='chunked']");
        w.push("arg", new String[] { "name", "chunked", "type", "boolean", "min-occurs", "0", "max-occurs", "1" });
        addDescription(w, de.value("arg[@name='chunked']"));
        if (chunked != null) {
            w.add("value", chunked);
        } else {
            // Defaults to false
            w.add("value", "false");
        }
        w.pop();
    }

    private static void describeUnknownSinkArgs(String sinkName, XmlDoc.Element de, XmlDoc.Element se, XmlWriter w)
            throws Throwable {
        List<XmlDoc.Element> ades = de.elements("arg");
        if (ades == null) {
            return;
        }
        for (XmlDoc.Element ade : ades) {
            String argName = ade.value("@name");
            String argType = parseType(ade.value());
            String argValue = se.value("destination/arg[@name='" + argName + "']");
            w.push("arg", new String[] { "name", argName, "type", argValue == null ? argType : "constant",
                    "min-occurs", "1", "max-occurs", "1" });
            addDescription(w, ade.value());
            if (argValue != null) {
                w.add("value", argValue);
            }
            w.pop();
        }
    }
}
