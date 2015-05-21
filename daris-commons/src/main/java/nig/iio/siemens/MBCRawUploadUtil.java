package nig.iio.siemens;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import nig.mf.client.util.AssetUtil;
import nig.mf.pssd.client.util.PSSDUtil;

import org.apache.commons.io.FileUtils;


import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;


/**
 * SOme functions shared by the PET/CT and MR uploaders for Siemens raw data files.
 * 
 * @author nebk
 *
 */

public class MBCRawUploadUtil {

	public enum SUBJECT_FIND_METHOD {NAME, ID, NAME_ID};


	public static PrintWriter createFileLogger (Boolean hasLogger, String logPath, String defLogPath)  throws Throwable {
		PrintWriter logger = null;
		if (hasLogger){ 

			// Set default log file path if not supplied
			if (logPath==null) logPath = defLogPath;
			checkPath (logPath);

			// Construct file name
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy_HH:mm:ss");
			String dt = sdf.format(date);
			logPath += "/rawdata-upload-" + dt + ".log";
			//
			logger = new PrintWriter(logPath);
		}
		return logger;
	}


	public static void log (PrintWriter logger, String text) {
		if (logger!=null) {
			logger.println(text);
			logger.flush();
		}
		System.out.println(text);
	}


	public static void deleteFile (File file, PrintWriter logger) throws Throwable {
		try {
			FileUtils.forceDelete(file);
			log (logger, "     Successfully deleted file " + file.getAbsolutePath());
		} catch (Exception e) {
			log (logger, "     *** Failed to delete file " + file.getPath() + " with error " + e.getMessage());
		}
	}


	public static String getCheckSum (ServerClient.Connection cxn, String id, String cid) throws Throwable {
		XmlDoc.Element r = nig.mf.client.util.AssetUtil.getMeta(cxn, id, cid);
		return r.value("asset/content/csum");

	}


	public static String nameSpaceQuery (String ns) {
		return "namespace='" + ns + "'";
	}


	public static String findSubjectAsset (ServerClient.Connection cxn, String DICOMNameSpace, String cid, Boolean addNIGSubjectMeta,  
			SUBJECT_FIND_METHOD method, String firstName, String lastName, String patientID,  PrintWriter logger) throws Throwable {

		// Does an asset exist for the supplied CID ?
		int depth = nig.mf.pssd.CiteableIdUtil.getIdDepth(cid);
		if (depth!=2 && depth!=3 && depth!=4) {
			throw new Exception ("The depth of the supplied id must be 2 (Repository), 3 (Project) or 4 (Subject)");
		}
		boolean exists = AssetUtil.exists(cxn, cid, true);


		// Handle Subject finding and creation
		String subjectCID = null;
		if (!exists) {

			// We are going to handle the special case that the CID is the repository (depth 2)
			// for which an asset does not exist. Under these conditions, we will try to 
			// find the Subject pre-existing in some PSSD project.  
			if (depth==2) {
				// Null if can't find or multiples
				subjectCID = MBCRawUploadUtil.findPatientAssetFromDICOM(cxn, method, firstName, lastName, patientID, cid, 
						DICOMNameSpace, logger);
			} else {
				String msg = "The supplied citable ID (with no asset)'" + cid + "' does not represent a Repository (the only allowed CID type with no asset).";
				throw new Exception (msg);
			}
		} else {
			// An object for the given CID exists. FInd the PSSD type of it
			String pssdType = PSSDUtil.typeOf(cxn, cid);
			if (pssdType.equals("project")) {

				// The object is a Project, first try to find the Subject pre-existing
				// from the meta-data. Null if not found or multiples
				subjectCID = MBCRawUploadUtil.findPatientAssetFromDICOM(cxn, method, firstName, lastName, patientID,
						cid, DICOMNameSpace, logger);
			} else if (pssdType.equals("subject")) {
				subjectCID = cid;			
			} else {
				throw new Exception ("The object associated with the supplied citable ID '" + cid + "' is neither a Project nor a Subject");
			}
		}

		// Add NIG-domain-specific meta-data.
		if (subjectCID!=null && addNIGSubjectMeta) {
			//			setNIGDomainMetaData (cxn, subjectCID, pm);
		}
		return subjectCID;
	}

