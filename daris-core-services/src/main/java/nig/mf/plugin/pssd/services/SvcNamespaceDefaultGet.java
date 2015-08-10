package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.Application;
import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcNamespaceDefaultGet extends PluginService {

    public static final String SERVICE_NAME = "daris.namespace.default.get";
    public static final String SERVICE_DESCRIPTION = "Returns the default (root) namespace for DaRIS.";
    private Interface _defn;

    public SvcNamespaceDefaultGet() {
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
        return SERVICE_DESCRIPTION;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        w.add("namespace", new String[] { "default", "true" },
                Application.defaultNamespace(executor()));
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
