package daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDownloaderPut extends PluginService {

    public static final String SERVICE_NAME = "daris.downloader.put";

    public static final String DOWNLOADER_PATH = "/plugin/lib/daris-downloader.jar";

    private Interface _defn;

    public SvcDownloaderPut() {
        _defn = new Interface();
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
        String mfHome = executor().execute("server.java.environment").value("property[@key='mf.home']");

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("file", mfHome + DOWNLOADER_PATH);
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

    public static String getDownloaderJarPath(ServiceExecutor executor) throws Throwable {
        String mfHome = executor.execute("server.java.environment").value("property[@key='mf.home']");
        StringBuilder sb = new StringBuilder();
        sb.append(mfHome);
        sb.append(SvcDownloaderPut.DOWNLOADER_PATH);
        return sb.toString();
    }

}
