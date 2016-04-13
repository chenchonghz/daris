package nig.mf.plugin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;

import nig.mf.pssd.plugin.util.CiteableIdUtil;

import org.apache.commons.compress.utils.IOUtils;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class AssetUtil {



	/**
	 * Add a relationship between two assets
	 * 
	 * @param executor
	 * @param fromID
	 * @param toID
	 * @param relationship
	 * @param updateInverse
	 * @throws Throwable
	 */
	public static void addRelationship (ServiceExecutor executor, String fromID, String toID, String relationship,
			Boolean updateInverse) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", fromID);
		dm.add("to", new String[]{"inverse", Boolean.toString(updateInverse), "relationship", relationship}, toID);
		executor.execute("asset.relationship.add", dm.root());
	}

	/**
	 * Gets the asset content from a local asset and save it into the specified
	 * directory in a file.  Return a handle to that file.
	 * 
	 * @param executor
	 * @param id
	 *            Asset id
	 * @param dstDir
	 * @return
	 * @throws Throwable
	 */
	public static File getContent(ServiceExecutor executor, String id,
			File dstDir) throws Throwable {

		PluginService.Outputs sos = new PluginService.Outputs(1);
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", doc.root(), null, sos);

		if (r.element("asset/content") == null) {
			return null;
		}

		String ext = r.value("asset/content/type/@ext");
		if (!dstDir.exists()) {
			throw new FileNotFoundException("The dst directory "
					+ dstDir.getAbsolutePath() + " is not found.");
		}
		File file = new File(dstDir, id + "." + ext);

		PluginService.Output so = sos.output(0);
		InputStream is = so.stream();
		FileOutputStream os = new FileOutputStream(file);
		try {
			IOUtils.copy(is, os);
		} finally {
			os.close();
			is.close();
		}

		return file;

	}

	/**
	 * Get the content directly into the given file
	 * 
	 * @param executor
	 * @param id
	 * @param file
	 * @throws Throwable
	 */

	public static void getContentInFile (ServiceExecutor executor, String id, File file) throws Throwable {

		PluginService.Outputs sos = new PluginService.Outputs(1);
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", doc.root(), null, sos);

		if (r.element("asset/content") == null) file = null;


		PluginService.Output so = sos.output(0);
		InputStream is = so.stream();
		FileOutputStream os = new FileOutputStream(file);
		try {
			IOUtils.copy(is, os);
		} finally {
			os.close();
			is.close();
			so.close();  // Probably unecessary
		}
	}


	/**
	 * Gets the asset content from a local asset into a stream
	 * 
	 * @param executor
	 * @param id
	 *            Asset id
	 * @return
	 * @throws Throwable
	 */
	public static InputStream getContentInStream(ServiceExecutor executor,
			String id) throws Throwable {

		PluginService.Outputs sos = new PluginService.Outputs(1);
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", doc.root(), null, sos);
		if (r.element("asset/content") == null)
			return null;
		//
		PluginService.Output so = sos.output(0);
		return so.stream();
	}

	/**
	 * Set content
	 * 
	 * @param id
	 * @param archive
	 * @param contentType
	 * @throws Throwable
	 */
	public static void setContent (ServiceExecutor executor, String id, File archive, String contentType) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		// 
		FileInputStream f = new FileInputStream(archive);
		PluginService.Input ip = new PluginService.Input(f, archive.length(), contentType, null);
		PluginService.Inputs in = new PluginService.Inputs(ip);
		executor.execute("asset.set",dm.root(), in, null);
	}



	/**
	 * Get the sum of the content size recursively for this local citable ID.
	 * 
	 * @param executor
	 * @param id
	 *            citable ID of interest
	 * @return XmlDoc.Element as returned by asset.query :action sum :xpath
	 *         content/size
	 * @throws throwable
	 */
	public static XmlDoc.Element contentSizeSum(ServiceExecutor executor,
			String id) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");

		//
		String query = "(cid starts with '" + id + "' or cid = '" + id + "')";
		dm.add("where", query);
		dm.add("action", "sum");
		dm.add("xpath", "content/size");
		dm.add("pdist", 0); // Force local
		return executor.execute("asset.query", dm.root());
	}


	/**
	 * Prune content versions leaving just the latest
	 * 
	 * @param executor
	 * @param id asset ID of interest
	 * @return XmlDoc.Element as returned by asset.query :action sum :xpath
	 *         content/size
	 * @throws throwable
	 */
	public static void pruneContent (ServiceExecutor executor, String id) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		executor.execute("asset.prune", dm.root());

	}

	/**
	 * Returns the asset citeable id for this local id
	 * 
	 * @param id
	 * @param executor
	 * @return
	 * @throws Throwable
	 */
	public static String getCid(ServiceExecutor executor, String id)
			throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", doc.root());
		return r.value("asset/cid");
	}

	/**
	 * Returns the asset id for this local CID
	 * 
	 * @param cid
	 * @param executor
	 * @return
	 * @throws Throwable
	 */
	public static String getId(ServiceExecutor executor, String cid)
			throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", cid);
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", doc.root());
		return r.value("asset/@id");

	}

	public static String getModel(ServiceExecutor executor, String id,
			boolean citeable) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		if (citeable) {
			doc.add("cid", id);
		} else {
			doc.add("id", id);
		}
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", doc.root());
		return r.value("asset/model");

	}

	public static String getType (ServiceExecutor executor, String id, boolean isCID) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("agrs");
		if (isCID) {
			dm.add("cid", id);
		} else {
			dm.add("id", id);
		}
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		return r.value("asset/type");
	}



	/**
	 * Change the parent of the local asset (and its descendants).
	 * 
	 * @param cid
	 *            The citeable id of the asset.
	 * @param parent
	 *            The new citeable id of the new parent asset.
	 * @param recursive
	 * @return
	 * @throws Throwable
	 */
	public static String changeParent(ServiceExecutor executor, String cid,
			String pid, boolean recursive) throws Throwable {

		String proute = null;  // local

		String id = getId(executor, cid);

		if (CiteableIdUtil.getIdDepth(pid) + 1 != CiteableIdUtil
				.getIdDepth(cid)) {
			throw new Exception("Citeable id depth/length do not match. (cid="
					+ cid + " pid=" + pid + ")");
		}

		String newCid = pid + "." + CiteableIdUtil.getLastSection(cid);
		if (exists(executor, newCid, true)) {
			String pdist = "0";     // Local only
			newCid = CiteableIdUtil.createCid(executor, pdist, pid);
		} else {
			if (!nig.mf.pssd.plugin.util.CiteableIdUtil.cidExists(proute, executor, newCid)) {
				CiteableIdUtil.importCid(executor, newCid, 1);
			}
		}

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("cid", newCid);
		executor.execute("asset.cid.set", dm.root(), null, null);

		if (recursive) {
			dm = new XmlDocMaker("args");
			dm.add("where", "cid in " + "'" + cid + "'");
			dm.add("size", "infinity");
			dm.add("action", "get-cid");
			dm.add("pdist", 0); // Force local
			XmlDoc.Element r = executor.execute("asset.query", dm.root());
			Collection<String> ccids = r.values("cid");
			if (ccids != null) {
				for (String ccid : ccids) {
					changeParent(executor, ccid, newCid, recursive);
				}
			}
		}

		return newCid;

	}


	/**
	 * DOes an asset exist on the local server?
	 * 
	 * @param executor
	 * @param id
	 * @param citeable Is the id a citeable ID 
	 * @return
	 * @throws Throwable
	 */
	public static boolean exists(ServiceExecutor executor, String id,
			boolean citeable) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		if (citeable) {
			dm.add("cid", id);
		} else {
			dm.add("id", id);
		}
		XmlDoc.Element r = executor.execute("asset.exists", dm.root());
		return r.booleanValue("exists");
	}

	public static String getNamespace(ServiceExecutor executor, String id,
			String proute) throws Throwable {

		ServerRoute sr = proute == null ? null : new ServerRoute(proute);
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute(sr, "asset.get", dm.root());
		return r.value("asset/namespace");
	}

	public static boolean hasContent(ServiceExecutor executor, String id,
			String proute) throws Throwable {

		ServerRoute sr = proute == null ? null : new ServerRoute(proute);
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		XmlDoc.Element r = executor.execute(sr, "asset.get", dm.root());
		if (r.element("asset/content") != null) {
			return true;
		}
		return false;
	}


	/**
	 * Copy documents from one asset to another. All attributes are transferred except 'id' which is
	 * generated afresh in the new asset.
	 * 
	 * @param executor
	 * @param docTypes  The names of the documents; i.e. the document types
	 * @param cidIn
	 * @param cidOut
	 * @param ns - add an optional namespace attribute (ns) string
	 * @param tag - addd an optional tag attribute (tag) string
	 * @throws Throwable
	 */
	public static void copyMetaData (ServiceExecutor executor, Collection<String> docTypes, String idIn, String cidOut, Boolean isCIDIn, 
			String ns, String tag) throws Throwable {

		// Get meta-data in input object
		XmlDocMaker dm = new XmlDocMaker("args");
		if (isCIDIn) {
		   dm.add("cid", idIn);
		} else {
			dm.add("id", idIn);
		}
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		if (r==null) return;

		// Iterate through given document types
		for (String docType : docTypes) {

			// If we have this document type, copy it
			XmlDoc.Element docIn = r.element("asset/meta/"+docType);
			if (docIn!=null) {

				// We don't want the 'id' attribute. We want all other attributes
				// such as namespace and tag
				removeAttribute(executor, docIn, "id");

				// Do it
				XmlDocMaker dm2 = new XmlDocMaker("args");
				dm2.add("cid", cidOut);
				dm2.push("meta", new String[]{"action", "add"});
				if (ns!=null) {
					XmlDoc.Attribute attr = new XmlDoc.Attribute("ns", ns);
					docIn.add(attr);
				}
				if (tag!=null) {
					XmlDoc.Attribute attr = new XmlDoc.Attribute("tag", tag);
					docIn.add(attr);
				}
				dm2.add(docIn);
				dm2.pop();	
				executor.execute("asset.set", dm2.root());
			}
		}			
	}


	/**
	 * Get the asset's meta-data
	 * 
	 * @param executor
	 * @param cid Citable ID
	 * @param id Asset ID (give one of id or cid)
	 * @throws Throwable
	 */
	public static XmlDoc.Element getAsset (ServiceExecutor executor, String cid, String id) throws Throwable {

		// Get meta-data in input object
		XmlDocMaker dm = new XmlDocMaker("args");
		if (cid!=null) {
			dm.add("cid", cid);
		} else if (id!=null) {
			dm.add("id", id);
		} else {
			throw new Exception("One of cid or id must be given");
		}

		return executor.execute("asset.get", dm.root());

	}


	/**
	 * Returns the asset content encapsulation mime type (content/type). Null if none.  
	 * 
	 * @param id
	 * @param executor
	 * @return
	 * @throws Throwable
	 */
	public static String getContentMimeType (ServiceExecutor executor, String id) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", doc.root());
		return r.value("asset/content/type");
	}
	

	/**
	 * Returns the asset content logical mime type (content/ltype). Null if none.  
	 * 
	 * @param id
	 * @param executor
	 * @return
	 * @throws Throwable
	 */
	public static String getContentLogicalMimeType (ServiceExecutor executor, String id) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", id);
		doc.add("pdist", 0); // Force local
		XmlDoc.Element r = executor.execute("asset.get", doc.root());
		return r.value("asset/content/ltype");
	}
	



	/**
	 * Remove the given document (i.e. a full instantiation of a document type) from the given asset.
	 * 
	 * @param executor
	 * @param cid asset CID (give cid or id)
	 * @param id asset ID
	 * @param doc 
	 * @throws Throwable
	 */
	public static void removeDocument (ServiceExecutor executor, String cid, String id, XmlDoc.Element doc) throws Throwable {


		String[] attString = doc.attributeArray();

		// Remove
		XmlDocMaker dm = new XmlDocMaker("args");
		if (cid!=null) {
			dm.add("cid", cid);
		} else {
			if (id!=null) dm.add("id", id);
		}
		dm.push("meta", new String[]{"action", "remove"});
		dm.push(doc.qname(), attString);        // Use qualified name to set attributes; includes document name space
		dm.pop();
		dm.pop();
		//
		executor.execute("asset.set", dm.root());
	}



	/**
	 * Remove all elements of the given name from all instances of the doc type by name only
	 * for the given local asset. Only works if the element is an immediate child.
	 * 
	 * @param executor
	 * @param cid  citable id
	 * @param id asset id. must give cid or id
	 * @param docType
	 * @param name
	 * @param allowInvalid
	 * @throws Throwable
	 */
	static public void removeElementByName (ServiceExecutor executor, String cid, String id, String docType, String name, Boolean allowInvalid) throws Throwable { 

		if (id == null && cid == null) {
			throw new Exception("Neither id nor cid was specified.");
		}
		if (id == null) id = AssetUtil.getId(executor, cid);


		// Get the asset meta-data for the given Document Type
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("pdist", 0);        // Force local
		XmlDoc.Element r = executor.execute("asset.get", dm.root());

		Collection<XmlDoc.Element> docs= r.elements("asset/meta/" + docType);
		if (docs==null) return;

		// Iterate
		Boolean some = false;
		for (XmlDoc.Element doc : docs) {

			// Find the desired element in the Document and remove it
			Collection<XmlDoc.Element> elIns = doc.elements(name);
			if (elIns!=null) {
				for (XmlDoc.Element elIn : elIns) {
					some = true;
					doc.remove(elIn);
				}
			}
			if (some) {
				dm = new XmlDocMaker("args");
				dm.add("id", id);
				dm.add("allow-invalid-meta", allowInvalid);
				dm.push("meta", new String[] { "action", "replace" });
				dm.add(doc);
				dm.pop();
				executor.execute("asset.set", dm.root());
			}
		}
	}

	/**
	 * Remove the named atrtibute from the document
	 * 
	 * @param executor
	 * @param doc
	 * @param attributeName
	 * @throws Throwable
	 */
	public static void removeAttribute (ServiceExecutor executor, XmlDoc.Element doc, String attributeName) throws Throwable {
		XmlDoc.Attribute attr = doc.attribute(attributeName);
		if (attr != null) {
			doc.remove(attr);
		}

	}
}
