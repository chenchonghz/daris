package nig.mf.pssd.client.util;

import java.util.Collection;
import java.util.Iterator;

import nig.iio.metadata.SubjectMethodMetadata;
import nig.mf.Executor;
import nig.mf.client.util.ClientExecutor;
import nig.mf.client.util.LogUtil;
import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlStringWriter;

public class PSSDUtil extends nig.mf.pssd.CiteableIdUtil{

	/**
	 * Returns the object type for the given local object. Exception if does not exist
	 * 
	 * @param executor
	 * @param cid   citeable ID
	 * @return  PSSD type
	 * @throws Throwable
	 */

	public static String typeOf(ServerClient.Connection cxn, String cid) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("cid", cid);
		w.add("pdist",0);                 // Force local 		
		XmlDoc.Element r = cxn.execute("asset.get", w.document());
		return r.stringValue("asset/meta/daris:pssd-object/type");
	}

	
	/**
	 * Function to try to auto-create a Subject with the given CID (must be Subject depth)
	 * If CID exists, re-use
	 * If CID does not exist, import
	 * Set Method defined meta-data on the Subject
	 * 
	 * 
	 * @param cid
	 * @param logFile. Can be null for no server-side logging
	 * @param subjectCID 
	 * @return
	 * @throws Throwable
	 */
	public static boolean createSubject (ServerClient.Connection cxn, String logFile, String subjectCID) throws Throwable {

		// CHeck CID depth
		if (!nig.mf.pssd.CiteableIdUtil.isSubjectId(subjectCID)) return false;

		// Get Project CID and get any Methods
		String pid = nig.mf.pssd.CiteableIdUtil.getProjectId(subjectCID);
		XmlStringWriter w = new XmlStringWriter();
		w.add("id", pid);
		XmlDoc.Element r = cxn.execute("om.pssd.object.describe", w.document());
		Collection<XmlDoc.Element> methods = r.elements("object/method");

		// We can't proceed if there are no Methods or more than one (how could we choose ?)
		if (methods==null) return false;
		if (methods.size()>1) return false;

		// Get the Method CID
		Iterator<XmlDoc.Element> it = methods.iterator();
		XmlDoc.Element method = it.next();
		String mid = method.value("id");
		if (mid==null) return false;

		// If the Subject CID has not been allocated, import it.  
		if (!nig.mf.pssd.client.util.CiteableIdUtil.cidExists(cxn, subjectCID)) {
			String importedCID = nig.mf.pssd.client.util.CiteableIdUtil.importCid(cxn, subjectCID);
			if (!importedCID.equals(subjectCID)) {
				String errMsg = "   Imported Subject CID:" + importedCID + " is not consistent with expected:" + subjectCID;
				LogUtil.logError(cxn, logFile, errMsg);
				throw new Exception(errMsg);
			}
		}


		// We already know the asset does not exist so now we can try to create it
		String subjectNumber = nig.mf.pssd.CiteableIdUtil.getLastSection(subjectCID);
		XmlDocMaker dm = new XmlDocMaker("args");
		// XmlStringWriter dm = new XmlStringWriter();
		dm.add("pid", pid);
		dm.add("subject-number", subjectNumber);
		dm.add("method", mid);

		// Set the meta-data pre-specified by the Method.  Because this code is shared 
		// with plugins (DICOM server) use the executor wrapping framework that combines the two
		Executor cExecutor = new ClientExecutor(cxn);
		SubjectMethodMetadata.addSubjectMethodMeta (cExecutor, mid, dm);

		// Create the subject
		r = cExecutor.execute("om.pssd.subject.create", dm);

		return true;
	}



}
