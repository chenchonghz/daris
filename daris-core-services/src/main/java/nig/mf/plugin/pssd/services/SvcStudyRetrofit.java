package nig.mf.plugin.pssd.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;

import nig.mf.MimeTypes;
import nig.mf.dicom.plugin.util.DICOMPatient;
import nig.mf.plugin.pssd.dicom.study.CIDAndMethodStep;
import nig.mf.plugin.pssd.dicom.study.StudyMetadata;
import nig.mf.plugin.pssd.dicom.subject.SubjectHandler;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.CiteableIdUtil;
import nig.mf.pssd.plugin.util.PSSDUtil;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.DictionaryEnumType;
import arc.mf.plugin.dtype.BooleanType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcStudyRetrofit extends PluginService {
	private Interface _defn;

	public SvcStudyRetrofit() {

		_defn = new Interface();

		_defn.add(new Element("id", AssetType.DEFAULT,
				"The asset ID of the local DICOM object model Study asset to be retrofitted.", 1, 1));
		_defn.add(new Element("pid", CiteableIdType.DEFAULT,
				"The citeable ID of the local, parent PSSD Project, Subject, ExMethod or Study. If a Project the Subject will be looked for first (using DICOM meta-data in mf-dicom-patient and if needed and configured, domain-specific meta-data), if not found a Subject will be created. If a Subject a va;idity check will be made and then a Studyu created. If an ExMethod, a new Study is created. If a Study, that Study is used and the Series are converted to DataSets under that Study.",
				1, 1));
		_defn.add(new Element("series-id", AssetType.DEFAULT,
				"Specify the specific Series asset IDs  in the parent Study you want to migrate. Defaults to all.", 0, Integer.MAX_VALUE));
		_defn.add(new Element("step", CiteableIdType.DEFAULT,
				"The step within the method that resulted in this study.", 1, 1));

		try {
			DictionaryEnumType eType = new DictionaryEnumType("pssd.study.types");
			_defn.add(new Element("type", eType,
					"The type of the study. If not specified, then method must be specified.", 0, 1));
		} catch (Throwable e) {
			e.printStackTrace();
		}

		_defn.add(new Element("internalize",
				new EnumType(new String[] { "none", "copy", "move" }),
				"'none' turns off content internalization. 'copy' takes a copy, leaving the orgininal. 'move' will move the content (if an accessible file) into the file-system data store (if there is one). Defaults to 'move'.",
				0, 1));

		_defn.add(new Element("destroy-old-assets", BooleanType.DEFAULT,
				"If true internalize the contents (implies 'internalize=move' argument to be true) and it will destroy the old study & series assets when migrating is finished. Defaults to true.",
				0, 1));
		//
		_defn.add(new Element("sort-by-date", BooleanType.DEFAULT,
				"If true (default), sort the Series assets by date as they are moved over to the new Study",
				0, 1));
		_defn.add(new Interface.Element("meta-service", arc.mf.plugin.dtype.StringType.DEFAULT, 
				"The service that populates domain-specific meta-data on the Subject and Study by mapping from DICOM elments", 0, 1));
		_defn.add(new Interface.Element("subject-find-method", arc.mf.plugin.dtype.StringType.DEFAULT, 
				"If the parent is a project, this service tries to find the subject pre-existing via the given method: Allowed values are 'id',name', 'name+', 'all'. 'id' means mf-dicom-patient/id, 'name' means the full name from mf-dicom-patient/name (first and last), 'name+' means match on name,sex and DOB, 'all' means match on id,name,sex and DOB.  The default if not specified is 'id'.", 0, 1));

	}

	public String name() {
		return "om.pssd.dicom.study.retrofit";
	}

	public String description() {
		return "Retrofit DICOM object model Study asset to pre-existing PSSD Project, Subject, ExMethod or Study. Creates Subject,ExMethod and Study as  needed.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Args
		String dicomStudyID = args.value("id");
		String pid = args.value("pid");
		Collection<String> seriesAssetIds = args.values("series-id");
		if (PSSDUtil.isReplica(executor(), pid)) {
			throw new Exception ("The given object '" + pid + "' is a replica. Cannot import data into it.");
		}
		String type = args.value("type");
		String step = args.value("step");
		String internalize = args.value("internalize");
		Boolean sortByDate = args.booleanValue("sort-by-date", true);
		String domainService = args.value("meta-service");
		String subjectFindMethod = args.stringValue("subject-find-method", "id");

		// Some parsing
		String name = null;
		String description = null;	
		//
		if (internalize == null) {
			internalize = "move";
		}
		boolean destroyOldAssets = true;
		if (args.value("destroy-old-assets") != null) {
			if (args.value("destroy-old-assets").equals("true")) {
				destroyOldAssets = true;
			}
			if (args.value("destroy-old-assets").equals("false")) {
				destroyOldAssets = false;
			}
		}
		if (destroyOldAssets == true) {
			internalize = "move";
		}
		if (destroyOldAssets == true && internalize.equals("none")) {
			throw new Exception("destroy-old-assets implies internalize=move.");
		}

		// Validate input Study 
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", dicomStudyID);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());

		String assetType = r1.value("asset/type");
		if (!assetType.equals("dicom/study")) {
			throw new Exception("Asset (id=" + dicomStudyID + ") is not a valid dicom/study asset.");
		}
		XmlDoc.Element mfDICOMStudy = r1.element("asset/meta/mf-dicom-study");
		if (mfDICOMStudy==null) {
			throw new Exception("Asset (id=" + dicomStudyID + ") does not hold mf-dicom-study meta-data");
		}

		// Find the parent DICOM patient asset
		String dicomPatientID = findDICOMPatientAssetID(dicomStudyID);
		if (dicomPatientID==null) {
			throw new Exception ("Could not find the parent DICOM patient asset to DICOM study " + dicomStudyID);
		}

		// What kind of Object is the parent ?
		String sCID = null;
		String stCID = null;
		String exCID = null;
		Boolean fillIn = true;
		if (PSSDUtil.isValidProject (executor(), pid, false)) {
			// Try to find the Subject
			sCID = findSubject (executor(), pid, subjectFindMethod, dicomPatientID);
			w.add("subject", new String[]{"found", "true", "created", "false"}, sCID);

			// If not found, create Subject (setting fixed Method meta-data) and ExMethod
			if (sCID!=null) {
				// FInd child ExMethod. The best we can do is find the first one (there could be multiples).
				XmlDocMaker dm = new XmlDocMaker("args");
				dm.add("id", sCID);
				XmlDoc.Element r = executor().execute("om.pssd.collection.member.list", dm.root());
				exCID = r.value("object/id");	
				w.add("ex-method", new String[]{"found", "true", "created", "false"}, exCID);
			} else {
				String[] ids = PSSDUtil.createSubject (executor(), pid, null, exCID, null, fillIn);
				sCID = ids[0];
				exCID = ids[1];
				if (sCID!=null) {
					System.out.println("       Subject " + sCID + " auto created with ExMethod " + exCID);
				}
				w.add("subject", new String[]{"found", "false", "created", "true"}, sCID);
				w.add("ex-method", new String[]{"found", "false", "created", "true"}, exCID);

				// Copy  mf-dicom-patient 
				Vector<String> dt = new Vector<String>();
				dt.add("mf-dicom-patient");
				AssetUtil.copyMetaData(executor(), dt, dicomPatientID, sCID, false, "pssd.private", null);

				// Set domain-specific meta-data
				// This is a bit clumsy as this framework was premised on holding the DICOM Study meta-data
				// in the StudyMeta container.
				if (domainService!=null) setDomainMetaData (executor(), domainService, sCID, mfDICOMStudy, dicomPatientID, null);
			}
		} else if (PSSDUtil.isValidSubject(executor(), pid, false)) {
			throw new Exception("The parent object cannot be a Subject (enhancement required). Specify the ExMethod");
			// Validate the subject
			// Find the child ExMethod 
			// exCID =       	
		} else if (PSSDUtil.isValidExMethod(executor(), pid, false)) {
			// Just use the ExMethod
			exCID = pid;
			w.add("ex-method", new String[]{"found", "false", "created", "false"}, exCID);

		} else if (PSSDUtil.isValidStudy(executor(), pid, false))  {
			// Just use the Study
			stCID = pid;
			w.add("study", new String[]{"found", "false", "created", "false"},stCID);
		} else {
			throw new Exception ("Parent object must be Project, Subject, ExMethod or Study");
		}


		// Create output Study if needed
		if (stCID == null) {
			if (type == null) type = "Magnetic Resonance Imaging";
			name = r1.value("asset/meta/mf-dicom-study/id"); // There is nothing else to use
			description = r1.value("asset/meta/mf-dicom-study/description");

			// om.pssd.study.create
			doc = new XmlDocMaker("args");
			doc.add("fillin", true);
			if (name != null) doc.add("name", name);
			if (description != null) doc.add("description", description);
			doc.add("pid", exCID);
			doc.add("type", type);
			if (step != null) doc.add("step", step);

			// Create
			XmlDoc.Element r2 = executor().execute("om.pssd.study.create", doc.root());
			stCID = r2.value("id");
			w.add("study", new String[]{"found", "false", "created", "true"},stCID);
		} 

		// Now copy mf-dicom-study over from DICOM study to PSSD Study
		// and add attributes
		Vector<String> dt = new Vector<String>();
		dt.add("mf-dicom-study");
		AssetUtil.copyMetaData(executor(), dt, dicomStudyID, stCID, false, "dicom", "pssd.meta");

		// Find related or give by users series IDs
		if (seriesAssetIds==null) {
			seriesAssetIds = r1.values("asset/related[@type='contains']/to");
		}
		if (seriesAssetIds!=null) {
			if (sortByDate) {
				Collection<String> t = sortSeriesByDate (seriesAssetIds);
				migrateSeriesAll(t, stCID);
			} else {
				migrateSeriesAll(seriesAssetIds, stCID);
			}
		}

		// Internalize Assets
		if (!internalize.equals("none")) {
			internalizeAssets(stCID, internalize);
		}

		// Destroy old Assets
		if (destroyOldAssets == true) {
			destroyOldStudy(dicomStudyID);
			destroyAsset(dicomPatientID);
		}
	}


	private String findSubject (ServiceExecutor executor, String pid, String findSubjectMethod, 
			String dicomPatientID) throws Throwable {

		// This function that we re-use from the DICOM server needs the patient meta-data in the Stduy Meta data
		// container.  So create it with the bits we need.
		XmlDoc.Element t = AssetUtil.getAsset(executor, null, dicomPatientID);
		XmlDoc.Element mfDICOMPatient = t.element("asset/meta/mf-dicom-patient");
		
		System.out.println("mf-dicom-patient=" + mfDICOMPatient);
		if (mfDICOMPatient==null) return null;

		StudyMetadata studyMeta = StudyMetadata.createFrom(mfDICOMPatient.value("id"), mfDICOMPatient.value("sex"),
                mfDICOMPatient.dateValue("dob"),  mfDICOMPatient.value("name[@type='first']"),
          		mfDICOMPatient.value("name[@type='last']"));
		
		System.out.println("StudyeMeta ID = " + studyMeta.patientID());
		System.out.println("Try to find subject");
		CIDAndMethodStep cms = SubjectHandler.findSubjectByDetail (executor, findSubjectMethod, 
				studyMeta, pid);	
		if (cms==null) return null;
		return cms.cid();
	}

	private static StudyMetadata createStudyMeta (XmlDoc.Element mfDICOMPatient) throws Throwable {

		// Be very clear here !   mfDICOMStudy contains a document from mf-dicom-study.  This is similar to,
		// but not exactly the same as what we want in the XML for the function below (this XML is intended
		// for interchange with the StudyMetadata objects and its internal structure is up to it)
		// For our purposes, we only need the patient components. So make a specialised version
		// with just these bits.
		return StudyMetadata.createFrom(mfDICOMPatient.value("id"), mfDICOMPatient.value("sex"),
				                        mfDICOMPatient.dateValue("dob"), 
	                         			mfDICOMPatient.value("name[@type='first']"),
		                          		mfDICOMPatient.value("name[@type='last']"));
	}


	private void setDomainMetaData (ServiceExecutor executor, String domainService, String sCID, XmlDoc.Element studyMeta, String patientID,
			String seriesID) throws Throwable {


		// This is a bit clumsy as this framework was premised on holding the DICOM Study meta-data
		// in the StudyMeta container.  However, this is not available to us, so we just have to
		// fish things out of mf-dicom-study and mf-dicom-patient
		XmlDoc.Element patientMeta = AssetUtil.getAsset(executor, null, patientID).element("asset/meta/mf-dicom-patient");
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", sCID);
		dm.push("dicom");
		//
		String t = studyMeta.value("sdate");
		if (t!=null) dm.add("date", t);
		//
		t = studyMeta.value("description");
		if (t!=null) dm.add("description", t);
		//    
		t = studyMeta.value("id");
		if (t!=null) dm.add("id", t);
		//
		t = studyMeta.value("location/institution");
		if (t!=null) dm.add("institution", t);
		//
		t = studyMeta.value("location/station");
		if (t!=null) dm.add("station", t);
		//
		t = studyMeta.value("equipment/manufacturer");
		if (t!=null) dm.add("manufacturer", t);
		//
		t = studyMeta.value("equipment/manufacturer");
		if (t!=null) dm.add("manufacturer", t);
		//
		t = studyMeta.value("equipment/model");
		if (t!=null) dm.add("model", t);
		//
		t = studyMeta.value("rpn");
		if (t!=null) dm.add("rpn", t);
		//
		t = studyMeta.value("uid");
		if (t!=null) dm.add("uid", t);	


		// Where would we get SeriesMeta data from ?  It would have to be from one Series only
		// and hope representative.... Leave out for now, these fields aren't important
		// and any way will be set on mf-dicom-series
		XmlDoc.Element seriesMeta = null;
		if (seriesMeta!=null) {
			t = seriesMeta.value("modality");
			if (t!=null) dm.add("modality", t);
			//
			t = seriesMeta.value("protocol");
			if (t!=null) dm.add("protocol", t);
		}

		//
		dm.push("subject");
		//
		t = studyMeta.value("subject/age");
		if (t!=null) dm.add("age", t);
		//
		t = studyMeta.value("subject/sex");
		if (t!=null) dm.add("sex", t);
		//
		t = studyMeta.value("subject/size");
		if (t!=null) dm.add("size", t);
		//
		t = studyMeta.value("subject/weight");
		if (t!=null) dm.add("weight", t);
		//
		if (patientMeta!=null) {
			t = patientMeta.value("dob");
			if (t!=null) dm.add("dob", t);
			//
			t = patientMeta.value("id");
			if (t!=null) dm.add("id", t);
			//
			DICOMPatient dp = new DICOMPatient(patientMeta);
			String fn = dp.getFullName();
			if (fn!=null) dm.add("name",fn);
		}
		dm.pop();
		dm.pop();

		// Execute
		executor.execute(domainService, dm.root());

	}

	private void migrateSeriesAll(Collection<String> seriesAssetIds, String pssdStudyCid) throws Throwable {

		for (String seriesAssetId : seriesAssetIds) {
			String[] dataSetCIDs = migrateSeries(seriesAssetId, pssdStudyCid);

			// Ensure names of DataSets are correctly formed from DICOM "protocol_description".
			// Also ensure that the DataSets description is filled in.
			// These services are consistent with the behaviour of the DICOM PSSD server
			XmlDocMaker doc = null;
			if (dataSetCIDs[0] != null) {
				doc = new XmlDocMaker("args");
				doc.add("cid", dataSetCIDs[0]);
				doc.add("overwrite", true);
				try {
					executor().execute("om.pssd.dataset.name.grab", doc.root());
				} catch (Throwable t) {
					// It's not really fatal; just nothing gets grabbed and in loops
					// we don't want an exception
				}
				try {
					executor().execute("om.pssd.dataset.description.grab", doc.root());
				} catch (Throwable t) {
					// It's not really fatal; just nothing gets grabbed and in loops
					// we don't want an exception
				}
			}
			if (dataSetCIDs[1] != null) {
				doc = new XmlDocMaker("args");
				doc.add("cid", dataSetCIDs[1]);
				doc.add("overwrite", true);
				try {
					executor().execute("om.pssd.dataset.name.grab", doc.root());
				} catch (Throwable t) {
					//
				}
				try {
					executor().execute("om.pssd.dataset.description.grab", doc.root());
				} catch (Throwable t) {
					//
				}
			}
		}

	}

	private String[] migrateSeries(String seriesAssetId, String pssdStudyCid) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", seriesAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());
		String seriesCid = r1.value("asset/cid");

		// Check whether the content type is supported.
		String seriesAssetType = r1.value("asset/type");
		if (!(seriesAssetType.equals(MimeTypes.DICOM_SERIES) || seriesAssetType.equals(MimeTypes.BRUKER_SERIES))) {
			throw new Exception("The content type of asset(id=" + seriesAssetId + ") is not supported.");
		}

		// Check whether the content is in DB or File System.
		String seriesAssetContentUrl = r1.value("asset/content/url");
		String seriesAssetContentType = r1.value("asset/content/type");
		String name = r1.value("asset/meta/mf-dicom-series/protocol");

		if (seriesAssetContentUrl == null && r1.element("asset/content") != null) {
			throw new Exception(
					"Content of asset(id="
							+ seriesAssetId
							+ ") is in database. It is not supported by this service. You can move it into namespace which uses file system.");
		}

		String pssdPrimaryDatasetCid = null;
		String pssdPrimaryDatasetVid = null;
		String pssdDerivationDatasetCid = null;

		String proxySeriesAssetId = r1.value("asset/related[@type='proxy']/to");
		if (proxySeriesAssetId != null) {
			// migrate proxy asset as primary dataset.
			pssdPrimaryDatasetCid = migrateProxySeries(proxySeriesAssetId, seriesAssetId, pssdStudyCid);
			doc = new XmlDocMaker("args");
			doc.add("cid", pssdPrimaryDatasetCid);
			doc.add("pdist", 0);       // FOrce local
			XmlDoc.Element r2 = executor().execute("asset.get", doc.root(), null, null);
			pssdPrimaryDatasetVid = r2.value("asset/@vid");
		}

		// migrate series asset as derivation dataset.
		doc = new XmlDocMaker("args");
		doc.add("fillin", true);
		if (name == null) {
			name = "Migrated_from_" + seriesAssetId;
		}
		doc.add("name", name);
		doc.add("pid", pssdStudyCid);
		if (pssdPrimaryDatasetCid != null && pssdPrimaryDatasetVid != null) {
			doc.add("input", new String[] { "vid", pssdPrimaryDatasetVid }, pssdPrimaryDatasetCid);
		}
		doc.add("type", seriesAssetType);

		// Copy over the mf-dicom-series meta-data
		doc.push("meta");
		if (seriesAssetType.equals(MimeTypes.DICOM_SERIES)) {
			XmlDoc.Element xeMFDicomSeries = r1.element("asset/meta/mf-dicom-series");
			xeMFDicomSeries.add(new XmlDoc.Attribute("ns", "dicom"));
			doc.add(xeMFDicomSeries);
		}

		doc.push("mf-note");
		String note = "Retrofitted from " + seriesAssetId;
		if (seriesCid != null) {
			note += "," + seriesCid;
		}
		doc.add("note", note);
		doc.pop();

		doc.pop();

		if (pssdPrimaryDatasetCid != null) {
			String methodCid = getMethodCid(pssdStudyCid);
			doc.push("transform");
			doc.add("id", methodCid);
			doc.pop();
		}
		XmlDoc.Element r3 = executor().execute("om.pssd.dataset.derivation.create", doc.root());
		pssdDerivationDatasetCid = r3.value("id");

		if (seriesAssetContentUrl != null) {
			setAssetContentUrlAndType(pssdDerivationDatasetCid, seriesAssetContentUrl, seriesAssetContentType);
		}

		//
		String[] cids = new String[2];
		cids[0] = pssdDerivationDatasetCid;
		cids[1] = pssdPrimaryDatasetCid;
		return cids;

	}

	private String migrateProxySeries(String proxySeriesAssetId, String seriesAssetId, String pssdStudyCid)
			throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", seriesAssetId);
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());
		String name = r1.value("asset/meta/mf-dicom-series/protocol");

		doc = new XmlDocMaker("args");
		doc.add("id", proxySeriesAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r2 = executor().execute("asset.get", doc.root());
		String proxySeriesCid = r2.value("asset/cid");
		String proxySeriesAssetContentUrl = r2.value("asset/content/url");
		String proxySeriesAssetContentType = r2.value("asset/content/type");
		String proxySeriesAssetType = r2.value("asset/type");

		if (proxySeriesAssetContentUrl == null && r2.element("asset/content") != null) {
			throw new Exception(
					"Content of asset(id="
							+ proxySeriesAssetId
							+ ") is in database. It is not supported by this service. You can move it into namespace which uses file system.");
		}

		doc = new XmlDocMaker("args");
		doc.add("fillin", true);
		if (name == null) {
			name = "Migrated_from_" + proxySeriesAssetId;
		}
		doc.add("name", name);
		doc.add("pid", pssdStudyCid);
		doc.add("type", proxySeriesAssetType);
		doc.push("meta");
		if (proxySeriesAssetType.equals(MimeTypes.DICOM_SERIES)) {
			XmlDoc.Element xeMFDicomSeries = r1.element("asset/meta/mf-dicom-series");
			xeMFDicomSeries.add(new XmlDoc.Attribute("ns", "dicom"));
			doc.add(xeMFDicomSeries);
		}
		doc.push("mf-note");
		String note = "M_FROM " + proxySeriesAssetId;
		if (proxySeriesCid != null) {
			note += "," + proxySeriesCid;
		}
		doc.add("note", note);
		doc.pop();
		doc.pop();

		doc.push("subject");
		doc.add("state", 1);
		doc.pop();

		XmlDoc.Element r3 = executor().execute("om.pssd.dataset.primary.create", doc.root());
		String pssdPrimaryDatasetCid = r3.value("id");

		if (proxySeriesAssetContentUrl != null) {
			setAssetContentUrlAndType(pssdPrimaryDatasetCid, proxySeriesAssetContentUrl, proxySeriesAssetContentType);
		}

		return pssdPrimaryDatasetCid;

	}

	private void setAssetContentUrlAndType(String cid, String contentUrl, String contentType) throws Throwable {

		// asset.set :cid $cid :url -by reference $url
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", cid);
		doc.add("url", new String[] { "by", "reference" }, contentUrl);
		doc.add("ctype", contentType);
		executor().execute("asset.set", doc.root());
	}

	private String getMethodCid(String pssdStudyCid) throws Throwable {

		String pssdExMethodCid = CiteableIdUtil.getParentId(pssdStudyCid);
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", pssdExMethodCid);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r = executor().execute("asset.get", doc.root());
		String methodCid = r.value("asset/meta/daris:pssd-ex-method/method");

		return methodCid;

	}


	private void internalizeAssets(String cid, String method) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", cid);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r = executor().execute("asset.get", doc.root());
		if (r.value("asset/meta/daris:pssd-object/type") == null) {
			throw new Exception("asset(cid=" + cid + ") is not a PSSD object.");
		}

		doc = new XmlDocMaker("args");
		doc.add("where", "cid starts with '" + cid + "' and content is external");
		doc.add("size", "infinity");
		doc.add("action", "pipe");
		doc.push("service", new String[] { "name", "asset.internalize" });
		doc.add("method", method);
		doc.pop();
		doc.add("pdist", 0);       // FOrce local
		doc.add("stoponerror", true);
		System.out.println(doc.root());
		executor().execute("asset.query", doc.root());
	}

	void destroyOldStudy(String OldStudyAssetId) throws Throwable {

		// Find the patient asset that goes with this Study

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", OldStudyAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r = executor().execute("asset.get", doc.root());
		Collection<String> PSSSeriesAssetIds = r.values("asset/related[@type='contains']/to");
		if (PSSSeriesAssetIds != null) {
			for (String seriesAssetId : PSSSeriesAssetIds) {
				destroySeries(seriesAssetId);
			}
		}

		destroyAsset(OldStudyAssetId);

	}

	void destroySeries(String seriesAssetId) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", seriesAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r = executor().execute("asset.get", doc.root());
		Collection<String> proxyAssetIds = r.values("asset/related[@type='proxy']/to");
		if (proxyAssetIds != null) {
			for (String proxyAssetId : proxyAssetIds) {
				destroyAsset(proxyAssetId);
			}
		}

		destroyAsset(seriesAssetId);

	}

	void destroyAsset(String assetId) throws Throwable {

		if (assetId == null)
			return;
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", assetId);
		doc.add("imc", "true");
		executor().execute("asset.destroy", doc.root());
	}



	String findDICOMPatientAssetID(String dicomStudyAssetId) throws Throwable {
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", dicomStudyAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r1 = executor().execute("asset.get", doc.root());

		return r1.value("asset/related[@type='had-by']/to");
	}



	String[] getDicomPatientName(String dicomPatientAssetId) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", dicomPatientAssetId);
		doc.add("pdist", 0);       // FOrce local
		XmlDoc.Element r2 = executor().execute("asset.get", doc.root());
		String firstName = r2.value("asset/meta/mf-dicom-patient/name[@type='first']");
		String lastName = r2.value("asset/meta/mf-dicom-patient/name[@type='last']");

		String[] name = null;
		if (firstName != null && lastName != null) {
			name = new String[2];
			name[0] = firstName;
			name[1] = lastName;
		}
		if (name == null) {
			throw new Exception("Patient name is null or incomplete.");
		}

		return name;

	}

	private ArrayList<String>  sortSeriesByDate (Collection<String> assetIDs) {

		ArrayList<String> t = new ArrayList<String>(assetIDs);
		Collections.sort(t, new Comparator<String>() {

			@Override
			public int compare(String ae1, String ae2) {

				Date d1 = null; 
				Date d2 = null; 
				try {
					XmlDoc.Element m1 = AssetUtil.getAsset(executor(), null, ae1);
					d1 = m1.dateValue("asset/meta/mf-dicom-series/sdate");
				} catch (Throwable e) {
				}
				try {
					XmlDoc.Element m2 = AssetUtil.getAsset(executor(), null, ae2);
					d2 = m2.dateValue("asset/meta/mf-dicom-series/sdate");
				} catch (Throwable e) {
				}

				if (d2.after(d1)) return -1;
				if (d1.after(d2)) return 1;
				return 0;
			}
		});
		return t;
	}

}
