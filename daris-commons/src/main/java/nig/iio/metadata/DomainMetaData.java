package nig.iio.metadata;

import arc.xml.*;
import java.util.Collection;
import nig.mf.Executor;


/**
 * Base class for the framework to supply domain-specific meta-data for
 * objects (Projects, Subjects, R-SUbjects and Studies) to the DICOM server 
 * and Bruker client (which should remain domain agnostic). 
 * 
 * The framework is driven by the data model so that only the meta-data that is 
 * correctly relevant to the specified object type is considered.
 * 
 * The asymmetric plugin and client execution environments are supported via the Executor wrapper class.
 * 
 * See NIGDomainMetaData in the nig-pssd package and MNCDomainMetaData in the mnc-pssd package
 * as  examples of derived classes for the Neuroimaging domain
 * 
 * Note that this framework is different to (and is executed subsequently to) the framework 
 * that is used when Subjects are auto-created by the DICOM server (this locates constant 
 * Method pre-specified meta-data on that Subject.   
 * 
 * The current framework takes meta-data specified by the data themselves (DICOM or
 * Bruker currently) and then supplies a mapping of this meta-data to specific
 * document types.
 * 
 * 
 * In this way, DICOM and Bruker meta-data can be located on specific domain-specific document types.
 * It would be wrong for these clients to know about these document types directly.
 * 
 * @author nebk
 *
 */
public class DomainMetaData {

	// Object types as per the fundamental PSSD classes (e.g. Project.java)
	public static final String PROJECT_TYPE = "project";
	public static final String SUBJECT_TYPE = "subject";
	public static final String RSUBJECT_TYPE = "r-subject";
	public static final String STUDY_TYPE = "study";

	// Constructor
	public DomainMetaData () 
	{
		//
	}


	/**
	 * Add meta-data to the object by filling from the supplied meta-data and a mapping to
	 * known document types
	 * 
	 * @param executor
	 * @param cid
	 * @param meta The meta-data to be parsed and located on objects. The structure is up to you, 
	 *  but it has to be consistent with your derived class (e.g. NIGDomainMetaData
	 *    and the service (e.g. nig.pssd.subject.meta.set) that ultimately calls this class
	 *    and whatever way in which you populate the interface to this service (e.g. the
	 *    DICOM server)
	 * @throws Throwable
	 */
	public void addObjectMetaData (Executor executor, String cid, XmlDoc.Element meta) throws Throwable {


		// Get the current (existing in the :meta element)  and possible (from the data model,
		// templates and existing) meta-data for the Object
		XmlDoc.Element current = describe(executor, cid, false);
		XmlDoc.Element possible = describe(executor, cid, true);    

		// Find object type
		String objectType = current.value("object/@type");

		// Update the object with the new mapped meta-data
		if (objectType.equals(PROJECT_TYPE)) {
			// Projects have all meta-data under the "meta" element
			updateObject (executor, cid, meta, "meta", current, possible, objectType);
		} else if (objectType.equals(SUBJECT_TYPE)) {
			// 	Subjects may hold meta-data under the "public" and "private" elements 
			updateObject (executor, cid, meta, "public", current, possible, objectType);
			updateObject (executor, cid, meta, "private", current, possible, objectType);

			// If the SUbject refers to an R-SUbject, look there as well
			String rsCID = current.value("object/"+RSUBJECT_TYPE);
			if (rsCID != null) {

				// Get the current and possible R-SUbject meta
				current  = describe(executor, rsCID, false);
				possible = describe(executor, rsCID, true);

				// Update "public", "private" and "identity" elements of R-Subject
				updateObject (executor, rsCID, meta, "public", current, possible, objectType);
				updateObject (executor, rsCID, meta, "private", current, possible, objectType);
				updateObject (executor, rsCID, meta, "identity", current, possible, objectType);	
			}
		} else if (objectType.equals(STUDY_TYPE)) {
			// Studies have all meta-data under the "meta" element
			updateObject (executor, cid, meta, "meta", current, possible, objectType);
		}
	}

	/**
	 * Remove all existing instances of mapped meta-data elements in all documents associated with
	 * the object
	 * 
	 * @param executor
	 * @param cid The object to consider
	 * @param metaType SHould hold children elements "dicom" and/or "bruker" (but their children are irrelevant). This
	 * gives the context for which document types we are interested in.
	 * @throws Throwable
	 */
	public void removeObjectMetaData (Executor executor, String cid, XmlDoc.Element metaType) throws Throwable {

		// Get the current (existing)  meta-data for the object
		XmlDoc.Element current = describeNative(executor, cid, false);

		// Find object type
		String objectType = current.value("daris:pssd-object/type");

		// The removal work has to be done by the domain-specific implementation
		// as only it knows about the document types that are relevant. So this function
		// has to be over-ridden
		removeElements (executor, metaType, cid, objectType, current);

		// Make a consolidation pass to remove documents that are now empty (because all
		// of their elements were removed
		removeEmptyDocuments (executor, cid);
	}



