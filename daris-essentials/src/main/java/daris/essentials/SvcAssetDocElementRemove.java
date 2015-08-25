package daris.essentials;


import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

/**
 * Specialised function to remove an element in a document.
 * 
 * @author Neil Killeen
 * 
 */
public class SvcAssetDocElementRemove extends PluginService {
	private Interface _defn;

	public SvcAssetDocElementRemove() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT, "Asset ID", 0, 1));
		_defn.add(new Element("cid", CiteableIdType.DEFAULT, "The citeable id of the asset.", 0, 1));
		_defn.add(new Element("type", StringType.DEFAULT, "The Document Type name.", 1, 1));
		_defn.add(new Element("element", StringType.DEFAULT, "The meta-data element name to remove.", 1, 1));
		_defn.add(new Element("allow-invalid-meta", BooleanType.DEFAULT, "Allow invalid meta-data; required if new doc type definition does not include old and new names (default true)", 0, 1));
	}

	public String name() {
		return "nig.asset.doc.element.remove";
	}

	public String description() {
		return "Specialized service to remove an element by name from all instances of a Document type from a local asset. Only works if the element is an immediate child of the document.";
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
		String docType = args.value("type");
		String elName = args.value("element");
		boolean allowInvalid = args.booleanValue("allow-invalid-meta", true);
		//
		AssetUtil.removeElementByName (executor(), cid, id, docType, elName, allowInvalid);
	}
	

}
