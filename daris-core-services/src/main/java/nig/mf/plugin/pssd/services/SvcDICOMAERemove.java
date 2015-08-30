package nig.mf.plugin.pssd.services;


import nig.mf.plugin.pssd.util.AssetRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcDICOMAERemove extends PluginService {


	private Interface _defn;

	public SvcDICOMAERemove() {
		_defn = new Interface();
		SvcDICOMAEAdd.addInterface(_defn, false, null);
		_defn.add(new Interface.Element("access", new EnumType(new String[] {"public", "private"}),
				"The access type. The default,  'public', means accessible to all, 'private' means accessible only by the creator.", 0, 1));
	}

	public String name() {
		return "om.pssd.dicom.ae.remove";
	}

	public String description() {
		return "Removes a DICOM Application Entity from the DICOM AE registry.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
	throws Throwable {

		XmlDoc.Element ae0 = args.element("ae");
		XmlDoc.Element ae = SvcDICOMAEAdd.rebuildAEInOrder (ae0);         // Static order so matching code works
		//
		String access = args.stringValue("access", "public");
		AssetRegistry.AccessType accessType = AssetRegistry.parseAccessType(access);

		// Add the 'remote' attribute. We also have a 'local' AE but this is stored
		// as a sever property and fetched by om.pssd.ae.list.  It's not stored
		// in the AE registry.
		XmlDoc.Attribute att = new XmlDoc.Attribute("type", "remote");
		ae.add(att);

		// Add the 'access' attribute
		att = new XmlDoc.Attribute("access", access);
		ae.add(att);

		// See if the singleton asset already exists; else create.
		String id = AssetRegistry.findRegistry(executor(), SvcDICOMAEAdd.REGISTRY_ASSET_TYPE, accessType);
		if (id==null) return;

		// See if pre-exists
		Boolean checkTopName = true;
		String docId = AssetRegistry.checkExists(executor(), id, SvcDICOMAEAdd.DOCTYPE, ae, checkTopName);

		// Remove the entry 
		if (docId!=null) {
			AssetRegistry.removeItem(executor(), id,  docId, ae, SvcDICOMAEAdd.DOCTYPE);
			w.add("id", id);
		}
	}
}
