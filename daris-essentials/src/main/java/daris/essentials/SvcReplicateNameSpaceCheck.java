package daris.essentials;

import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import arc.mf.plugin.*;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcReplicateNameSpaceCheck extends PluginService {


	private Interface _defn;
	private Integer idx_ = 1;
	private Integer count_ = 0;


	public SvcReplicateNameSpaceCheck() {
		_defn = new Interface();
		_defn.add(new Interface.Element("peer",StringType.DEFAULT, "Name of peer that objects have been replicated to.", 1, 1));
		_defn.add(new Interface.Element("where",StringType.DEFAULT, "Query predicate to restrict the selected assets on the local host. If unset, all assets are considered.", 0, 1));
		_defn.add(new Interface.Element("size",IntegerType.DEFAULT, "Limit the accumulation loop to this number of assets per iteration (if too large, the host may run out of virtual memory).  Defaults to 10000.", 0, 1));
		_defn.add(new Interface.Element("move", BooleanType.DEFAULT, "The destination parent namespace. If true, (default false) assets will actually be moved to the correct namespace (one at a time; not efficient).", 0, 1));
		_defn.add(new Interface.Element("exclude-daris-proc", BooleanType.DEFAULT, "By default, processed DaRIS DataSets (ones for which (pssd-derivation/processed)='true' AND mf-dicom-series is absent) are included. Set to true to exclude these.", 0, 1));
		_defn.add(new Interface.Element("use-indexes", BooleanType.DEFAULT, "Turn on or off the use of indexes in the query. Defaults to true.", 0, 1));
		_defn.add(new Interface.Element("debug", BooleanType.DEFAULT, "Write some stuff in the log. Default to false.", 0, 1));
		_defn.add(new Interface.Element("include-destroyed", BooleanType.DEFAULT, "Include soft destroyed assets (so don't include soft destroy selection in the where predicate. Default to false.", 0, 1));
		_defn.add(new Interface.Element("rep-inc", IntegerType.DEFAULT, "When debug is true, messages are written to the server log. This parameter specifies the increment to report that assets have been replicated.  Defaults to 1000. ", 0, 1));
	}
	public String name() {
		return "nig.replicate.namespace.check";
	}

	public String description() {
		return "Lists assets (both primaries, and replicas from other hosts) that have been replicated but for which the namespaces don't agree. Can optionally move them into the correct namespace (primary prefixed by primary server UUID).";
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


		// Init
		idx_ = 1;
		count_ = 0;
		Date date = new Date();
		String dateTime = date.toString();     // Just used to tag message in log file

		// Get inputs
		String where = args.value("where");
		String peer = args.value("peer");
		String size = args.stringValue("size", "10000");
		Boolean exclDaRISProc = args.booleanValue("exclude-daris-proc", false);
		Boolean useIndexes = args.booleanValue("use-indexes", true);
		Boolean dbg = args.booleanValue("debug", false);
		Boolean includeDestroyed = args.booleanValue("include-destroyed", false);
		Integer repInc = args.intValue("rep-inc", 1000);
		Boolean move = args.booleanValue("move", false);


		// Find route to peer. Exception if can't reach and build in extra checks to make sure we are 
		// being very safe
		ServerRoute remoteSR = DistributedAssetUtil.findPeerRoute(executor(), peer);
		if (remoteSR==null) {
			throw new Exception("Failed to generated the ServerRoute for the remote host");
		}
		String uuidLocal = serverUUID(executor(), null);
		if (remoteSR.target().equals(uuidLocal)) {
			throw new Exception ("Remote peer UUID appears to be the same as the local host (" + uuidLocal + ") - cannot proceed");
		}

		// Iterate through cursor and build list of assets 
		boolean more = true;
		Vector<XmlDoc.Element> assets = new Vector<XmlDoc.Element>();
		while (more) {
			more = find (executor(),  dateTime, where, peer, remoteSR, uuidLocal, size, 
					assets, exclDaRISProc, useIndexes, 
					dbg,  includeDestroyed, w);
			if (dbg) {
				log(dateTime, "nig.replicate.namespace.check : checking for abort \n");
			}
			PluginTask.checkIfThreadTaskAborted();
		}

		// Correct asset namespaces, one at a time
		w.add("total-to-move", assets.size());
		if (dbg) {
			log(dateTime, "   nig.replicate.namespace.check : total checked = " + count_);
			log(dateTime, "   nig.replicate.namespace.check : total to move = " + assets.size());
		}
		if (move!=null) {
			if (dbg) {
				log(dateTime,"Starting replication of " + assets.size() + " assets");
			}
			int c = 1;
			int nRep = 0;
			for (XmlDoc.Element asset : assets) {
				// Check for abort
				PluginTask.checkIfThreadTaskAborted();

				// Print out stuff
				if (dbg) {
					int rem = c % repInc; 
					if (c==1 || rem==1) {
						log(dateTime, "nig.replicatenamespace.check: replicating asset # " + c);
					}
				}

				// Move
				String replicaID = asset.value("replica/id");
				XmlDocMaker dm = new XmlDocMaker("args");
				dm.add("id", replicaID);
				dm.add("namespace", "/" + remoteSR.target() + asset.value("primary/namespace"));

				try {
					executor().execute(remoteSR, "asset.move", dm.root());
					nRep++;
				} catch (Throwable t) {
					log(dateTime, "Failed to move remote asset " + replicaID + " with error " + t.getMessage());
				}
				c++;
			}
			w.add("total-moved", nRep);
			if (dbg) {
				log(dateTime, "   nig.replicate.namespace.check : total moved = " + nRep);
			}
		}
	}

	private static void log (String dateTime, String message) {
		System.out.println(dateTime + " : " + message);
	}

	private static String serverUUID(ServiceExecutor executor, String proute) throws Throwable {

		XmlDoc.Element r = executor.execute(new ServerRoute(proute), "server.uuid");
		return r.value("uuid");
	}

	private boolean find (ServiceExecutor executor,  String dateTime, String where, String peer, ServerRoute sr, String uuidLocal, String size, 
			Vector<XmlDoc.Element> assetList, Boolean exclDaRISProc, 
			Boolean useIndexes, Boolean dbg,
			Boolean includeDestroyed, XmlWriter w)	throws Throwable {

		// Find local  assets  with the given query. We work through the cursor else
		// we may run out of memory
		if (dbg) log(dateTime, "nig.replicate.check : find assets on primary in chunk starting with idx = " + idx_);
		XmlDocMaker dm = new XmlDocMaker("args");
		if (exclDaRISProc) {
			// Drop processed DataSets from query
			if (where==null) where = "";
			// Need this complex expression as there the simpler generates a NULLP
			// (not(xpath(pssd-derivation/processed)='true') or (mf-dicom-series has value))
			where += " and ( (xpath(daris:pssd-derivation/processed)='false' or daris:pssd-derivation hasno value or daris:pssd-derivation/processed hasno value) or (mf-dicom-series has value) )";
		}
		if (includeDestroyed) {
			if (where==null) {
				where = "asset has been destroyed";
			} else {
				where += "and (asset has been destroyed)";
			}
			dm.add("include-destroyed", true);
		}
		if (where!=null) dm.add("where", where);

		dm.add("idx", idx_);
		dm.add("size", size);
		dm.add("pdist", 0);
		dm.add("action", "get-meta");
		dm.add("use-indexes", useIndexes);
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		if (r==null) return false;  
		Collection<XmlDoc.Element> assets = r.elements("asset");
		if (assets==null) return false;
		count_ += assets.size();

		// Get the cursor and increment for next time
		XmlDoc.Element cursor = r.element("cursor");
		boolean more = !(cursor.booleanValue("total/@complete"));
		if (more) {
			Integer next = cursor.intValue("next");
			idx_ = next;
		}

		// See if the replicas exist on the peer. 
		// One query per asset

		// Make a list of rids to find
		dm = new XmlDocMaker("args");	
		for (XmlDoc.Element asset : assets) {
			// Get the asset id, and the rid (asset may already be a replica from elsewhere)
			String id = asset.value("@id");

			// If the asset is already a replica, its rid remains the same
			// when replicated to another peer
			String rid = asset.value("rid");    

			// If primary, set expected rid on remote peer
			if (rid==null) rid = uuidLocal + "." + id;
			dm.add("rid", rid);
		}

		// Now check if they exist
		if (dbg) {
			log(dateTime, "   nig.replicate.check : checking if " + assets.size() + " assets exist on DR");
		}
		XmlDoc.Element r2 = executor.execute(sr, "asset.exists", dm.root());
		if (r2==null) return more;
		Collection<XmlDoc.Element> results = r2.elements("exists");


		// Create a list of assets to replicate
		if (dbg) {
			log(dateTime, "   nig.replicate.check : iterate through " + results.size() + " results and build list for replication.");
		}
		for (XmlDoc.Element result : results) {

			// Fetch the rid and pull out the id
			String rid = result.value("@rid");
			String[] t = rid.split("\\.");
			String primaryID = t[1];

			/*
			System.out.println("rid="+rid);
			System.out.println("id="+primaryID);
			System.out.println("value="+res.booleanValue());
			 */

			if (result.booleanValue()) {

				// The asset exists as a replica, but perhaps it's been moved
				// See if the primary has been modified since the replica was made
				XmlDoc.Element asset = AssetUtil.getAsset(executor, null, primaryID);
				String assetNameSpace = asset.value("asset/namespace");

				// To get the remote asset we have to know its id... There is no asset.get :id rid
				// We have to query for it !
				dm = new XmlDocMaker("args");
				dm.add("where", "rid='" + rid + "'");;
				dm.add("action", "get-meta");
				XmlDoc.Element remoteAsset = executor.execute(sr, "asset.query", dm.root());
				String remoteAssetNameSpace = remoteAsset.value("asset/namespace");
				if (dbg) {
					log(dateTime, "      nig.replicate.check :namespaces=" + assetNameSpace + ", " +remoteAssetNameSpace);
				}
				String expectedRemoteAssetNameSpace = "/" + uuidLocal + assetNameSpace;
				if (!expectedRemoteAssetNameSpace.equals(remoteAssetNameSpace)) {
					dm = new XmlDocMaker("args");
					dm.push("asset");
					dm.push("primary");
					dm.add("namespace",assetNameSpace);
					dm.add("id", primaryID);
					dm.pop();
					dm.push("replica");
					dm.add("namespace", remoteAssetNameSpace);
					dm.add("expected-namespace", expectedRemoteAssetNameSpace);
					dm.add("id", remoteAsset.value("asset/@id"));
					dm.add("rid", rid);
					dm.pop();
					dm.pop();
					w.add(dm.root());
					assetList.add(dm.root());	
				}
			}
		}
		//
		return more;
	}
}
