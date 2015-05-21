package nig.mf.plugin.pssd.user.self.settings;

import nig.mf.plugin.pssd.Application;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public class DarisUserSelfSettings {

    private DarisUserSelfSettings() {
    }

    public static void set(ServiceExecutor executor, XmlDoc.Element se)
            throws Throwable {
        UserSelfSettings.set(executor, Application.DARIS, se);
    }

    public static XmlDoc.Element get(ServiceExecutor executor) throws Throwable {
        return UserSelfSettings.get(executor, Application.DARIS);
    }

    public static void remove(ServiceExecutor executor) throws Throwable {
        UserSelfSettings.remove(executor, Application.DARIS);
    }

    public static void setElement(ServiceExecutor executor, XmlDoc.Element ee)
            throws Throwable {
        UserSelfSettings.setElement(executor, Application.DARIS, ee);
    }

    public static XmlDoc.Element getElement(ServiceExecutor executor,
            String element) throws Throwable {
        return UserSelfSettings
                .getElement(executor, Application.DARIS, element);
    }

    public static void removeElement(ServiceExecutor executor, String element)
            throws Throwable {
        UserSelfSettings.removeElement(executor, Application.DARIS, element);
    }
}
