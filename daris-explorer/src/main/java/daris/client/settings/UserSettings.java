package daris.client.settings;

import arc.mf.client.util.UnhandledException;
import arc.mf.desktop.server.ServiceCall;
import arc.mf.desktop.server.ServiceResponseHandler;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.model.task.DownloadOptions;
import arc.xml.XmlStringWriter;
import arc.xml.XmlWriter;

public class UserSettings {

    public static final String APP = "daris-explorer";

    private DownloadSettings _downloadSettings;

    UserSettings(XmlDoc.Element xe) throws Throwable {
        if (xe != null && xe.elementExists("download")) {
            _downloadSettings = new DownloadSettings(xe.element("download"));
        } else {
            _downloadSettings = new DownloadSettings();
        }
    }

    UserSettings() {
        _downloadSettings = new DownloadSettings();
    }

    public void save(XmlWriter w) throws Throwable {
        _downloadSettings.save(w);
    }

    public DownloadSettings downloadSettings() {
        return _downloadSettings;
    }

    public void save() throws Throwable {
        XmlStringWriter w = new XmlStringWriter();
        w.add("app", APP);
        w.push("settings");
        save(w);
        w.pop();
        new ServiceCall("user.self.settings.set").setArguments(w.document())
                .setResponseHandler(new ServiceResponseHandler() {

                    @Override
                    public boolean failed(Throwable ex) {
                        UnhandledException.report("Resolving user settings",
                                ex);
                        return false;
                    }

                    @Override
                    public void response(Element re) throws Throwable {
                    }
                });
    }

    public void setDownloadSettings(DownloadOptions downloadOptions) {
        _downloadSettings.set(downloadOptions);
    }

}
