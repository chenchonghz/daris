package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.Application;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcNamespaceDefaultSet extends PluginService {

    public static final String SERVICE_NAME = "daris.namespace.default.set";
    public static final String SERVICE_DESCRIPTION = "The the default (root) namespace for DaRIS.";

    private Interface _defn;

    public SvcNamespaceDefaultSet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("namespace", StringType.DEFAULT,
                "The namespace to be used as default.", 1, 1));
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
        return SERVICE_DESCRIPTION;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String namespace = args.value("namespace");
        setDefaultNamespace(executor(), namespace);
    }

    public static void setDefaultNamespace(ServiceExecutor executor,
            String namespace) throws Throwable {
        boolean namespaceExists = executor.execute("asset.namespace.exists",
                "<args><namespace>" + namespace + "</namespace></args>", null,
                null).booleanValue("exists", false);
        if (!namespaceExists) {
            throw new Exception("The namespace: " + namespace
                    + " does not exist.");
        }
        boolean propertyExists = executor.execute(
                "application.property.exists",
                "<args><property app=\"\">" + Application.NAMESPACE_PROPERTY
                        + "</property></args>", null, null).booleanValue(
                "exists", false);
        if (propertyExists) {
            // set property
            executor.execute("application.property.set",
                    "<property app=\"daris\" name=\""
                            + Application.NAMESPACE_PROPERTY + "\">"
                            + namespace + "</property>", null, null);
        } else {
            // create property
            executor.execute("application.property.create",
                    "<property app=\"daris\" name=\""
                            + Application.NAMESPACE_PROPERTY + "\"><value>"
                            + namespace + "</value></property>", null, null);
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
