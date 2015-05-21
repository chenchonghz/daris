package nig.mf.plugin.pssd.services;

import nig.mf.plugin.util.AssetRegistry;

import nig.mf.plugin.pssd.dicom.LocalDicomAE;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcDICOMAERegList extends PluginService {
	
	public static final String SERVICE_NAME = "om.pssd.dicom.ae.list";
	private Interface _defn;

	public SvcDICOMAERegList() {

		_defn = new Interface();
		_defn.add(new Interface.Element("type", new EnumType(new String[] { "local", "remote", "all" }),
				"The type of aes to list. Defaults to all.", 0, 1));
		_defn.add(new Interface.Element("access", new EnumType(new String[] {"public", "private", "all"}),
				"The access type for remote repositories. The default, 'all' means list private and public entries, 'public', means list public entries accessible to all users, 'private' means list entries accessible only by the calling user.", 0, 1));
	}

	public String name() {

		return SERVICE_NAME;
	}

	public String description() {

		return "Lists the contents of the DICOM Application Entity registry";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String access = args.stringValue("access", "all");
		AssetRegistry.AccessType accessType = AssetRegistry.parseAccessType(access);
		//
		String type = args.stringValue("type", "all");
		boolean showLocal = type.equals("local") || type.equals("all");
		boolean showRemote = type.equals("remote") || type.equals("all");
		if (showLocal) {
			LocalDicomAE.describe(executor(), w);
		}
		if (showRemote) {
			if (accessType==AssetRegistry.AccessType.ALL) {
				showRemoteAEList(w, AssetRegistry.AccessType.PUBLIC);
				showRemoteAEList(w, AssetRegistry.AccessType.PRIVATE);
			} else {
				showRemoteAEList(w, accessType);
			}
		}
	}

	private void showRemoteAEList(XmlWriter w, AssetRegistry.AccessType accessType) throws Throwable {


		// Find the Registry. Return if none yet.
		String id = AssetRegistry.findRegistry(executor(), SvcDICOMAEAdd.REGISTRY_ASSET_TYPE, accessType);
		if (id == null)
			return;

		// List
		String xpath = "asset/meta/" + SvcDICOMAEAdd.DOCTYPE + "/ae";
		AssetRegistry.list(executor(), id, xpath, w);
	}
}
