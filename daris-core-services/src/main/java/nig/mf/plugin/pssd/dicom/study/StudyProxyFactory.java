package nig.mf.plugin.pssd.dicom.study;

import java.util.Collection;
import java.util.StringTokenizer;

import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dicom.DataElementMap;
import arc.mf.plugin.dicom.DicomAssetEngine;
import arc.mf.plugin.dicom.DicomEngineContext;
import arc.mf.plugin.dicom.DicomPersonName;
import arc.mf.plugin.dicom.StudyProxy;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.dicom.DICOMProjectSelector;
import nig.mf.plugin.pssd.dicom.DicomElements;
import nig.mf.plugin.pssd.dicom.DicomIngestControls;
import nig.mf.plugin.pssd.dicom.DicomLog;
import nig.mf.plugin.pssd.dicom.NIGDicomAssetEngine;
import nig.mf.plugin.pssd.dicom.DicomAssetEngineConstants;
import nig.mf.plugin.pssd.dicom.subject.SubjectHandler;
import nig.mf.plugin.pssd.util.MailHandler;
import nig.mf.pssd.CiteableIdUtil;
import nig.util.DateUtil;

public class StudyProxyFactory {

    /**
     * Creates the "right" (PSSD or DICOM/PSS) study proxy for the incoming
     * study.
     * 
     * @param executor
     * @param studyUID
     * @param dem
     * @param ic
     * @return
     * @throws Throwable
     */
    public static StudyProxy createStudyProxy(ServiceExecutor executor, DicomEngineContext ec, String studyUID,
            DataElementMap dem, DicomIngestControls ic) throws Throwable {

        // Look for citeable id first..
        StudyProxy study = createPSSDStudy(executor, studyUID, dem, ic);
        if (study != null) {
            return study;
        }

        // We had a null return. Continue on and see if any
        // any more engines are configured to fall through to
        String nextType = ec.nextEngineTypeAfter(NIGDicomAssetEngine.TYPE_NAME);
        if (nextType != null) {
            if (nextType.equals(DicomAssetEngineConstants.PSS_ENGINE_NAME)) {
                DicomAssetEngine pss = ec.engine(DicomAssetEngineConstants.PSS_ENGINE_NAME);
                if (pss == null) {
                    String msg = "Failed to create dicom engine: " + NIGDicomAssetEngine.TYPE_NAME;
                    Exception ex = new Exception(msg);
                    DicomLog.error(msg);
                    throw ex;
                }
                DicomLog.warn("Fall through to dicom engine: " + DicomAssetEngineConstants.PSS_ENGINE_NAME);
                return pss.createStudyProxy(executor, studyUID, dem);
            }
        }
        DicomLog.warn(NIGDicomAssetEngine.TYPE_NAME
                + " engine can not handle the data. No study proxy is created. Fall through to next available engine. It may throw NullPointerException if no other dicom engine is configured.");
        return null;
    }

