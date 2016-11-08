package daris.essentials;

import java.util.Collection;

import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.*;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcNameSpaceMetaDataCopy extends PluginService {


	private Interface _defn;

	public SvcNameSpaceMetaDataCopy() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("from", StringType.DEFAULT, "The parent namespace to copy from.", 0, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this namespace.  If not supplied, then the namespace will be assumed to be local.", 0));
		_defn.add(me);
		//
		me = new Interface.Element("to", StringType.DEFAULT, "The parent namespace to copy to.", 0, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this namespace.  If not supplied, then the namespace will be assumed to be local.", 0));
		_defn.add(me);
		//
		_defn.add(new Interface.Element("create", BooleanType.DEFAULT, "By default this service expects all the recipient namespaces to pre-exist. Set to true to create the child namespaces. If false and namespace does not exist, that namespace is skipped."+ 
				" If they don't they are skipped.  Set this to true to create any missing namespaces.", 0, 1));
		_defn.add(new Interface.Element("list", BooleanType.DEFAULT, "List all namespaces traversed (defaults to false).", 0, 1));
	}
	
	public String name() {
		return "nig.namespace.metadata.copy";
	}

	public String description() {
		return "Specialised service to recrusively copy (set) namespace meta-data and template meta-data from one namespace root to another.  For example to copy from namespace parent /CAPIM (from) to new parent  /projects/proj-CAPIM-101.3.1 (to).";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public int executeMode() {
		return EXECUTE_LOCAL;
	}

	public boolean canBeAborted() {

		return true;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String fromParent = args.value("from");
		String fromRoute = args.value("from/@proute");              // Local if null
		String toParent = args.value("to");
		String toRoute = args.value("to/@proute");              // Local if null
		Boolean create = args.booleanValue("create", false);
		Boolean list = args.booleanValue("list", false);

		// See if the peer is reachable. Kind of clumsy as it fails silently with no exception
		ServerRoute srFrom = new ServerRoute(fromRoute);
		if (srFrom!=null) {
			XmlDoc.Element r = executor().execute(srFrom, "server.uuid");
			if (r.element("uuid")==null) {
				throw new Exception ("Can't reach remote 'from'  peer " + fromRoute);
			}
		}
		ServerRoute srTo = new ServerRoute(toRoute);
		if (srTo!=null) {
			XmlDoc.Element r = executor().execute(srTo, "server.uuid");
			if (r.element("uuid")==null) {
				throw new Exception ("Can't reach remote 'to' peer " + toRoute);
			}
		}
	
		
		// Recursively set meta-data
		copy (executor(), create, srFrom, srTo, fromParent, toParent, fromParent, toParent, list, w);
	}


	private void createNameSpace (ServiceExecutor executor, String nameSpace) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("namespace", nameSpace);
		executor.execute("asset.namespace.create", dm.root());
	}

	private String replaceRoot (String from, String fromRoot, String toRoot) throws Throwable {
		//  e.g.  /CAPIM   ->  /projects/proj-<name>-<id>
		int nF = fromRoot.length();
		String t = from.substring(nF+1);
		return  toRoot + t;
	}



	private void copy (ServiceExecutor executor, Boolean create, ServerRoute srFrom, ServerRoute srTo, String fromNS, String toNS, 
			String fromParent, String toParent, Boolean list, XmlWriter w) throws Throwable {
	

		// Check abort
		PluginTask.checkIfThreadTaskAborted();


		// Meta-data
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("namespace",fromNS);
		XmlDoc.Element asset = executor().execute(srFrom, "asset.namespace.describe", dm.root());
		
		// Create namespace as needed		
		if (!AssetUtil.assetNameSpaceExists(executor(), toNS)) {
			if (create) {
				if (list) w.add("to", new String[]{"from", fromNS, "created", "true"}, toNS);
				createNameSpace (executor(), toNS);
			} else {
				if (list) w.add("to", new String[]{"from", fromNS, "skipped", "true"}, toNS);
				return;
			}
		} else {
			if (list) w.add("to", new String[]{"from", fromNS, "created", "false"}, toNS);
		}

		
		// Set meta-data 
		XmlDoc.Element meta = asset.element("namespace/asset-meta");
		boolean some = false;
		if (meta!=null) {
			dm = new XmlDocMaker("args");
			dm.add("namespace", toNS);
			dm.push("asset-meta");
			Collection<XmlDoc.Element> els = meta.elements();
			for (XmlDoc.Element el : els) {
				dm.add(el);
				some = true;
			}
			dm.pop();
		}
		if (some) executor().execute(srTo, "asset.namespace.asset.meta.set", dm.root());

		// Template
		XmlDoc.Element template = asset.element("namespace/template");
		some = false;
		if (template!=null) {
			dm = new XmlDocMaker("args");
			dm.add("namespace", toNS);
			dm.push("template");
			Collection<XmlDoc.Element> els = template.elements();
			for (XmlDoc.Element el : els) {
				dm.add(el);
				some = true;
			}
			dm.pop();
		}
		if (some) {
			executor().execute(srTo, "asset.namespace.template.set", dm.root());
		}


		// Now descend into the children	
		dm = new XmlDocMaker("args");
		dm.add("namespace", fromNS);
		XmlDoc.Element r = executor().execute(srFrom, "asset.namespace.list", dm.root());
		XmlDoc.Element pathEl = r.element("namespace");
		String path = pathEl.value("@path");
		Collection<String> nss = pathEl.values("namespace");
		if (nss==null) return;
		if (nss.size()==0) return;
		//
		for (String ns : nss) {
			String fns = path + "/" + ns;
			String tns = replaceRoot (fns, fromParent, toParent);
			copy (executor(), create, srFrom, srTo, fns, tns, fromParent, toParent, list, w);
		}
	}
}
