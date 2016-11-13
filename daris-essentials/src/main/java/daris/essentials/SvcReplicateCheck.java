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


	public SvcReplicateCheck() {
		_defn = new Interface();
		_defn.add(new Interface.Element("peer",StringType.DEFAULT, "Name of peer that objects have been replicated to.", 1, 1));
		_defn.add(new Interface.Element("where",StringType.DEFAULT, "Query predicate to restrict the selected assets on the local host. If unset, all assets are considered.", 0, 1));
		_defn.add(new Interface.Element("size",IntegerType.DEFAULT, "Limit the accumulation loop to this number of assets per iteration (if too large, the host may run out of virtual memory).  Defaults to 10000.", 0, 1));
		_defn.add(new Interface.Element("dst", StringType.DEFAULT, "The destination parent namespace. If supplied (use '/' for root namespace), assets will actually be replicated (one at a time; not efficient). The default is no replication.", 0, 1));
		_defn.add(new Interface.Element("check-asset", BooleanType.DEFAULT, "Check modification time and size of existing replicas (default false) as well as their existence (hugely slows the process if activated).", 0, 1));
		_defn.add(new Interface.Element("exclude-daris-proc", BooleanType.DEFAULT, "By default, processed DaRIS DataSets (ones for which (pssd-derivation/processed)='true' AND mf-dicom-series is absent) are included. Set to true to exclude these.", 0, 1));
		_defn.add(new Interface.Element("use-indexes", BooleanType.DEFAULT, "Turn on or off the use of indexes in the query. Defaults to true.", 0, 1));
		_defn.add(new Interface.Element("debug", BooleanType.DEFAULT, "Write some stuff in the log. Default to false.", 0, 1));
		_defn.add(new Interface.Element("include-destroyed", BooleanType.DEFAULT, "Include soft destroyed assets (so don't include soft destroy selection in the where predicate. Default to false.", 0, 1));
		_defn.add(new Interface.Element("list", BooleanType.DEFAULT, "List all the IDs of assets to be replicated. Default to false.", 0, 1));
		_defn.add(new Interface.Element("rep-inc", IntegerType.DEFAULT, "When debug is true, messages are written to the server log. This parameter specifies the increment to report that assets have been replicated.  Defaults to 1000. ", 0, 1));
	}
	public String name() {
		return "nig.replicate.check";
	}

	public String description() {
		return "Lists assets (both primaries, and replicas from other hosts) that haven't been replicated to the remote peer. You can abort during the accumulation phase once per size chunk and also during actual replication of assets (between each asset). Assets can also be actually replicated rather than juyst listed by specifying the destinational root namespace.";
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
		int[] idx = new int[]{1};
		int[] count = new int[]{0};
		Date date = new Date();
		String dateTime = date.toString();     // Just used to tag message in log file

		// Get inputs
		String where = args.value("where");
		String peer = args.value("peer");
		String size = args.stringValue("size", "10000");
		String dst = args.value("dst");
		Boolean checkAsset = args.booleanValue("check-asset", false);
		Boolean exclDaRISProc = args.booleanValue("exclude-daris-proc", false);
		Boolean useIndexes = args.booleanValue("use-indexes", true);
		Boolean dbg = args.booleanValue("debug", false);
		Boolean list = args.booleanValue("list", false);
		Boolean includeDestroyed = args.booleanValue("include-destroyed", false);
		Integer repInc = args.intValue("rep-inc", 1000);


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
			more = find (executor(),  dateTime, where, peer, sr, uuidLocal, size, 
					assetIDs, checkAsset, exclDaRISProc, useIndexes, 
					dbg,  list, includeDestroyed, idx, count, w);
			if (dbg) {
				log(dateTime, "nig.replicate.check : checking for abort \n");
			}
			PluginTask.checkIfThreadTaskAborted();
		}

		// Replicate one at a time
		w.add("total-checked", count[0]);
		w.add("total-to-replicate", assetIDs.size());
		if (dbg) {
			log(dateTime, "   nig.replicate.check : total checked = " + count[0]);
			log(dateTime, "   nig.replicate.check : total to replicate = " + assetIDs.size());
		}
		if (dst!=null) {
			if (dbg) {
				log(dateTime,"Starting replication of " + assetIDs.size() + " assets");
			}
			int c = 1;
			int nRep = 0;
			for (String id : assetIDs) {
				// Check for abort
				PluginTask.checkIfThreadTaskAborted();

				// Print out stuff
				if (dbg) {
					int rem = c % repInc; 
					if (c==1 || rem==1) {
						log(dateTime, "nig.replicate.check: replicating asset # " + c);
					}
				}

				// Replicate
				XmlDocMaker dm = new XmlDocMaker("args");
				dm.add("id", id);
				dm.add("cmode", "push");
				dm.add("dst", dst);
				dm.push("peer");
				dm.add("name", peer);
				dm.pop();
				dm.add("related", "0");
				dm.add("update-doc-types", false);
				dm.add("update-models", false);
				dm.add("allow-move", true);
				if (includeDestroyed) dm.add("include-destroyed", true);

				try {
					executor().execute("asset.replicate.to", dm.root());
					nRep++;
				} catch (Throwable t) {
					log(dateTime, "Failed to send asset " + id + " with error " + t.getMessage());
				}
				c++;
			}
			w.add("total-replicated", nRep);
			if (dbg) {
				log(dateTime, "   nig.replicate.check : total replicated = " + nRep);
			}
		}
	}

	private void log (String dateTime, String message) {
		System.out.println(dateTime + " : " + message);
	}

	private String serverUUID(ServiceExecutor executor, String proute) throws Throwable {

		XmlDoc.Element r = executor.execute(new ServerRoute(proute), "server.uuid");
		return r.value("uuid");
	}

	private boolean find (ServiceExecutor executor,  String dateTime, String where, String peer, ServerRoute sr, String uuidLocal, String size, 
			Vector<String> assetList, Boolean checkAsset, Boolean exclDaRISProc, 
			Boolean useIndexes, Boolean dbg, Boolean list,
			Boolean includeDestroyed, int[] idx, int[] count, XmlWriter w)	throws Throwable {

		// Find local  assets  with the given query. We work through the cursor else
		// we may run out of memory
		if (dbg) log(dateTime, "nig.replicate.check : find assets on primary in chunk starting with idx = " + idx[0]);
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
				where = "((asset has been destroyed) or (asset has not been destroyed))";
			} else {
				where += "and ((asset has been destroyed) or (asset has not been destroyed))";
			}
			dm.add("include-destroyed", true);
		}
		if (where!=null) dm.add("where", where);

		dm.add("idx", idx[0]);
		dm.add("size", size);
		dm.add("pdist", 0);
		dm.add("action", "get-meta");
		dm.add("use-indexes", useIndexes);
		if (dbg) {
			log(dateTime, "   nig.replicate.check : query string is '" + where + "'");

		}
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		if (r==null) return false;  
		Collection<XmlDoc.Element> assets = r.elements("asset");
		if (assets==null) return false;
		count[0] += assets.size();

		// Get the cursor and increment for next time
		XmlDoc.Element cursor = r.element("cursor");
		boolean more = !(cursor.booleanValue("total/@complete"));
		if (more) {
			Integer next = cursor.intValue("next");
			idx[0] = next;
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

			if (result.booleanValue()==false) {
				if (list) w.add("id", new String[]{"exists", "false"},  primaryID);
				assetList.add(primaryID);
			} else {

				// The asset exists as a replica, but perhaps it's been modified.
				// Very time consuming...
				if (checkAsset) {
					// See if the primary has been modified since the replica was made
					XmlDoc.Element asset = AssetUtil.getAsset(executor, null, primaryID);
					Date mtime = asset.dateValue("asset/mtime");
					String csize = asset.value("asset/content/size");

					/*
					// To get the remote asset we have to know its id... There is no asset.get :id rid
					// We have to query for it !
					dm = new XmlDocMaker("args");
					dm.add("where", "rid='" + rid + "'");;
					dm.add("action", "get-meta");
					XmlDoc.Element remoteAsset = executor.execute(sr, "asset.query", dm.root());
					*/
					// Use id overload e.g. "asset.get :id rid=1004.123455"
					dm = new XmlDocMaker("args");
					dm.add("id","rid="+rid);
					XmlDoc.Element remoteAsset = executor.execute(sr, "asset.get", dm.root());
					
					Date mtimeRep = remoteAsset.dateValue("asset/mtime");
					String csizeRep = remoteAsset.value("asset/content/size");
					String cidRep = remoteAsset.value("asset/cid");            // Same for primary and replica
					if (dbg) {
						if (mtime!=null && mtimeRep!=null) {
							log(dateTime, "      nig.replicate.check : mtimes=" + mtime + ", " + mtimeRep);
						} else {			
							log(dateTime, "      nig.replicate.check : mtimes are unexpectedly null for asset " + rid);
						}
						if (csize!=null && csizeRep!=null) {
							log(dateTime, "      nig.replicate.check : sizes =" + csize + ", " + csizeRep);
						}
					}
					if (csize!=null && csizeRep!=null) {
						if (mtime.after(mtimeRep) || !csize.equals(csizeRep)) {
							w.add("id", new String[]{"exists", "true", "cid", cidRep, "mtime-primary", mtime.toString(), "mtime-replica", mtimeRep.toString(),
									"csize-primary", csize, "csize-replica", csizeRep},  primaryID);
							assetList.add(primaryID);	
						}
					} else {
						if (mtime.after(mtimeRep)) {
							w.add("id", new String[]{"exists", "true", "cid", cidRep, "mtime-primary", mtime.toString(), "mtime-replica", mtimeRep.toString()},
									primaryID);
							assetList.add(primaryID);	
						}
					}
				}
			}
		}
		//
		return more;
	}
}