    private static StudyProxy createPSSDStudy(ServiceExecutor executor, String studyUID, DataElementMap dem,
            DicomIngestControls ic) throws Throwable {

        // Do we have a citeable ID anywhere (as configured by the engine) in
        // the DICOM meta-data ?
        // We look for something of the form <cid>_<method step> where <method
        // step> is optional
        // If we have a Method step, then we know which step of the Method to
        // create the Study with
        // If we don't have it (usual case), other server heuristics are used:
        // - If the CID specifies a Study Method step is irrelevant
        // - If looks for first modality compliant step or if modality is
        // configured to be
        // ignored looks for the first step in the Method that has no Studies.
        String dateTime = DateUtil.todaysTime();
        DicomLog.info("Creating PSSD Study proxy at " + dateTime);
        CIDAndMethodStep cms = extractCiteableID(executor, dem, ic);

        // Create the metadata to be stored with the study from the given
        // element map
        // This also contains subject-specific information
        StudyMetadata studyMeta = StudyMetadata.createFrom(dem);

        // If we have a CID we are happy. If not, try and find subject by
        // meta-data with
        // the specified matching method (id or patient details)
        if (cms == null || (cms != null && cms.cid() == null)) {
            DicomLog.warn("No citable id extracted from dicom metadata header.");
            DicomLog.info("Searching by subject/patient details...");

            // So we didn't find a CID. Look for the subject by DICOM or
            // domain specific meta-data. Limited use as the same subject
            // in multiple projects will cause failure at this point.
            cms = SubjectHandler.findSubjectByDetail(executor, ic.findSubjectMethod(), studyMeta, null);

            // Although we did not find a pre-existing Subject
            // we can still limit the project scope by looking for
            // projects that are configured
            if (cms == null) {
                DicomLog.info("Searching by project details...");
                cms = findProjectConfiguredForProjectFind(executor, studyMeta);
            }

            // Fall through to next engine, if any
            if (cms == null) {
                DicomLog.info("StudyProxyFactor::createPSSDStudy: cid_step is null");
                DicomLog.warn("No subject is found. Fall through to next available engine.");
                return null;
            }
        } else {
            DicomLog.info("Extracted citeable id " + cms.toString());
        }
        DicomLog.info("StudyProxyFactory::createPSSDStudy:  cid_step = " + cms.toString());

        // Get CID of target object and parent project
        String cid = cms.cid();
        String projectId = nig.mf.pssd.CiteableIdUtil.getProjectId(cid);

        // Make sure the prefix of the CID is for this server because we don't
        // want to make objects for other people's
        // servers (probably other checking for object existence would find this
        // out but it's cleaner
        // to have a high level check). If it's not, all we can do is throw an
        // exception
        try {
            nig.mf.pssd.plugin.util.CiteableIdUtil.checkCIDIsForThisServer(executor, cid, true);
        } catch (Throwable t) {
            // We want to fall through to the next engine. The situation might
            // be that
            // auto-routed data still need to come to this repository
            // e.g. Structured Dose Reports (PET/CT operations)
            sendWrongServerMessage(executor, cid);
            DicomLog.warn(NIGDicomAssetEngine.TYPE_NAME + " engine encountered an error: " + t.getMessage());
            return null;
        }

        // Validate Project access per the dicom control nig.dicom.project.ps,
        // or if provided. Exception if not allowed access
        DICOMProjectSelector ps = new DICOMProjectSelector(ic);
        ps.print();
        String modelUser = ModelUser.domainAndName(executor);
        if (!ps.allowAccessToProject(modelUser, projectId)) {
            // Send a message to the email of the DICOM proxy user if there is
            // one
            String message = sendDeniedProjectMessage(executor, ps, modelUser, projectId);
            DicomLog.error(message);
            throw new Exception(message);
        }

        // The next question.. Does the object specified by the CID exist and
        // if so, what type of PSSD object is it?
        boolean exists = nig.mf.plugin.util.AssetUtil.exists(executor, cid, true);
        try {
            // We have a CID; if it's a non-existing subject and auto-create is
            // requested, we can
            // try to create it
            if (!exists && nig.mf.pssd.CiteableIdUtil.isSubjectId(cid)) {
                DicomLog.info("Subject " + cid + " does not exist");
                if (ic.autoSubjectCreate()) {
                    // If fails, return with null
                    DicomLog.info("Try to auto-create subject " + cid);
                    String t = SubjectHandler.handleSubjectCreation(executor, projectId, cid, ic, studyMeta);
                    if (t == null) {
                        DicomLog.warn("Failed to create subject " + cid);
                        return null;
                    }
                    DicomLog.info("Created subject " + t);
                    if (t.equals(cid)) {
                        exists = true;
                    }
                } else {
                    if (projectId != null) {
                        String subject = "DaRIS " + NIGDicomAssetEngine.TYPE_NAME
                                + " engine: subject with citable identifier " + cid + " does not exist";
                        String msg = NIGDicomAssetEngine.TYPE_NAME + " engine: subject " + cid
                                + " does not exist and the engine is not configured to auto-create subject. The process may fall through to next available dicom engine if it is configured.";
                        DicomLog.warn(msg);
                        MailHandler.sendAdminMessage(executor, projectId, subject, msg);
                    }
                    return null;
                }
            }

            // If the specified object does not exist at this point, we must
            // return null and it falls through
            // to the next engine if any.
            if (!exists) {
                DicomLog.warn("Object with citable id " + cid + " is not found.");
                return null;
            }

            // Get type of existing object
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("id", cid);
            XmlDoc.Element r = executor.execute("om.pssd.object.type", dm.root());
            String pssdType = r.stringValue("type", "unknown");
            if (pssdType.equals("unknown")) {
                DicomLog.warn("Asset with citable id " + cid + " is not a DaRIS PSSD object.");
                return null;
            }

            // We can also auto-create a child Subject if all we have is a
            // Project CID
            if (pssdType.equals("project")) {
                if (ic.autoSubjectCreate()) {

                    // We first try to find the Subject by name etc in this
                    // project.
                    // We can use DICOM mf-dicom-patient or
                    // mf-dicom-patient-encrypted
                    // if it exists on the Subject, or fall back on configured
                    // domain-specific meta-data
                    // given by pssd-dicom-ingest document type
                    cms = SubjectHandler.findSubjectByDetail(executor, ic.findSubjectMethod(), studyMeta, cid);

                    // We didn't find the Subject, so let's make a new one
                    if (cms == null) {
                        DicomLog.info("No subject found in project " + cid + ". Trying to auto-create subject.");
                        cid = SubjectHandler.handleSubjectCreation(executor, projectId, null, ic, studyMeta);
                        if (cid == null) {
                            DicomLog.warn("Failed to auto-create subject");
                            return null;
                        }
                        DicomLog.info("Auto-created subject " + cid);
                        cms = new CIDAndMethodStep(cid, null);
                    } else {
                        cid = cms.cid();
                        DicomLog.info("Found existing subject " + cid);
                    }

                    // We successfully found or auto-created the subject. We
                    // have replaced 'cid' (which was a project object)
                    // by the new subject cid and pretend that the object type
                    // was 'subject' all along (as if the
                    // specified CID had been for a subject). Then all the
                    // existing following code will just work.
                    pssdType = "subject";
                } else {
                    if (projectId != null) {
                        String subject = "DaRIS " + NIGDicomAssetEngine.TYPE_NAME + " engine: citeable id " + cid
                                + " is incompatible with the engine configuration.";
                        String msg = NIGDicomAssetEngine.TYPE_NAME + " engine: citeable id " + cid
                                + " embeded in dicom data represents a project but subject auto-creation is not enabled in the engine configration. The process may fall through to next available dicom engine if it is configured.";
                        DicomLog.warn(msg);
                        MailHandler.sendAdminMessage(executor, projectId, subject, msg);
                    }
                    return null;
                }
            }

            // The namespace is set by the namespace that the parent Project is
            // created in.
            // The DICOM engine cannot choose the namespace for assets.
            String nameSpace = null;

            // Stick on the standard mf-dicom-patient of
            // mf-dicom-patient-encrypted meta-data in private. We
            // do this
            // so we can find SUbjects by name in a predictable place.
            // Now create/update the Study depending on the type of object that
            // the CID represents
            String domainMetaService = ic.subjectMetaService();
            Boolean ignoreModality = ic.ignoreModality();
            Boolean writeDICOMPatient = ic.writeDICOMPatient();
            Boolean dropSR = ic.dropSR();

            if (pssdType.equalsIgnoreCase("subject")) {
                if (writeDICOMPatient) {
                    SubjectHandler.addSubjectDICOMMetaData(executor, ic, cid, studyMeta);
                }

                // Sanity checks
                SubjectHandler.compareSubjectDICOMMetaData(executor, cid, studyMeta, false);
                SubjectHandler.compareSubjectDICOMMetaData(executor, cid, studyMeta, true);
                return new PSSDStudyProxy(nameSpace, studyUID, cid, null, cms.methodStep(), null, studyMeta,
                        domainMetaService, ignoreModality, dropSR);
            }

            if (pssdType.equalsIgnoreCase("ex-method")) {
                String subjectCid = CiteableIdUtil.getParentId(cid);
                if (writeDICOMPatient) {
                    SubjectHandler.addSubjectDICOMMetaData(executor, ic, subjectCid, studyMeta);
                }

                // Snaity checks
                SubjectHandler.compareSubjectDICOMMetaData(executor, subjectCid, studyMeta, false);
                SubjectHandler.compareSubjectDICOMMetaData(executor, subjectCid, studyMeta, true);
                //
                return new PSSDStudyProxy(nameSpace, studyUID, subjectCid, cid, cms.methodStep(), null, studyMeta,
                        domainMetaService, ignoreModality, dropSR);
            }

            if (pssdType.equalsIgnoreCase("study")) {
                String exMethodCid = CiteableIdUtil.getParentId(cid);
                String subjectCid = CiteableIdUtil.getParentId(exMethodCid);
                if (writeDICOMPatient) {
                    SubjectHandler.addSubjectDICOMMetaData(executor, ic, subjectCid, studyMeta);
                }

                // Sanity checks
                SubjectHandler.compareSubjectDICOMMetaData(executor, subjectCid, studyMeta, false);
                SubjectHandler.compareSubjectDICOMMetaData(executor, subjectCid, studyMeta, true);
                //
                return new PSSDStudyProxy(nameSpace, studyUID, subjectCid, exMethodCid, cms.methodStep(), cid,
                        studyMeta, domainMetaService, ignoreModality, dropSR);
            }

            // The CID does not refer to an object of type that we can handle.
            // Send a message
            // to the admin of the project if possible (i.e. if we can find the
            // project cid).
            // Otherwise we return null and it falls through to the next engine
            // if any
            if (projectId != null) {
                String subject = "DaRIS " + NIGDicomAssetEngine.TYPE_NAME + " engine: unhandled citeable id ";
                String msg = NIGDicomAssetEngine.TYPE_NAME + " engine: the citeable id " + cid
                        + " does not represent an DaRIS PSSD object " + NIGDicomAssetEngine.TYPE_NAME
                        + " engine can upload data to. The process may fall through to next available dicom engine if it is configured.";
                DicomLog.warn(msg);
                MailHandler.sendAdminMessage(executor, projectId, subject, msg);
            }
            return null;
        } catch (Throwable t) {
            DicomLog.error(t.getMessage(), t);
            throw t;
        }
    }

