package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.ApplicationProperty;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcTempNamespaceSet extends PluginService {

    private Interface _defn;

    public SvcTempNamespaceSet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("namespace", StringType.DEFAULT, "The namespace for temporary assets.", 1, 1));
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
        return "Sets the namespace for temporary assets.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        String ns = args.value("namespace");
        ApplicationProperty.TemporaryNamespace.set(executor(),ns);
    }

    @Override
    public String name() {
        return "om.pssd.temp.namespace.set";
    }

}
