package daris.client.settings;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import arc.mf.client.util.UnhandledException;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDocWriter;
import arc.xml.XmlWriter;

public class Preferences {

    public static final String LOCATION = System.getProperty("user.home")
            + File.separator + ".daris" + File.separator + "preferences.xml";

    public static final String DEFAULT_DOWNLOAD_DIR = System
            .getProperty("user.home") + File.separator + "Downloads"
            + File.separator + "DaRIS";

    private String _downloadDir;
    private boolean _downloadAlwaysAsk;

    private Preferences(XmlDoc.Element xe) throws Throwable {
        _downloadDir = xe.stringValue("download/directory",
                DEFAULT_DOWNLOAD_DIR);
        _downloadAlwaysAsk = xe.booleanValue("download/always-ask", true);
    }

    private Preferences() {
        _downloadDir = DEFAULT_DOWNLOAD_DIR;
        _downloadAlwaysAsk = true;
    }

    public String downloadDirectory() {
        return _downloadDir;
    }

    public boolean downloadAlwaysAsk() {
        return _downloadAlwaysAsk;
    }

    public void save(XmlWriter w) throws Throwable {
        w.push("download");
        w.add("always-ask", _downloadAlwaysAsk);
        w.add("directory", _downloadDir);
        w.pop();
    }

    public void save() {
        try {
            XmlDocMaker dm = new XmlDocMaker("preferences");
            XmlDocWriter xw = new XmlDocWriter(dm);
            save(xw);
            XmlDoc.Element root = dm.root();
            OutputStreamWriter w = null;
            try {
                File file = new File(LOCATION);
                File dir = file.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                w = new OutputStreamWriter(
                        new BufferedOutputStream(new FileOutputStream(file)),
                        "UTF-8");
                w.write(root.toString());
            } finally {
                if (w != null) {
                    w.close();
                }
            }
        } catch (Throwable e) {
            UnhandledException.report("Saving connection settings", e);
        }
    }

    public static Preferences load() {
        File file = new File(LOCATION);
        if (file.exists()) {
            try {
                InputStreamReader reader = null;
                XmlDoc.Element xe = null;
                try {
                    reader = new InputStreamReader(
                            new BufferedInputStream(new FileInputStream(file)),
                            "UTF-8");
                    xe = new XmlDoc().parse(reader);
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
                if (xe == null) {
                    throw new IOException(
                            "Failed to parse " + file.getAbsolutePath() + ".");
                }
                return new Preferences(xe);
            } catch (Throwable e) {
                UnhandledException.report("Loading connection settings", e);
            }
        }
        return new Preferences();
    }

}
