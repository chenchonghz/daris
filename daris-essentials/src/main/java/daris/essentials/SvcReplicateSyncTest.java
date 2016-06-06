package daris.essentials;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import arc.mf.plugin.*;
import arc.mf.plugin.PluginService.Interface;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcReplicateSyncTest extends PluginService {


	private Interface _defn;
	private Integer idx_ = 1;
	private Integer n_ = 0;
	private Integer nMissingAll_ = 0;
	private Integer nNameSpaceDiff_ = 0;
	private Integer nNameSpaceRootDiff_ = 0;


	public SvcReplicateSyncTest() {
		_defn = new Interface();
		_defn.add(new Interface.Element("peer",StringType.DEFAULT, "Name of peer that objects have been replicated to.", 1, 1));
		_defn.add(new Interface.Element("where",StringType.DEFAULT, "Query predicate to restrict the selected assets on the peer (replica). If the peer holds replicas not from your primary, exclude them in this predicate query string. If unset, all assets are considered.", 0, 1));
		_defn.add(new Interface.Element("size",IntegerType.DEFAULT, "Limit loop that looks for replica assets to delete to this number of assets per iteration (if too large, the VM may run out of virtual memory).  Defaults to 10000.", 0, 1));
		_defn.add(new Interface.Element("idx",IntegerType.DEFAULT, "Start index in the cursor. Defaults to 1.", 0, 1));
		_defn.add(new Interface.Element("use-indexes", BooleanType.DEFAULT, "Turn on or off the use of indexes in the query. Defaults to true.", 0, 1));
		_defn.add(new Interface.Element("debug", BooleanType.DEFAULT, "Write some stuff in the log. Default to false.", 0, 1));
		_defn.add(new Interface.Element("show", BooleanType.DEFAULT, "For every asset not found on the primary show a record with rid. For every asset found on the primary, show a record with namespaces for replica and primary. Default to false.", 0, 1));
	}

	public String name() {
		return "nig.replicate.synctest";
	}

	public String description() {
		return "Deletes replica assets (independent of data model) on the remote peer that don't have a primary (won't find assets that are already replicas) on the local host. This task first builds the list of assets on the remote peer (in a loop considering :size assets at a time) to destroy and then destroys them (by asset id) in one operation on the local host. You can abort this task and there is are abort checks 1) in the build loop, 2) before assets are destroyed and after each asset is destroyed.";
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
		
		//
		// <rid,count>
		HashMap<String, Integer> ridMap = new HashMap<String,Integer>();

		// Get inputs
		String where = args.value("where");
		String peer = args.value("peer");
		String size = args.stringValue("size", "10000");
		Integer idx = args.intValue("idx", 1);
		Boolean useIndexes = args.booleanValue("use-indexes", true);
		Boolean dbg = args.booleanValue("debug", false);
		Boolean show = args.booleanValue("show", false);

		// Find route to peer. Exception if can't reach and build in extra checks to make sure we are 
		// being very safe
		ServerRoute srPeer = DistributedAssetUtil.findPeerRoute(executor(), peer);
		if (srPeer==null) {
			throw new Exception("Failed to generated the ServerRoute for the remote host");
		}
		String uuidLocal = serverUUID(executor(), null);
		if (srPeer.target().equals(uuidLocal)) {
			throw new Exception ("Remote peer UUID appears to be the same as the local host (" + uuidLocal + ") - cannot proceed");
		}

		// Iterate through cursor and build list of assets to destroy on remote host
		boolean more = true;


		// FInd assets
		idx_ = idx;
		nMissingAll_ = 0;
		nNameSpaceDiff_ = 0;
		nNameSpaceRootDiff_ = 0;
		n_ = 0;
		while (more) {
			more = findNew (executor(), useIndexes, where, peer, srPeer, uuidLocal, size, dbg, show, ridMap, w);
			PluginTask.checkIfThreadTaskAborted();
		}

		// See if user wants to abandon

		if (dbg) {
			System.out.println("");
			System.out.println("CHecked " + n_ + " assets of which " + nMissingAll_ + " do not exist on the primary");
			System.out.println("   of the assets that exist " + nNameSpaceDiff_ + " had different namespaces");
			System.out.println("   of the assets that exist " + nNameSpaceRootDiff_ + " had different namespace roots");
		}
		
		
		Set<String> keySet = ridMap.keySet();
		Iterator<String> keyIt = keySet.iterator();
		Integer nKeys = keySet.size();
		System.out.println("The rid keySet is of size " + nKeys);
		int nMulti = 0;
		while (keyIt.hasNext()) {
			String rid = keyIt.next();
			int nAssets = ridMap.get(rid);
			if (nAssets>1) nMulti++;;
		}
		System.out.println("There were " + nMulti + " rids that were used by more than 1 asset on the DR system");
	}




	private static String serverUUID(ServiceExecutor executor, String proute) throws Throwable {

		XmlDoc.Element r = executor.execute(new ServerRoute(proute), "server.uuid");
		return r.value("uuid");
	}



	private boolean findNew (ServiceExecutor executor,  Boolean useIndexes, String where, String peer, ServerRoute srPeer, String uuidLocal, String size, 
			Boolean dbg, Boolean show, HashMap<String,Integer> ridMap, XmlWriter w)
					throws Throwable {

		// Find replica assets on  the remote peer.  We work through the cursor else
		// we may run out of memory
		if (dbg) {
			System.out.println("CHunk starting with idx = " + idx_);
			System.out.println("  Find replicas on peer " + srPeer.target());
		}

		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "(rid in '" + uuidLocal + "')";
		if (where!=null) query += " and " + where;
		dm.add("where", query);
		dm.add("action", "get-value");
		dm.add("use-indexes", useIndexes);
		dm.add("idx", idx_);
		dm.add("size", size);
		dm.add("pdist", 0);
		dm.add("xpath", "id");
		dm.add("xpath", "rid");
		dm.add("xpath", "cid");
		dm.add("xpath", "type");
		dm.add("xpath", "namespace");
		//		System.out.println("dm="+dm.root());
		XmlDoc.Element r = executor.execute(srPeer, "asset.query", dm.root());
		if (r==null) return false;  
		Collection<XmlDoc.Element> rAssets = r.elements("asset");
		if (rAssets==null) return false;
		if (dbg) {
			System.out.println("Found " + rAssets.size() + " assets");
		}

		// Get the cursor and increment for next time
		XmlDoc.Element cursor = r.element("cursor");
		boolean more = false;
		if (cursor !=null) more = !(cursor.booleanValue("total/@complete"));
		if (more) {
			Integer next = cursor.intValue("next");
			idx_ = next;
		}

		// See if the primaries for the found replicas exist on the local server
		if (dbg) System.out.println("  Look for primaries matching replicas.");

		int nMissing = 0;
		int nn = 0;
		int mm = 0;
		for (XmlDoc.Element rAsset : rAssets) {
			nn++;
			int rem = nn % 10000; 
			if (nn==1 || rem==1) {
				System.out.println("      Processing asset # " + nn);
			}
			List<XmlDoc.Element> t = rAsset.elements("value");

			String rid = t.get(1).value();
			String rNS = t.get(4).value();
			
			if (ridMap.containsKey(rid)) {
				int nAssets = ridMap.get(rid);
				nAssets++;
				ridMap.put(rid, nAssets);
			} else {
				ridMap.put(rid, 1);
			}

			// Does asset exist
			String[] p = rid.split("\\.");
			String id = p[1];
			dm = new XmlDocMaker("args");
			dm.add("id", id);   
			XmlDoc.Element r2 = executor.execute("asset.exists", dm.root());    
			Boolean exists = r2.booleanValue("exists");
			if (!exists) {
				nMissing++;
				nMissingAll_++;
			}
			n_++;

			// Get its local namespace
			if (exists) {
				dm = new XmlDocMaker("args");
				dm.add("id", rid);
				r2 = executor.execute("asset.namespace.get", dm.root());
				String lNS = r2.value("namespace");
				//
				String[] rs = rNS.split("/");
				String[] ls = lNS.split("/");
//				   System.out.println("Lengths=" + rs.length + ", " + ls.length);
//				   System.out.println("rs=" + rs[2] + "," + rs[3]);
//				   System.out.println("ls="+ ls[1] + "," + ls[2]);
				Boolean projNSRootDiff =  (!rs[2].equals(ls[1])) || (!rs[3].equals(ls[2]));
				Boolean containsNS = rNS.contains(lNS);
				if (projNSRootDiff) {
					mm++;
					nNameSpaceRootDiff_++;
				}
				if (!containsNS) {
					nNameSpaceDiff_++;
				}
				if (show && !containsNS) w.add("rid", new String[]{"exists", "true", "rNameSpace", rNS, "lNameSpace", lNS}, rid);
			} else {
				if (show) w.add("rid", new String[]{"exists", "false"}, rid);
			}

		}
		
		if (dbg) {
			System.out.println("Checked " + rAssets.size() + " remote assets of which " + nMissing + " were not found on the primary");
			System.out.println("   Of that do exist " + mm + " had inconsistent namespace roots");
		}
		//
		return more;
	}
}
