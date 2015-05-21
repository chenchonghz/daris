package nig.mf.pssd.plugin.util;

import java.util.Collection;
import java.util.HashMap;


import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

/**
 * This class supplies the server that manages a given CID.  Only one server
 * will ever be the CID manager; it is the server that allocates a 
 * child CID in a CID tree.  This class uses static data so it can be shared
 * by other objects in the same thread.
 * 
 * 
 * @author nebk
 *
 */
public class ManagingCIDServer {

	private static HashMap<String,String> _map = null;           // Maps CID root to server UUID
	private static ServiceExecutor _thread = null;
 
	/**
	 * Find the server UUID for the given citeable ID (can be root or dot separated)
	 * 
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static String serverUUID (String cid) throws Throwable {
		// Initialize known values and executor
		if (_map==null) init();
		
		// Short cut if local cid root is the same as the root of the CID we are interested in
		String cidRoot = getCIDRoot (cid);
		String localCIDRoot = getLocalServerCIDRoot();
		if (cidRoot.equals(localCIDRoot)) {
			return PluginService.serverIdentityAsString();
		}
		
		// Fetch/set
		String uuid = null;
		if (_map.containsKey(cidRoot)) {
			uuid = _map.get(cidRoot);
		} else {
			uuid = findServerUUID(cidRoot);
			_map.put(cidRoot, uuid);
		}
		return uuid;
	}
	
	// Private functions. 
	private static String getLocalServerCIDRoot () throws Throwable {
		XmlDoc.Element r = _thread.execute("citeable.root.get");
		return r.value("cid");
	}

	
	/**
	 * Initialize with known servers. Others will be dynamically computed as needed              
	 * 
	 */
	private static void init () {
		_thread = PluginThread.serviceExecutor();
		//
		// <CID root, UUID>
		_map = new HashMap<String,String>();
		_map.put("1", "1004");          // daris-1.rvm.nectar.org.au
		_map.put("1053", "1053");       // mediaflux-2.melbourne.nectar.org.au
		_map.put("101", "101");         // nebk laptop
		_map.put("687", "687");         // wilson laptop
		//
		_map.put("1008", "1035");       // vera059 (Monash)
		
	}
	
	
	/**
	 * Find the server UUID for the given CID root
	 * 
	 * @param targetCIDRoot
	 * @return
	 * @throws Throwable
	 */
	private static String findServerUUID (String targetCIDRoot) throws Throwable {

        // FInd all the peers
		ServerRoute serverRoute = new ServerRoute(ServerRoute.EXECUTE_DISTRIBUTED_ALL);
		XmlDoc.Element r = _thread.execute(serverRoute, "citeable.root.get");		
		
		Collection<XmlDoc.Element> peers = r.elements("peer");  
		if (peers!=null) {

			// Iterate through the known peers (will include local server first)				
			for (XmlDoc.Element peer : peers ) {
				String proute = peer.value("@proute");
				String cidRoot = peer.value("cid");
				if (cidRoot.equals(targetCIDRoot)) {
					String uuid = DistributedAssetUtil.serverUUIDFromProute(proute);
					return uuid;
				}
			}
		}
		//
	    throw new Exception ("Cannot discover managing server for CID root " + targetCIDRoot + " in the federation");
	}
	
	
	
	private static String getCIDRoot (String cid) {
	
		int idx = cid.indexOf(".");
		if (idx>=0) {
			return cid.substring(0, idx);
		} else {
			return cid;
		}
	}	 
}
