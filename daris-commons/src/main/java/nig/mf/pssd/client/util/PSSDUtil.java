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
	 * Function to try to auto-create a Subject with the given CID (must be Subject or Project depth)
	 * If CID is SUbject depth and exists, re-use
	 * If CID is SUbject depth and does not exist, import
	 * If CID is Project create Subject under
	 * 
	 * Set Method defined meta-data on the Subject
	 * 
	 * 
	 * @param cid
	 * @param logFile. Can be null for no server-side logging
	 * @param subjectCID 
	 * @return the subject CID (either that passed in or created under Project)
	 * @throws Throwable
	 */
	public static String createSubject (ServerClient.Connection cxn, String logFile, String objectCID) throws Throwable {

		String projectCID = null;
		boolean cidIsSubject = true;
		if (nig.mf.pssd.CiteableIdUtil.isProjectId(objectCID)) {
			projectCID = objectCID;
			cidIsSubject = false;
		} else if (nig.mf.pssd.CiteableIdUtil.isSubjectId(objectCID)) {
			projectCID = nig.mf.pssd.CiteableIdUtil.getProjectId(objectCID);
		} else {
			return null;
		}

		// Get Project CID and get any Methods
		XmlStringWriter w = new XmlStringWriter();
		w.add("id", projectCID);
		XmlDoc.Element r = cxn.execute("om.pssd.object.describe", w.document());
		Collection<XmlDoc.Element> methods = r.elements("object/method");

		// We can't proceed if there are no Methods or more than one (how could we choose ?)
		if (methods==null) return null;
		if (methods.size()>1) return null;

		// Get the Method CID
		Iterator<XmlDoc.Element> it = methods.iterator();
		XmlDoc.Element method = it.next();
		String mid = method.value("id");
		if (mid==null) return null;

		// Populate meta-data for Subject creation
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("pid", projectCID);

		// If the Subject CID has not been allocated, import it.  
		if (cidIsSubject) {
			if (!nig.mf.pssd.client.util.CiteableIdUtil.cidExists(cxn, objectCID)) {
				String importedCID = nig.mf.pssd.client.util.CiteableIdUtil.importCid(cxn, objectCID);
				if (!importedCID.equals(objectCID)) {
					String errMsg = "   Imported Subject CID:" + importedCID + " is not consistent with expected:" + objectCID;
					LogUtil.logError(cxn, logFile, errMsg);
					throw new Exception(errMsg);
				}
			}


			// We already know the asset does not exist so now we can try to create it
			String subjectNumber = nig.mf.pssd.CiteableIdUtil.getLastSection(objectCID);
			// XmlStringWriter dm = new XmlStringWriter();
			dm.add("subject-number", subjectNumber);			
		}
		dm.add("method", mid);

		// Set the meta-data pre-specified by the Method.  Because this code is shared 
		// with plugins (DICOM server) use the executor wrapping framework that combines the two
		Executor cExecutor = new ClientExecutor(cxn);
		SubjectMethodMetadata.addSubjectMethodMeta (cExecutor, mid, dm);

		// Create the subject and return the CID
		r = cExecutor.execute("om.pssd.subject.create", dm);
		return r.value("id");
	}




}