    /**
     * If possible, generate a citeable identifier from the
     * 
     * @param dem
     * @return
     * @throws Throwable
     */
    private static CIDAndMethodStep extractCiteableID(ServiceExecutor executor, DataElementMap dem,
            DicomIngestControls ic) throws Throwable {

        // Container for output (CID and Method step)
        CIDAndMethodStep cms = new CIDAndMethodStep();

        // If CID is specified directly by configuration we are done...
        if (ic.citableID() != null) {
            String sid = ic.citableID();
            if (ic.cidPrefix() != null) {
                sid = ic.cidPrefix() + "." + sid; // Stick on the
                // server.namespace prefix
            }
            if (CiteableIdUtil.isCiteableId(sid)) {
                if (CiteableIdUtil.getIdDepth(sid) >= CiteableIdUtil.projectDepth()) {
                    cms.setCID(sid);
                    return cms;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        // Continue and see if can extract from DICOM meta-data
        if (ic.cidElements() == null) {
            return null;
        }

        // Loop over the configured DICOM ingest controls
        // and try to extract a <cid>_<step path> from the DICOM meta-data
        // The <step path> is usually null
        for (int i = 0; i < ic.cidElements().length; i++) {
            cms = extractID(ic.cidElements()[i], dem, ic.ignoreNonDigits(), ic.ignoreBeforeLastDelim(),
                    ic.ignoreAfterLastDelim());

            // The study part is optional:
            //
            // project.subject.ex-method[.study]
            //
            if (cms != null) {
                String sid = cms.cid();
                String step = cms.methodStep(); // Usually null
                //
                if (sid != null) {

                    // Stick on the server.namespace prefix. Do this first
                    // else a bare integer (e.g. CID = subject) won't be valid
                    if (ic.cidPrefix() != null) {
                        sid = ic.cidPrefix() + "." + sid;
                        cms.setCID(sid);
                    }
                    boolean sidOK = CiteableIdUtil.isCiteableId(sid)
                            && CiteableIdUtil.getIdDepth(sid) >= CiteableIdUtil.projectDepth();
                    boolean stepOK = true; // Null is ok
                    if (step != null) {
                        stepOK = isValidStepPath(step);
                    }
                    if (sidOK && stepOK) {
                        return cms;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Identifies whether the given string is a valid step path STep paths look
     * just likes CIDs so we can use the same code
     * 
     * @param id
     * @return
     */
    public static boolean isValidStepPath(String step) {
        return CiteableIdUtil.isCiteableId(step);
    }

    /**
     * Find the CID for any projects configured by specified DICOM element
     */
    static private CIDAndMethodStep findProjectConfiguredForProjectFind(ServiceExecutor executor, StudyMetadata sm)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        String query = "model='om.pssd.project' and xpath(daris:pssd-dicom-ingest/project/find)='true'";
        dm.add("where", query);
        dm.add("size", "infinity");
        dm.add("pdist", 0); // Local server only
        dm.add("action", "get-meta");
        XmlDoc.Element r = executor.execute("asset.query", dm.root());
        if (r == null)
            return null;
        //
        String matchedCID = null;
        Collection<XmlDoc.Element> projects = r.elements("asset");
        if (projects != null) {
            for (XmlDoc.Element project : projects) {
                String cid = project.value("cid");

                // You can specify multiple dicom element values
                Collection<XmlDoc.Element> dicoms = project.elements("meta/daris:pssd-dicom-ingest/project/dicom");
                if (dicoms != null) {
                    for (XmlDoc.Element dicom : dicoms) {
                        String dicomType = dicom.value("@type");
                        String dicomValue = dicom.value();
                        // Check specified DICOM element for a matching value
                        String studyValue = null;
                        if (dicomType.equals("protocol_name")) {
                            studyValue = sm.protocolName();
                        } else if (dicomType.equals("study_description")) {
                            studyValue = sm.description();
                        } else {
                            throw new Exception("Unhandled value for daris:pssd-ingest/project/dicom attribute 'type':"
                                    + dicomType);
                        }

                        // Return the match
                        if (dicomValue != null && studyValue != null && dicomValue.equalsIgnoreCase(studyValue)) {
                            if (matchedCID == null) {
                                matchedCID = cid;
                            } else {
                                throw new Exception(
                                        "Multiple Project matches specified by daris:pssd-dicom-ingest/project/dicom were found.");
                            }
                        }
                    }
                }
            }
        }
        if (matchedCID != null) {
            DicomLog.info("         Matched Project " + matchedCID + " with DICOM data");
            return new CIDAndMethodStep(matchedCID, null);
        } else {
            return null;
        }
    }

    /**
     * This function has a kludge in it to handle the case when patient names
     * are incorrectly encoded in the DICOM header. The DICOM patient name field
     * should be of the form "Last^Middle2^Middle1^First". However, it is not
     * uncommon (e.g. RCH DICOM client) to find first and middle names separated
     * only by spaces. Thus "Last^First Middle". Here, in the PSSD engine we are
     * really only concerned with CIDs (not the actual names) which we are
     * expecting to find in the as configured DICOM tags (e.g.first name), so
     * there is not really a lot of code to write. The PSS engine needs more.
     * 
     * @param ele
     * @param dem
     * @param ignoreLeadingNonDigits
     * @param ignoreBeforeLastDelim
     * @return
     * @throws Throwable
     */
    private static CIDAndMethodStep extractID(int ele, DataElementMap dem, boolean ignoreLeadingNonDigits,
            String ignoreBeforeLastDelim, String ignoreAfterLastDelim) throws Throwable {
        switch (ele) {
        case DicomIngestControls.ID_NONE:
            return null;

        case DicomIngestControls.ID_BY_PATIENT_FULL_NAME: {
            DicomPersonName pn = (DicomPersonName) dem.valueOf(DicomElements.PATIENT_NAME);
            if (pn == null)
                return null;

            String fullName = pn.fullName();
            DicomLog.info("StudyProxyFactory::extractID:Full Name = " + fullName);
            // Hopefully they don't set both of these...
            if (ignoreAfterLastDelim != null)
                fullName = nig.mf.pssd.CiteableIdUtil.removeAfterLastDelim(fullName, ignoreAfterLastDelim);
            if (ignoreBeforeLastDelim != null)
                fullName = nig.mf.pssd.CiteableIdUtil.removeBeforeLastDelim(fullName, ignoreBeforeLastDelim);
            //
            if (ignoreLeadingNonDigits)
                fullName = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(fullName);
            return extractCIDAndStep(fullName);
            // return fullName; // Full names should be ok
        }

        case DicomIngestControls.ID_BY_PATIENT_LAST_NAME: {
            DicomPersonName pn = (DicomPersonName) dem.valueOf(DicomElements.PATIENT_NAME);
            if (pn == null)
                return null;
            String lastName = pn.last(); // Last names should be ok
            DicomLog.info("StudyProxyFactory::extractID:Last Name = " + lastName);
            // Hopefully they don't set both of these...
            if (ignoreAfterLastDelim != null)
                lastName = nig.mf.pssd.CiteableIdUtil.removeAfterLastDelim(lastName, ignoreAfterLastDelim);
            if (ignoreBeforeLastDelim != null)
                lastName = nig.mf.pssd.CiteableIdUtil.removeBeforeLastDelim(lastName, ignoreBeforeLastDelim);
            //
            if (ignoreLeadingNonDigits)
                lastName = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(lastName);
            return extractCIDAndStep(lastName);
            // return lastName;
        }

        case DicomIngestControls.ID_BY_PATIENT_FIRST_NAME: {
            DicomPersonName pn = (DicomPersonName) dem.valueOf(DicomElements.PATIENT_NAME);
            if (pn == null)
                return null;
            String firstName = pn.first(); // May be a combination of first and
            // middle
            DicomLog.info("StudyProxyFactory::extractID:First Name = " + firstName);
            // Hopefully they don't set both of these...
            if (ignoreAfterLastDelim != null)
                firstName = nig.mf.pssd.CiteableIdUtil.removeAfterLastDelim(firstName, ignoreAfterLastDelim);
            if (ignoreBeforeLastDelim != null)
                firstName = nig.mf.pssd.CiteableIdUtil.removeBeforeLastDelim(firstName, ignoreBeforeLastDelim);
            //
            if (ignoreLeadingNonDigits)
                firstName = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(firstName);
            if (firstName == null)
                return null;

            //
            String[] s = firstName.split("_");
            StringTokenizer st = new StringTokenizer(s[0]);
            String name = null;
            if (st.hasMoreTokens())
                name = st.nextToken();
            //
            if (s.length == 1) {
                return extractCIDAndStep(name);
                // return name;
            } else if (s.length == 2) {
                CIDAndMethodStep cms = new CIDAndMethodStep(name, s[1]);
                return cms;
            }
        }

        case DicomIngestControls.ID_BY_PATIENT_ID: {
            String id = dem.stringValue(DicomElements.PATIENT_ID);
            if (id == null)
                return null;

            DicomLog.info("StudyProxyFactory::extractID:Patient ID = " + id);
            // Hopefully they don't set both of these...
            if (ignoreAfterLastDelim != null)
                id = nig.mf.pssd.CiteableIdUtil.removeAfterLastDelim(id, ignoreAfterLastDelim);
            if (ignoreBeforeLastDelim != null)
                id = nig.mf.pssd.CiteableIdUtil.removeBeforeLastDelim(id, ignoreBeforeLastDelim);
            //
            if (ignoreLeadingNonDigits)
                id = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(id);
            return extractCIDAndStep(id);
        }

        case DicomIngestControls.ID_BY_STUDY_ID: {
            String id = dem.stringValue(DicomElements.STUDY_ID);
            if (id == null)
                return null;

            DicomLog.info("StudyProxyFactory::extractID:Study ID = " + id);
            // Hopefully they don't set both of these...
            if (ignoreAfterLastDelim != null)
                id = nig.mf.pssd.CiteableIdUtil.removeAfterLastDelim(id, ignoreAfterLastDelim);
            if (ignoreBeforeLastDelim != null)
                id = nig.mf.pssd.CiteableIdUtil.removeBeforeLastDelim(id, ignoreBeforeLastDelim);
            //
            if (ignoreLeadingNonDigits)
                id = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(id);
            return extractCIDAndStep(id);
        }

        case DicomIngestControls.ID_BY_REFERRING_PHYSICIAN_NAME: {
            String id = dem.stringValue(DicomElements.REFERRING_PHYSICIANS_NAME);
            if (id == null)
                return null;

            DicomLog.info("StudyProxyFactory::extractID:Referring Physician = " + id);
            // Hopefully they don't set both of these...
            if (ignoreAfterLastDelim != null)
                id = nig.mf.pssd.CiteableIdUtil.removeAfterLastDelim(id, ignoreAfterLastDelim);
            if (ignoreBeforeLastDelim != null)
                id = nig.mf.pssd.CiteableIdUtil.removeBeforeLastDelim(id, ignoreBeforeLastDelim);
            if (ignoreLeadingNonDigits)
                id = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(id);
            return extractCIDAndStep(id);
        }

        case DicomIngestControls.ID_BY_REFERRING_PHYSICIAN_PHONE: {
            String id = dem.stringValue(DicomElements.REFERRING_PHYSICIANS_PHONE);
            if (id == null)
                return null;

            DicomLog.info("StudyProxyFactory::extractID:Referring Physician = " + id);
            // Hopefully they don't set both of these...
            if (ignoreAfterLastDelim != null)
                id = nig.mf.pssd.CiteableIdUtil.removeAfterLastDelim(id, ignoreAfterLastDelim);
            if (ignoreBeforeLastDelim != null)
                id = nig.mf.pssd.CiteableIdUtil.removeBeforeLastDelim(id, ignoreBeforeLastDelim);
            if (ignoreLeadingNonDigits)
                id = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(id);
            return extractCIDAndStep(id);
        }

        case DicomIngestControls.ID_BY_PERFORMING_PHYSICIAN: {
            String id = dem.stringValue(DicomElements.PERFORMING_PHYSICIANS_NAME);
            if (id == null)
                return null;

            DicomLog.info("StudyProxyFactory::extractID:Performing Physician = " + id);
            // Hopefully they don't set both of these...
            if (ignoreAfterLastDelim != null)
                id = nig.mf.pssd.CiteableIdUtil.removeAfterLastDelim(id, ignoreAfterLastDelim);
            if (ignoreBeforeLastDelim != null)
                id = nig.mf.pssd.CiteableIdUtil.removeBeforeLastDelim(id, ignoreBeforeLastDelim);
            if (ignoreLeadingNonDigits)
                id = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(id);
            return extractCIDAndStep(id);
        }

        case DicomIngestControls.ID_BY_REQUESTING_PHYSICIAN: {
            String id = dem.stringValue(DicomElements.REQUESTING_PHYSICIAN);
            if (id == null) {
                return null;
            }
            DicomLog.info("StudyProxyFactory::extractID:Requesting Physician = " + id);
            // Hopefully they don't set both of these...
            if (ignoreAfterLastDelim != null)
                id = nig.mf.pssd.CiteableIdUtil.removeAfterLastDelim(id, ignoreAfterLastDelim);
            if (ignoreBeforeLastDelim != null)
                id = nig.mf.pssd.CiteableIdUtil.removeBeforeLastDelim(id, ignoreBeforeLastDelim);
            if (ignoreLeadingNonDigits)
                id = nig.mf.pssd.CiteableIdUtil.removeLeadingNonDigits(id);
            return extractCIDAndStep(id);
        }

        default:
            return null;
        }
    }

    private static CIDAndMethodStep extractCIDAndStep(String str) throws Throwable {
        if (str == null)
            return null;
        CIDAndMethodStep cms = new CIDAndMethodStep();
        String[] s = str.split("_");
        if (s.length == 1) {
            cms.setCID(str);
        } else if (s.length == 2) {
            cms.setCID(s[0]);
            cms.setMethodStep(s[1]);
        } else {
            // Whatever we got, it was not of the form <a>_<b> so it's not
            // <cid>_<step>
            return null;
            // throw new Exception ("Citeable ID/Step string should be of the
            // form <cid>_<step> or <cid>; found " + str);
        }
        return cms;
    }

    private static void sendWrongServerMessage(ServiceExecutor executor, String cid) throws Throwable {
        String cidRoot = nig.mf.pssd.CiteableIdUtil.getRootParentId(cid);
        XmlDoc.Element r = executor.execute("server.identity");
        String name = r.stringValue("server/name", "Unknown");
        String org = r.stringValue("server/organization", "Unknown");
        String subject = "Data sent to wrong DaRIS server";
        String message = "Dear sender of DICOM data \n You appear to have sent data with CID " + cid
                + "\n to the wrong DaRIS server (which has  CID root " + cidRoot + ")\n"
                + "The name of the server you sent to is " + name + "\n"
                + "The organization of the server you sent to is " + org + "\n Regards DaRIS...";
        sendMessageToProxyUser(executor, subject, message);
    }

    private static String sendDeniedProjectMessage(ServiceExecutor executor, DICOMProjectSelector ps, String modelUser,
            String cid) throws Throwable {
        String cidRoot = nig.mf.pssd.CiteableIdUtil.getRootParentId(cid);
        XmlDoc.Element r = executor.execute("server.identity");
        String name = r.stringValue("server/name", "Unknown");
        String org = r.stringValue("server/organization", "Unknown");
        String subject = "DICOM proxy user (" + modelUser + ") denied access to project " + cid;
        String message = "Dear sender of DICOM data \n You have sent data with CID " + cid
                + "\n to the DaRIS server with CID root " + cidRoot + ", name " + name + ", & organization " + org
                + " \n " + "via the DICOM proxy user :" + modelUser + "\n "
                + "However, this DICOM server is configured with a project selector which prevents \n "
                + "this specific DICOM user from uploading to the specified project \n "
                + "The project selector is confgured for this server as : \n" + ps.toString() + "\n " + "regards DaRIS";
        sendMessageToProxyUser(executor, subject, message);
        return message;
    }

    private static void sendMessageToProxyUser(ServiceExecutor executor, String subject, String message)
            throws Throwable {
        String email = ModelUser.email(executor);
        if (email != null) {
            MailHandler.sendMessage(executor, email, subject, message, false);
        }
    }

}
