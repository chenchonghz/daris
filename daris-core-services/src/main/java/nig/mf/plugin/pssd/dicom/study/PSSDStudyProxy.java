package nig.mf.plugin.pssd.dicom.study;


import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import nig.iio.metadata.StudyMethodMetadata;
import nig.mf.Executor;
import nig.mf.plugin.pssd.util.MailHandler;
import nig.mf.plugin.util.PluginExecutor;
import nig.util.DateUtil;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.Session;
import arc.mf.plugin.dicom.StudyProxy;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;


/**
 * The PSSD study occurs under an ex-method. The incoming configuration
 * can connect to either:
 * 
 *  1. The subject, in which case the ex-method and study may need to 
 *     be created. If existing method, then or directly to the study.
 *     
 *  2. The study, which has already been created. In that case, there
 *     is nothing to do.
 *     
 * @author Jason
 *
 */
public class PSSDStudyProxy extends StudyProxy {

	//	private String _ns;                     // namespace to put data in; not currently used; namespace set by parent Project
	private String _subject;                // cid of Subject
	private String _exmethod;               // cid of Ex-Method
	private String _study;                  // cid of Study
	private String _methodStep;             // The STep in the Method to use
	private StudyMetadata _sm;
	private boolean _createdStudy;
	private String _domainMetaService;      // Service to set research domain-specific meta-data
	private Boolean _ignoreModality;        // Ignore modality in DICOM data and Method
	private Boolean _dropDoseReports;       // Drop dose reports for non-human Merthods

	/**
	 * Constructor.  We expect 1) just the Subject, 2) the Subject and ExMethod or 3) The Subject,
	 * ExMethod and Study CIDs to be given.
	 * 
	 * If the Method step is supplied as well, it can be used to create the Study.  If it's not
	 * supplied, DICOM controls dictate the behaviour regarding use of Method step.
	 *     
	 * @param namespace   Note that this is not used. The namespace is set by the parent Project
	 * @param studyId
	 */

	public PSSDStudyProxy(String namespace, String studyUID, String subject, String exmethod,
			String methodStep, String study, StudyMetadata sm, String subjectMetaService, 
			Boolean ignoreModality, Boolean dropSR) {
		super(studyUID);
		//		_ns      = namespace;           
		_subject = subject; 
		_exmethod  = exmethod; 
		_methodStep = methodStep; 
		_study   = study;   
		_sm      = sm;
		_createdStudy = false;
		_domainMetaService = subjectMetaService;
		_ignoreModality = ignoreModality;
		_dropDoseReports = dropSR;
	}

	public String id() {
		return _study;
	}

	public String subject() {
		return _subject;
	}
	public String exMethod() {
		return _exmethod;
	}

	public String methodStep() {
		return _methodStep;
	}

	public StudyMetadata metaData() {
		return _sm;
	}
	
	public Boolean dropDoseReports () {
		return _dropDoseReports;
	}

	public long createOrUpdateAsset(ServiceExecutor executor) throws Throwable {
		// Find the Ex-Method object that is registered with this Subject
		//  If not specified, then should return the
		// primary method for the subject.
		if ( _exmethod == null ) {
			findExMethod(executor);
		}

		// If the DICOM meta-data and server configuration do not specify
		// a Study CID, then _study will be null. Find or create the 
		// study to which this DICOM Study will be attached.  
		if ( _study == null ) {
			findOrCreateStudy(executor);
		}


		// If the Study was created by this DICOM server, the _methodStep will
		// be filled in.  If the Study has been supplied by CID, or a pre-existing
		// DICOM/Bruker Study found in findOrCreateStudy, we still need
		// to find the method step of that study 
		if ( _methodStep == null ) {
			findMethodStep(executor);
		}

		// At this point the STudy has either been created by this class
		// or it pre-existed.   Now add the standard DICOM meta-data mf-dicom-study
		// to the Study.  Overwrite if it pre-exists
		addStudyMetaData(executor);

		// Attach domain-dependent meta-data to the Subject and Study via the specified external service (DICOM control)
		if (_domainMetaService!=null) {
			XmlDocMaker dmSubject = new XmlDocMaker("args");
			XmlDocMaker dmStudy = new XmlDocMaker("args");
			dmSubject.add("id", _subject);
			dmStudy.add("id", _study);

			// Convert container to XML.  The parent is "dicom" ready to be added to the "dicom" element of the service.
			XmlDoc.Element m = _sm.toXML();

			// 
			if (m!=null) {
				dmSubject.add(m);
				dmStudy.add(m);
				try {
					executor.execute(_domainMetaService, dmSubject.root());	
				} catch (Throwable t) {
					// If it fails, we don't want to throw an exception. Just write to logfile
					System.out.println("Failed to set domain-specific subject meta-data with service " + _domainMetaService + " : " + t.getMessage());
				}
				try {
					executor.execute(_domainMetaService, dmStudy.root());	
				} catch (Throwable t) {
					// If it fails, we don't want to throw an exception. Just write to logfile
					System.out.println("Failed to set domain-specific study meta-data with service " + _domainMetaService + " : " + t.getMessage());
				}
			}
		}

		// Get ID of Study
		String studyId = nig.mf.pssd.plugin.util.CiteableIdUtil.cidToId(executor, _study);
		return  (long)Integer.valueOf(studyId);
	}

