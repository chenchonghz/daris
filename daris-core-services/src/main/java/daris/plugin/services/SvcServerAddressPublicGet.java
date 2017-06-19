package daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import nig.mf.plugin.pssd.Application;

public class SvcServerAddressPublicGet extends PluginService {

    public static final String SERVICE_NAME = "daris.server.address.public.get";

    private Interface _defn;

    public SvcServerAddressPublicGet() {
        _defn = new Interface();
    }

    @Override
    public Access access() {
        return ACCESS_ACCESS;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Get the public server address from application property.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        ServerAddress addr = getServerAddress(executor());
        if (addr != null) {
            w.push("server");
            w.add("host", addr.host);
            w.add("port", addr.port);
            w.add("transport", addr.transport);
            w.pop();
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    public static ServerAddress getServerAddress(ServiceExecutor executor) throws Throwable {
        XmlDoc.Element re = executor.execute("application.property.list",
                "<args><app>" + Application.DARIS + "</app></args>", null, null);
        if (re.elementExists("property[@name='" + SvcServerAddressPublicSet.PROPERTY_SERVER_HOST + "']")
                && re.elementExists("property[@name='" + SvcServerAddressPublicSet.PROPERTY_SERVER_PORT + "']")) {
            String host = re.value("property[@name='" + SvcServerAddressPublicSet.PROPERTY_SERVER_HOST + "']");
            int port = re.intValue("property[@name='" + SvcServerAddressPublicSet.PROPERTY_SERVER_PORT + "']");
            String transport = re
                    .value("property[@name='" + SvcServerAddressPublicSet.PROPERTY_SERVER_TRANSPORT + "']");
            return new ServerAddress(host, port, transport);
        }
        return null;
    }

    public static class ServerAddress {
        public final String host;
        public final int port;
        public final String transport;

        public ServerAddress(String host, int port, String transport) {
            this.host = host;
            this.port = port;
            this.transport = transport;
        }
    }

}
