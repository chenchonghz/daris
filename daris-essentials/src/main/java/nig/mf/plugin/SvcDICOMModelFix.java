package nig.mf.plugin;

import java.util.Collection;

import nig.mf.dicom.plugin.util.DICOMModelUtil;
import nig.mf.plugin.util.AssetUtil;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.BooleanType;

import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;


public class SvcDICOMModelFix extends PluginService {
	private Interface _defn;

	public SvcDICOMModelFix() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("where",StringType.DEFAULT, "Query predicate to restrict assets for consideration.  If not set, all assets are considered.Do not use to select assets by document type as the service will make queries with document types; use to restrict by namespace, or time", 0, 1));
		_defn.add(new Interface.Element("fix", BooleanType.DEFAULT, "Fix broken relationships if possible (default is false, just report intended work). This means re-establish bi-directional relationshjips if only uni-directional remains.", 0, 1));
		_defn.add(new Element("include-siemens-raw", BooleanType.DEFAULT, "Include raw Siemens data (associated with DICOM patient records) in the listing, defaults to false.", 0, 1));
	}

	@Override
	public String name() {

		return "dicom.model.fix";
	}

	@Override
	public String description() {

		return "Checks and optionally fixes relationships for assets held in the DICOM data model on he local server. Ensures that the bi-directional relationships are correct. Can only fix if at least one direction exists. Lists the asset to which a relationship is added, and the asset to which the relationship is added. Only finds assets fully orphaned.";
	}

	@Override
	public Interface definition() {

		return _defn;
	}

	@Override
	public Access access() {

		return ACCESS_ADMINISTER;
	}

	@Override
	public boolean canBeAborted() {

		return true;
	}

	@Override
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// TBD: this service won't find e.g. Studies that have some relationships
		// to Series but not others.  It only finds fully orphaned ones.
		//

		// Arguments
		String where = args.value("where");
		Boolean fix = args.booleanValue("fix", false);
		Boolean includeSiemens = args.booleanValue("include-siemens-raw");
		DICOMModelUtil.FormatType formatType = DICOMModelUtil.FormatType.DICOM;
		if (includeSiemens) formatType = DICOMModelUtil.FormatType.BOTH;

		// Find/fix all projects with no primary relationships to Patients
		// that do have secondary relationships back to the Project
		checkProjectsPrimary (executor(), where, fix, formatType, w);

		// Find/fix all patients with no primary relationships to Studies
		// that do have secondary relationship back to the Patient
		checkPatientsPrimary (executor(), where, fix, formatType, w);

		// Find/fix all studies with no primary relationships to Series
		// that do have a secondary relationship back to the Study
		checkStudiesPrimary (executor(), where, fix, formatType, w);

		// Find/fix all Patients with no secondary relationship to Projects
		// that do have a primary relationship to the Patient
		checkPatientsSecondary (executor(), where, fix, w);

		// Find/fix all Studies with no secondary relationship to Patients
		// that do have a primary relationship to the Study
		checkStudiesSecondary (executor(), where, fix, formatType, w);

		// Find/fix all Series with no secondary relationship to Studies
		// that do have a primary relationship to the Series
		checkSeriesSecondary (executor(), where, fix, formatType, w);
	}


	private static void checkProjectsPrimary (ServiceExecutor executor, String where, Boolean fix, DICOMModelUtil.FormatType formatType, XmlWriter w) throws Throwable {

		// FInd orphaned projects (that have no primary relationship to anything [expected to be patients])
		Collection<String> projects = DICOMModelUtil.findProjectsWithNoPatient(executor, where);

		// Now try and find patients related to each orphaned project with the reverse relationship (had-by)
		if (projects!=null && projects.size()>0) {
			for (String project : projects) {
				Collection<String> patients = DICOMModelUtil.findPatients (executor, project);

				// Add the forwards relationship from project to patient for each patient
				if (patients!=null && patients.size()>0) {
					w.push("project", new String[]{"id", project});
					for (String patient : patients) {
						if (fix) DICOMModelUtil.addPatientToProjectPrimary (executor, project, patient);
						String type = AssetUtil.getType (executor, patient, false);
						w.add("patient", new String[]{"type", type, "relationship", "has"}, patient);
					}
					w.pop();
				}
			}
		}
	}




	private static void checkPatientsPrimary (ServiceExecutor executor, String where, Boolean fix, DICOMModelUtil.FormatType formatType, XmlWriter w) throws Throwable {

		// FInd orphaned patients (that have no primary relationship to anything [expected to be Studies])
		Collection<String> patients = DICOMModelUtil.findPatientsWithNoStudy(executor, where);

		// Now try and find studies related to each orphaned patient with the reverse relationship (had-by)
		if (patients!=null && patients.size()>0) {
			for (String patient :patients) {
				Collection<String> studies = DICOMModelUtil.findStudies (executor, patient, formatType);

				// Add the forwards relationship from patient to study for each study
				if (studies!=null && studies.size()>0) {
					w.push("patient", new String[]{"id", patient});
					for (String study : studies) {
						if (fix) DICOMModelUtil.addStudyToPatientPrimary (executor, patient, study);
						String type = AssetUtil.getType (executor, study, false);
						w.add("study", new String[]{"type", type, "relationship", "has"}, study);
					}
					w.pop();
				}
			}
		}
	}


	private static void checkStudiesPrimary (ServiceExecutor executor, String where, Boolean fix, DICOMModelUtil.FormatType formatType, XmlWriter w) throws Throwable {

		// FInd orphaned Studies (that have no primary relationship containing Series)
		Collection<String> studies = DICOMModelUtil.findStudiesWithNoSeries(executor, where, formatType);

		// Now try and find series related to each orphaned study with the reverse relationship (container)
		if (studies!=null && studies.size()>0) {
			for (String study : studies) {
				Collection<String> serieses = DICOMModelUtil.findSeries (executor, study, formatType);

				// Add the forwards relationship from study to series for each found series
				if (serieses!=null && serieses.size()>0) {
					String type = AssetUtil.getType (executor, study, false);
					w.push("study", new String[]{"type", type, "id", study});
					for (String series : serieses) {
						if (fix) DICOMModelUtil.addSeriesToStudyPrimary (executor, study, series);
						type = AssetUtil.getType (executor, series, false);
						w.add("series", new String[]{"type", type, "relationship", "contains"}, series);
					}
					w.pop();
				}
			}
		}
	}		


	private static void checkSeriesSecondary (ServiceExecutor executor, String where, Boolean fix, DICOMModelUtil.FormatType formatType, XmlWriter w) throws Throwable {
		// Find orphaned Series (that have no secondary relationship  container by studies) 
		Collection<String> serieses =DICOMModelUtil.findSeriesWithNoStudy(executor, where, formatType);

		// Find any Studies related to each series with the primary relationship ('contains')
		if (serieses!=null && serieses.size()>0) {
			for (String series : serieses) {
				Collection<String> studies = DICOMModelUtil.findStudiesFromSeries (executor, series, formatType);

				// Add the secondary relationship from Series to Study  for each Study
				if (studies!=null && studies.size()>0) {
					String type = AssetUtil.getType (executor, series, false);
					w.push("series", new String[]{"type", type, "id", series});
					for (String study : studies) {
						if (fix) DICOMModelUtil.addSeriesToStudySecondary (executor, series, study);
						type = AssetUtil.getType (executor, study, false);
						w.add("study", new String[]{"type", type, "relationship", "container"}, study);
					}
					w.pop();
				}
			}
		}
	}		


	private static void checkPatientsSecondary (ServiceExecutor executor, String where, Boolean fix, XmlWriter w) throws Throwable {
		// Find orphaned Patients (that have no secondary relationship  had by Projects)
		Collection<String> patients = DICOMModelUtil.findPatientsWithNoProject(executor, where);

		// Find a Project related to each patient with the primary relationship ('has')
		if (patients!=null && patients.size()>0) {
			for (String patient : patients) {
				Collection<String> projects = DICOMModelUtil.findProjectsFromPatient (executor, patient);

				// Add the secondary relationship from patient to project for each project
				if (projects!=null) {
					for (String project : projects) {
						String type = AssetUtil.getType (executor, project, false);
						w.push("patient", new String[]{"type", type, "id", patient});
						if (fix) DICOMModelUtil.addPatientToProjectSecondary (executor, patient, project);
						w.add("project", new String[]{"relationship", "had-by"}, project);
						w.pop();
					}
				}
			}
		}
	}

	private static void checkStudiesSecondary (ServiceExecutor executor, String where, Boolean fix, DICOMModelUtil.FormatType formatType, XmlWriter w) throws Throwable {
		// Find orphaned Studies (that have no secondary relationship  had by patients)
		Collection<String> studies = DICOMModelUtil.findStudiesWithNoPatient(executor, where, formatType);

		// Find a Patient related to each study with the primary relationship ('has')
		if (studies!=null && studies.size()>0) {
			for (String study : studies) {
				String patient = DICOMModelUtil.findPatientsFromStudy (executor, study);

				// Add the secondary relationship from Study to patient for each patient
				if (patient!=null) {
					String type = AssetUtil.getType (executor, study, false);
					w.push("study", new String[]{"type", type, "id", study});
					if (fix) DICOMModelUtil.addStudyToPatientSecondary (executor, study, patient);
					w.add("patient", new String[]{"relationship", "had-by"}, patient);
					w.pop();
				}
			}
		}
	}
}