	/**
	 * Interface function; destroy the Study but only if we made it ourselves.
	 * 
	 */
	public void destroyAsset(ServiceExecutor executor) throws Throwable {
		if ( !_createdStudy ) {
			return;
		}

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",_study);

		executor.execute("om.pssd.object.destroy",dm.root());

		_createdStudy = false;
	}

	/**
	 * Interface function; find if the Study pre-exists
	 * 
	 */
	public boolean assetExists (ServiceExecutor executor) throws Throwable {
		String studyCID = doesStudyPreExist (executor);
		if (studyCID!=null) {
			System.out.println("    ***Study " + _sm.UID() + " with CID " + studyCID + " already exists in ExMethod " + _exmethod + " - re-using");
		}
		return (studyCID!=null);
	}

	/**
	 * Find the Method to which the study belongs. We may (or may not) 
	 * have a Method specified.
	 * 
	 * @param executor
	 * @throws Throwable
	 */
	private void findExMethod(ServiceExecutor executor) throws Throwable {
		if ( _exmethod != null ) {
			return;
		}

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",_subject);

		// Find the Method object registered in the Subject
		XmlDoc.Element r = executor.execute("om.pssd.object.describe",dm.root());
		String mid = r.value("object/method/id");	
		if ( mid == null ) {
			throw new Exception("There is no Method for PSSD subject " + _subject);
		}

		// Find the Ex-Method within the subject that is executing the primary
		// method. It's an error if there is no ex-method.
		dm = new XmlDocMaker("args");
		dm.add("id",_subject);
		dm.add("method",mid);	
		r = executor.execute("om.pssd.subject.method.find",dm.root());
		_exmethod = r.value("id");        // FIrst if more than one.

		if ( _exmethod == null ) {
			throw new Exception("There is no instantiated ex-method for PSSD subject " + _subject);
		}
	}

