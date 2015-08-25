package daris.essentials;

import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * Specialised function to rename an element in a document type. Both elements must exist. Remove the old one after
 * finishing all renaming.
 * 
 * This service could be extended to operate across Document Types and/or assets
 * 
 * @author Neil Killeen
 * 
 */
public class SvcAssetDocElementRename extends PluginService {
	private Interface _defn;

	public SvcAssetDocElementRename() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT, "Asset ID", 0, 1));
		_defn.add(new Element("cid", CiteableIdType.DEFAULT, "The citeable id of the asset.", 0, 1));
     	_defn.add(new Element("type", StringType.DEFAULT, "The Document Type name.", 1, 1));
		_defn.add(new Element("old", StringType.DEFAULT, "The old element name.", 1, 1));
		_defn.add(new Element("new", StringType.DEFAULT, "The new element name.", 1, 1));
		_defn.add(new Element("allow-invalid-meta", BooleanType.DEFAULT, "Allow invalid meta-data; required if new doc type definition does not include old and new names (default true)", 0, 1));
	}

	public String name() {
		return "nig.asset.doc.element.rename";
	}

	public String description() {
		return "Specialized service to rename an element in a Document attached to a local asset. Both element names must exist in the Document.";
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
		String docType = args.value("type");
		String elNameOld = args.value("old");
		String elNameNew = args.value("new");
		boolean allowInvalid = args.booleanValue("allow-invalid-meta", true);

		// Get the asset meta-data for the give Document Type
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0);        // Force local
		XmlDoc.Element r = executor().execute("asset.get", doc.root());
		XmlDoc.Element docIn = r.element("asset/meta/" + docType);
		if (docIn==null) return;

		// Modify meta if exists
		if (docIn != null) {

			// Remove old document (replace will only replace 'matching' elements which is dumb...)
			AssetUtil.removeDocument(executor(), null, id, docIn);

			// Rename element in document
			docIn.renameElement(elNameOld, elNameNew);

			// Add new document (name space is embodied in docIn)
			doc = new XmlDocMaker("args");
			doc.add("id", id);
			doc.add("allow-invalid-meta", allowInvalid);
			doc.push("meta", new String[] { "action", "add" });
			doc.add(docIn);
			doc.pop();
			r = executor().execute("asset.set", doc.root());
		}

	}
}
