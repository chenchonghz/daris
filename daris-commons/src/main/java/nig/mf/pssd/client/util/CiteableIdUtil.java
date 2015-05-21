package nig.mf.pssd.client.util;

import arc.mf.client.ServerClient;
import arc.mf.client.ServerRoute;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlStringWriter;

public class CiteableIdUtil extends nig.mf.pssd.CiteableIdUtil{

	/**
	 * Get the citeable id root for the projects.
	 * 
	 * @param cxn
	 * @return
	 * @throws Throwable
	 */
	public static String getProjectIdRoot(ServerClient.Connection cxn) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("name", "pssd.project");
		XmlDoc.Element r = cxn.execute("citeable.named.id.create", w.document());
		return r.value("cid");
	}
	
	
	/**
	 * Find out if a CID already exists on the given server
	 * 
	 * @param proute Route to server.  Null means local
	 * @param executor
	 * @param cid
	 * @return
	 * @throws Throwable
	 */
	public static boolean cidExists(ServerClient.Connection cxn, String cid) throws Throwable {

		XmlStringWriter w = new XmlStringWriter();
		w.add("cid", cid);
		XmlDoc.Element r = cxn.execute("citeable.id.exists", w.document());
		String exists = r.value("exists");
		if (exists != null) {
			if (exists.equals("true")) {
				return true;
			}
		}
		return false;

	}
	
	
	/**
	 * Get the next  CID for this parent on the local host 
	 * Exception if root is not local host
	 * 
	 * @param cxn
	 * @param parent
	 * @throws Throwable
	 */
	public static String getNextAvailableCID (ServerClient.Connection cxn, String parent) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("pid", parent);
		w.add("pdist", 0);
		XmlDoc.Element r = cxn.execute("citeable.id.create", w.document());
		return r.value("cid");
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
	public static String importCid(ServerClient.Connection cxn, String cid, int rootDepth) throws Throwable {

		XmlStringWriter w = new XmlStringWriter();
		w.add("cid", cid);
		w.add("root-depth", rootDepth);
		XmlDoc.Element r = cxn.execute("citeable.id.import", w.document());
		return r.value("cid");

	}
	
	/**
	 * This function imports a CID and will replace just the first value (the
	 * server) with the server id. This is a way to create a CID as specified.
	 * 
	 * @param executor
	 * @param serverRoute
	 *            the route to the server that manages (allocates this CID tree)
	 * @param The
	 *            citable ID to import
	 * @throws Throwable
	 */
	public static String importCid(ServerClient.Connection cxn, String cid) throws Throwable {

		return importCid(cxn, cid, 1);
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
	public static Boolean checkCIDIsForThisServer (ServerClient.Connection cxn, String cid, Boolean throwIt) throws Throwable {
		String parentRoot = nig.mf.pssd.CiteableIdUtil.getRootParentId(cid);
		if (parentRoot==null) {
			if (throwIt) {
				throw new Exception ("Could not determine root parent of " + cid);
			} else {
				return false;
			}
		}
		String cidRoot = citeableIDRoot(cxn, null);
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
	
	   /**
     * Returns the server's root citeable identifier.
     * 
     * @param executor
     * @param proute
     *            Route to remote server. If null use local
     * @return
     * @throws Throwable
     */
    public static String citeableIDRoot(ServerClient.Connection cxn, String proute) throws Throwable {
        XmlDoc.Element r = null;
        if (proute == null) {
            r = cxn.execute("citeable.root.get");
        } else {
            r = cxn.execute(new ServerRoute(proute), "citeable.root.get");
        }
        return r.value("cid");
    }


}
