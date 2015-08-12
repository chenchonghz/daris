package nig.mf.plugin.pssd.dicom.subject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import nig.mf.dicom.plugin.util.DICOMPatient;
import nig.mf.plugin.pssd.dicom.DicomIngestControls;
import nig.mf.plugin.pssd.dicom.study.CIDAndMethodStep;
import nig.mf.plugin.pssd.dicom.study.StudyMetadata;
import nig.mf.plugin.pssd.util.MailHandler;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.CiteableIdUtil;
import nig.mf.pssd.plugin.util.PSSDUtil;
import nig.util.DateUtil;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dicom.DicomPersonName;
import arc.utils.StringUtil;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;


/**
 * A collection of helper functions mainly used by the DICOM server to deal with Subject
 *  finding and creating. SPlit out from StudyProxyFactory
 * @author nebk
 *
 */
public class SubjectHandler {



	/**
	 * Handle auto-subject creation, dealing with cloning and standard creation
	 * 
	 * @param executor
	 * @param projectCid
	 * @param subjectCid  If null, then just create subject with next available cid. Otherwise create with this cid.
	 * @param ic
	 * @param studyMeta
	 * @return
	 * @throws Throwable
	 */
	static public String handleSubjectCreation (ServiceExecutor executor, String projectCid, String subjectCid, 
			DicomIngestControls ic, StudyMetadata studyMeta) throws Throwable {
		Boolean subjectWasNullOnEntry = (subjectCid==null);
		try {

			if (ic.cloneFirstSubject()) {
				// Try and clone first subject if desired. If there is no first subject to clone, then the clone returns
				// null and we proceed on to create normally
				String sid = cloneFirstSubject (executor, projectCid, subjectCid, ic.subjectMetaService());
				if (sid!=null) return sid;
			}

			// If we didn't clone then just normal auto-create. subjectCid may be null on input 
			boolean fillin = true;

			// We may be asked to name the Subject from the DICOM meta-data
			String name = setName (ic, studyMeta);
			//
			String[] sids  = PSSDUtil.createSubject (executor, projectCid, subjectCid, null, name, fillin);
			String sid = sids[0];
			if (sid!=null) System.out.println("       Subject " + sid + " auto created");
			return sid;
		} catch (Throwable t) {
			if (projectCid != null) {
				String msgSubject = "PSSD DICOM Engine - auto Subject creation for project '" + projectCid + "'failed";
				String msg = null;
				if (subjectWasNullOnEntry) {
					msg = "Auto-creation of a Subject with the next available citable identifier " + " \n failed with message: " + t.getMessage() + "\n The process will fall through to the standard Mediaflux DICOM data model engine if configured."; 
				} else {
					msg = "Auto-creation of a Subject with citable identifier '" + subjectCid + "' \n failed with message: " + t.getMessage() + "\n The process will fall through to the standard Mediaflux DICOM data model engine if configured."; 
				}
				System.out.println(msgSubject + " because " + msg);
				MailHandler.sendAdminMessage(executor, projectCid, msgSubject, msg);
			}
			return null;
		}
	}




	/**
	 * 
	 * @param executor
	 * @param findSUbjectMethod  'id' or 'name'
	 * @param studyMeta
	 * @param projectCID constrain by this project if possible
	 * @return
	 * @throws Throwable
	 */
	public static CIDAndMethodStep findSubjectByDetail (ServiceExecutor executor, String findSubjectMethod, 
			StudyMetadata studyMeta, String projectCID) throws Throwable {		

		// If we have no Patient details in the incoming Study there is nothing to find !
		if (!studyMeta.havePatientDetails()) return null;

		// FInd proxy user AET
		XmlDoc.Element r = executor.execute("user.self.describe");
		String  aet = r.value("user/@user").toUpperCase();

		// First we look for mf-dicom-patient (either directly if we know the
		// project ID or for projects configured with pssd-dicom-ingest/subject/find=true)
		DicomPersonName pn = studyMeta.patientName();
		String pns = null;
		if (pn!=null) {
			pns = studyMeta.patientName().toString();
		}
		String subjectCID = findSubjectFromDICOMMeta (executor, findSubjectMethod, projectCID, 
				studyMeta, aet);
		if (subjectCID!=null) {
			System.out.println("         Subject " + subjectCID + " found");
			return new CIDAndMethodStep(subjectCID, null);
		}

		// If we didn't find it mf-dicom-patient we can now try with Projects and meta-data
		// configured with pssd-dicom-ingest/subject/find=true and specified domain-specific meta-data
		System.out.println("         Subject not found - search in domain meta-data");
		subjectCID = findSubjectFromDomainMeta (executor, projectCID, studyMeta, aet);
		if (subjectCID!=null) {
			System.out.println("         Subject " + subjectCID + " found");
			return new CIDAndMethodStep(subjectCID, null);
		} else {
			System.out.println("         Subject not found");
		}
		//
		return null;
	}


