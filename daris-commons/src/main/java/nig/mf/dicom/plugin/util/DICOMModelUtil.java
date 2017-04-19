package nig.mf.dicom.plugin.util;

import java.util.Collection;
import java.util.Vector;

import nig.mf.plugin.util.AssetUtil;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * Some helper functions for manipulating assets in the enhanced DICOM data model
 *   Project 
 *    has  (inverse had-by)
 *   Patient
 *    has   (inverse had-by)
 *   Study
 *  contains (inverse container)
 *  Series
 * 
 */
public class DICOMModelUtil {

	public enum FormatType{ DICOM, SIEMENS_RAW, BOTH};


	/**
	 * Destroy patient and all related studies and contained series
	 * 
	 * @param executor
	 * @param patientAsset
	 * @param listOnly don't destroy just list
	 * @param w
	 * @throws Throwable
	 */
	public static void destroyPatient (ServiceExecutor executor, String patientAssetID, Boolean listOnly, XmlWriter w) throws Throwable {

		DICOMPatient patient = DICOMModelUtil.getPatientDetails(executor, patientAssetID);					
		w.push("patient",  new String[]{"name", patient.getFullName(), "id", patientAssetID});


		// Destroy Study and contained Series
		Collection<String> ids = findStudies(executor, patientAssetID, FormatType.BOTH);
		if (ids!=null) {
			for (String id : ids) {
				w.add("study", id);
				if (!listOnly) destroyStudy (executor, id);
			}
		}
		w.pop();
		if (!listOnly) {
			// Destroy patient
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", patientAssetID);
			executor.execute("asset.destroy", dm.root());
		}
	}