	/**
	 *  Find the subject in DICOM or PSSD data model from the DICOM meta-data.  Use patient name,
	 *  patient ID or both
	 *  
	 * @param cxn
	 * @param method  find method, name or id or both
	 * @param {first,last}Name Patient Name
	 * @param patientID Patient ID
	 * @param cid  The citable ID may be a repository (depth 2) or Project (depth 3)
	 * @param logger
	 * @return
	 * @throws Throwable
	 */
	public static String findPatientAssetFromDICOM (ServerClient.Connection cxn, SUBJECT_FIND_METHOD method, String firstName, String lastName,
			String patientID, String cid, String DICOMNameSpace, PrintWriter logger) throws Throwable {
		// Build command
		XmlStringWriter w = new XmlStringWriter();

		// Make a query for a PSSD subject or a DICOM patient
		String query = null;
		if (cid!=null) {
			// DICOM data model; don't needs to qualify with namespace
			query = "model='om.pssd.subject' and cid starts with '" + cid + "'";	
			w.add("action", "get-cid");
		} else {
			// PSSD Data model
			query = nameSpaceQuery(DICOMNameSpace);
		}
		query += " and mf-dicom-patient has value";

		// Populate the rest of the query
		boolean proceed = false;
		if (method==SUBJECT_FIND_METHOD.NAME || method==SUBJECT_FIND_METHOD.NAME_ID) {
			if (lastName!=null) {
				// The first name may be empty (e.g. the phantom)
				String lastNameQuery = "xpath(mf-dicom-patient/name[@type='last'])=ignore-case('" + lastName + "')";
				query += " and " + lastNameQuery;
				if (firstName!=null) {
					String firstNameQuery = "xpath(mf-dicom-patient/name[@type='first'])=ignore-case('" + firstName + "')";
					query +=  " and " + firstNameQuery;
				}
				proceed = true;
			}
		}

		if (method==SUBJECT_FIND_METHOD.ID || method==SUBJECT_FIND_METHOD.NAME_ID) {
			// Patient ID is optional
			if (patientID!=null) {
				query += " and xpath(mf-dicom-patient/id)=ignore-case('" + patientID + "')";
			}
			proceed = true;
		}
		if (!proceed) return null;    // Insufficent meta-data to query with
		
		// Do the query
		w.add("where", query);
		XmlDoc.Element r = cxn.execute("asset.query", w.document());
		// TBD create DICOM patient asset if missing


		// Inspect result and validate
		String name = lastName;
		if (firstName!=null) name = firstName + " " + lastName;
		if (patientID!=null) name += "/" + patientID;
		if (cid!=null) {
			if (r==null) {
				log (logger, "     *** PSSD Subject asset not found for " + name + " - skipping");
				return null;
			}

			Collection<String> ids = r.values("cid");
			if (ids==null) {
				log (logger, "     *** PSSD Subject not found for " + name + " - skipping");
				return null;
			}
			if (ids!=null && ids.size() > 1) {
				log (logger, "     *** Multiple PSSD Subjects found for " + name + " - skipping");
				for (String id : ids) {
					log (logger, "     ***   ID :  " + id);
				}
				return null;
			}
			return r.value("cid");
		} else {
			if (r==null) {
				log (logger, "     *** Patient asset not found for " + name + " - skipping");
				return null;
			}
			Collection<String> ids = r.values("id");
			if (ids==null) {
				log (logger, "     *** Patient asset not found for patient " + name + " - skipping");
				return null;
			}
			if (ids!=null && ids.size() > 1) {
				log (logger, "     *** Multiple assets found for patient " + name + " - skipping");
				return null;
			}
			return r.value("id");
		}
	}










	public static String findExMethod (ServerClient.Connection cxn, String subjectCID) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("id",subjectCID);

		// Find the Method object registered in the Subject
		XmlDoc.Element r = cxn.execute("om.pssd.object.describe", w.document());
		String mid = r.value("object/method/id");	
		if ( mid == null ) {
			throw new Exception("There is no Method for PSSD subject " + subjectCID);
		}

		// Find the Ex-Method within the subject that is executing the primary
		// method. It's an error if there is no ex-method.
		w = new XmlStringWriter();
		w.add("id",subjectCID);
		w.add("method",mid);	
		r = cxn.execute("om.pssd.subject.method.find", w.document());
		String exMethodCID = r.value("id");        // FIrst if more than one.

		if ( exMethodCID == null ) {
			throw new Exception("There is no instantiated ex-method for PSSD subject " + subjectCID);
		}

		return exMethodCID;
	}

	public static String getFirstMethodStep (ServerClient.Connection cxn, String exMethodCid, 
			Collection<String> studyTypes) throws Throwable {

		// The DICOM modality is no-longer equated to Study type. We must find the
		// first Step in the Method that specifies DICOM data of the current modality
		// is acceptable.  No specification in the Method means that any DICOM modality
		// is allowed.
		String ss = "";
		for (String studyType : studyTypes) {
			XmlStringWriter w = new XmlStringWriter();
			w.add("id",exMethodCid);
			w.add("type",studyType);	
			XmlDoc.Element r = cxn.execute("om.pssd.ex-method.study.step.find",w.document());
			if (r!=null) {
				XmlDoc.Element s = r.element("ex-method/step");      // First one
				if (s!=null) {
					return s.value();
				}
			}
			ss += studyType + " ";
		}
		throw new Exception ("No step in the Method found for studies of types " + ss);

	}



	public static void checkPath (String path) throws Throwable {
		File tt = new File(path);
		if (!tt.exists()) {
			throw new Exception ("The path " + path + " does not exist");
		}

	}

}