	private static String setName (DicomIngestControls ic, StudyMetadata studyMeta) throws Throwable {
		DicomPersonName pn = studyMeta.patientName();
		String name = null;
		if (ic.setSubjectNameFromFirst()) {
			name = pn.first();
		} else if (ic.setSubjectNameFromLast()) {
			name = pn.last();
		} else if (ic.setSubjectNameFromFull()) {
			name = pn.fullName();
		} else if (ic.setSubjectNameFromID()) {
			name = studyMeta.patientID();
		}
		//
		String r = ic.setSubjectNameFromIndexRange();
		if (r != null) {
			String[] parts = r.split(",");
			int p0 = Integer.parseInt(parts[0]);
			int p1 = Integer.parseInt(parts[1]) + 1;    // Silly Java String index handling
			if (parts.length==2 && p1>=p0) {
				name = name.substring(p0,p1);
			}
		}

		String d = ic.setSubjectNameFromIgnoreAfterLastDelim();
		if (d!=null) {
			name = CiteableIdUtil.removeAfterLastDelim(name, d);
		}

		return name;
	}

	private static String findSubjectFromDomainMeta (ServiceExecutor executor, String projectCID, 
			StudyMetadata studyMeta, String aet) throws Throwable {

		// Find  Projects that are configured to be searched for subject details by a given AET
		// If we have a project CID, this constrains it further
		Collection<XmlDoc.Element> projects = null;
		if (projectCID==null) {
			projects = findProjectsConfiguredForSubjectFind (executor, aet, projectCID);
		} else {
			// If we have the CID already, we don't need to go looking for it.
			projects = new ArrayList<XmlDoc.Element>();
			projects.add(AssetUtil.getAsset(executor, projectCID, null).element("asset"));
		}
		if (projects==null) return null;

		// Iterate over projects and look for the Subject
		String subjectCID = null;
		for (XmlDoc.Element project : projects) {
			String projectID = project.value("cid");
			// Fetch the paths where the name is stored from the Project meta-data
			XmlDoc.Element namePaths = project.element("meta/daris:pssd-dicom-ingest/subject/name");
			if (namePaths==null) return null;
			String t = findSubjectFromDomainForOneProject (executor, namePaths, projectID, studyMeta);
			if (subjectCID==null) {
				subjectCID = t;
			} else {
				throw new Exception ("Multiple Subjects across Projects were found matching the DICOM patient record - cannot disambiguate");
			}
		}
		return subjectCID;
	}

	private static Collection<XmlDoc.Element> findProjectsConfiguredForSubjectFind (ServiceExecutor  executor, 
			String aet, String projectCID) throws Throwable {     

		// Do any Projects hold the required pssd-dicom-ingest configuration meta-data
		// that says we the calling AET can look in there?
		XmlDocMaker dm = new XmlDocMaker("args");
		//
		String query = "model='om.pssd.project' and xpath(daris:pssd-dicom-ingest/subject/find)='true'";
		if (aet!=null) query += " and xpath(daris:pssd-dicom-ingest/subject/aet)=ignore-case('"+aet+"')";
		if (projectCID!=null) query += " and cid='" + projectCID + "'";
		dm.add("where", query);
		dm.add("size", "infinity");
		dm.add("pdist", 0);         // Local server only
		dm.add("action", "get-meta");
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		Collection<XmlDoc.Element> t = null;
		if (r!=null) t=r.elements("asset");

		// If we don't find anything, try again without the AET as it's optional
		if (t==null || (t!=null && t.size()==0)) {
			dm = new XmlDocMaker("args");
			//
			query = "model='om.pssd.project' and xpath(daris:pssd-dicom-ingest/subject/find)='true'";
			if (projectCID!=null) query += " and cid='" + projectCID + "'";
			dm.add("where", query);
			dm.add("size", "infinity");
			dm.add("pdist", 0);         // Local server only
			dm.add("action", "get-meta");
			r = executor.execute("asset.query", dm.root());
		}
		if (r!=null) {
			return r.elements("asset");
		} else {
			return null;
		}
	}


