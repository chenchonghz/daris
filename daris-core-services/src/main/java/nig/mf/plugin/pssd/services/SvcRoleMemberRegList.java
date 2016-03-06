package nig.mf.plugin.pssd.services;

import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;
import nig.mf.plugin.pssd.util.AssetRegistry;

public class SvcRoleMemberRegList extends PluginService {
    private Interface _defn;

    public SvcRoleMemberRegList() {
        _defn = new Interface();

    }

    public String name() {
        return "om.pssd.role-member-registry.list";
    }

    public String description() {
        return "Lists the local roles that are available for use as a role-member when creating local projects.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_MODIFY;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out,
            XmlWriter w) throws Throwable {

        List<XmlDoc.Element> roleMemberElements = getRoleMembersFromRegistry(
                executor());
        if (roleMemberElements != null) {
            for (XmlDoc.Element rme : roleMemberElements) {
                w.add(rme);
            }
        }
    }

    static String getRoleMemberRegistryAssetId(ServiceExecutor executor)
            throws Throwable {
        return AssetRegistry.findRegistry(executor,
                SvcRoleMemberRegAdd.REGISTRY_ASSET_NAME,
                AssetRegistry.AccessType.PUBLIC);
    }

    static List<XmlDoc.Element> getRoleMembersFromRegistry(
            ServiceExecutor executor) throws Throwable {
        // Find the Registry. Return if none yet.
        String roleMemberRegistryAssetId = getRoleMemberRegistryAssetId(
                executor);
        if (roleMemberRegistryAssetId == null) {
            return null;
        }

        // get the roles from asset meta
        String xpath = "asset/meta/" + SvcRoleMemberRegAdd.DOCTYPE + "/role";
        return executor.execute("asset.get",
                "<args><id>" + roleMemberRegistryAssetId + "</id></args>", null,
                null).elements(xpath);
    }

}
