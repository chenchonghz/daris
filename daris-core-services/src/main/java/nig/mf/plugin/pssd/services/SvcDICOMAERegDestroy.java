package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.*;

import nig.mf.plugin.util.AssetRegistry;


public class SvcDICOMAERegDestroy extends PluginService {
	
	private Interface _defn;

	public SvcDICOMAERegDestroy() {
		_defn = new Interface();
		_defn.add(new Interface.Element("access", new EnumType(new String[] {"public", "private"}),
				"The access type; 'public', means accessible to all (only priviledged users can destroy it), 'private' means accessible only by the creator.", 1, 1));
	}

	public String name() {
		return "om.pssd.dicom.ae.destroy";
	}

	public String description() {
		return "Destroys the local DICOM Application Entity registry that registers DICOM AEs for use (e.g. send them data).";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
		
		//
		String access = args.stringValue("access");
		AssetRegistry.AccessType accessType = AssetRegistry.parseAccessType(access);

		// Destroy
		String id = AssetRegistry.destroyRegistry(executor(), SvcDICOMAEAdd.REGISTRY_ASSET_TYPE, accessType);
		if (id!=null) {
			w.add("id", id);
		}
	}
}