	/** 
	 * For the given project find the Subject  by DICOM meta-data mf-dicom-patient
	 * 
	 * @param executor
	 * @param namePaths - the XPATHS of the name fields to search in
	 * @param projectCID
	 * @param studyMeta
	 * @return
	 * @throws Throwable
	 */
	private static String findSubjectFromDomainForOneProject (ServiceExecutor executor, XmlDoc.Element namePaths, String projectCID, StudyMetadata studyMeta) throws Throwable {

		// Do a query for the given project looking for all patients that match
		// the DICOM name credential
		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "cid starts with '" + projectCID + "' and model='om.pssd.subject'";
		
		// If the name is null we can't match
		DicomPersonName pn = studyMeta.patientName();
		if (pn==null) return null;
		//
		query += matchSubjectQuery (executor, pn, namePaths);
		dm.add("where", query);	
		dm.add("pdist", "0");
		dm.add("action", "get-cid");
		XmlDoc.Element r = executor.execute("asset.query", dm.root());

		// If we find more than one Subject matching for this Project (means the user has
		// created the same Subject twice) we have to throw an exception
		Collection<String> cids = r.values("cid");
		if (cids==null) return null;
		if (cids.size()>1) {
			throw new Exception ("Multiple Subjects matching the DICOM Subject name have been found for project CID '" +
					projectCID + "';  cannot disambiguate");
		}
		return r.value("cid");
	}



	private static String findSubjectFromDICOMMeta (ServiceExecutor executor, String findSubjectMethod,
			String projectCID, StudyMetadata studyMeta, String aet) throws Throwable {

		// See if we can find the subject 
		String subjectCID = null;
		if (projectCID!=null) {

			// We have a Project CID and can constrain the search to that subject
			subjectCID = findSubjectFromDICOMForOneProject (executor, findSubjectMethod, projectCID, studyMeta);
		} else {

			// We don't have a project CID.  So find  Projects that are configured
			// to be searched for subject details by a given AET
			Collection<XmlDoc.Element> projects = findProjectsConfiguredForSubjectFind (executor, aet, null);

			// Now look for the DICOM meta-data match 
			if (projects!=null) {
				for (XmlDoc.Element project : projects) {
					String projectID = project.value("cid");
					String t = findSubjectFromDICOMForOneProject (executor, findSubjectMethod, projectID, studyMeta);
					if (subjectCID==null) {
						subjectCID = t;
					} else {
						throw new Exception ("Multiple Subjects were found matching the DICOM patient record - cannot disambiguate");
					}
				}
			}
		}
		return subjectCID;
	}


