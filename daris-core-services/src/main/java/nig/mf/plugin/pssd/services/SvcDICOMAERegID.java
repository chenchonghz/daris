package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.*;
import nig.mf.plugin.util.AssetRegistry;


public class SvcDICOMAERegID extends PluginService {
	private Interface _defn;

	public SvcDICOMAERegID() {
		_defn = new Interface();
		_defn.add(new Interface.Element("access", new EnumType(new String[] {"public", "private", "all"}),
				"The access type for remote repositories. The default, 'all' means list private and public entries, 'public', means list public entries accessible to all users, 'private' means list entries accessible only by the calling user.", 0, 1));
	}

	public String name() {
		return "om.pssd.dicom.ae.id";
	}

	public String description() {
		return "Find the asset ID of the local DICOM Application Entity registry asset.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
	throws Throwable {

		String access = args.stringValue("access", "all");
		AssetRegistry.AccessType accessType = AssetRegistry.parseAccessType(access);

		// Find the Registry. Return if none yet.
		if (accessType==AssetRegistry.AccessType.ALL){ 
			String id = AssetRegistry.findRegistry(executor(), SvcDICOMAEAdd.REGISTRY_ASSET_TYPE, AssetRegistry.AccessType.PUBLIC);
			if (id!=null) w.add("id", new String[]{"access", "public"}, id);
			id = AssetRegistry.findRegistry(executor(), SvcDICOMAEAdd.REGISTRY_ASSET_TYPE, AssetRegistry.AccessType.PRIVATE);
			if (id!=null) w.add("id", new String[]{"access", "private"}, id);
		} else {
			String id = AssetRegistry.findRegistry(executor(), SvcDICOMAEAdd.REGISTRY_ASSET_TYPE, accessType);
			if (id!=null) w.add("id", new String[]{"access", access}, id);
		}
	}	
}
