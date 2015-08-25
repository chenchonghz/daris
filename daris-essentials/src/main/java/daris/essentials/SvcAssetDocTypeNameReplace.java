package daris.essentials;

import java.util.Collection;

import nig.iio.metadata.XMLUtil;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.CiteableIdUtil;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetDocTypeNameReplace extends PluginService {

	private Interface _defn;

	public SvcAssetDocTypeNameReplace() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT, "Asset ID", 0, 1));
		_defn.add(new Element("cid",  CiteableIdType.DEFAULT, "Asset citeable ID", 0, 1));
		Interface.Element ae = new Interface.Element("replace", XmlDocType.DEFAULT, "The replacement details", 0, Integer.MAX_VALUE);
		ae.add(new Interface.Element("type-from", StringType.DEFAULT, "The old name of the document type.", 1, 1));
		ae.add(new Interface.Element("type-to", StringType.DEFAULT, "The new name of the document type.", 1, 1));
		_defn.add(ae);
	}

	public String name() {

		return "nig.asset.doc.name.replace";

	}

	public String description() {

		return "This service is used to replace the name of a document type in an asset.  The old and new document type names are expected to exist and differ only in their name (not checked).";

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
		//
		Collection<XmlDoc.Element> replaces = args.elements("replace");
		if (replaces==null) throw new Exception ("No replacement element supplied");

		// Fetch the asset
		XmlDoc.Element el = AssetUtil.getAsset (executor(), cid, id);

		// Get documents
		XmlDoc.Element meta = el.element("asset/meta"); 
		if (meta==null) return;
		Collection<XmlDoc.Element> docs = meta.elements();

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
			String name = doc.qname();   //Qualified name includes meta namespace

			// Iterate over replacement strings
			for (XmlDoc.Element replace : replaces) {
				String old = replace.value("type-from");
				String to = replace.value("type-to");
				if (old.equals(name)) {
					some = true;
					dmRemove.add(XMLUtil.copyParentOnly(doc));
					//
					doc.setName(to);
					XMLUtil.removeAttribute( doc, "id");
					dmReplace.add(doc);
				}
			}	
		}	

		if (some) {
			// Remove old
			dmRemove.pop();
			executor().execute("asset.set", dmRemove.root());

			// Set new
			dmReplace.pop();
			executor().execute("asset.set", dmReplace.root());
		}
	}
}