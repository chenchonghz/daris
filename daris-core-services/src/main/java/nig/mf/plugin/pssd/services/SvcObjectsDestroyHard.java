package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.user.Self;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectsDestroyHard extends PluginService {
	private Interface _defn;

	public SvcObjectsDestroyHard() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The citable ID of an optional parent object to constrain the search. Defaults to all objects.", 0, 1));
		_defn.add(new Interface.Element("asset-type", new EnumType(DistributedQuery.ResultAssetType.stringValues()),
				"Specify type of asset to find ('primary', 'replica' or 'all'). Defaults to all.", 0, 1));
		_defn.add(new Interface.Element("hard-destroy", BooleanType.DEFAULT,
						"Hard destroy assets (defaults to false) rather than just list the found assets.", 0, 1));
	}

	public String name() {
		return "om.pssd.objects.hard.destroy";
	}

	public String description() {
		return "Finds and optionally hard destroys any soft-deleted local PSSD objects (Projects and children, Methods, R-Subjects).";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        // Parse
		String id = args.value("id");
		
		// Must hold an "admin" role on local server
		Self.isAdmin(id, false);

		Boolean hardDestroy = args.booleanValue("hard-destroy", false);
		String aType = args.stringValue("asset-type", "all");
		DistributedQuery.ResultAssetType assetType = DistributedQuery.ResultAssetType.instantiate(aType);

		// Build query
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("pdist", 0);
		dm.add("size", "infinity");
		dm.add("include-destroyed", true);
		//
		String query = "(model starts with 'om.pssd') and (asset has been destroyed)";
		if (id!=null) query += " and (cid starts with '" + id + "' or cid='" + id + "')";
		//
		StringBuilder sb = new StringBuilder(query);
		DistributedQuery.appendResultAssetTypePredicate(sb, assetType);
		dm.add("where", sb.toString());
		//
		dm.add("action", "get-meta");
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		if (r==null) return;

		// Get collection
		Collection<XmlDoc.Element> assets = r.elements("asset");
		if (assets==null) return;

		// List and build destroy list
		dm = new XmlDocMaker("args");	
		for (XmlDoc.Element asset : assets) {
			String cid = asset.value("cid");
			String rid = asset.value("rid");
			String id2 = asset.value("@id");
			String type = asset.value("meta/daris:pssd-object/type");
			w.add("asset", new String[]{"cid", cid, "rid", rid, "type", type}, id2);    	
			//
			if (hardDestroy) {
				dm.add("id", id2);
			}
		}

		// Destroy the assets
		if (hardDestroy) {
			dm.add("members", false);
			executor().execute("asset.hard.destroy", dm.root());
		}

		// TBD Destroy CIDs ?

	}
}
