package daris.plugin.services;

import java.io.File;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDownloaderPut extends PluginService {

    public static final String SERVICE_NAME = "daris.downloader.put";

    public static final String JAR_PATH = "plugin/lib/daris-downloader.jar";

    public static final String WINDOWS_EXE_PATH = "plugin/lib/daris-downloader-win.zip";

    public static final String MAC_APP_PATH = "plugin/lib/daris-downloader-mac.zip";

    private Interface _defn;

    public SvcDownloaderPut() {
        _defn = new Interface();
        addToDefn(_defn);
    }

    static void addToDefn(Interface defn) {
        defn.add(new Interface.Element("platform", new EnumType(new String[] { "java", "mac", "windows" }),
                "The target platform to run the downloader. Defaults to java.", 0, 1));
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
        return "Upload daris-downloader.jar into $MFLUX_HOME/plugin/lib/ directory.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

        String platform = args.stringValue("platform", "java");
        String downloaderPath = getDownloaderPath(executor(), platform);

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("file", downloaderPath);
        dm.add("replace", true);
        executor().execute("server.file.put", dm.root(), inputs, null);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public int minNumberOfInputs() {
        return 1;
    }

    @Override
    public int maxNumberOfInputs() {
        return 1;
    }

    public static String getDownloaderPath(ServiceExecutor executor, String platform) throws Throwable {
        String mfHome = executor.execute("server.java.environment").value("property[@key='mf.home']");
        StringBuilder sb = new StringBuilder();
        sb.append(mfHome).append(File.separator);
        if ("windows".equalsIgnoreCase(platform)) {
            sb.append(WINDOWS_EXE_PATH);
        } else if ("mac".equalsIgnoreCase(platform)) {
            sb.append(MAC_APP_PATH);
        } else {
            sb.append(JAR_PATH);
        }
        return sb.toString();
    }

}
