package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.pssd.CiteableIdUtil;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcReplicateCheck extends PluginService {


	private Interface _defn;


	public SvcReplicateCheck() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of a single project to check. Defaults to all projects.", 0, 1));
		_defn.add(new Interface.Element("peer",StringType.DEFAULT, "Name of peer that objects have been replicated to.", 1, 1));
	}

	public String name() {
		return "om.pssd.replicate.check";
	}

	public String description() {
		return "Finds the number Methods and the number of children objects for each Project on the local and peer (where objects have been replicated) servers.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}


	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Get inputs
		String peer = args.value("peer");
		ServerRoute sr = DistributedAssetUtil.findPeerRoute(executor(), peer);
		String id = args.value("id");

		// Projects
		if (id!=null) {
			if (CiteableIdUtil.isProjectId(id)) {
				String nl = countProject (executor(), null, id);
				String nr = countProject (executor(), sr, id);
				w.add("Project",  new String[]{"local", nl, peer, nr}, id);
			} else{
				throw new Exception ("Given id is not a Project");
			}
		} else {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("pdist", 0);
			XmlDoc.Element r = executor().execute("om.pssd.collection.member.list", dm.root());
			if (r!=null) {
				Collection<String> ids = r.values("object/id");
				if (ids!=null) {
					for (String id2 : ids) {
						String nl = countProject (executor(), null, id2);
						String nr = countProject (executor(), sr, id2);
						Boolean ok = (nl.equals(nr));
						String ok2 = "true";
						if (!ok) ok2 = "false";
						w.add("Project",  new String[]{"local", nl, peer, nr, "ok", ok2}, id2);
					}
				}
			}
		}

		// Methods
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("pdist", 0);
		XmlDoc.Element rl = executor().execute("om.pssd.method.list", dm.root());
		int nl = 0;
		if (rl!=null) {
			nl = rl.values("method").size();
		}
		XmlDoc.Element rr = executor().execute(sr, "om.pssd.method.list");
		int nr = 0;
		if (rr!=null) {
			Collection<String> methods = rr.values("method");
			if (methods!=null) {
				nr = methods.size();
			} else {
				nr = 0;
			}
		}
		w.add("Methods", new String[]{"local", ""+nl, peer, ""+nr});

	}

	

	private String countProject (ServiceExecutor executor, ServerRoute sr, String id) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "cid starts with '" + id + "'";
		if (sr==null) {
			query += " and rid hasno value";
		} else {
			String uuid = PSSDUtils.serverUUID(executor, null);
			query += " and (rid in '" + uuid + "')";
		}
		dm.add("where", query);
		dm.add("size", "infinity");
		dm.add("action", "count");
		dm.add("pdist", 0);
		XmlDoc.Element r = executor.execute(sr, "asset.query", dm.root());
		if (r==null) return "0";
		return r.value("value");
	}
}
