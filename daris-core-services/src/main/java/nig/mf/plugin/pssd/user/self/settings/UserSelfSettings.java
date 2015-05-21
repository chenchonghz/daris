package nig.mf.plugin.pssd.user.self.settings;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDocWriter;
import arc.xml.XmlWriter;

public class UserSelfSettings {

    public static void set(ServiceExecutor executor, String app,
            XmlDoc.Element se) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        XmlWriter w = new XmlDocWriter(dm);
        w.add("app", app);
        w.push("settings");
        w.add(se, false);
        w.pop();
        executor.execute("user.self.settings.set", dm.root());
    }

    public static XmlDoc.Element get(ServiceExecutor executor, String app)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("app", app);
        return executor.execute("user.self.settings.get", dm.root()).element(
                "settings");
    }

    public static void remove(ServiceExecutor executor, String app)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("app", app);
        executor.execute("user.self.settings.remove", dm.root());
    }

    public static void setElement(ServiceExecutor executor, String app,
            XmlDoc.Element ee) throws Throwable {
        XmlDoc.Element se = get(executor, app);
        if (se == null) {
            se = new XmlDoc.Element("settings");
        }
        if (se.elementExists(ee.name())) {
            se.removeAll(se.element(ee.name()));
        }
        se.add(ee);
        set(executor, app, se);
    }

    public static XmlDoc.Element getElement(ServiceExecutor executor,
            String app, String element) throws Throwable {
        XmlDoc.Element se = get(executor, app);
        if (se == null) {
            return null;
        }
        return se.element(element);
    }

    public static void removeElement(ServiceExecutor executor, String app,
            String element) throws Throwable {
        XmlDoc.Element se = get(executor, app);
        if (se != null && se.elementExists(element)) {
            se.removeAll(se.element(element));
            set(executor, app, se);
        }
    }

}