	/**
	 * Describe an object, optionally for editing
	 * 
	 * @param exec
	 * @param id
	 * @param forEdit
	 * @return
	 * @throws Throwable
	 */
	private XmlDoc.Element describe (Executor exec, String id, boolean forEdit) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("foredit", forEdit);

		// WHen foredit is true, it returns meta-data from the categories:
		//  - existing
		//  - specified by the Method
		//  - specified by templates
		//  - specified by registation with the data model
		return exec.execute("om.pssd.object.describe", dm);

	}

	/**
	 * Returns meta-data from object with asset.get
	 * 
	 * @param exec
	 * @param id
	 * @param forEdit
	 * @return The contents of the 'asset/meta' element
	 * 
	 * @throws Throwable
	 */
	private XmlDoc.Element describeNative (Executor exec, String id, boolean forEdit) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);

		// WHen foredit is true, it returns meta-data from the categories:
		//  - existing
		//  - specified by the Method
		//  - specified by templates
		//  - specified by registation with the data model
		XmlDoc.Element r = exec.execute("asset.get", dm);
		if (r==null) return null;
		return r.element("asset/meta");

	}

	/**
	 * Extract current and possible meta-data for the given object
	 * 
	 * @param executor
	 * @param id  Object CID
	 * @param meta Meta data to set
	 * @param privacyType The element to find the meta-data in the object description
	 * 					   For SUbjects and RSubjects should be one of "public", "private", "identity" 
	 *                     For other object types, should be "meta"
	 * @param rCurrent
	 * @param rPossible
	 * @throws Throwable
	 */
	private void updateObject (Executor executor, String id, XmlDoc.Element meta, String privacyType, 
			XmlDoc.Element rCurrent, XmlDoc.Element rPossible, String objectType) throws Throwable {

		if (rPossible==null) return;
		{
			// Get the possible  document definitions
			Collection<XmlDoc.Element> possible = rPossible.elements("object/"+privacyType+"/metadata");

			// Get the current (existing) attached documents
			XmlDoc.Element current = null;
			if (rCurrent!=null) current = rCurrent.element("object/"+privacyType); 

			// Iterate over the possible document definitions for this object
			if (possible!=null) {
				for (XmlDoc.Element el : possible) {
					// Get the document type
					String docType = el.value("@type");

					// Map the meta-data supplied in the meta container  to the document types/elements that are possible.
					// Then update the documents with the mapped meta-data.
					// Each document may need to handle the meta-data in a different way ("add", "merge" etc) so 
					// each document is wrapped separately and also for clarity in the derived class.
					if (objectType.equals(PROJECT_TYPE)) {
						addTranslatedProjectDocument (executor, id, meta, privacyType, docType, current);
					} else if (objectType.equals(STUDY_TYPE)) {
						String ns = el.value("@ns");
						addTranslatedStudyDocument (executor, id, meta, privacyType, docType, ns, current);
					} else if (objectType.equals(SUBJECT_TYPE) || objectType.equals(RSUBJECT_TYPE)) { 
						addTranslatedSubjectDocument (executor, id, meta, privacyType, docType, current);
					}
				}
			}
		}
		
		

		// There may also be Study specific meta-data lurking in the object/method/meta element
		// as well as in object/meta. This is meta-data that the Method says should be located
		// on a Study. When it's actually located on the Study, it is in the usual MF "meta" element
		// but just with a Method-specific namespace.  So we can use the same structure to locate it as well
		{
			// Get the possible  document definitions
			Collection<XmlDoc.Element> possible = rPossible.elements("object/method/"+privacyType+"/metadata");

			// Get the current (existing) attached documents
			XmlDoc.Element current = null;
			if (rCurrent!=null) current = rCurrent.element("object/"+privacyType); 

			// Update the meta-data on the possible documents with meta-data specified in the container
			// Iterate over the possible document definitions for this subject
			if (possible!=null) {
				for (XmlDoc.Element el : possible) {
					// Get the document type and document namespace
					String docType = el.value("@type");
					String ns = el.value("@ns");

					// Update
					if (objectType.equals(STUDY_TYPE)) {
						addTranslatedStudyDocument (executor, id, meta, privacyType, docType, ns, current);
					}
				}
			}
		}
	}


	/**
	 * Update the meta-data on the  object for the given Document Type. This function must
	 * do the actual update with the appropriate service (e.g. om.pssd.project.update)
	 * 
	 * @param id The citeable ID of the object to update
	 * @param meta  The Metadata 
	 * @param docType the document type to write meta-data for.  
	 * @param currentMeta  The meta-data that are attached to the asset (:foredit false)
	 * @throws Throwable
	 */
	protected void addTranslatedProjectDocument (Executor executor, String id, XmlDoc.Element meta, 
			String privacyType, String docType, XmlDoc.Element currentMeta) throws Throwable {

		throw new Exception ("Function must be implemented by derived class");

	}

	/**
	 * Update the meta-data on the  object for the given Document Type. This function must
	 * do the actual update with the appropriate service (e.g. om.pssd.subject.update)
	 * 
	 * @param id The citeable ID of the object to update
	 * @param meta The Metadata 
	 * @param privacyType "public", "private", "identity" indicating which element of the meta-data structure
	 *          we are working with. This is needed when re-setting meta-data
	 * @param docType the document type to write meta-data for.  The values must be mapped from the Study MetaData
	 * @param currentMeta  The meta-data that are attached to the asset (:foredit false)
	 * @throws Throwable
	 */
	protected void addTranslatedSubjectDocument (Executor executor, String id, XmlDoc.Element meta, String privacyType, 
			String docType, XmlDoc.Element currentMeta) throws Throwable {

		throw new Exception ("Function must be implemented by derived class");

	}

	/**
	 * Update the meta-data on the  object for the given Document Type. This function must
	 * do the actual update with the appropriate service (e.g. om.pssd.study.update)
	 * 
	 * @param id The citeable ID of the object to update
	 * @param meta The Metadata 
	 * @param docType the document type to write meta-data for.  
	 * @param ns An addition namespace to be set on the meta-data being updated.  Its purpose is for
	 *       Method namespaces like cid_step that must be set on the Method specified Study meta-data
	 * @param currentMeta  The meta-data that are attached to the asset (:foredit false)
	 * @throws Throwable
	 */
	protected void addTranslatedStudyDocument (Executor executor, String id, XmlDoc.Element meta, 
			String privacyType, String docType, String ns, XmlDoc.Element currentMeta) throws Throwable {

		throw new Exception ("Function must be implemented by derived class");

	}

	/**
	 * 
	 * @param executor
	 * @param metaType SHould hold children elements "dicom" and/or "bruker" (but their children are irrelevant). This
	 * gives the context for which document types we are interested in.
	 * @param id
	 * @param objectType "project", "subject", "study"
	 * @param currentMeta The contents of xpath("asset/meta") after retrieval by asset.get
	 * @throws Throwable
	 */
	protected void removeElements (Executor executor, XmlDoc.Element metaType, String id, String objectType, XmlDoc.Element currentMeta) throws Throwable {

		throw new Exception ("Function must be implemented by derived class");

	}


	private void removeEmptyDocuments (Executor executor, String id) throws Throwable {		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		XmlDoc.Element r = executor.execute("asset.get", dm);
		if (r==null) return;

		// Iterate over the documents 
		XmlDoc.Element meta = r.element("asset/meta");
		if (meta==null) return;
		//
		dm = new XmlDocMaker("args");
		dm.add("cid", id);
		Collection<XmlDoc.Element> docs = meta.elements();
		if (docs==null) return;
		//
		boolean some = false;
		for (XmlDoc.Element doc : docs) {
			if (!doc.hasSubElements()) {
				dm.push("meta", new String[]{"action", "remove"});
				dm.add(doc);
				dm.pop();
				some = true;
			}
		}

		// Do it
		if (some) executor.execute("asset.set", dm);
	}
	
	
	// For use by derived classes
	protected void updateProject (Executor executor, XmlDocMaker dm) throws Throwable {
		executor.execute("om.pssd.project.update", dm);
	}


	protected void updateSubject (Executor executor, XmlDocMaker dm) throws Throwable {
		executor.execute("om.pssd.subject.update", dm);
	}

	protected void updateStudy (Executor executor, XmlDocMaker dm) throws Throwable {
		executor.execute("om.pssd.study.update", dm);
	}
	
	protected boolean checkDocTypeExists (Executor executor, String docType) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", docType);
		XmlDoc.Element r = executor.execute("asset.doc.type.exists", dm);
		return r.booleanValue("exists");
	}
	
	/**
	 * Prepare the XmlDocMaker with the meta-data ready to re-set on the object so that the
	 * mapped elements of interest have been removed from their documents.
	 * 
	 * @param dm
	 * @param currentMeta The meta-data currently attached to the object (in native asset.get form)
	 * @param docType The document type we are going to remove elements for
	 * @param elementNames A list of children element names to remove
	 * @return
	 * @throws Throwable
	 */
	protected boolean prepareRemovedMetaData (XmlDocMaker dm, XmlDoc.Element currentMeta, String docType, String[] elementNames) throws Throwable {

		for (int i=0; i<elementNames.length; i++) {
			// Remove the named element and return the modified document
			Collection<XmlDoc.Element> docs = XMLUtil.removeElements(currentMeta, docType, elementNames);
			if (docs.size()>0) {
				dm.push("meta", new String[] {"action", "replace"});
				for (XmlDoc.Element doc : docs) {

					// The modified document may have no children which is ok because
					// the new document will just be empty. Empty documemts are removed
					// subsequently by the base class (DomainMetaData)
					dm.add(doc);
				}
				dm.pop();
				return true;
			}
		}
		return false;
	}

}
