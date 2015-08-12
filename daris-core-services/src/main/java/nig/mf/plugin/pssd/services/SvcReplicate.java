package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcReplicate extends PluginService {


	private Interface _defn;


	public SvcReplicate() {
		_defn = new Interface();
		//
		Interface.Element me = new Interface.Element("pid", CiteableIdType.DEFAULT, "The identity of the parent PSSD object to recursively replicate", 0, 1);
		me.add(new Interface.Attribute( "dmf", BooleanType.DEFAULT,
				"Are the data on a DMF file system (defaults to false)?  If so, pre-staged fetching will occur. The data will be put back in their original state at the end of the process.",
				0));
		me.add(new Interface.Attribute( "inclusive", BooleanType.DEFAULT,
				"Is the cid inclusive of itself ?  Defaults to true.",
				0));
		_defn.add(me);
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of a single object to replicate.", 0, 1));
		_defn.add(new Interface.Element("dst", StringType.DEFAULT, "Destination namespace (e.g. server UUID). If not given, defaults to root namespace", 0, 1));
		_defn.add(new Interface.Element("methods",BooleanType.DEFAULT, "Replicate all Methods. Defaults to false.", 0, 1));
		_defn.add(new Interface.Element("users",BooleanType.DEFAULT, "Replicate all Users. Defaults to false.", 0, 1));
		_defn.add(new Interface.Element("peer",StringType.DEFAULT, "Name of peer to replicate to", 1, 1));
		_defn.add(new Interface.Element("batch", BooleanType.DEFAULT, "Replicate assets one by one or batched up (default) into one cumulative archive.", 0, 1));
		_defn.add(new Interface.Element("parts", new EnumType(new String[] {"meta", "content", "all"}),
				"Components to replicate; meta, content, all (default).", 0, 1));
	}

	public String name() {
		return "om.pssd.replicate";
	}

	public String description() {
		return "Replicates PSSD objects and their children. Must be run by a user authorised on the current and peer system. Uses push mode, compatible-with=2, include-components=true, update-doc-types=false";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}


	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Get inputs
		XmlDoc.Element pid = args.element("pid");
		String id = args.value("id");
		Boolean methods = args.booleanValue("methods");
		Boolean users = args.booleanValue("users");
		String peer = args.value("peer");
		String dst = args.value("dst");
		Boolean batch = args.booleanValue("batch", true);
		String parts = args.stringValue("parts", "all");

		// Dispatch
		if (pid!=null) replicateProject(executor(), parts, pid, peer, dst, batch, w);
		if (id!=null) replicateAsset (executor(), parts, id, peer, dst, w);
		if (methods) replicateMethods(executor(), id, peer, dst, w);
		if (users) replicateUsers (executor(), peer, dst, w);

	}



	private void replicateProject (ServiceExecutor executor, String parts, XmlDoc.Element pid, String peer, String dst, Boolean batch, XmlWriter w) throws Throwable {

		String model = nig.mf.plugin.util.AssetUtil.getModel(executor,pid.value(), true);
		if (model == null) {
			throw new Exception("No asset/model found. Asset(cid=" + pid.value() + ") is not a valid PSSD object.");
		}
		int depth = nig.mf.pssd.CiteableIdUtil.getIdDepth(pid.value());
		if (depth<3 || depth>6) {
			throw new Exception ("Parent object must be project,subject,study, or exmethod");
		}


		// Fetch DMF files that are offline. If the dmf services are not not installed on the server,
		// then we want it to fail so don't catch anything
		Boolean dmf = pid.booleanValue("@dmf");
		String id = pid.value();
		Boolean inclusive = pid.booleanValue("@inclusive", true);
		XmlDoc.Element dmfStatus = null;
		if (dmf) {
			// Find current status
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", id);
			dm.add("recursive", true);
			dmfStatus = executor.execute("om.pssd.dmf.status", dm.root());

			// Bring the data sets on line in one efficient call
			dm = new XmlDocMaker("args");
			dm.add("id", id);
			dm.add("where", "model=om.pssd.dataset");
			dm.add("async", false);
			executor.execute("om.pssd.dmf.get", dm.root());
		}

		// Find all primary objects under the parent
		XmlDocMaker dm = new XmlDocMaker("args");
		String query = null;
		if (inclusive) {
			query = "(cid='" + id+ "' or cid starts with '" + id + "') and rid hasno value";
		} else {
			query = "(cid starts with '" + id + "') and rid hasno value";
		}
		replicateAssets (executor, parts, query, peer, dst, batch, w);

		// Now bring all files online
		if (dmf && dmfStatus!=null) {
			Collection<XmlDoc.Element> states = dmfStatus.elements("state");
			if (states != null) {
				for (XmlDoc.Element state : states) {
					// Asynchronously put offline whatever was offline at the start of this
					if (state.value().equals("OFL")) {
						String cid = state.value("@id");
						dm = new XmlDocMaker("args");
						dm.add("id", cid);
						dm.add("async", true);
						executor.execute("om.pssd.dmf.put", dm.root());
					}
				}
			}
		}
	}



	private void replicateMethods (ServiceExecutor executor, String id, String peer, String dst, XmlWriter w) throws Throwable {

		if (id==null) {
			XmlDoc.Element r = executor.execute("om.pssd.method.list");
			if (r!=null) {
				Collection<String> ids = r.values("method");
				if (ids!=null) {
					for (String id2 : ids) {
						replicateAsset (executor, "all", id2, peer, dst, w);
					}
				}
			} else {
				replicateAsset (executor, "all", id, peer, dst, w);
			}	
		}
	}

	private void replicateAsset (ServiceExecutor executor, String parts, String id, String peer, String dst, XmlWriter w) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.push("peer");
		dm.add("name", peer);
		dm.pop();
		dm.add("parts", parts);
		dm.add("where", "cid='"+id+"'");
		dm.add("cmode", "push");
		dm.add("include-components", true);
		dm.add("update-doc-types", false);             // Don't synchorize doc types on destination
		dm.add("compatible-with", "2");
		if (dst!=null) {
			dm.add("dst", new String[]{"create", "true"}, dst);
		}
		dm.add("related", 1);  // Attachments
		executor.execute("asset.replicate.to", dm.root());
		w.add("id", id);
	}

	private void replicateAssets (ServiceExecutor executor, String parts, String where, String peer, String dst, Boolean batch, XmlWriter w) throws Throwable {
		if (batch) {
			replicateAssetsBatch (executor, parts, where, peer, dst, w);
		} else {
			replicateAssetsSingle (executor, parts, where, peer, dst, w);

		}
	}

	private void replicateAssetsBatch (ServiceExecutor executor, String parts, String where, String peer, String dst, XmlWriter w) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.push("peer");
		dm.add("name", peer);
		dm.pop();
		dm.add("parts", parts);
		dm.add("where", where);
		dm.add("cmode", "push");
		dm.add("include-components", true);
		dm.add("update-doc-types", false);             // Don't synchronize doc types on destination
		dm.add("compatible-with", "2");
		if (dst!=null) {
			dm.add("dst", new String[]{"create", "true"}, dst);
		}
		dm.add("related", 1); // Attachments
		executor.execute("asset.replicate.to", dm.root());
	}

	private void replicateAssetsSingle (ServiceExecutor executor, String parts,  String where, String peer, String dst, XmlWriter w) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", where);
		dm.add("pdist", 0);
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r==null) return;
		Collection<String> ids = r.values("id");
		if (ids==null) return;
		for (String id : ids) {
			dm = new XmlDocMaker("args");
			dm.push("peer");
			dm.add("name", peer);
			dm.pop();
			dm.add("parts", parts);
			dm.add("cmode", "push");
			dm.add("include-components", true);
			dm.add("update-doc-types", false);             // Don't synchronize doc types on destination
			dm.add("compatible-with", "2");
			if (dst!=null) {
				dm.add("dst", new String[]{"create", "true"}, dst);
			}
			dm.add("id", id);
			dm.add("related", 1);  // Attachments
			executor.execute("asset.replicate.to", dm.root());
		}
	}

	private void replicateUsers (ServiceExecutor executor, String peer, String dst, XmlWriter w) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("peer", peer);
		dm.add("cmode", "push");
		dm.add("where", "namespace>=/system/users");
		dm.add("include-components", true);
		dm.add("update-doc-types", false);             // Don't synchronize doc types on destination
		dm.add("compatible-with", "2");
		executor.execute("asset.replicate.to", dm.root());
	}
}
