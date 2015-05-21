package nig.mf.plugin;

import java.util.Collection;

import nig.iio.metadata.XMLUtil;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.CiteableIdUtil;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetDocNamespaceReplace extends PluginService {

	private Interface _defn;

	public SvcAssetDocNamespaceReplace() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT, "Asset ID", 0, 1));
		_defn.add(new Element("cid",  CiteableIdType.DEFAULT, "Asset citeable ID", 0, 1));
		_defn.add(new Interface.Element("type", StringType.DEFAULT, "The  document type.", 1, Integer.MAX_VALUE));
		_defn.add(new Interface.Element("from", StringType.DEFAULT, "The old value of the -ns attribute", 1, 1));
		_defn.add(new Interface.Element("to", StringType.DEFAULT, "The new value of the -ns attribute.", 1, 1));
		_defn.add(new Interface.Element("check-template", BooleanType.DEFAULT, "Check the :template element is consistent with target namespace/doc type (default true).", 0, 1));
	}

	public String name() {

		return "nig.asset.doc.namespace.replace";

	}

	public String description() {

		return "This service is used to replace the -ns attribute of a document instantiated on an asset. Does not modify :template element which may also require attending to (see asset.template.remove and asset.template.set).  However, it does check and throws an exception if there is an inconsistency.";

	}

	public Interface definition() {

		return _defn;

	}

	public Access access() {

		return ACCESS_ADMINISTER;

	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String id = args.value("id");
		String cid = args.value("cid");
		if (id==null && cid==null) throw new Exception ("You must supply 'id' or 'cid'");
		if (id!=null && cid!=null) throw new Exception ("You can't supply 'id' and 'cid'");
		if (cid!=null) id = CiteableIdUtil.cidToId(executor(), cid);
		if (cid==null) cid = CiteableIdUtil.idToCid(executor(), id);
		Collection<String> docTypes = args.values("type");
		String nsFrom = args.value("from");
		String nsTo = args.value("to");
		if (nsFrom.equals(nsTo)) {
			throw new Exception ("The from and to namespaces must differ");
		}
		boolean checkTemplate = args.booleanValue("check-template", true);


		// Iterate over doc types
		for (String docType : docTypes) {

			// Fetch the asset. Has to be inside the loops because 
			// the mere act of calling asset.set will change document
			// id attributes (primary document identifier)
			XmlDoc.Element asset = AssetUtil.getAsset (executor(), cid, id);

			// Get documents
			XmlDoc.Element meta = asset.element("asset/meta"); 
			if (meta==null) return;

			// Get template and check there are no inconsistencies for this document type
			// A further enhancement would be to fix the template field... (which can now be done)
			if (checkTemplate) {
				Collection<XmlDoc.Element> templates = asset.elements("asset/template");
				if (templates!=null) {
					for (XmlDoc.Element template : templates) {
						String tns = template.value("@ns");
						Collection<XmlDoc.Element> tmetas = template.elements("metadata");
						if (tmetas!=null) {
							for (XmlDoc.Element tmeta : tmetas) {
								String tname = tmeta.value("definition");
								if (tname.equals(docType)) {               // The target doc type has a template entry
									// Since the target has a namespace, we assume it's an error for the document type
									// to have a target namespace but be present in :template with no namespace.
									// May have to relax this !
									if (tns==null || !tns.equals(nsTo)) {
										throw new Exception ("The :template element is not consistent with your target namespace for type " + docType);
									}
								}
							}		
						}
					}
				}
			}


			// FInd all document instances for this document type
			Collection<XmlDoc.Element> docs = meta.elements(docType);

			// Modify their meta-data
			if (processDocumentType (executor(), id, nsFrom, nsTo, docs)) {
				w.add("id", new String[]{"type", docType, "cid", cid}, id);
			}
		}
	}

	private boolean processDocumentType (ServiceExecutor executor, String id, String nsFrom, String nsTo, 
			Collection<XmlDoc.Element> docs) throws Throwable {
		if (docs==null) return false;
		if (docs.size()==0) return false;


		// Build output list to remove.
		XmlDocMaker dmRemove = new XmlDocMaker("args");
		dmRemove.add("id", id);
		dmRemove.push("meta", new String[] {"action", "remove"});

		// Build replacement list
		XmlDocMaker dmReplace = new XmlDocMaker("args");
		dmReplace.add("id", id);
		dmReplace.push("meta");

		// Iterate over documents
		Boolean some = false;
		for (XmlDoc.Element doc : docs) {
			XmlDoc.Attribute att = doc.attribute("ns");
			if (att!=null) {
				String ns = att.value();
				if (ns.equals(nsFrom)) {
					some = true;
					dmRemove.add(XMLUtil.copyParentOnly(doc));

					// Replace -ns with new value
					doc.remove(att);
					att.setValue(nsTo);
					doc.add(att);


					// Remove -id attribute
					XmlDoc.Attribute att2 = doc.attribute("id");
					if (att2!=null) doc.remove(att2);

					// Add new document
					dmReplace.add(doc);
				}
			}
		}

		// Do the work for this doc type
		if (some) {
			// Remove old
			dmRemove.pop();
			executor().execute("asset.set", dmRemove.root());
			AssetUtil.getAsset (executor(), null, id);

			// Set new
			dmReplace.pop();
			executor().execute("asset.set", dmReplace.root());
			AssetUtil.getAsset (executor(), null, id);
			return true;
		}
		return false;
	}
}
