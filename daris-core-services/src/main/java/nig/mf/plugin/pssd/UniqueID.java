package nig.mf.plugin.pssd;

import java.util.Collection;

import arc.mf.plugin.PluginThread;

public class UniqueID {

    public static long next(String name) throws Throwable {
        return PluginThread
                .serviceExecutor()
                .execute("uniqueid.next",
                        "<args><name>" + name + "</name></args>", null, null)
                .longValue("id");
    }

    public static long describe(String name) throws Throwable {
        return PluginThread
                .serviceExecutor()
                .execute("uniqueid.describe",
                        "<args><name>" + name + "</name></args>", null, null)
                .longValue("id");

    }

    public static Collection<String> list() throws Throwable {
        return PluginThread.serviceExecutor().execute("uniqueid.list")
                .values("id/@name");
    }

}
