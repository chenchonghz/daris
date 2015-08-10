package nig.mf.plugin.pssd;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDocMaker;

public class Application {

    public static final String DARIS = "daris";
    public static final String NAME = "PSSD";

    public static final String NAMESPACE_PROPERTY = "daris.namespace.default";

    public static final String META_NAMESPACE = "daris";
    public static final String DICT_NAMESPACE = "daris";

    public final static String defaultNamespace(ServiceExecutor executor)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("property", new String[] { "app", "daris" }, NAMESPACE_PROPERTY);
        boolean propertyExists = executor.execute(
                "application.property.exists",
                "<args><property app=\"daris\">" + NAMESPACE_PROPERTY
                        + "</property></args>", null, null).booleanValue(
                "exists", false);
        if (propertyExists) {
            String ns = executor.execute(
                    "application.property.get",
                    "<args><property app=\"daris\">" + NAMESPACE_PROPERTY
                            + "</property></args>", null, null).value(
                    "property");
            boolean namespaceExists = executor.execute(
                    "asset.namespace.exists",
                    "<args><namespace>" + ns + "</namespace></args>", null,
                    null).booleanValue("exists", false);
            if (namespaceExists) {
                return ns;
            } else {
                throw new Exception("The default namespace: " + ns
                        + " does not exist.");
            }
        } else {
            if (executor.execute("asset.namespace.exists",
                    "<args><namespace>daris</namespace></args>", null, null)
                    .booleanValue("exists", false)) {
                executor.execute("application.property.create",
                        "<args><property app=\"daris\" name=\"" + NAMESPACE_PROPERTY
                                + "\"><value>daris</value></property></args>", null,
                        null);
                return "daris";
            } else if (executor.execute("asset.namespace.exists",
                    "<args><namespace>pssd</namespace></args>", null, null)
                    .booleanValue("exists", false)) {
                executor.execute("application.property.create",
                        "<args><property app=\"daris\" name=\"" + NAMESPACE_PROPERTY
                                + "\"><value>pssd</value></property></args>", null,
                        null);
                return "pssd";
            } else {
                throw new Exception(
                        "The default namespace: daris does not exist.");
            }
        }
    }

    public static String defaultProjectNamespace(ServiceExecutor executor)
            throws Throwable {
        String ns = defaultNamespace(executor);
        if ("pssd".equals(ns) || ns.endsWith("pssd")) {
            return ns;
        } else {
            return ns + "/pssd";
        }
    }
}
