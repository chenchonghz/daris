package nig.mf.plugin.pssd.sc;

import java.util.Collection;

import nig.mf.plugin.pssd.sink.Sink;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlWriter;

public class DeliveryDestination {

    public static final String BROWSER = "browser";

    public static void list(ServiceExecutor executor, XmlWriter w) throws Throwable {
        w.add("destination", new String[] { "method", DeliveryMethod.download.name() }, DeliveryDestination.BROWSER);
        Collection<String> sinkUrls = Sink.listUrls(executor);
        if (sinkUrls != null) {
            for (String sinkUrl : sinkUrls) {
                w.add("destination", new String[] { "method", DeliveryMethod.deposit.name() }, sinkUrl);
            }
        }
    }

}
