package nig.mf.plugin.pssd.sink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import arc.mf.plugin.ServiceExecutor;

public class Sink {

    public static final String URL_PREFIX = "sink:";

    public static final String SERVICE_SINK_LIST = "sink.list";

    public static boolean exists(ServiceExecutor executor, String name) throws Throwable {
        Collection<String> sinkNames = listNames(executor);
        if (sinkNames == null || sinkNames.isEmpty()) {
            return false;
        }
        return sinkNames.contains(name);
    }

    public static String nameFromUrl(String url) {
        return url.substring(URL_PREFIX.length());
    }

    public static String urlFromName(String name) {
        return URL_PREFIX + name;
    }

    public static Collection<String> listNames(ServiceExecutor executor) throws Throwable {
        Collection<String> sinkNames = executor.execute(SERVICE_SINK_LIST).values("sink");
        if (sinkNames == null || sinkNames.isEmpty()) {
            return null;
        }
        return sinkNames;
    }

    public static List<String> listUrls(ServiceExecutor executor) throws Throwable {
        Collection<String> sinkNames = listNames(executor);
        if (sinkNames == null || sinkNames.isEmpty()) {
            return null;
        }
        List<String> sinkUrls = new ArrayList<String>(sinkNames.size());
        for (String sinkName : sinkNames) {
            sinkUrls.add(urlFromName(sinkName));
        }
        return sinkUrls;
    }

}
