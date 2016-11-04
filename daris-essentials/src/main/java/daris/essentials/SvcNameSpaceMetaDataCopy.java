package daris.essentials;

import java.util.Collection;

import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.*;
import arc.mf.plugin.PluginService.Interface;
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
		Interface.Element me = new Interface.Element("from", StringType.DEFAULT, "The root from namespace.", 0, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this namespace.  If not supplied, then the namespace will be assumed to be local.", 0));
		_defn.add(me);
		_defn.add(new Interface.Element("to", StringType.DEFAULT, "To root to namespace.", 0, 1));
		_defn.add(new Interface.Element("create", BooleanType.DEFAULT, "By default this service expects all the recipient namespaces to pre-exist."+ 
				" If they don't they are skipped.  Set this to true to create any missing namespaces.", 0, 1));
	}
	public String name() {
		return "nig.namespace.meta.copy";
	}

	public String description() {
		return "Specialised service to copy (add) namespace meta-data and template meta-data from one namespace root to another.  For example from /CAPIM to /projects/proj-CAPIM-101.3.1.";
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

		String fromRoot = args.value("from");
		String fromRoute = args.value("from/@proute");              // Local if null
		String toRoot = args.value("to");
		Boolean create = args.booleanValue("create", false);

		// Walk down the from tree.  For every namespace, look for a corresponding one
		// in the to tree and copy the meta-data there
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("namespace", fromRoot);
		XmlDoc.Element r = executor().execute("asset.namespace.list", dm.root());
		XmlDoc.Element pathEl = r.element("namespace");
		String path = pathEl.value("@path");
		Collection<String> nss = pathEl.values("namespace");
		
		// See if the peer is reachable. Kind of clumsy as it fails silently with no exception
		ServerRoute sr = new ServerRoute(fromRoute);
		if (sr!=null) {
			r = executor().execute(sr, "server.uuid");
			System.out.println("r="+r);
			if (r.element("uuid")==null) {
				throw new Exception ("Can't reach remote peer");
			}
		}

		for (String ns : nss) {
			String fromNS = path + "/" + ns;
			String toNS = replaceRoot (fromNS,fromRoot, toRoot);
			if (AssetUtil.assetNameSpaceExists(executor(), toNS)) {
				copy (executor(), sr, fromNS, toNS, fromRoot, toRoot);
			} else {
				if (create) {
					createNameSpace (executor(), toNS);
					copy (executor(), sr, fromNS, toNS, fromRoot, toRoot);
				}
			}
			System.out.println("");
		}
	}

	
	private void createNameSpace (ServiceExecutor executor, String nameSpace) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("namespace", nameSpace);
		executor.execute("asset.namespace.create", dm.root());
	}
	
	private String replaceRoot (String from, String fromRoot, String toRoot) throws Throwable {
		//  e.g.  /CAPIM   ->  /projects/proj-<name>-<id>
		int nF = fromRoot.length();
		String t = from.substring(nF);
		return  toRoot + t;
	}



	private void copy (ServiceExecutor executor, ServerRoute sr, String fromNS, String toNS, String fromRoot, String toRoot) throws Throwable {
		System.out.println("from=" + fromNS);
		System.out.println("to="+toNS);


		// Meta-data
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("namespace",fromNS);
		XmlDoc.Element r = executor().execute(sr, "asset.namespace.describe", dm.root());
		XmlDoc.Element meta = r.element("namespace/asset-meta");
		//
		if (meta!=null) {
			dm = new XmlDocMaker("args");
			dm.add("namespace", toNS);
			dm.push("asset-meta");
			Collection<XmlDoc.Element> els = meta.elements();
			for (XmlDoc.Element el : els) dm.add(el);
			dm.pop();
			executor().execute("asset.namespace.asset.meta.add", dm.root());
		}

		// Template
		XmlDoc.Element template = r.element("namespace/template");
		if (template!=null) {
			dm = new XmlDocMaker("args");
			dm.add("namespace", toNS);
			dm.push("template");
			Collection<XmlDoc.Element> els = template.elements();
			for (XmlDoc.Element el : els) dm.add(el);
			dm.pop();
			executor().execute("asset.namespace.template.add", dm.root());
		}


		// Now descend into the children	
		dm = new XmlDocMaker("args");
		dm.add("namespace", fromNS);
		r = executor().execute("asset.namespace.list", dm.root());
		XmlDoc.Element pathEl = r.element("namespace");
		String path = pathEl.value("@path");
		Collection<String> nss = pathEl.values("namespace");
		if (nss==null) return;
		if (nss.size()==0) return;
		//
		for (String ns : nss) {
			String fns = path + "/" + ns;
			String tns = replaceRoot (fns,fromRoot, toRoot);
			if (AssetUtil.assetNameSpaceExists(executor(), tns)) {
				copy (executor(), sr, fns, tns, fromRoot, toRoot);
			}
		}
	}
}
