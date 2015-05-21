package nig.mf.plugin.sink.settings;

import nig.mf.plugin.sink.user.UserSelfSettings;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public abstract class UserSelfSinkSettings {

    public static int ARG_MAX_OCCURS = 10;

    private static String sinkType(ServiceExecutor executor, String sinkName) throws Throwable {
        return executor.execute("sink.describe", "<args><name>" + sinkName + "</name></args>", null, null).value(
                "sink/destination/type");
    }

    public static String appName(ServiceExecutor executor, String sinkName) throws Throwable {
        return sinkType(executor, sinkName) + "-sink." + sinkName;
    }

    public static XmlDoc.Element get(ServiceExecutor executor, String sinkName) throws Throwable {

        return UserSelfSettings.get(executor, appName(executor, sinkName));
    }

    public static void remove(ServiceExecutor executor, String sinkName) throws Throwable {
        UserSelfSettings.reset(executor, appName(executor, sinkName));
    }

    public static void set(ServiceExecutor executor, String sinkName, XmlDoc.Element se) throws Throwable {
        UserSelfSettings.set(executor, appName(executor, sinkName), se);
    }
}
