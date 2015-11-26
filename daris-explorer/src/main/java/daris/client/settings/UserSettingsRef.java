package daris.client.settings;

import arc.mf.client.gui.Session;
import arc.mf.client.xml.XmlWriterNe;
import arc.mf.object.ObjectRef;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import daris.client.app.MainApp;

public class UserSettingsRef extends ObjectRef<UserSettings> {

    @Override
    public String idToString() {
        return Session.domain() + ":" + Session.user();
    }

    @Override
    protected UserSettings instantiate(Element xe) throws Throwable {
        XmlDoc.Element se = xe.element("settings");
        if (se != null) {
            return new UserSettings(se);
        }
        return new UserSettings();
    }

    @Override
    public String referentTypeName() {
        return "user.self.settings";
    }

    @Override
    protected void resolveServiceArgs(XmlWriterNe w) {
        w.add("app", MainApp.APP);
    }

    @Override
    protected String resolveServiceName() {
        return "user.self.settings.get";
    }

    private UserSettingsRef() {
    }

    private static UserSettingsRef _instance;

    public static UserSettingsRef get() {
        if (_instance == null) {
            _instance = new UserSettingsRef();
        }
        return _instance;
    }

}
