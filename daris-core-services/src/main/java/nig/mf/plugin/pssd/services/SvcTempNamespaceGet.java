package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.ApplicationProperty;
import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcTempNamespaceGet extends PluginService {

    private Interface _defn;

    public SvcTempNamespaceGet() {
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
        return "Gets the namespace for temporary assets.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        w.add("namespace", ApplicationProperty.TemporaryNamespace.get(executor()));
    }

    @Override
    public String name() {
        return "om.pssd.temp.namespace.get";
    }

}
