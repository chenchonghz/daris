package nig.mf.pssd.plugin.util;


import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class CiteableIdUtil extends nig.mf.pssd.CiteableIdUtil {



	/**
	 * Returns the named root identifier. Will create if non-existent or return existing
	 * 
	 * @param executor
	 * @param proute
	 *            Route to remote server. If null use local
	 * @param name
	 *            Name of desired citeable root; if null, returns the server citeable ID root
	 * @return
	 * @throws Throwable
	 */
	public static String citeableIDRoot(ServiceExecutor executor, String proute, String name) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("name", name);
		//
		XmlDoc.Element r = null;
		if (name == null) {
			r = executor.execute(new ServerRoute(proute), "citeable.root.get", dm.root());
		} else {
			// If exists returns existing else creates
			r = executor.execute(new ServerRoute(proute), "citeable.named.id.create", dm.root());
		}
		return r.value("cid");
	}

	/**
	 * Return the standard CID root for creating projects under
	 * 
	 * @param executor
	 * @param proute
	 *            Route to remote server. If null use local
	 * @return
	 * @throws Throwable
	 */
	public static String projectIDRoot(ServiceExecutor executor, String proute) throws Throwable {

		return citeableIDRoot(executor, proute, nig.mf.pssd.CiteableIdUtil.PROJECT_ID_ROOT_NAME);
	}

	/**
	 * Find the standard root CID for creating Method objects
	 * 
	 * @param executor
	 * @param proute
	 *            route to remote server. If null use local
	 * @return
	 * @throws Throwable
	 */
	public static String methodIDRoot(ServiceExecutor executor, String proute) throws Throwable {

		return citeableIDRoot(executor, proute, nig.mf.pssd.CiteableIdUtil.METHOD_ID_ROOT_NAME);
	}

	/**
	 * Create the next CID under the given root.  The underlying service can ask the correct
	 * server, if it can reach it, to allocate the CID. Exception if the server cannot be reached.
	 * 
	 * @param executor
	 * @param pid
	 * @param pdist Specifies federation distation. If > 0, the system will look for the correct server
	 *        in the federation to allocate the CID from. 
	 * @return
	 * @throws Throwable
	 */

	public static String createCid(ServiceExecutor executor, String pid, String pdist) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("pid", pid);
		if (pdist!=null) dm.add("pdist", pdist);
		XmlDoc.Element r = executor.execute("citeable.id.create", dm.root());
		return r.value("cid");

	}


	/**
	 * Find out if a CID already exists on the given server
	 * 
	 * @param proute
	 *            Route to server. Null means local
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static boolean cidExists(String proute, ServiceExecutor executor, String cid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		XmlDoc.Element r = executor.execute(new ServerRoute(proute), "citeable.id.exists", dm.root());
		if (r==null) return false;
		// Don't know the use case for this.
		String exists = r.value("exists");
		if (exists == null) {
			throw new Exception("citeable.id.exists service returns null.");
		}
		//
		return r.booleanValue("exists");

	}


	/**
	 * Import the given CID Into the local server
	 * 
	 * @param executor
	 * @param cid
	 * @param rootDepth
	 * @return
	 * @throws Throwable
	 */
	public static String importCid(ServiceExecutor executor, String cid, int rootDepth) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		dm.add("root-depth", rootDepth);
		XmlDoc.Element r = executor.execute("citeable.id.import", dm.root());
		return r.value("cid");

	}


	/**
	 * Destroy a CID. This means it cannot be re-used
	 * 
	 * @param executor
	 * @param cid
	 * @throws Throwable
	 */
	public static void destroyCID(String proute, ServiceExecutor executor, String cid) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", cid);
		XmlDoc.Element r1 = executor.execute(new ServerRoute(proute), "citeable.id.exists", doc.root());
		if (r1.value("exists").equals("true")) {
			doc = new XmlDocMaker("args");
			doc.add("cid", cid);
			executor.execute("citeable.id.destroy", doc.root());
		}

	}

	/**
	 * Convert asset ID to asset CID (if it has one)
	 * 
	 * @param executor
	 * @param id
	 * @return
	 * @throws Throwable
	 */
	public static String idToCid(ServiceExecutor executor, String id) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		return r.value("asset/cid");
	}

	/**
	 * Convert asset CID to asset ID
	 * 
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static String cidToId(ServiceExecutor executor, String cid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		dm.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		return r.value("asset/@id");
	}


	

	/**
	 * Generate the next CID child under the given parent CID on the local server
	 * 
	 * If the child number is specified, generate the required CID; this pre-specified CID must be unallocated or
	 * allocated but naked (no associated assets).
	 * 
	 * If the child number is not specified, generate the next CID and if desired, fill in the allocator space. The
	 * returned CID is allocated and ready for use.
	 * 
	 * Re-using CIDs or  is a very risky process as you must be certain it. YOu can only
	 * attempt to re-use CIDs on the local server
	 * 
	 * @param executor
	 * @param pid
	 *            The parent CID for the parent
	 * @param pdist Distation for finding the managing CID server. Only relevant when not re-using CID space.
	 * @param childNUmber
	 *            The child number of the object to use if possible. Set to -1 to just make a new CID under pcid
	 * @param fillIn
	 *            If childNumber is not given, then if fillIn is true, will look for the next available CID either an
	 *            already allocated naked CID or create the next one. This fills in the allocator space.
	 * @return
	 * @throws Throwable
	 */
	public static String generateCiteableID(ServiceExecutor executor, String pid, String pdist, long childNumber,
			boolean fillIn) throws Throwable {

		// If fillin is true, we are ONLY allowed to try to fill in allocator space on the local host
		// when the parent is on the local host. Any other condition is disallowed.
		Boolean rootsOK = checkCIDIsForThisServer (executor, pid, false);
		if ((fillIn || childNumber>0) && !rootsOK) {
			throw new Exception ("You can only attempt to re-use CIDs when the parent CID is from the local host");
		}
		//  	

		String cid = null;
		if (childNumber == -1) {
			// User does not specify the child number
			if (fillIn) {
				// Find the first already allocated naked CID or unallocated
				// CID. Whichever comes first
				long startChild = 1;
				cid = getFirstAvailableCid(executor, pid, startChild);
			} else {
				// Just create the next CID under this parent. No asset checks are required
				// The underlying service will ask the correct server to allocate the CID
				cid = createCid(executor, pid, pdist);
			}
		} else {
			// Generate the CID for the given child number if we can
			cid = generateCiteableID(executor, pid, childNumber);
		}
		//
		return cid;
	}

	/**
	 * This function gets the first *unallocated* or *naked* (allocated cid with no asset) child of the parent CID
	 * starting with a given number. It will fill in unallocated 'holes' in CID allocations. The returned CID is
	 * allocated so it is available for use.
	 * 
	 * Function operates with the local server only.
	 * 
	 * Currently throws an exception if it fails to find a CID after 1000 tries
	 * 
	 * @param executor
	 * @param pid
	 *            is the parent cid
	 * @param id
	 *            number to start with. So if parent=1.5 and id=2 we begin looking from 1.5.2 .. 1.5.N
	 * @return The cid
	 * @throws Throwable
	 */
	public static String getFirstAvailableCid(ServiceExecutor executor, String pid, long id) throws Throwable {

		if (id <= 0)
			throw new Exception("First CID child number must be positive");

		while (true) {
			// Make desired CID string
			String cid = pid + "." + id;
			// We check for the existence of the CID on the local server 
			if (cidExists(null, executor, cid)) {
				// The CID is already created; now we must see if an asset exists with
				// this CID exists locally
				if (!nig.mf.plugin.util.AssetUtil.exists(executor, cid, true)) {
					return cid;
				}
			} else {
				// Import (allocate) this CID on the local server
				importCid(executor, cid, 1);
				return cid;
			}

			// This number was an allocated CID with an asset. Try again
			id++;

			// Remove when secure
			if (id == 1000) {
				throw new Exception("CID child finder appears to be in an infinite loop");
			}
		}
	}

	/**
	 * Compares the parent root of a cid (e.g. 101.4.2 -> 101) with the cid root of the
	 * local server
	 * 
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static Boolean checkCIDIsForThisServer (ServiceExecutor executor, String cid, Boolean throwIt) throws Throwable {
		String parentRoot = nig.mf.pssd.CiteableIdUtil.getRootParentId(cid);
		if (parentRoot==null) {
			if (throwIt) {
				throw new Exception ("Could not determine root parent of " + cid);
			} else {
				return false;
			}
		}
		String cidRoot = nig.mf.pssd.CiteableIdUtil.citeableIDRoot(executor, null);
		if (cidRoot==null) {
			if (throwIt) {
				throw new Exception ("Could not determine cid root for local server");
			} else {
				return false;
			}
		}
		//
		Boolean ok = parentRoot.equals(cidRoot);
		if (throwIt && !ok) {
			throw new Exception("The extracted citeable ID root'" + parentRoot
					+ "' is not valid for this server; it has the wrong root which should be '" + cidRoot + "'");
		}
		return ok;
	}

	
	// Private functions



	/**
	 * Generate the next CID child under the given parent CID. If the child number is specified generate the required
	 * CID; this pre-specified CID must be unallocated or allocated and naked (no associated assets). The returned CID
	 * is allocated and ready for use
	 * 
	 * @param executor
	 * @param pid Parent cid
	 * @param childNUmber
	 *            The child number of the object to use if possible. Must be > 0
	 * @return
	 * @throws Throwable
	 */
	private static String generateCiteableID(ServiceExecutor executor, String pid, long childNumber)
			throws Throwable {

		if (childNumber <= 0) {
			throw new Exception ("Child number must be > 0");
		}

		// Check that this CID has no locally associated assets.
		String cid = pid + "." + childNumber;
		if (nig.mf.plugin.util.AssetUtil.exists(executor, cid, true)) {
			throw new Exception("Cannot reuse cid " + cid + " as it has an existing local asset.");
		}

		// Import the CID locally. Because we disallow re-use of external CIDs locally, we
		// are always importing CIDs on the local server.
		// If it has been allocated and destroyed it remains unallocated
		// If it is as yet unallocated, it will be allocated.  
		importCid(executor, cid, 1);
		//
		return cid;
	}







}
