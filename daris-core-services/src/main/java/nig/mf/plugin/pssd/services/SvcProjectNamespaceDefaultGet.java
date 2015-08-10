package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.Application;
import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcProjectNamespaceDefaultGet extends PluginService {

    public static final String SERVICE_NAME = "daris.project.namespace.default.get";

    public static final String SERVICE_DESCRIPTION = "The default namespace for DaRIS projects.";

    private Interface _defn;

    public SvcProjectNamespaceDefaultGet() {
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
    public void execute(Element arg0, Inputs arg1, Outputs arg2, XmlWriter w)
            throws Throwable {
        w.add("namespace", new String[] { "default", "true" },
                Application.defaultProjectNamespace(executor()));
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
