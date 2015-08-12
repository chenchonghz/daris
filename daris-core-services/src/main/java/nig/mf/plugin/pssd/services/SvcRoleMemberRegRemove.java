package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.plugin.util.AssetRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;



public class SvcRoleMemberRegRemove extends PluginService {

	private Interface _defn;

	public SvcRoleMemberRegRemove() {
		_defn = new Interface();
		_defn.add(new Interface.Element("role", StringType.DEFAULT, "The role to remove", 1, 1));

	}

	public String name() {
		return "om.pssd.role-member-registry.remove";
	}

	public String description() {
		return "Removes a local role from being available for use as a role-member when creating local projects.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
	throws Throwable {

		// Add the role ID on
		XmlDoc.Element role = args.element("role");
		PSSDUtils.checkRoleExists(executor(), role.value(), true);
		SvcRoleMemberRegAdd.addRoleID(executor(), role);

		// Find the registry asset. If does not exist yet return silently.
		String id = AssetRegistry.findRegistry(executor(), SvcRoleMemberRegAdd.REGISTRY_ASSET_NAME, AssetRegistry.AccessType.PUBLIC);
		if (id==null) return;

		// See if role already exists
		Boolean checkTopName = true;
		String docId = AssetRegistry.checkExists(executor(), id, SvcRoleMemberRegAdd.DOCTYPE, role, checkTopName);

		// Remove the role 
		if (docId!=null) {	
			AssetRegistry.removeItem(executor(), id, docId, role, SvcRoleMemberRegAdd.DOCTYPE);
			w.add("id", id);
		}
	}
}
