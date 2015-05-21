package nig.mf.client.util;

import java.io.File;


import arc.mf.client.*;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class AssetUtil {


	/**
	 * Gets the asset content from a local asset and save it into the specified file
	 * file.
	 * 
	 * @param executor
	 * @param id
	 *            Asset id
	 * @param dstDir
	 * @return
	 * @throws Throwable
	 */
	public static void getContent(ServerClient.Connection conn, String id, File fileName)
			throws Throwable {

		XmlStringWriter w = new XmlStringWriter();
		w.add("id", id);
		w.add("pdist", 0); // Force local
		ServerClient.Output output = new ServerClient.FileOutput(fileName);
		conn.execute("asset.get", w.document(), null, output);
	}

	public static XmlDoc.Element getMeta (ServerClient.Connection conn, String id, String cid)
			throws Throwable {

		XmlStringWriter w = new XmlStringWriter();
		if (cid!=null) {
			w.add("cid", cid);
		} else {
			w.add("id", id);
		}
		w.add("pdist", 0); // Force local
		return conn.execute("asset.get", w.document());
	}

	public static void destroy (ServerClient.Connection conn, String id, String cid) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		if (cid!=null) {
			w.add("cid", cid);
		} else {
			w.add("id", id);
		}
		conn.execute("asset.get", w.document());
	}

	public static Boolean exists (ServerClient.Connection conn, String id, boolean isCID) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		if (isCID) {
			w.add("cid", id);
		} else {
			w.add("id", id);
		}
		XmlDoc.Element r = conn.execute("asset.exists", w.document());
		return r.booleanValue("exists");
	}
}