	private void findMethodStep(ServiceExecutor executor) throws Throwable {
		if ( _methodStep != null ) {
			return;
		}

		if ( _study == null ) {
			return;
		}

		// Find the Step in the Method that was used to create this Study
		// If a study is pre-created, the step should be in the Study meta-data
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",_study);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe",dm.root());
		_methodStep = r.value("object/method/step");
		if (_methodStep==null) {
			// The flow of the DICOM server means we shouldn't get to this point.
			// But if we do somehow then we default to the first step in Method that has
			// a Study TYPE that matches the DICOM modality.
			// CLients should fill in the method step when creating Studies
			_methodStep = getFirstStepPath (executor, _exmethod, _sm.modality());
		} else {
			// Having obtained the Method step, we now need to validate that
			// this step is consistent with the DICOM modality. 
			// We are no longer doing this validation as it prevents us from uploading e.g. modality SR
			// after MR and appending to existing Study

			// Find all the steps implicitly consistent with this DICOM modality
			/*
			Boolean explicit = false;
			Collection<XmlDoc.Element> steps = getMethodStudySteps (executor, _exmethod, _sm.modality(), explicit);
			Boolean found = false;
			if (steps != null) {
				for (XmlDoc.Element step : steps) {
					if (step.value().equals(_methodStep)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				throwModalityError (executor, _exmethod, _methodStep, _sm.modality());
			}
			 */
		}
	}

	private void findOrCreateStudy(ServiceExecutor executor) throws Throwable {
		if ( _study != null ) {
			return;
		}

		// Do we have this study already?
		XmlDocMaker dm = new XmlDocMaker("args");
		String sid = doesStudyPreExist (executor);

		// We found the STudy already, bug out here.  Method step is irrelevant in this process.
		if (sid!=null) {
			_study = sid;
			return;
		}

		// There is no pre-existing DICOM or Bruker study to be updated so we must make a new Study.
		if (_ignoreModality) {

			// Find the first step of the Method that has no Studies.  We ignore modality and
			// make no attempt to associate it with Study types (flawed concept). If no free
			// step then exception...
			if (_methodStep==null) {
				_methodStep = getFirstEmptyStepPath (executor, _exmethod);
			}
		} else {

			// Ask the ExMethod to create a new Study of the correct type and modality.
			// Defer the addition of the optional meta-data to later
			String modality = _sm.modality();
			if (_methodStep==null) {
				_methodStep = getFirstStepPath (executor, _exmethod, modality);
			}
		}

		// Now create the Study for the specified step
		// There is not really a piece of DICOM meta-data
		// that is appropriate for the name. Perhaps the description, 
		// although for most of our RCH data this is always the same string

		// If the Method step was supplied via the DICOM meta-data, rather than being
		// discovered here, the study create process will validate it so we don't
		// need checking code locally.		
		dm = new XmlDocMaker("args");
		dm.add("fillin", false);
		dm.add("pid",_exmethod);
		dm.add("step",_methodStep);
		if (_sm.description() != null) {
			// Use for both name and description
			dm.add("description", _sm.description());
			dm.add("name", _sm.description());
		}
		
		// Set the meta-data pre-specified by the Method.
		Executor pExecutor = new PluginExecutor(executor);
		StudyMethodMetadata.addStudyMethodMeta (pExecutor, _exmethod, _methodStep, dm);

		XmlDoc.Element r = executor.execute("om.pssd.study.create",dm.root());
		_study = r.value("id");
		//
		_createdStudy = true;
	}



	private String getFirstEmptyStepPath (ServiceExecutor executor,  String exMethodCid) throws Throwable {

		// Find all Study making steps in the Method
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",exMethodCid);
		XmlDoc.Element r = executor.execute("om.pssd.ex-method.study.step.find",dm.root());
		if (r==null) return null;
		Collection<XmlDoc.Element> steps = r.elements("ex-method/step");
		if (steps==null)  throw new Exception ("The ex-method " + exMethodCid + " does not have a study generating step");

		// FInd the first step with no Studies
		for (XmlDoc.Element step : steps) {
			String stepPath = step.value();
			XmlDocMaker dm2 = new XmlDocMaker("args");
			dm2.add("id", exMethodCid);
			dm2.add("step", stepPath);
			XmlDoc.Element r2 = executor.execute("om.pssd.ex-method.step.study.find", dm2.root());
			if (r2==null) return stepPath;
			Collection<XmlDoc.Element> objects = r2.elements("object");
			if (objects==null) return stepPath;	
		}

		// What to do if there are no empty steps ?
		throw new Exception("All Method steps have existing Studies; cannot create Study in the absence of an empty step");
	}


	private String getFirstStepPath (ServiceExecutor executor,  String exMethodCid, String modality) throws Throwable {

		// Find the first Step in the Method that specifies DICOM data of the current modality
		// is acceptable.  No specification in the Method means that any DICOM modality
		// is allowed.
		Collection<XmlDoc.Element> steps = getMethodStudySteps (executor, exMethodCid, modality, false);
		if (steps==null) throwModalityError (executor, exMethodCid, null, modality);

		// Get the first step for this modality
		Iterator<XmlDoc.Element> it = steps.iterator();
		XmlDoc.Element stepEl = it.next();
		String step = stepEl.value();
		// System.out.println("getFirstStepPath : found first step " + step);

		// It is quite possible there is more than Step in the Method that makes
		// a Study of this kind.  This server will only find the first.
		// To be smarter, the Study would need to be pre-created and the
		// CID specified in the DICOM meta-data. Many of our projects
		// now do this. 
		if ( step == null ) throwModalityError (executor, exMethodCid, null, modality);
		return step;
	}

	private Collection<XmlDoc.Element> getMethodStudySteps (ServiceExecutor executor, String exMethodCid, String modality, Boolean explicit) throws Throwable {

		// The DICOM modality is no-longer equated to Study type. We must find the
		// first Step in the Method that specifies DICOM data of the current modality
		// is acceptable.  No specification in the Method means that any DICOM modality
		// is allowed.
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",exMethodCid);
		//dm.add("type",modality);	
		// Implicit search; will find Steps with no modality since this means allow any modality
		dm.add("dicom-modality", new String[] {"explicit", explicit.toString()}, modality);  
		XmlDoc.Element r = executor.execute("om.pssd.ex-method.study.step.find",dm.root());
		if (r==null) return null;
		Collection<XmlDoc.Element> steps = r.elements("ex-method/step");
		return steps;
	}


	private void throwModalityError (ServiceExecutor executor, String exMethodCid, String stepPath, String modality) throws Throwable {
		// Send an email to the admins. This is a bit easier than faffing about with
		// notifications and triggers.  Since it's an error condition it's ok to 
		// do this without any end-user configuration (see daris:pssd-notification)
		String projectId = nig.mf.pssd.CiteableIdUtil.getProjectId(exMethodCid);
		String msg = "The ex-method " + exMethodCid + " does not have a study generating step which supports DICOM modality : " + modality;
		if (stepPath!=null) {
			msg = "The ex-method " + exMethodCid + " for step " + stepPath + " does not have a study which supports DICOM modality : " + modality;
		}
		String subject = "Failed DICOM upload for project " + projectId;
		MailHandler.sendAdminMessage (executor, projectId, subject, msg);

		// Generate exception
		throw new Exception(msg);
	}


	private void addStudyMetaData (ServiceExecutor executor) throws Throwable {

		// Retrieve meta-data on PSSD Study object
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",_study);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe",dm.root());
		XmlDoc.Element object = r.element("object");

		// Copy "interface" arguments to output document 
		dm = new XmlDocMaker("args");
		dm.add("id", _study);
		dm.add("type", object.value("type"));
		//
		String name = object.value("name");
		if (name != null) dm.add("name", name);
		String desc = object.value("description");
		if (desc != null) dm.add("description", desc);

		// Now add in element "meta/mf-dicom-study". 
		XmlDoc.Element oldMeta = object.element("meta");
		fillStudyMetadata(dm, oldMeta);

		// Set the meta-data
		executor.execute("om.pssd.study.update",dm.root());
	}




	private void fillStudyMetadata(XmlDocMaker dm, XmlDoc.Element oldMeta) throws Throwable {

		// Format the new date in MF date format
		Date newDate = _sm.date();
		String newDateStr = DateUtil.formatDate(newDate, true, false);
		String newUID = _sm.UID();
		String newID = _sm.id();
		String newDesc = _sm.description();
		//
		// We will  *add* the new metadata (not replace) but handle the case
		// of an overwrite (i.e. mf-dicom-study already exists for this Study)
		// By adding the mf-dicom-study meta-data, we will help ourselves if we end up
		// with wrong additional data in a Study somehow ; we will have more of the
		// audit trail with an extra mf-dicom-study
		Collection<XmlDoc.Element> mfStudies = null;
		if (oldMeta!=null) mfStudies = oldMeta.elements("mf-dicom-study");
		if (mfStudies!=null) {
			for (XmlDoc.Element mfStudy : mfStudies) {

				String oldUID = mfStudy.value("uid");
				String oldID = mfStudy.value("id");
				String oldDate= mfStudy.value("sdate");
				String oldDesc = mfStudy.value("description");

				// This should be enough...
				if (oldUID!=null && newUID !=null && !oldUID.equals(newUID)) break;
				if (oldID!=null && newID !=null && !oldID.equals(newID)) break;
				if (oldDate!=null && newDateStr !=null && !oldDate.equals(newDateStr)) break;
				if (oldDesc!=null && newDesc !=null && !oldDesc.equals(newDesc)) break;

				// We didn't break out so they match. Return but update the ingest date
				dm.push("meta", new String[] {"action", "merge"});
				dm.push("mf-dicom-study",new String[] { "ns", "dicom" });

				dm.push("ingest");
				dm.add("date","now");
				dm.add("domain",Session.user().domain());
				dm.add("user",Session.user().name());
				dm.pop();
				dm.pop();
				dm.pop();

				return;
			}
		}


		// Add new
		dm.push("meta", new String[] {"action", "add"});
		dm.push("mf-dicom-study",new String[] { "ns", "dicom" });

		if ( _sm.UID() != null ) {
			dm.add("uid",_sm.UID());
		}

		if ( _sm.id() != null ) {
			dm.add("id",_sm.id());
		}

		dm.push("ingest");
		dm.add("date","now");
		dm.add("domain",Session.user().domain());
		dm.add("user",Session.user().name());
		dm.pop();

		if ( _sm.institution() != null || _sm.station() != null ) {
			dm.push("location");
			if ( _sm.institution() != null ) {
				dm.add("institution",_sm.institution());
			}

			if ( _sm.station() != null ) {
				dm.add("station",_sm.station());
			}

			dm.pop();
		}

		if ( _sm.manufacturer()!= null || _sm.model() != null ) {
			dm.push("equipment");
			if ( _sm.manufacturer() != null ) {
				dm.add("manufacturer",_sm.manufacturer());
			}

			if ( _sm.model() != null ) {
				dm.add("model",_sm.model());
			}

			dm.pop();
		}


		if ( _sm.description() != null ) {
			dm.add("description",_sm.description());
		}

		if ( _sm.date() != null ) {
			dm.add("sdate", _sm.date());
		}

		if ( _sm.rpn() != null ) {
			dm.add("rpn",_sm.rpn());
		}

		if ( _sm.havePatientDetails() ) {
			dm.push("subject");

			if ( _sm.patientSex() != null ) {
				dm.add("sex",_sm.patientSex());
			}

			if ( _sm.patientAge() != -1 ) {
				dm.add("age",_sm.patientAge());
			}

			if ( _sm.patientWeight() != -1 ) {
				dm.add("weight",_sm.patientWeight());
			}

			if ( _sm.patientLength() != -1 ) {
				dm.add("size",_sm.patientLength());
			}

			dm.pop();
		}

		dm.pop();
	}




	private String doesStudyPreExist (ServiceExecutor executor) throws Throwable {

		// We need the ExMethod to find the Study under the create CID tree.
		// The DICOM framework will call this function (via assetExists). Since
		// we don't know when it will call it, if the ExMEthod has not been populated
		// we need to set it. This call is safe because it only consumes _subject
		// which is supplied in the PSSDStudyProxy constructor
		if (_exmethod==null) findExMethod(executor);

		// Note that this Study might have been created by the Bruker 
		// client or a DICOM client.  The goal is to have one STudy with either/both DICOM 
		// and Bruker meta-data attached and both DICOM and Bruker format
		// DataSets. The DICOM DataSets are derivations of the Bruker DataSets
		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "cid starts with '" + _exmethod + "' and xpath(mf-dicom-study/uid)='" + super.studyUID() + "'";
		dm.add("where",query);
		dm.add("action","get-cid");	
		dm.add("pdist", 0);      // Force local
		XmlDoc.Element r = executor.execute("asset.query",dm.root());
		String sid= r.value("cid");
		if (sid!=null) return sid;

		// If no DICOM try Bruker Study.   Could enhance by testing for both...but what
		// would we do if we had both ?
		dm = new XmlDocMaker("args");
		query = "cid starts with '" + _exmethod + "' and xpath(daris:bruker-study/uid)='" + super.studyUID() + "'";
		dm.add("where",query);
		dm.add("action","get-cid");	
		dm.add("pdist", 0);      // Force local
		r = executor.execute("asset.query",dm.root());
		sid = r.value("cid");
		if (sid!=null) return sid;
		return null;
	}
}
