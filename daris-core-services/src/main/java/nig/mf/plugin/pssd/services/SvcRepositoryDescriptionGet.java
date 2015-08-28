package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.RepositoryDescription;
import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcRepositoryDescriptionGet extends PluginService {

    public static final String SERVICE_NAME = "daris.repository.description.get";
    public static final String SERVICE_DESCRIPTION = "Get the description (asset) of daris repository.";

    private Interface _defn;

    public SvcRepositoryDescriptionGet() {
        _defn = new Interface();
    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return SERVICE_DESCRIPTION;
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_ACCESS;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
            throws Throwable {
        XmlDoc.Element ae = RepositoryDescription.getAssetMeta(executor());
        if (ae != null) {
            w.push("repository", new String[] { "id", ae.value("@id") });
            w.add(ae, false);
            w.pop();
        }
    }
}
