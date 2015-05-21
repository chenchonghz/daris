package nig.mf.plugin.sink.user;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDocWriter;
import arc.xml.XmlWriter;

public class UserSettings {

    public static void set(ServiceExecutor executor, String domain, String user, String app, XmlDoc.Element se)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        XmlWriter w = new XmlDocWriter(dm);
        w.add("app", app);
        w.add("domain", domain);
        w.add("user", user);
        w.push("settings");
        w.add(se, false);
        w.pop();
        executor.execute("user.settings.set", dm.root());
    }

    public static XmlDoc.Element get(ServiceExecutor executor, String domain, String user, String app) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("app", app);
        dm.add("domain", domain);
        dm.add("user", user);
        return executor.execute("user.settings.get", dm.root()).element("settings");
    }

    public static void remove(ServiceExecutor executor, String domain, String user, String app, XmlDoc.Element se)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("app", app);
        dm.add("domain", domain);
        dm.add("user", user);
        dm.push("settings");
        dm.add(se);
        dm.pop();
        executor.execute("user.settings.remove.from", dm.root());
    }

    public static void reset(ServiceExecutor executor, String domain, String user, String app) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("app", app);
        dm.add("domain", domain);
        dm.add("user", user);
        executor.execute("user.settings.remove", dm.root());
    }

}
