package daris.essentials;

import java.util.Collection;
import java.util.Date;

import nig.util.DateUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetDocElementDateFix extends PluginService {

	private Interface _defn;

	public SvcAssetDocElementDateFix() {

		_defn = new Interface();
		_defn.add(new Element("id", CiteableIdType.DEFAULT,
				"The asset ID of the asset of interest on the local server.", 1, 1));
		_defn.add(new Element("type", StringType.DEFAULT, "The document type of interest. E.g. nig-daris:pssd-animal-subject", 1,
				1));
		_defn.add(new Element("path", StringType.DEFAULT,
				"The path of the element within the document type to modify. E.g. birthDate", 1, 1));
	}

	public String name() {

		return "nig.asset.doc.element.date.fix";
	}

	public String description() {

		return "Replaces date strings of the form dd-MMM-YYYY HH:MM:SS with the form dd-MMM-YYYY.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String id = args.value("id");
		String docType = args.value("type");
		String elementPath = args.value("path");
		replace(executor(), id, docType, elementPath, w);
	}

	private void replace(ServiceExecutor executor, String id, String docType, String path, XmlWriter w) throws Throwable {

		// Get asset meta-data
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		if (r == null) return;
		XmlDoc.Element r2 = r.element("asset/meta");
		if (r2 == null) return;

		// Get documents of given type
		Collection<XmlDoc.Element> docs = r2.elements(docType);
		if (docs == null) return;

		// Merge doc maker.
		XmlDocMaker dm2 = new XmlDocMaker("args");
		dm2.add("id", id);
		dm2.push("meta", new String[] { "action", "merge" });


		// Iterate over documents
		int n = 0;
		for (XmlDoc.Element doc : docs) {
			Boolean some = false;

			// Prepare meta-data for this instance of the document
			XmlDocMaker dm3 = new XmlDocMaker("args");
			String[] attList = doc.attributeArray();
			if (attList != null && attList.length > 0) {
				dm3.push(docType, attList);
			} else {
				dm3.push(docType);
			}

			// Find all instances of the element in this document
			Collection<XmlDoc.Element> els = doc.elements(path);
			if (els != null) {
				for (XmlDoc.Element el : els) {

					// Convert the date value and add to merge meta-data
					String date = el.value();
					int l = date.length();
					String date2 = date;
					if (l>11) {
						Date t = el.dateValue();
						date2 = DateUtil.formatDate (t, "dd-MMM-yyyy");
						el.setValue(date2);
						dm3.add(el);
						some = true;
					}
				}
			}

			// We found something to replace in this instance of the document so add it in
			if (some) {
				n++;
				Collection<XmlDoc.Element> t = dm3.root().elements();
				dm2.addAll(t);
			}
		}

		if (n>0) {
			dm2.pop();
			dm2.add("allow-invalid-meta", true);
			executor.execute("asset.set", dm2.root());
			w.add("id", id);
		}
	}
}