	/**
	 * Destroy study and all contained series
	 * 
	 * @param executor
	 * @param patientAsset
	 * @param w
	 * @throws Throwable
	 * 
	 */
	public static void destroyStudy (ServiceExecutor executor, String studyAsset) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", studyAsset);
		dm.add("members", true);
		executor.execute("asset.destroy", dm.root());
	}



	/**
	 * Find a list of Series assets. The inputs give a query where clause, and one of
	 * a list of parent patient, study, or series asset IDs to start from
	 * 
	 * @param executor
	 * @param where
	 * @param patientAssets
	 * @param studyAssets
	 * @param seriesAssets
	 * @return
	 * @throws Throwable
	 */
	public static Collection<String> findSeries (ServiceExecutor executor, String where, Collection<String> patientAssets, 
			Collection<String> studyAssets, Collection<String> seriesAssets) throws Throwable {
		Collection<String> assets = new Vector<String>();
		//
		XmlDocMaker dm = new XmlDocMaker("args");
		if (where!=null) {
			if (where!=null) dm.add("where", where);
			dm.add("size", "infinity");
			dm.add("pdist", 0);           // local query
			XmlDoc.Element r = executor.execute("asset.query", dm.root());
			if (r!=null) assets.addAll(r.values("id"));
		} else if (patientAssets!=null) {
			// Iterate through patient assets
			for (String patientAsset : patientAssets) {

				// Find related Studies
				Collection<String> studies = DICOMModelUtil.findStudies (executor, patientAsset, DICOMModelUtil.FormatType.DICOM);
				if (studies!=null) {

					// Iterate through studies and find series
					for (String study : studies) {
						Collection<String> series = DICOMModelUtil.findSeries (executor, study, DICOMModelUtil.FormatType.DICOM);
						if (series!=null) assets.addAll(series);
					}
				}
			}
		} else if (studyAssets!=null) {
			for (String studyAsset : studyAssets) {
				Collection<String> series = DICOMModelUtil.findSeries (executor, studyAsset, DICOMModelUtil.FormatType.DICOM);
				if (series!=null) assets.addAll(series);
			}
		} else if (seriesAssets!=null) {
			assets.addAll(seriesAssets);
		}
		return assets;
	}


	/**
	 * See if  a project asset for this project ID string already exists.  If there are multiples, it's an error
	 * and an exception is triggered.  The asset is found via mf-dicom-project.  
	 * 
	 * @param executor
	 * @param projectID
	 * @throws Throwable
	 */
	public static String findProject (ServiceExecutor executor, String projectID) throws Throwable {

		// Find the project asset.  Assets of type {dicom,siemens-raw-petct}/study  may also
		// hold mf-dicom-project, so make sure it's not a study when querying.
		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "type!=dicom/study and type!=siemens-raw-petct/study" + 
				"and xpath(mf-dicom-project/id)='" + projectID + "'";
		//
		dm.add("size", "infinity");
		dm.add("pdist", 0);           // local query
		dm.add("where", query);
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r==null) return null;
		Collection<String> projects = r.values("id");
		if (projects==null) return null;
		if (projects.size()>1) {
			throw new Exception ("Found multiple projects for this id '" + projectID + "'");
		}
		return r.value("id");
	}


	/**
	 * Find all patients had by this project asset
	 * 
	 * @param executor
	 * @param patientAsset  
	 * @param formatType
	 * @throws Throwable
	 */
	public static Collection<String> findPatients (ServiceExecutor executor, String projectAsset) throws Throwable {

		// Find related Patients
		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "(mf-dicom-patient has value or mf-dicom-patient-encrypted has value) and (related to{had-by} (id=" + projectAsset + "))";
		//
		dm.add("size", "infinity");
		dm.add("pdist", 0);           // local query
		dm.add("where", query);
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r==null) return null;
		return r.values("id");
	}


	/**
	 * Find all Studies (DICOM and/or  Siemens raw) had by this patient asset
	 * 
	 * @param executor
	 * @param patientAsset  
	 * @param formatType
	 * @throws Throwable
	 */
	public static Collection<String> findStudies (ServiceExecutor executor, String patientAsset, FormatType formatType) throws Throwable {

		// Find related Studies
		XmlDocMaker dm = new XmlDocMaker("args");
		String query = null;
		if (formatType==FormatType.DICOM) {
			query = "mf-dicom-study has value and (related to{had-by} (id=" + patientAsset + "))";
		} else if (formatType==FormatType.SIEMENS_RAW) {
			query = "daris:siemens-raw-petct-study has value and (related to{had-by} (id=" + patientAsset + "))";
		} else if (formatType==FormatType.BOTH) {
			query = "(mf-dicom-study has value or daris:siemens-raw-petct-study has value) and (related to{had-by} (id=" + patientAsset + "))";
		}
		if (query==null) return null;
		//
		dm.add("size", "infinity");
		dm.add("pdist", 0);           // local query
		dm.add("where", query);
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r==null) return null;
		return r.values("id");
	}


	/**
	 * Find all Series (DICOM and/or Siemens raw) contained by this Study asset
	 * 
	 * @param executor
	 * @param studyAsset
	 * @param formatType
	 * @return
	 * @throws Throwable
	 */
	public static Collection<String> findSeries (ServiceExecutor executor,  String studyAsset, FormatType formatType) throws Throwable {
		String query = null;
		if (formatType==FormatType.DICOM) {
			query = "mf-dicom-series has value and (related to{'container'} (id=" + studyAsset + "))";
		} else if (formatType==FormatType.SIEMENS_RAW) {
			query = "daris:siemens-raw-petct-series has value and (related to{'container'} (id=" + studyAsset + "))";
		} else if (formatType==FormatType.BOTH) {
			query = "(mf-dicom-series has value or daris:siemens-raw-petct-series has value) and (related to{'container'} (id=" + studyAsset + "))";
		}	
		if (query==null) return null;
		//
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("size", "infinity");
		dm.add("pdist", 0);           // local query
		dm.add("where", query);
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r==null) return null;
		return r.values("id");
	}



	/**
	 *  FInd orphaned projects (that have no primary relationship 'has patient') 
	 *  
	 * @param executor
	 * @param where
	 * @return
	 * @throws Throwable
	 */
	public static Collection<String> findProjectsWithNoPatient (ServiceExecutor executor, String where) throws Throwable {
		String query = "mf-dicom-project has value and (not (related to{'has'} any))";		
		if (where!=null) query = where + " and (" + query + ")";
		return query(executor, query);
	}


	/**
	 * Find orphaned Patients (DICOM and/or Siemens raw) that have no secondary relationship had-by project
	 * 
	 * @param executor
	 * @param where
	 * @param formatType
	 * @return
	 * @throws Throwable
	 */
	public static Collection<String> findPatientsWithNoProject (ServiceExecutor executor, String where) throws Throwable {		
		String query = "type=dicom/patient and (not (related to{'had-by'} any))";
		if (where!=null) query = where + " and (" + query + ")";
		return query(executor, query);
	}


	/**
	 *  FInd orphaned patients (that have no primary relationship 'has study')  
	 *  
	 * @param executor
	 * @param where
	 * @return
	 * @throws Throwable
	 */
	public static Collection<String> findPatientsWithNoStudy (ServiceExecutor executor, String where) throws Throwable {
		String query = "type=dicom/patient and (not (related to{'has'} any))";		
		if (where!=null) query = where + " and (" + query + ")";
		return query(executor, query);
	}


	/**
	 * Find orphaned Studies (DICOM and/or Siemens raw) that have no project-related meta-data (mf-dicom-project)
	 * Projects have Patients Have Studies contain Series.  But Studies also have project meta-data so we know
	 * which project a Study was acquired for.
	 * 
	 * @param executor
	 * @param where additional selection
	 * @param formatType
	 * @return
	 * @throws Throwable
	 */
	public static Collection<String> findStudiesWithNoProject (ServiceExecutor executor, String where, FormatType formatType) throws Throwable {		

		String query = null;
		if (formatType==FormatType.DICOM) {
			query = "type=dicom/study and mf-dicom-project hasno value";
		} else if (formatType==FormatType.SIEMENS_RAW) {
			query = "type=siemens-raw-petct/study and mf-dicom-project hasno value";
		} else if (formatType==FormatType.BOTH) {
			query = "(type=dicom/study or type=siemens-raw-petct/study) and mf-dicom-project hasno value";
		}
		if (query==null) return null;
		//		
		if (where!=null) query = where + " and (" + query + ")";
		return query(executor, query);
	}




	/**
	 * FInd orphaned studies (that have no primary relationship 'contains series') (DICOM and/or Siemens raw)
	 * 
	 * @param executor
	 * @param where
	 * @param formatType
	 * @return
	 * @throws Throwable
	 */
	public static Collection<String> findStudiesWithNoSeries (ServiceExecutor executor, String where, FormatType formatType) throws Throwable {
		String query = null;
		if (formatType==FormatType.DICOM) {
			query = "type=dicom/study and (not (related to{'contains'} any))";
		} else if (formatType==FormatType.SIEMENS_RAW) {
			query = "type=siemens-raw-petct/study and (not (related to{'contains'} any))";
		} else if (formatType==FormatType.BOTH) {
			query = "(type=dicom/study or type=siemens-raw-petct/study) and (not (related to{'contains'} any))";
		}	
		if (query==null) return null;
		//	
		if (where!=null) query = where + " and (" + query + ")";
		return query(executor, query);
	}

	/**
	 * Find orphaned Studies (DICOM and/or Siemens raw) that have no secondary relationship had-by Patients
	 * 
	 * @param executor
	 * @param where
	 * @param formatType
	 * @return
	 * @throws Throwable
	 */
	public static Collection<String> findStudiesWithNoPatient (ServiceExecutor executor, String where, FormatType formatType) throws Throwable {		
		String query = null;
		if (formatType==FormatType.DICOM) {
			query = "type=dicom/study and (not (related to{'had-by'} any))";
		} else if (formatType==FormatType.SIEMENS_RAW) {
			query = "type=siemens-raw-petct/study and (not (related to{'had-by'} any))";
		} else if (formatType==FormatType.BOTH) {
			query = "(type=dicom/study or type=siemens-raw-petct/study) and (not (related to{'had-by'} any))";
		}
		if (query==null) return null;
		//		
		if (where!=null) query = where + " and (" + query + ")";
		return query(executor, query);
	}


	/**
	 * Find orphaned Series (DICOM and/or Siemens raw) that have no secondary relationship  container by studies
	 * 
	 * @param executor
	 * @param where
	 * @param formatType
	 * @return
	 * @throws Throwable
	 */
	public static Collection<String> findSeriesWithNoStudy  (ServiceExecutor executor, String where, FormatType formatType) throws Throwable {
		String query = null;
		if (formatType==FormatType.DICOM) {
			query = "type=dicom/series and (not (related to{'container'} any))";
		} else if (formatType==FormatType.SIEMENS_RAW) {
			query = "type=siemens-raw-petct/series and (not (related to{'container'} any))";
		} else if (formatType==FormatType.BOTH) {
			query = "(type=dicom/series or type=siemens-raw-petct/series) and (not (related to{'container'} any))";		}	
		if (query==null) return null;
		//		
		if (where!=null) query = where + " and (" + query + ")";
		return query(executor, query);
	}




	/**
	 * Find projects (there may be multiples) that has this patient. 
	 * 
	 * @param executor
	 * @param study
	 * @return
	 * @throws Throwable
	 */
	public static Collection<String> findProjectsFromPatient (ServiceExecutor executor, String patient) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		// There is no type=dicom/project yet
		String query = "type!=dicom/study and mf-dicom-project has value and (related to{'has'} (id=" + patient + "))";
		dm.add("where", query);
		dm.add("pdist", 0);
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r!=null) {
			Collection<String> t = r.values("id");
			return t; 
		} else {
			return null;
		}
	}


	/**
	 * Find patient that has this study. Exception if more than one
	 * 
	 * @param executor
	 * @param study
	 * @return
	 * @throws Throwable
	 */
	public static String findPatientsFromStudy (ServiceExecutor executor, String study) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "(mf-dicom-patient has value or mf-dicom-patient-encrypted has value) and (related to{'has'} (id=" + study + "))";
		dm.add("where", query);
		dm.add("pdist", 0);
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r!=null) {
			Collection<String> t = r.values("id");
			if (t==null) return null;
			//
			if (t!=null && t.size()>1) {
				throw new Exception ("Found multiple patients for Study asset " + study);
			}
			return r.value("id");
		} else {
			return null;
		}
	}

	/**
	 * Find the containing studies (DICOM and/or Siemens raw) from the given series
	 * 
	 * @param executor
	 * @param series
	 * @param formatType
	 * @return
	 * @throws Throwable
	 */
	public static Collection<String> findStudiesFromSeries (ServiceExecutor executor, String series, FormatType formatType) throws Throwable {		
		String query = null;
		if (formatType==FormatType.DICOM) {
			query = "type=dicom/study and (related to{'contains'} (id=" + series + "))";
		} else if (formatType==FormatType.SIEMENS_RAW) {
			query = "type=siemens-raw-petct/study and (related to{'contains'} (id=" + series + "))";

		} else if (formatType==FormatType.BOTH) {
			query = "(type=dicom/study or type=siemens-raw-petct/study) and (related to{'contains'} (id=" + series + "))";
		}
		if (query==null) return null;
		//
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", query);
		dm.add("pdist", 0);
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r!=null) {
			return r.values("id");
		} else {
			return null;
		}
	}



	/**
	 * Add the primary 'has' relationship from project to patient
	 * 
	 * @param executor
	 * @param project
	 * @param patient
	 * @throws Throwable
	 */
	public static void addPatientToProjectPrimary (ServiceExecutor executor, String project, String patient) throws Throwable {
		AssetUtil.addRelationship(executor, project, patient, "has", false);
	}

	/**
	 * Establish a bi-directional relationship between a project asset and a patient asset
	 * 
	 * @param executor
	 * @param projectID
	 * @param patientID
	 * @throws Throwable
	 */
	public static void addPatientToProject (ServiceExecutor executor, String projectID, String patientID) throws Throwable {
		AssetUtil.addRelationship(executor, projectID, patientID, "has", true);
	}

	/**
	 * Add the primary 'has' relationship from Patient to Study 
	 *
	 * @param executor
	 * @param patient
	 * @param study
	 * @throws Throwable
	 */
	public static void addStudyToPatientPrimary (ServiceExecutor executor, String patient, String study) throws Throwable {
		AssetUtil.addRelationship(executor, patient, study, "has", false);
	}

	/**
	 * Add the primary 'contains' relationship from Study to Series
	 * 
	 * @param executor
	 * @param study
	 * @param series
	 * @throws Throwable
	 */
	public static void addSeriesToStudyPrimary (ServiceExecutor executor,  String study, String series) throws Throwable {
		AssetUtil.addRelationship(executor, study, series, "contains", false);
	}

	/**
	 * Add inverse relationship 'had-by' from patient to project
	 * 
	 * @param executor
	 * @param patient
	 * @param project
	 * @throws Throwable
	 */
	public static void addPatientToProjectSecondary (ServiceExecutor executor,  String patient, String project) throws Throwable {
		AssetUtil.addRelationship(executor, patient, project, "had-by", false);
	}

	/**
	 * Add inverse relationship 'had-by' from study to patient
	 *
	 * @param executor
	 * @param study
	 * @param patient
	 * @throws Throwable
	 */
	public static void addStudyToPatientSecondary (ServiceExecutor executor,  String study, String patient) throws Throwable {
		AssetUtil.addRelationship(executor, study, patient, "had-by", false);
	}

	/**
	 * Add inverse relationship 'container' from series to study
	 *
	 * @param executor
	 * @param series
	 * @param study
	 * @throws Throwable
	 */
	public static void addSeriesToStudySecondary (ServiceExecutor executor,  String series, String study) throws Throwable {
		AssetUtil.addRelationship(executor, series, study, "container", false);
	}


	/**
	 * Returns number and size (bytes)
	 * 
	 * @param executor
	 * @param studyAsset
	 * @param formatType
	 * @return
	 * @throws Throwable
	 */
	public static XmlDoc.Element sizeOfStudy (ServiceExecutor executor, String studyAsset, FormatType formatType) throws Throwable {
		String query = null;
		if (formatType==FormatType.DICOM) {
			query = "mf-dicom-series has value and (related to{'container'} (id=" + studyAsset + "))";
		} else if (formatType==FormatType.SIEMENS_RAW) {
			query = "daris:siemens-raw-petct-series has value and (related to{'container'} (id=" + studyAsset + "))";
		} else if (formatType==FormatType.BOTH) {
			query = "(mf-dicom-series has value or daris:siemens-raw-petct-series has value) and (related to{'container'} (id=" + studyAsset + "))";
		}	
		if (query==null) return null;
		//
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("size", "infinity");
		dm.add("pdist", 0);           // local query
		dm.add("where", query);
		dm.add("action", "sum");
		dm.add("xpath", "content/size");
		return executor.execute("asset.query", dm.root());
	}





	/**
	 * FIsh out Patient details from the patient asset
	 * 
	 * @param executor
	 * @param patientID
	 * @return DICOMPatient
	 * 
	 * @throws Throwable
	 */
	public static DICOMPatient getPatientDetails (ServiceExecutor executor, String patientID) throws Throwable {
		XmlDoc.Element r = AssetUtil.getAsset(executor, null, patientID);
		if (r==null) return null;
		return new DICOMPatient(r.element("asset/meta/mf-dicom-patient"));
	}

	private static Collection<String> query (ServiceExecutor executor, String where) throws Throwable {

		// FInd orphaned patients (that have no primary relationship having Studies)
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", where);
		dm.add("pdist", 0);
		dm.add("size", "infinity");
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r==null) return null;
		return r.values("id");
	}
}
