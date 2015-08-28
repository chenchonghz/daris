package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.RepositoryDescription;
import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcRepositoryDescribe extends PluginService {

    public static final String SERVICE_NAME = "daris.repository.describe";

    public static final String SERVICE_DESCRIPTION = "Describes the repository, including Mediaflux server information.";

    private Interface _defn;

    public SvcRepositoryDescribe() {

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

        int nbProjects = executor()
                .execute(
                        "asset.query",
                        "<args><where>model='om.pssd.project'</where><pdist>0</pdist><action>count</action></args>",
                        null, null).intValue("value", 0);
        // Repository description
        XmlDoc.Element ae = RepositoryDescription.getAssetMeta(executor());
        if (ae == null) {
            w.push("repository");
        } else {
            String assetId = ae.value("@id");
            w.push("repository", new String[] { "id", assetId });
            w.add(ae.element("meta/" + RepositoryDescription.DOC_TYPE), false);
        }
        w.add("number-of-projects", nbProjects);
        // Mediaflux server information
        w.push("server");
        XmlDoc.Element se = executor().execute("server.identity").element(
                "server");
        w.add("uuid", se.value("uuid"));
        w.add("version", executor().execute("server.version").value("version"));
        if (se.elementExists("name")) {
            w.add("name", se.value("name"));
        }
        if (se.elementExists("organization")) {
            w.add("organization", se.value("organization"));
        }
        w.pop();
        w.pop();
    }
}
