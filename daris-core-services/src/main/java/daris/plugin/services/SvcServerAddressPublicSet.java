package daris.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.plugin.pssd.Application;

public class SvcServerAddressPublicSet extends PluginService {

    public static final String SERVICE_NAME = "daris.server.address.public.set";

    public static final String PROPERTY_SERVER_HOST = "daris.server.public.host";
    public static final String PROPERTY_SERVER_PORT = "daris.server.public.port";
    public static final String PROPERTY_SERVER_TRANSPORT = "daris.server.public.transport";

    private Interface _defn;

    public SvcServerAddressPublicSet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("host", StringType.DEFAULT, "Host name or IP address.", 1, 1));
        _defn.add(new Interface.Element("port", new IntegerType(1, 65535), "Server port.", 1, 1));
        _defn.add(new Interface.Element("transport", new EnumType(new String[] { "http", "https", "tcp/ip" }),
                "Server transport.", 1, 1));
    }

    @Override
    public Access access() {
        return ACCESS_ADMINISTER;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Set server public address in application property.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        String host = args.value("host");
        int port = args.intValue("port");
        String transport = args.value("transport");

        setServerAddress(executor(), host, port, transport);
    }

    public static void setServerAddress(ServiceExecutor executor, String host, int port, String transport)
            throws Throwable {
        setServerHost(executor, host);
        setServerPort(executor, port);
        setServerTransport(executor, transport);
    }

    private static void setServerHost(ServiceExecutor executor, String host) throws Throwable {
        if (propertyExists(executor, PROPERTY_SERVER_HOST)) {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("property", new String[] { "app", Application.DARIS, "name", PROPERTY_SERVER_HOST }, host);
            executor.execute("application.property.set", dm.root());
        } else {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("ifexists", "ignore");
            dm.push("property", new String[] { "app", Application.DARIS, "name", PROPERTY_SERVER_HOST });
            dm.add("value", host);
            dm.pop();
            executor.execute("application.property.create", dm.root());
        }
    }

    private static void setServerPort(ServiceExecutor executor, int port) throws Throwable {
        if (propertyExists(executor, PROPERTY_SERVER_PORT)) {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("property", new String[] { "app", Application.DARIS, "name", PROPERTY_SERVER_PORT },
                    Integer.toString(port));
            executor.execute("application.property.set", dm.root());
        } else {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("ifexists", "ignore");
            dm.push("property", new String[] { "app", Application.DARIS, "name", PROPERTY_SERVER_PORT });
            dm.push("type", new String[] { "type", "integer" });
            dm.push("restriction", new String[] { "base", "integer" });
            dm.add("minimum", 1);
            dm.add("maximum", 65535);
            dm.pop();
            dm.pop();
            dm.add("value", port);
            dm.pop();
            executor.execute("application.property.create", dm.root());
        }
    }

    private static void setServerTransport(ServiceExecutor executor, String transport) throws Throwable {
        if (propertyExists(executor, PROPERTY_SERVER_TRANSPORT)) {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("property", new String[] { "app", Application.DARIS, "name", PROPERTY_SERVER_TRANSPORT }, transport);
            executor.execute("application.property.set", dm.root());
        } else {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("ifexists", "ignore");
            dm.push("property", new String[] { "app", Application.DARIS, "name", PROPERTY_SERVER_TRANSPORT });
            dm.add("value", transport);
            dm.pop();
            executor.execute("application.property.create", dm.root());
        }
    }

    private static boolean propertyExists(ServiceExecutor executor, String name) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("property", new String[] { "app", Application.DARIS }, name);
        return executor.execute("application.property.exists", dm.root()).booleanValue("exists");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
