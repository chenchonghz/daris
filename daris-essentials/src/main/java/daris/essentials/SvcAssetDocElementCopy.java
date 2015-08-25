package daris.essentials;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetDocElementCopy extends PluginService {

	private Interface _defn;

	public SvcAssetDocElementCopy() {

		_defn = new Interface();
		_defn.add(new Element("id", CiteableIdType.DEFAULT,
				"The  asset ID of interest.", 1, 1));
		_defn.add(new Element("type-from", StringType.DEFAULT, "The source document type to locate the element of interest. E.g.  'daris:pssd-project-harvest'.", 1,
				1));
		_defn.add(new Element("type-to", StringType.DEFAULT, "The destination document type to locate the element of interest. E.g.  'daris:pssd-project-owner'.", 1,
				1));
		_defn.add(new Element("path", StringType.DEFAULT,
				"The path of the element within the document types (must be the same) to modify. E.g. 'project-owner'.", 1, 1));
		_defn.add(new Interface.Element("remove", BooleanType.DEFAULT, "Remove the element from the source Document (defaults to false)", 0, 1));
	}

	public String name() {

		return "nig.asset.doc.element.copy";
	}

	public String description() {

		return "Specialised service to copy one element from one DocType to another (the element must exist in both definitions). The service will fail if there is already an element of the given name in the destination document type.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
	
		String id = args.value("id");
		String typeFrom = args.value("type-from");
		String typeTo = args.value("type-to");
		String elementPath = args.value("path");
		Boolean removeOld = args.booleanValue("remove", false);
		copy(executor(), id, typeFrom, typeTo, elementPath, removeOld, w);
	}

	private void copy (ServiceExecutor executor, String id, String typeFrom, String typeTo,
			String path, Boolean removeOld, XmlWriter w) throws Throwable {

		// Get asset meta-data
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		if (r == null) return;

		// Get source document
		Collection<XmlDoc.Element> docsFrom = r.elements("asset/meta/"+typeFrom);
		if (docsFrom == null) return;
		if (docsFrom.size()>1) {
			throw new Exception("Can only handle one document of the specified source Document Type.");
		}
		XmlDoc.Element docFrom = r.element("asset/meta/"+typeFrom);
		XmlDoc.Element elFrom = docFrom.element(path);
		
		// Get destination document
		Collection<XmlDoc.Element> docsTo = r.elements("asset/meta/"+typeTo);
		if (docsTo == null) return;
		if (docsTo.size()>1) {
			throw new Exception("Can only handle one document of the specified destination Document Type.");
		}
		XmlDoc.Element docTo = r.element("asset/meta/"+typeTo);
				
		// Add to new
		docTo.add(elFrom);		
		dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.push("meta", new String[] { "action", "merge" });
		dm.add(docTo);
		dm.pop();
		executor.execute("asset.set", dm.root());
		
		// Remove from old
		if (removeOld) {
			dm = new XmlDocMaker("args");
			dm.add("id", id);
			dm.add("type", typeFrom);
			dm.add("element", path);
			executor.execute("nig.asset.doc.element.remove", dm.root());
		}
	}
}
