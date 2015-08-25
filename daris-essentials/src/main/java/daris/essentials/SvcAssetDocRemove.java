package daris.essentials;

import java.util.Collection;

import nig.iio.metadata.XMLUtil;
import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcAssetDocRemove extends PluginService {
	private Interface _defn;

	public SvcAssetDocRemove() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT, "Asset ID", 0, 1));
		_defn.add(new Element("cid", CiteableIdType.DEFAULT, "The citeable id of the asset.", 0, 1));
		_defn.add(new Element("type", StringType.DEFAULT, "The Document Type name (include any document namespace).", 1, Integer.MAX_VALUE));
	}

	public String name() {
		return "nig.asset.doc.remove";
	}

	public String description() {
		return "Removes all instantiations of a document type, regardless of attributes (e.g. -ns),  on a local asset.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Get the ID of the specified Subject if any
		String id = args.value("id");
		String cid = args.value("cid");
		if (id == null && cid == null) {
			throw new Exception("Neither id nor cid was specified.");
		}
		if (id == null) id = AssetUtil.getId(executor(), cid);
		//
		Collection<String> docTypes = args.values("type");

		// Get the asset meta-data for the give Document Type
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("pdist", 0);        // Force local
		XmlDoc.Element r = executor().execute("asset.get", dm.root());
		if (r==null) return;

		// Iterate through found instantiations and build removal DM
		dm = new XmlDocMaker("args");
		dm.add("id", id);
		for (String docType : docTypes) {
			Collection<XmlDoc.Element> docs = r.elements("asset/meta/" + docType);
			if (docs!=null) { 
				dm.push("meta", new String[]{"action", "remove"});
				for (XmlDoc.Element doc : docs) {
					XmlDoc.Element topDoc = XMLUtil.copyParentOnly(doc);
					dm.add(topDoc);
				}
				dm.pop();
			}
		}

		// Do it all in one operation (so the -id attributes won't jump around)
		executor().execute("asset.set", dm.root());
	}
}
