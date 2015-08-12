package nig.mf.plugin.pssd.services;

import nig.mf.plugin.util.AssetRegistry;
import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;


public class SvcRoleMemberRegDestroy extends PluginService {
	
	private Interface _defn;

	public SvcRoleMemberRegDestroy() {
		_defn = new Interface();
	}

	public String name() {
		return "om.pssd.role-member-registry.destroy";
	}

	public String description() {
		return "Destroys the local registry that registers local roles as being available for use as a role-member when creating local projects.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
        
		// Destroy
		String id = AssetRegistry.destroyRegistry(executor(), SvcRoleMemberRegAdd.REGISTRY_ASSET_NAME, AssetRegistry.AccessType.PUBLIC);	
		w.add("id", id);
	}
}