	/** 
	 * For the given project find the Subject  by DICOM meta-data mf-dicom-patient
	 * 
	 * @param executor
	 * @param projectCID
	 * @param studyMeta
	 * @return
	 * @throws Throwable
	 */
	private static String findSubjectFromDICOMForOneProject (ServiceExecutor executor, String findSubjectMethod,
			String projectCID, StudyMetadata studyMeta) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "model='om.pssd.subject' and cid starts with '" + projectCID + "'" +
				" and mf-dicom-patient has value";
		dm.add("where", query);
		dm.add("size", "infinity");
		dm.add("pdist", 0);         // Local server only
		dm.add("action", "get-meta");
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r==null) return null;

		// Now look at each mf-dicom-patient record and try to match the patient credentials
		// Ideally there should only be one, but operational errors could result in multiples
		Collection<XmlDoc.Element> patients = r.elements("asset"); 
		if (patients!=null) {
			for (XmlDoc.Element patient : patients) {
				XmlDoc.Element oldPatientMeta = patient.element("meta/mf-dicom-patient");
				if (oldPatientMeta!=null) {
					if (matchDICOMDetail (findSubjectMethod, oldPatientMeta, studyMeta)) {
						return patient.value("cid");
					}
				}
			}
		}
		return null;
	}


	public static void addSubjectDICOMMetaData (ServiceExecutor executor, DicomIngestControls ic, String subject, StudyMetadata sm) throws Throwable {
		// Bug out if nothing to do
		if (!sm.havePatientDetails()) return;

		// Retrieve meta-data on PSSD Subject object
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", subject);
		XmlDoc.Element r = executor.execute("asset.get",dm.root());

		// There may be multiples - this audit trail can be useful
		Collection<XmlDoc.Element> patients = r.elements("asset/meta/mf-dicom-patient");

		// Fetch patient meta from DICOM
		String id = sm.patientID();
		String sex = sm.patientSex();
		Date dob = sm.patientDateOfBirth();
		DicomPersonName name = sm.patientName();

		// Now add mf-dicom-patient
		if (patients!=null) {
			// See if we can find pre-existing record matching on all patient meta-data details
			for (XmlDoc.Element patient : patients) {	
				String method = ic.findSubjectMethod();
				if (method==null) method = "name+";
				if (matchDICOMDetail (method, patient, sm)) {
					System.out.println("        Matched mf-dicom-patient");
					return;
				}
			}
			System.out.println("        Failed to match mf-dicom-patient");
		}

		// Prepare meta-data 
		dm = new XmlDocMaker("args");
		dm.add("cid", subject);
		dm.push("meta", new String[]{"action","add"});
		dm.push("mf-dicom-patient", new String[]{"ns", "pssd.private"});

		if (id!=null) dm.add("id", id);      
		if (sex!=null) dm.add("sex", sex);
		if (dob!=null) {
			// Canonicalize the Time to 00:00:00 (meaingless for DOB)
			String t = DateUtil.formatDate(dob, true, true);
			dm.add("dob", t);
		}

		if (name!=null) {
			addName (dm, name.first(), "first");
			addName (dm, name.middle(), "middle");
			addName (dm, name.last(), "last");
		}
		dm.pop();

		// Set the meta-data
		executor.execute("asset.set",dm.root());
	}


	/**
	 * This function compares pre-existing patient meta-data with new and sends 
	 * an email as configured if there are discrepancies (a sanity check)
	 * This is different behaviour to matching meta-data for pre-finding
	 * subjects
	 * 
	 * @param executor
	 * @param subject
	 * @param sm
	 * @throws Throwable
	 */
	public static void compareSubjectDICOMMetaData (ServiceExecutor executor, String subject, StudyMetadata sm) throws Throwable {

		// Bug out if nothing to do
		if (!sm.havePatientDetails()) return;

		// Retrieve meta-data on PSSD Subject object
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", subject);
		XmlDoc.Element r = executor.execute("asset.get",dm.root());

		// There may be multiples - this audit trail can be useful
		Collection<XmlDoc.Element> patients = r.elements("asset/meta/mf-dicom-patient");

		// Prepare meta-data 
		dm = new XmlDocMaker("args");
		dm.add("cid", subject);
		dm.push("meta", new String[]{"action","merge"});

		// Fetch patient meta from DICOM
		String id = sm.patientID();
		String sex = sm.patientSex();
		Date dob = sm.patientDateOfBirth();
		DicomPersonName name = sm.patientName();

		// Now add mf-dicom-patient
		if (patients==null || patients.size()==0) {
			return;
		} else {

			// For each pre-existing patient record, compare with the incoming meta-data
			// Prepare an email if any of the meta-data differ
			String msg = null;
			for (XmlDoc.Element patient : patients) {
				String t = comparePatient (id, name, dob, sex, patient);
				if (t!=null) {
					if (msg==null){
						msg = t;
					} else {
						msg += t;
					}	
				}
			}

			if (msg!=null) {

				// Send emails based on who is configured to receive.
				String project = CiteableIdUtil.getParentId(subject);

				dm = new XmlDocMaker ("args");
				dm.add("id", project);
				dm.add("async", true);
				dm.add("use-notification",new String[]{"category", "dicom-upload"}, true);
				dm.add("message", msg);
				dm.add("subject", "Subject " + subject + " - inconsistent existing and new DICOM meta-data");
				executor.execute("om.pssd.project.mail.send", dm.root());
			}
		}
	}

	private static String comparePatient (String idNew, DicomPersonName nameNew, Date dobNew, String sexNew,
			XmlDoc.Element metaOld) throws Throwable {

		if (nameNew==null) return null;
		if (metaOld==null) return null;

		//  Java will write 'null' in Strings where a null variable is inserted so
		// this code is ok

		String t = "";
		String idOld = metaOld.value("id");
		if (!DICOMPatient.stringsMatch(idOld,idNew, true)) {
			t += "ID old/new = " + idOld + "/" + idNew + "\n";
		}
		Date dobOld = metaOld.dateValue("dob");
		if (!DICOMPatient.dicomDOBMatch(dobOld, dobNew)) {
			String dobOldString = null;
			if (dobOld!=null) dobOldString = DateUtil.formatDate(dobOld, false, false);
			String dobNewString = null;
			if (dobNew!=null) dobNewString = DateUtil.formatDate(dobNew, false, false);
			t += "DOB old/new = " + dobOldString + "/" + dobNewString + "\n";
		}
		if (!DICOMPatient.matchDICOMDetail("name", metaOld, nameNew, null, null, null)) {
			String nameOldString = null;
			if (metaOld!=null) {
				DICOMPatient dp = new DICOMPatient (metaOld);
				nameOldString = dp.getFullName();
			}
			t += "Name old/new = " + nameOldString + "/" + nameNew.fullName() + "\n";
		}
		//
		if (t.equals("") || t.length()==1) t=null;
		return t;
	}


	private static void addName (XmlDocMaker dm, String name, String type) throws Throwable {
		// Sometimes we put CIDs in the patient name  so we don't want them (I doubt someone is called a CID)
		if (name!=null && !CiteableIdUtil.isCiteableId(name)) {
			dm.add("name", new String[]{"type", type}, name);
		}
	}





	private static boolean matchDICOMDetail (String findSubjectMethod, XmlDoc.Element oldPatientMeta, StudyMetadata studyMeta) throws Throwable {
		DicomPersonName newName = studyMeta.patientName();
		return DICOMPatient.matchDICOMDetail (findSubjectMethod, oldPatientMeta, newName, studyMeta.patientSex(), 
				studyMeta.patientDateOfBirth(), studyMeta.patientID());
	}


	private static String matchSubjectQuery (ServiceExecutor executor, DicomPersonName pn, XmlDoc.Element namePaths) throws Throwable {

		String query = "";
		//
		String prefix = namePaths.value("prefix");
		if (prefix != null && pn.prefix()!=null) query += " and xpath(" + prefix + ")=ignore-case('" + pn.prefix() + "')";
		//
		String suffix = namePaths.value("suffix");
		if (suffix != null && pn.suffix()!=null) query += " and xpath(" + suffix + ")=ignore-case('" + pn.suffix() + "')";
		//
		String first = namePaths.value("first");
		if (first != null && pn.first()!=null) {
			String t = StringUtil.escapeSingleQuotes(pn.first());
			query += " and xpath(" + first + ")=ignore-case('" + t + "')";
		}
		//
		String last = namePaths.value("last");
		if (last != null && pn.last()!=null) {
			String t = StringUtil.escapeSingleQuotes(pn.last());
			query += " and xpath(" + last + ")=ignore-case('" + t + "')";
		}

		// There is only one middle name in DICOM 
		String middle = namePaths.value("middle");
		if (middle != null && pn.middle()!=null) {
			String t = StringUtil.escapeSingleQuotes(pn.middle());
			query += " and xpath(" + middle + ")=ignore-case('" +t + "')";
		}

		return query;	
	}





	/**
	 * Function to try to auto-create a Subject of the given CID, if it is of the correct depth
	 * 
	 * 
	 * @param projectCid
	 * @param subjectCid  if null, create with next available cid else create with this cid, if available
	 * @param sm
	 * @return
	 * @throws Throwable
	 */
	private static String cloneFirstSubject (ServiceExecutor executor, String projectCID, String subjectCID,
			String subjectMetaService) throws Throwable {

		// Find the first subject; if none, return for auto-create from Method meta-data
		String firstSubject = findFirstSubject (executor, projectCID);
		if (firstSubject==null) {
			return null;
		}

		// CLone it
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", firstSubject);
		if (subjectCID != null) {
			String subjectNumber = nig.mf.pssd.CiteableIdUtil.getLastSection(subjectCID);
			dm.add("subject-number", subjectNumber);
		}
		XmlDoc.Element r = executor.execute("om.pssd.subject.clone", dm.root());
		if (r==null) return null;
		subjectCID = r.value("id");       // Overwrite for when null on input

		// Remove any meta-data from the clone that the domain-specific layer might have set 
		// The correct domain-specific data will be set subsequently when the data are actually
		// uploaded.  These meta-data are subject specific so it's not correct that they
		// be cloned
		if (subjectMetaService!=null) {
			dm = new XmlDocMaker("args");
			dm.add("id", subjectCID);

			// TBD: it's bad that I have an argument here as it means all the services have to
			// have this argument. Really, I should make another service another DICOM control!
			// Ho hum.  Will use try/catch in case the argument is wrong and then at least
			// it won't break anything.
			dm.add("remove", "true");
			dm.push("dicom");
			dm.pop();
			try {
				executor.execute(subjectMetaService, dm.root());
			} catch (Throwable t) {
				System.out.println(subjectMetaService + " failed with " + t.getMessage());
				// Do nothing
			}
		}
		return subjectCID;
	}

	private static String findFirstSubject (ServiceExecutor executor, String pid) throws Throwable {
		XmlDocMaker w = new XmlDocMaker("args");
		w.add("id", pid);
		XmlDoc.Element r = executor.execute("om.pssd.collection.member.list", w.root());
		if (r==null) return null;
		//
		return r.value("object/id");     // Returns the first one
	}
}
