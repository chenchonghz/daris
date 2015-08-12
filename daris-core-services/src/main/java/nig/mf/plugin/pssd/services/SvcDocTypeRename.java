package nig.mf.plugin.pssd.services;



import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcDocTypeRename extends PluginService {
	private Interface _defn;

	public SvcDocTypeRename () {

		_defn = new Interface();
		Interface.Element me = new Interface.Element("type",XmlDocType.DEFAULT, "The old and new document type names.", 0, Integer.MAX_VALUE);
		me.add(new Interface.Element("old",StringType.DEFAULT,"Old document type.",1,1));
		me.add(new Interface.Element("new",StringType.DEFAULT,"New document type (include namespace if there is one, e.g. nig:fish).",1,1));
		_defn.add(me);
		_defn.add(new Interface.Element("templates", BooleanType.DEFAULT, "Update all Subject (and children recursively) :template elements after renaming doc types (default is true). This is not specific to particular doc types; it operates on all primary Subjects.", 0, 1));
	}


	public String name() {
		return "om.pssd.doctype.rename";
	}

	public String description() {
		return "Specialized Service to migrate document type names for PSSD objects (including Methods). For Project,Subject,ExMethod,Study and DataSet, document types will be renamed on ALL objects (primary and replica - this is what Mediaflux does).  For Method objects, only primaries are updated (it finds the document type name Strings in definitions and replaces them) as you are unlikely to have the Method objects anyway (you can't use them).  The :template update process looks for the :template element in meta-data of all Project hierarchies, and refreshes it based on the Method definition (now updated with new document type name).  Note that replicas do not hold the :template element so this process is only applied to primaries. ";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Parse
		final Collection<XmlDoc.Element> types = args.elements("type");
		final Boolean doTemplates = args.booleanValue("templates", true);

		// Unwind if we have a problem
		//		new AtomicTransaction(new AtomicOperation() {

		//			@Override
		//			public boolean execute(ServiceExecutor executor) throws Throwable {
		migrate (executor(), types, doTemplates);
		//				return false;
		//			}
		//		}).execute(executor());


	}

	public void migrate (ServiceExecutor executor, Collection<XmlDoc.Element> types, Boolean doTemplates) throws Throwable {



		// Iterate over document types
		if (types!=null && types.size()>0) {
			for (XmlDoc.Element type : types) {

				// DOes the Doc Type exist
				XmlDocMaker dm = new XmlDocMaker("args");
				dm.add("type", type.value("old"));
				XmlDoc.Element r = executor.execute("asset.doc.type.exists", dm.root());

				// Proceed if so
				if (r.booleanValue("exists")==true) {

					// Rename standard assets
					dm = new XmlDocMaker("args");
					dm.add("old-type", type.value("old"));
					dm.add("new-type", type.value("new"));
					executor.execute("asset.doc.type.rename", dm.root());	

					// Now fix up Methods. We are going to do this by just replacing
					// Strings anywhere we can find them in the Method. 
					dm = new XmlDocMaker("args");
					dm.add("size", "infinity");
					dm.add("pdist", 0);
					dm.add("where", "model='om.pssd.method' and rid hasno value");

					r = executor.execute("asset.query", dm.root());
					if (r!=null) {
						Collection<String> methods = r.values("id");
						if (methods!=null) {
							for (String method : methods) {
								replaceString (executor, null, method, type.value("old"), type.value("new"));
							}
						}
					}	
				}
			}
		}

		// NOw re-generate template elements in Subjects as these depend on Methods.
		// This is very non-specific. Just does them all.  I can't query for :template
		// elements with a specific String (document type).
		if (doTemplates) updateTemplates (executor);

	}

	private void updateTemplates (ServiceExecutor executor) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");

		// Replicas don't even have the :template element and perhaps that
		// makes sense (in our context as we don't allow them to be edited)
		dm.add("where", "model='om.pssd.subject' and rid hasno value");   
		dm.add("action", "get-cid");
		dm.add("pdist", 0);
		dm.add("size", "infinity");
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r!=null) {
			Collection<String> ids = r.values("cid");
			for (String id : ids) {
				XmlDocMaker dm2 = new XmlDocMaker("args");
				dm2.add("id", id);
				dm2.add("recursive", true);  // DOwn to Study level
				executor.execute("om.pssd.subject.method.replace", dm2.root());  // Primaries only
			}
		}
	}


	private void replaceString (ServiceExecutor executor, String cid, String id, String oldString, String newString) throws Throwable {
		XmlDocMaker dm2 = new XmlDocMaker("args");
		if (cid!=null) {
			dm2.add("cid", cid);
		} else {
			dm2.add("id", id);
		}
		dm2.add ("exact", true);
		dm2.add("old", oldString);
		dm2.add("new", newString);
		executor.execute("nig.asset.doc.string.replace", dm2.root());

	}
}
