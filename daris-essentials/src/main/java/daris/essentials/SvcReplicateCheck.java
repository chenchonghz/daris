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

public class SvcReplicateCheck extends PluginService {


	private Interface _defn;
	private Integer idx_ = 1;
	private Integer count_ = 0;


	public SvcReplicateCheck() {
		_defn = new Interface();
		_defn.add(new Interface.Element("peer",StringType.DEFAULT, "Name of peer that objects have been replicated to.", 1, 1));
		_defn.add(new Interface.Element("where",StringType.DEFAULT, "Query predicate to restrict the selected assets on the local host. If unset, all assets are considered.", 0, 1));
		_defn.add(new Interface.Element("size",IntegerType.DEFAULT, "Limit the accumulation loop to this number of assets per iteration (if too large, the VM may run out of virtual memory).  Defaults to 500.", 0, 1));
		_defn.add(new Interface.Element("dst", StringType.DEFAULT, "The destination parent namespace. If supplied (use '/' for root namespace), assets will actually be replicated (one at a time; not efficient). The default is no replication.", 0, 1));
		_defn.add(new Interface.Element("mod", BooleanType.DEFAULT, "Check modification time of existing replicas (default false) as well as their existence (hugely slows the process if activated).", 0, 1));
		_defn.add(new Interface.Element("exclude-daris-proc", BooleanType.DEFAULT, "By default, processed DaRIS DataSets (ones for which (pssd-derivation/processed)='true' AND mf-dicom-series is absent) are included. Set to true to exclude these.", 0, 1));
		_defn.add(new Interface.Element("use-indexes", BooleanType.DEFAULT, "Turn on or off the use of indexes in the query. Defaults to true.", 0, 1));
		_defn.add(new Interface.Element("debug", BooleanType.DEFAULT, "Write some stuff in the log. Default to false.", 0, 1));
	}
	public String name() {
		return "nig.replicate.check";
	}

	public String description() {
		return "Lists assets (both primaries, and replicas from other hosts) that haven't been replicated to the remote peer. You can abort during the accumulation phase once per size chunk.";
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

		// Get inputs
		String where = args.value("where");
		String peer = args.value("peer");
		String size = args.stringValue("size", "500");
		String dst = args.value("dst");
		Boolean checkMod = args.booleanValue("mod", false);
		Boolean exclDaRISProc = args.booleanValue("exclude-daris-proc", false);
		Boolean useIndexes = args.booleanValue("use-indexes", true);
		Boolean dbg = args.booleanValue("debug", false);


		// Find route to peer. Exception if can't reach and build in extra checks to make sure we are 
		// being very safe
		ServerRoute sr = DistributedAssetUtil.findPeerRoute(executor(), peer);
		if (sr==null) {
			throw new Exception("Failed to generated the ServerRoute for the remote host");
		}
		String uuidLocal = serverUUID(executor(), null);
		if (sr.target().equals(uuidLocal)) {
			throw new Exception ("Remote peer UUID appears to be the same as the local host (" + uuidLocal + ") - cannot proceed");
		}

		// Iterate through cursor and build list of assets 
		boolean more = true;
		Vector<String> assetIDs = new Vector<String>();
		while (more) {
			more = find (executor(),  where, peer, sr, uuidLocal, size, assetIDs, checkMod, exclDaRISProc, useIndexes,dbg,  w);
			if (dbg) {
				System.out.println("nig.replicate.check : checking for abort \n");
			}
			PluginTask.checkIfThreadTaskAborted();
		}

		// Replicate one at a time
		if (dst!=null) {
			for (String id : assetIDs) {
				XmlDocMaker dm = new XmlDocMaker("args");
				dm.add("id", id);
				dm.add("cmode", "push");
				dm.add("dst", dst);
				dm.push("peer");
				dm.add("name", peer);
				dm.pop();
				dm.add("related", "0");
				dm.add("update-doc-types", false);
				if (dbg) System.out.println("nig.replicate.check: replicating asset id " + id);
				try {
					executor().execute("asset.replicate.to", dm.root());
				} catch (Throwable t) {
					System.out.println("Failed to send asset " + id + " with error " + t.getMessage());
				}
			}
		}
		w.add("total-checked", count_);
	}


	private static String serverUUID(ServiceExecutor executor, String proute) throws Throwable {

		XmlDoc.Element r = executor.execute(new ServerRoute(proute), "server.uuid");
		return r.value("uuid");
	}

	private boolean find (ServiceExecutor executor,  String where, String peer, ServerRoute sr, String uuidLocal, String size, 
			Vector<String> assetList, Boolean checkMod, Boolean exclDaRISProc, Boolean useIndexes, Boolean dbg, XmlWriter w)
					throws Throwable {

		// Find local  assets  with the given query. We work through the cursor else
		// we may run out of memory
		if (dbg) System.out.println("nig.replicate.check : find assets on primary in chunk starting with idx = " + idx_);
		XmlDocMaker dm = new XmlDocMaker("args");
		if (exclDaRISProc) {
			// Drop processed DataSets from query
			if (where==null) where = "";
			// Need this complex expression as there the simpler generates a NULLP
			// (not(xpath(pssd-derivation/processed)='true') or (mf-dicom-series has value))
			where += " and ( (xpath(daris:pssd-derivation/processed)='false' or daris:pssd-derivation hasno value or daris:pssd-derivation/processed hasno value) or (mf-dicom-series has value) )";
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
			System.out.println("   nig.replicate.check : checking if " + assets.size() + " assets exist on DR");
		}
		XmlDoc.Element r2 = executor.execute(sr, "asset.exists", dm.root());
		if (r2==null) return more;
		Collection<XmlDoc.Element> results = r2.elements("exists");

		
		// Create a list of assets to replicate
		if (dbg) {
			System.out.println ("   nig.replicate.check : iterate through " + results.size() + " results and build list for replication.");
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

			if (result.booleanValue()==false) {
				w.add("id", new String[]{"exists", "false"},  primaryID);
				assetList.add(primaryID);
				if (dbg) {
					System.out.println("      nig.replicate.check : id '" + primaryID + "' is not found on the DR server");
				}
			} else {

				// The asset exists as a replica, but perhaps it's been modified.
				// Very time consuming...
				if (checkMod) {
					// See if the primary has been modified since the replica was made
					XmlDoc.Element asset = AssetUtil.getAsset(executor, null, primaryID);
					Date mtime = asset.dateValue("asset/mtime");
					String csize = asset.value("asset/content/size");

					// To get the remote asset we have to know its id... There is no asset.get :id rid
					// We have to query for it !
					dm = new XmlDocMaker("args");
					dm.add("where", "rid='" + rid + "'");;
					dm.add("action", "get-meta");
					XmlDoc.Element remoteAsset = executor.execute(sr, "asset.query", dm.root());
					Date mtimeRep = remoteAsset.dateValue("asset/mtime");
					String csizeRep = remoteAsset.value("asset/content/size");
					String cidRep = remoteAsset.value("asset/cid");            // Same for primary and replica
					if (dbg) {
						System.out.println("      nig.replicate.check : mtimes=" + mtime + ", " + mtimeRep);
						System.out.println("      nig.replicate.check : sizes =" + csize + ", " + csizeRep);
					}
					if (mtime.after(mtimeRep)) {
						w.add("id", new String[]{"exists", "true", "cid", cidRep, "mtime-primary", mtime.toString(), "mtime-replica", mtimeRep.toString(),
								"csize-primary", csize, "csize-replica", csizeRep},  primaryID);
						assetList.add(primaryID);	
					}
				}
			}
		}
		//
		return more;
	}
}
