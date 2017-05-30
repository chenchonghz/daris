package daris.essentials;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import arc.mf.plugin.*;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcReplicateSync extends PluginService {


	private Interface _defn;

	public SvcReplicateSync() {
		_defn = new Interface();
		_defn.add(new Interface.Element("peer",StringType.DEFAULT, "Name of peer that objects have been replicated to.", 1, 1));
		_defn.add(new Interface.Element("where",StringType.DEFAULT, "Query predicate to restrict the selected assets on the peer (replica). If the peer holds replicas not from your primary, exclude them in this predicate query string. If unset, all assets are considered.", 0, 1));
		_defn.add(new Interface.Element("destroy",BooleanType.DEFAULT, "Actually destroy the assets, rather than just listing them. Defaults to false.", 0, 1));
		_defn.add(new Interface.Element("size",IntegerType.DEFAULT, "Limit loop that looks for replica assets to delete to this number of assets per iteration (if too large, the VM may run out of virtual memory).  Defaults to 5000.", 0, 1));
		_defn.add(new Interface.Element("idx",IntegerType.DEFAULT, "Start index in the cursor. Defaults to 1.", 0, 1));
		_defn.add(new Interface.Element("use-indexes", BooleanType.DEFAULT, "Turn on or off the use of indexes in the query. Defaults to true.", 0, 1));
		_defn.add(new Interface.Element("debug", BooleanType.DEFAULT, "Write some stuff in the log. Default to false.", 0, 1));
		_defn.add(new Interface.Element("count-only", BooleanType.DEFAULT, "Just counts (rather than listing each asset) assets for synchronizing. Default to false.", 0, 1));
	}

	public String name() {
		return "nig.replicate.synchronize";
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
		// Get inputs
		String where = args.value("where");
		String peer = args.value("peer");
		Boolean destroy = args.booleanValue("destroy", false);
		Boolean count = args.booleanValue("count-only", false);
		String size = args.stringValue("size", "5000");
		Integer t = args.intValue("idx", 1);
		int[] idx = new int[]{t};

		Date date = new Date();
		String dateTime = date.toString();     // Just used to tag message in log file


		//		Boolean useNew = args.booleanValue("use-new", false);
		Boolean useIndexes = args.booleanValue("use-indexes", true);
		Boolean dbg = args.booleanValue("debug", false);

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

		// Maintain destruction lists for DICOM data model because asset.destroy
		// will destroy all Series when it destroys a Study.  This allows us to
		// make a self-consistency check that a Study is empty when we destroy it.
		XmlDocMaker destroyDICOMPatient = new XmlDocMaker("args");
		XmlDocMaker destroyDICOMStudy = new XmlDocMaker("args");
		XmlDocMaker destroyDICOMSeries = new XmlDocMaker("args");
		XmlDocMaker destroyOther = new XmlDocMaker("args");          // PSSD

		// FInd assets
		while (more) {
			more = find (executor(), dateTime, useIndexes, destroyOther, destroyDICOMPatient, destroyDICOMStudy,
					destroyDICOMSeries, where, peer, srPeer, uuidLocal, size, dbg, idx, w);
			PluginTask.checkIfThreadTaskAborted();
		}

		// See if user wants to abandon
		PluginTask.checkIfThreadTaskAborted();

		// Destroy assets on remote peer bottom up in DICOM data model
		// FOr PSSD, 'members=false' so it does not destroy children
		int n = destroyOrListAssets(executor(), dateTime, srPeer, destroyDICOMSeries, "DICOM Series", destroy, count, w);
		n += destroyOrListAssets(executor(), dateTime, srPeer, destroyDICOMStudy, "DICOM Study", destroy, count, w);
		n += destroyOrListAssets(executor(), dateTime, srPeer, destroyDICOMPatient, "DICOM Patient", destroy, count, w);
		n += destroyOrListAssets(executor(), dateTime, srPeer, destroyOther, "Other", destroy, count, w);

		if (dbg) {
			System.out.println("");
			if (destroy) {
				log(dateTime, "nig.replicate.synchronize: Destroyed in total " + n + " remote assets without primaries on the local host");
			} else {
				log(dateTime, "nig.replicate.synchronize: Found in total " + n + " remote assets without primaries on the local host");
			}
		}
	}



	private int destroyOrListAssets (ServiceExecutor executor, String dateTime, ServerRoute sr, XmlDocMaker list, String type, 
			Boolean destroy, Boolean count, XmlWriter w) throws Throwable {
		Collection<XmlDoc.Element> elements = list.root().elements("id");
		if (elements==null) return 0;
		//
		if (destroy) {
			log(dateTime, "nig.replicate.synchronize: Found " + elements.size() + " remote " + type + " assets for destruction without primaries on the local host");
		} else {
			log(dateTime, "nig.replicate.synchronize: Found " + elements.size() + " remote " + type + " assets without primaries on local host");
		}
		if (elements.size() > 0) {
			if (destroy) {

				// TBD: we really should make this check for all PSSD objects as well
				if (type.equals("DICOM Study")) {
					// Make sure none of the Studies in the list have any children
					// If they do, something needs to be manually resolved
					// as  Series are deleted first
					checkHasNoChildren(executor, sr, list);  // Exception
				}

				// Destroy one by one so can abort if in panic
				// (originally did in one go)
				for (XmlDoc.Element el : elements) {
					String id = el.value();
					String rid = el.value("@rid");
					String name = el.value("@name");
					XmlDocMaker dm = new XmlDocMaker("args");
					dm.add("members", false);
					dm.add("atomic", true);
					dm.add("id", id);
					executor.execute(sr, "asset.destroy", dm.root());
					PluginTask.checkIfThreadTaskAborted();
					if (!count) w.add("id", new String[]{"name", name, "rid", rid, "destroyed", "true"}, id);
				}
			} else {
				for (XmlDoc.Element el : elements) {
					String id = el.value();
					String rid = el.value("@rid");
					String name = el.value("@name");
					if (!count) w.add("id", new String[]{"name", name, "rid", rid, "destroyed", "false"}, id);
				}
			}
		}
		if (count) {
			if (destroy) {
				w.add("number-of-assets-destroyed", elements.size());
			} else {
				w.add("number-of-assets-to-destroy", elements.size());
			}
		}
		return elements.size();
	}


	private void checkHasNoChildren (ServiceExecutor executor, ServerRoute sr,
			XmlDocMaker list) throws Throwable {
		Collection<String> studyIDs = list.root().values("id");
		if (studyIDs!=null) {
			for (String studyID : studyIDs) {

				// See if we have any contained children
				XmlDocMaker dm = new XmlDocMaker("args");
				dm.add("id", studyID);      // ID on remote peer
				XmlDoc.Element r = executor.execute(sr, "asset.get", dm.root());
				Collection<XmlDoc.Element> relateds  = r.elements("asset/related");
				if (relateds!=null) {
					for (XmlDoc.Element related : relateds) {
						if (related.value("@type").equals("contains") &&
								related.booleanValue("@container")) {
							Collection<String> childrenIDs = related.values("to");
							if (childrenIDs!=null && childrenIDs.size()>0) {
								throw new Exception ("Remote peer DICOM Study " + studyID + " contains children Series - will not destroy; you may need to widen the search to find all Series for destruction.");
							}
						}
					}
				}
			}
		}
	}
	private String serverUUID(ServiceExecutor executor, String proute) throws Throwable {

		XmlDoc.Element r = executor.execute(new ServerRoute(proute), "server.uuid");
		return r.value("uuid");
	}

	// To handle replicated replicas (originally from some other server), the algorithm
	// would need to be enhanced.  Perhaps this would work:
	// 1. supply a list of server UUIDs of interest (defaults to local host)
	// 2. QUery for all replicas on remote peer
	// 3. Keep only those whose UUID is in the list (slow filter)
	// 4. On the local peer, if the asset was replicated from us check existence
	//     by id. If the asset was replicated from somewhere else check existence by rid
	// 
	private boolean find (ServiceExecutor executor,  String dateTime, Boolean useIndexes,  XmlDocMaker destroyOther, XmlDocMaker destroyDICOMPatient, XmlDocMaker destroyDICOMStudy,
			XmlDocMaker destroyDICOMSeries, String where, String peer, ServerRoute srPeer, String uuidLocal, String size, 
			Boolean dbg, int[] idx, XmlWriter w)
					throws Throwable {

		// Find replica assets on  the remote peer.  We work through the cursor else
		// we may run out of memory
		if (dbg) {
			log(dateTime, "nig.replicate.synchronize: CHunk starting with idx = " + idx[0]);
			log(dateTime,"   nig.replicate.synchronize:  Find replicas");
		}

		long time = System.nanoTime();
		//		System.out.println("nano time="+time);

		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "(rid in '" + uuidLocal + "')";
		if (where!=null) query += " and " + where;
		dm.add("where", query);
		dm.add("action", "get-value");
		dm.add("use-indexes", useIndexes);
		dm.add("idx", idx[0]);
		dm.add("size", size);
		dm.add("pdist", 0);
		dm.add("xpath", "id");
		dm.add("xpath", "rid");
		dm.add("xpath", "type");
		dm.add("xpath", "name");
		XmlDoc.Element r = executor.execute(srPeer, "asset.query", dm.root());
		if (dbg) {
			//			log (dateTime, "   nig.replicate.sycnhronize: asset.query on peer took " + duration(time));
		}
		if (r==null) return false;  
		Collection<XmlDoc.Element> rAssets = r.elements("asset");
		if (rAssets==null) return false;

		// Get the cursor and increment for next time
		XmlDoc.Element cursor = r.element("cursor");
		boolean more = false;
		if (cursor !=null) more = !(cursor.booleanValue("total/@complete"));
		if (more) {
			Integer next = cursor.intValue("next");
			idx[0] = next;
		}

		// See if the primaries for the found replicas exist on the local server
		if (dbg) log(dateTime, "  Look for primaries matching replicas.");
		time = System.nanoTime();
		//		System.out.println("nano time="+time);

		dm = new XmlDocMaker("args");
		for (XmlDoc.Element rAsset : rAssets) {
			List<XmlDoc.Element> t = rAsset.elements("value");
			String rid = t.get(1).value();
			dm.add("id", rid);   
		}
		XmlDoc.Element r2 = executor.execute("asset.exists", dm.root());       // Local execution only
		Collection<XmlDoc.Element> lAssets = r2.elements("exists");
		if (dbg) {
			log (dateTime, "   nig.replicate.sycnhronize: asset.exists on local took " + duration(time));
		}

		// Build a list of  assets to destroy on the remote peer.   I.e. the ones that don't exist
		// on the local server but do exist on the peer
		if (dbg) log(dateTime,"   nig.replicate.synchronize: Check list for missing primaries.");
		time = System.nanoTime();
		//		System.out.println("nano time="+time);

		Iterator<XmlDoc.Element> rIt = rAssets.iterator();
		int nExtra = 0;
		for (XmlDoc.Element lAsset : lAssets) {
			XmlDoc.Element rAsset = rIt.next();      // rAssets and lAssets have the same order
			if (!lAsset.booleanValue()) {
				// This asset does not exist on the local host
				// FInd the id of the replica of this asset on the remote peer
				List<XmlDoc.Element> t = rAsset.elements("value");
				String idOnPeer = t.get(0).value();
				String rid = t.get(1).value();
				String type = t.get(2).value();
				String name = t.get(3).value();
				nExtra++;


				// Put in correct list
				if (type==null) {
					destroyOther.add("id",  new String[]{"rid", rid}, idOnPeer);
				} else {
					if (type.equals("dicom/patient")) {
						destroyDICOMPatient.add("id", new String[]{"name", name, "rid", rid}, idOnPeer);
					} else if (type.equals("dicom/study") ||
							type.equals("siemens-raw-petct/study")) {         // Type, not as document type (so no daris:)
						destroyDICOMStudy.add("id", new String[]{"name", name, "rid", rid}, idOnPeer);
					} else if (type.equals("dicom/series") ||
							type.equals("siemens-raw-petct/series")) {
						destroyDICOMSeries.add("id", new String[]{"name", name, "rid", rid}, idOnPeer);
					} else {
						destroyOther.add("id", new String[]{"name", name, "rid", rid}, idOnPeer);
					}
				}
				if (dbg) {
					log(dateTime,"   nig.replicate.synchronize: Replicated asset with id '" + idOnPeer + "' is not on the primary");
				}
			}
		}
		if (dbg) {
			//			log (dateTime, "   nig.replicate.sycnhronize: checking asset.exists for true/false on local took " + duration(time));

			log(dateTime,"   nig.replicate.synchronize: Checked " + rAssets.size() + " remote assets of which " + nExtra + " were not found on the primary");
		}
		//
		return more;
	}

	private void log (String dateTime, String message) {
		System.out.println(dateTime + " : " + message);
	}

	private String duration (long lastTime) throws Throwable {
		long difference = System.nanoTime() - lastTime;
		return String.format("%d min, %d sec",
				TimeUnit.NANOSECONDS.toHours(difference),
				TimeUnit.NANOSECONDS.toSeconds(difference) -
				TimeUnit.MINUTES.toSeconds(TimeUnit.NANOSECONDS.toMinutes(difference)));
	}
}

