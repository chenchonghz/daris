package nig.mf.plugin.sink.user;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDocWriter;
import arc.xml.XmlWriter;

public class UserSelfSettings {

    public static void set(ServiceExecutor executor, String app, XmlDoc.Element se) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        XmlWriter w = new XmlDocWriter(dm);
        w.add("app", app);
        w.push("settings");
        w.add(se, false);
        w.pop();
        executor.execute("user.self.settings.set", dm.root());
    }

    public static XmlDoc.Element get(ServiceExecutor executor, String app) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("app", app);
        return executor.execute("user.self.settings.get", dm.root()).element("settings");
    }

    public static void reset(ServiceExecutor executor, String app) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("app", app);
        executor.execute("user.self.settings.remove", dm.root());
    }

}
