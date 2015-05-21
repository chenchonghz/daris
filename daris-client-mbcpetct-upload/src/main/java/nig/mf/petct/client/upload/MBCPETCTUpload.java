package nig.mf.petct.client.upload;

import java.io.File;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import nig.compress.ZipUtil;
import nig.iio.siemens.MBCRawUploadUtil;
import nig.iio.siemens.MBCRawUploadUtil.SUBJECT_FIND_METHOD;
import nig.mf.MimeTypes;
import nig.mf.client.util.ClientConnection;
import nig.mf.client.util.AssetUtil;
import nig.mf.client.util.UserCredential;
import nig.util.DateUtil;
import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.Connection;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;




public class MBCPETCTUpload {

	// The namespace for the original DICOM-based archive
	private static final String MF_NAMESPACE = "MBIC-Archive";
	//
	private static final String RAW_STUDY_DOC_TYPE = "daris:siemens-raw-petct-study";
	private static final String RAW_SERIES_DOC_TYPE = "daris:siemens-raw-petct-series ";
	// This is the MBC IU NAS host as seen from the iMac (where the client is run)
	private static final String DEFAULT_SRC_PATH = "/Volumes/meduser/PET-CT/Raw";
	// This is the SToR file system
	private static final String DEFAULT_LOGGER_PATH = "/Volumes/508-MBIU/PET-CT/Raw_Archive_Upload_Logs";

	// Only one study type as it may hold both PET and CT DataSets
	private static final String[] PSSD_STUDY_TYPES = {"Positron Emission Tomography/Computed Tomography",
		"Positron Emission Tomography","Computed Tomography", "Quality Assurance", "Unspecified"};

	// The authenticating security token must be made with :app == to this string.
	// The token must also hold the appropriate permissions to allow it to access the
	// PSSD Project or namespace into which it is uploading data.
	private static final String TOKEN_APP = "MBIC-PETCT-Raw-Upload";

	// This class sets the defaults for arguments that can be passed in
	// to the main program
	private static class Options {

		public String  path = null;
		public String  logpath = null;
		public boolean chksum = true;
		public boolean delete = true;
		public boolean expire = false;		
		public boolean logger = true;
		public String  id = null;
		public boolean addNIGSubjectMeta = false;
		public boolean decrypt = true;
		public String sleep = null;
		//
		public void print () {
			System.out.println("path          = " + path);
			System.out.println("no-chksum     = " + !chksum);
			System.out.println("no-delete     = " + !delete);
			System.out.println("expire        = " + expire);
			System.out.println("no-log        = " + !logger);
			System.out.println("logpath       = " + logpath);
			System.out.println("id            = " + id);
			//			System.out.println("nig-subject-meta-add = " + addNIGSubjectMeta);
			System.out.println("no-decrypt    = " + !decrypt);
			if (sleep!=null) System.out.println("sleep         = " + sleep + " minutes");
		}
		public void printToWriter (PrintWriter writer) {
			if (writer==null) return;
			writer.println("path          = " + path);
			writer.println("no-chksum     = " + !chksum);
			writer.println("no-delete     = " + !delete);
			writer.println("expire        = " + expire);
			writer.println("no-log        = " + !logger);
			writer.println("logpath       = " + logpath);
			writer.println("id            = " + id);
			//			writer.println("nig-subject-meta-add = " + addNIGSubjectMeta);
			writer.println("no-decrpyt    = " + !decrypt);
			if (sleep!=null) writer.println("sleep         = " + sleep + " minutes");
		}
	}


	public static final String HELP_ARG = "-help";
	public static final String PATH_ARG = "-path";
	public static final String LOGPATH_ARG = "-logpath";
	public static final String NOLOG_ARG = "-no-log";
	public static final String NOCHKSUM_ARG = "-no-chksum";
	public static final String NODELETE_ARG = "-no-delete";
	public static final String EXPIRE_ARG = "-expire";
	public static final String ID_ARG = "-id";
	public static final String NIG_META_ARG = "-nig-subject-meta-add";
	public static final String DECRYPT_ARG = "-no-decrypt";
	public static final String SLEEP_ARG = "-sleep";

	public static final String DEST_ARG = "-dest";  // Consumed by wrapper



	/**
	 * 
	 * the main function of this command line tool.
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) throws Throwable {

		// Parse user inputs
		Options ops = new Options();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase(HELP_ARG)) {
				printHelp();
				System.exit(0);
			} else if (args[i].equalsIgnoreCase(PATH_ARG)) {
				ops.path = args[++i];
			} else if (args[i].equalsIgnoreCase(LOGPATH_ARG)) {
				ops.logpath = args[++i];
			} else if (args[i].equalsIgnoreCase(SLEEP_ARG)) {
				ops.sleep = args[++i];
			} else if (args[i].equalsIgnoreCase(NOCHKSUM_ARG)) {
				ops.chksum = false;
			} else if (args[i].equalsIgnoreCase(NODELETE_ARG)) {
				ops.delete = false;
			} else if (args[i].equalsIgnoreCase(EXPIRE_ARG)) {
				ops.expire = true;
			} else if (args[i].equalsIgnoreCase(NOLOG_ARG)) {
				ops.logger = false;
			} else if (args[i].equalsIgnoreCase(ID_ARG)) {
				ops.id = args[++i];
			} else if (args[i].equalsIgnoreCase(NIG_META_ARG)) {
				ops.addNIGSubjectMeta = true;
			} else if (args[i].equalsIgnoreCase(DECRYPT_ARG)) {
				ops.decrypt = false;
			} else if (args[i].equalsIgnoreCase(DEST_ARG)) {
				// Consumed by wrapper
				i++;
			} else {
				System.err.println("MBCPETUpload: error: unexpected argument = " + args[i]);
				printHelp();
				System.exit(1);	 
			}
		}
		//
		if (ops.path == null) ops.path = DEFAULT_SRC_PATH;
		MBCRawUploadUtil.checkPath (ops.path);

		if (ops.id==null) {
			System.err.println("MBCPETUpload: you must specify the -id argument as usage is now only with the DaRIS data model");
		}

		// Create logger
		PrintWriter logger = MBCRawUploadUtil.createFileLogger(ops.logger, ops.logpath, DEFAULT_LOGGER_PATH);


		if (!ops.chksum) {
			if (ops.delete){
				MBCRawUploadUtil.log (logger, "*** Disabling deletion of input as check sum has been turned off");
				ops.delete = false;
			}
		}
		//
		ops.print();
		ops.printToWriter(logger);
		
		// Have a sleep
		if (ops.sleep!=null) {
			MBCRawUploadUtil.log (logger, "\nSleeping for " + ops.sleep + " minutes");
			Float s = Float.parseFloat(ops.sleep);
			if (s<0) throw new Exception ("Sleep period must be positive");
			s *= 60.0f * 1000.0f;                       // milli seconds
			Integer s2 = s.intValue();
			Thread.sleep(s2);
			MBCRawUploadUtil.log (logger, "   Waking from sleep");
		}

		// Upload data
		upload(logger, ops);
		if (logger!=null) {
			logger.flush();
			logger.close();
		}
	}







	private static void upload (PrintWriter logger, Options ops) throws Throwable {


		// Make connection to MF server 	
		Connection cxn = ClientConnection.createServerConnection();
		UserCredential cred = ClientConnection.connect (cxn, TOKEN_APP, ops.decrypt);
		if (cred==null) {
			throw new Exception ("Failed to extract user credential after authentication");
		}

		// Check CID is for this server
		if (ops.id!=null) {
			nig.mf.pssd.client.util.CiteableIdUtil.checkCIDIsForThisServer(cxn, ops.id, true);
		}


		// Iterate over all files in directory or upload given
		File path = new File(ops.path);
		if (path.isDirectory()) {
			File[] files = path.listFiles();
			if (files.length> 0) { 
				for (int i=0; i<files.length; i++) {
					uploadFile (cxn, files[i], ops, cred, logger);
				}
			} else {
				MBCRawUploadUtil.log (logger,"*** No files to upload in : " + path.toString());
			}

		} else {
			uploadFile (cxn, path, ops, cred, logger);
		}

		// CLose connection
		cxn.close();
	}


	private static void uploadFile (ServerClient.Connection cxn, File path, Options ops,  UserCredential cred, PrintWriter logger) throws Throwable {

		MBCRawUploadUtil.log (logger, "");
		MBCRawUploadUtil.log (logger, "Processing file " + path.toString());

		// Parse directory name
		PETCTMetaData pm = new PETCTMetaData (path);
		try {
			pm.parse();
		} catch (Throwable t) {
			MBCRawUploadUtil.log (logger, "   *** Failed to parse file name into meta-data - skipping file");
			return;
		}
		pm.print();
		pm.printToWriter(logger);

		// Filter out everything but raw data files
		String ext = pm.getExtension();
		if (!ext.equalsIgnoreCase("PTR") && !ext.equalsIgnoreCase("PTD")) {
			MBCRawUploadUtil.log (logger, "   *** File not a raw Siemens file - skipping");
			return;
		}

		// Branch for DICOM or PSSD data model
		if (ops.id==null) {

			// DICOM Data model

			// See if we can find the patient.
			String assetID = MBCRawUploadUtil.findPatientAssetFromDICOM (cxn, MBCRawUploadUtil.SUBJECT_FIND_METHOD.NAME, 
					pm.getFirstName(), pm.getLastName(), null, null, MF_NAMESPACE, logger);

			// Upload
			if (assetID != null) {
				MBCRawUploadUtil.log (logger, "     Patient asset = " + assetID);
				createDICOMAssets (cxn, path, pm, assetID, cred, ops, logger);
			} else {
				// Skip uploading this one
			}
		} else {
			// PSSD Data model

			// See if we can find the subject.  Null if we didn't find it or multiples
			String subjectID = MBCRawUploadUtil.findSubjectAsset (cxn, MF_NAMESPACE, ops.id, ops.addNIGSubjectMeta,
					MBCRawUploadUtil.SUBJECT_FIND_METHOD.NAME, pm.getFirstName(), pm.getLastName(), null, logger);


			// Upload
			if (subjectID != null) {
				MBCRawUploadUtil.log (logger, "     Subject asset = " + subjectID);
				createPSSDAssets (cxn, path, pm, subjectID, cred, ops, logger);
			} else {
				// Skip uploading this one
			}
		}
	}




	private static void setNIGDomainMetaData (ServerClient.Connection cxn, String cid, PETCTMetaData pm) throws Throwable {
		if (pm==null) return;

		String subjectMetaService = "nig.pssd.subject.meta.set";
		XmlStringWriter dm = new XmlStringWriter();
		dm.add("id", cid);

		// Pretend we are using DICOM as the container
		XmlStringWriter w = new XmlStringWriter("dicom");
		w.add("id", cid);
		w.push("dicom");
		w.push("subject");
		// This is how DICOM names end up being manifested <First> <Middle> <Last>
		w.add("name", pm.getFirstName() + " " + pm.getLastName());
		w.pop();
		w.pop();
		if (w!=null) {
			try {
				cxn.execute(subjectMetaService, w.document());
			} catch (Throwable t) {
				// If it fails, we don't want to throw an exception. Just write to logfile
				System.out.println("Failed to set domain-specific subject meta-data with service " + subjectMetaService + " : " + t.getMessage());
			}
		}
	}




	private static String createDICOMAssets (ServerClient.Connection cxn, File file, PETCTMetaData pm, String patientAssetID,
			UserCredential cred, Options ops, PrintWriter logger) throws Throwable {


		// Look for PET/CT raw STudy associated with this Patient
		String rawStudyID = findRawStudy  (cxn, pm, patientAssetID, null);

		// Create Study if needed
		if (rawStudyID==null) {
			rawStudyID = createRawStudy (cxn, pm, patientAssetID, null, cred);
			MBCRawUploadUtil.log (logger, "     Created raw Study asset = " + rawStudyID);
		} else {
			MBCRawUploadUtil.log (logger, "     Study asset = " + rawStudyID);
		}

		// Look for PET/CT raw Series
		String rawSeriesID = findRawSeries  (cxn, pm, rawStudyID, null);

		// Create asset for raw PET data file. Skip if pre-exists
		Boolean chkSumOK = false;
		if (rawSeriesID==null) {
			MBCRawUploadUtil.log (logger, "   Uploading file");
			long tsize = FileUtils.sizeOf(file);
			MBCRawUploadUtil.log (logger, "      File size = " + FileUtils.byteCountToDisplaySize(tsize));
			rawSeriesID = createRawSeries (cxn, file, pm, rawStudyID, null, ops.expire);
			if (rawSeriesID==null) {
				throw new Exception ("Failed to create series asset");
			}
			MBCRawUploadUtil.log (logger, "     Created raw Series asset = " + rawSeriesID);
			if (ops.chksum) {
				MBCRawUploadUtil.log (logger, "        Validating checksum");

				// Get chksum from disk
				MBCRawUploadUtil.log (logger, "           Computing disk file check sum");
				String chkSumDisk = ZipUtil.getCRC32(file, 16);

				// Get chksum from asset
				String chkSumAsset = MBCRawUploadUtil.getCheckSum (cxn, rawSeriesID, null);

				if (chkSumDisk.equalsIgnoreCase(chkSumAsset)) {
					MBCRawUploadUtil.log(logger, "            Checksums match");	
					chkSumOK = true;
				} else {
					MBCRawUploadUtil.log (logger, "       Checksums do not match. Checksums are:");	
					MBCRawUploadUtil.log (logger, "           Input file      = " + chkSumDisk);
					MBCRawUploadUtil.log (logger, "           Mediaflux asset = " + chkSumAsset);
					//
					AssetUtil.destroy(cxn, rawSeriesID, null);
					MBCRawUploadUtil.log (logger, "           Destroyed Mediaflux asset");
				}
			}
		} else {
			MBCRawUploadUtil.log (logger, "     *** Found existing raw Series ID = " + rawSeriesID + " - skipping");
			chkSumOK = true;  // This will trigger a deletion since the file has already been successfully uploaded
		}
		//
		// Clean up
		if (ops.delete && chkSumOK) MBCRawUploadUtil.deleteFile(file, logger);
		//
		return null;
	}


	private static String createPSSDAssets (ServerClient.Connection cxn, File file, PETCTMetaData pm, String subjectCID,
			UserCredential cred, Options ops, PrintWriter logger) throws Throwable {


		// Look for PET/CT raw STudy associated with this Patient
		String rawStudyCID = findRawStudy  (cxn, pm, null, subjectCID);

		// Create Study if needed
		if (rawStudyCID==null) {
			rawStudyCID = createRawStudy (cxn, pm, null, subjectCID, cred);
			MBCRawUploadUtil.log (logger, "     Created raw PSSD Study asset = " + rawStudyCID);
		} else {
			MBCRawUploadUtil.log (logger, "     PSSD Study asset = " + rawStudyCID);
		}

		// Look for PET/CT raw DataSets
		String rawSeriesCID = findRawSeries  (cxn, pm, null, rawStudyCID);

		// Create asset for raw PET data file. Skip if pre-exists
		Boolean chkSumOK = false;
		if (rawSeriesCID==null) {
			MBCRawUploadUtil.log (logger, "   Uploading file");
			long tsize = FileUtils.sizeOf(file);
			MBCRawUploadUtil.log (logger, "      File size = " + FileUtils.byteCountToDisplaySize(tsize));
			rawSeriesCID = createRawSeries (cxn, file, pm, null, rawStudyCID, ops.expire);
			if (rawSeriesCID==null) {
				throw new Exception ("Failed to create PSSD DataSet asset");
			}
			MBCRawUploadUtil.log (logger, "     Created raw PSSD DataSet asset = " + rawSeriesCID);
			if (ops.chksum) {
				MBCRawUploadUtil.log (logger, "        Validating checksum");

				// Get chksum from disk
				MBCRawUploadUtil.log (logger, "           Computing disk file check sum");
				String chkSumDisk = ZipUtil.getCRC32(file, 16);

				// Get chksum from asset
				String chkSumAsset = MBCRawUploadUtil.getCheckSum (cxn, null, rawSeriesCID);

				if (chkSumDisk.equalsIgnoreCase(chkSumAsset)) {
					MBCRawUploadUtil.log(logger, "            Checksums match");	
					chkSumOK = true;
				} else {
					MBCRawUploadUtil.log (logger, "       Checksums do not match. Checksums are:");	
					MBCRawUploadUtil.log (logger, "           Input file      = " + chkSumDisk);
					MBCRawUploadUtil.log (logger, "           Mediaflux asset = " + chkSumAsset);
					//
					AssetUtil.destroy(cxn, null, rawSeriesCID);
					MBCRawUploadUtil.log (logger, "           Destroyed Mediaflux asset");
				}
			}
		} else {
			MBCRawUploadUtil.log (logger, "     *** Found existing raw DataSet ID = " + rawSeriesCID + " - skipping");
			chkSumOK = true;  // This will trigger a deletion since the file has already been successfully uploaded
		}
		//
		// Clean up
		if (ops.delete && chkSumOK) MBCRawUploadUtil.deleteFile(file, logger);
		//
		return null;
	}




	/**
	 * FInd the raw Study in either the DICOM or PSSD data models
	 * 
	 * @param cxn
	 * @param pm
	 * @param patientAssetID
	 * @param subjectCID
	 * @return
	 * @throws Throwable
	 */
	private static String findRawStudy (ServerClient.Connection cxn, PETCTMetaData pm, String patientAssetID, String subjectCID) throws Throwable {
		// FInds by date only as there is no study 'UID'. Only UIDs for Series (data sets).
		XmlStringWriter w = new XmlStringWriter();
		String query = null;
		if (subjectCID!=null) {
			query = "model='om.pssd.study' and cid starts with '" + subjectCID + "'";	
			w.add("action", "get-cid");
		} else {
			query = MBCRawUploadUtil.nameSpaceQuery(MF_NAMESPACE);
			query += " and (related to{had-by} (id=" + patientAssetID + "))";
		}
		query += " and xpath(" + RAW_STUDY_DOC_TYPE + "/date)='" + 
				DateUtil.formatDate(pm.getAcquisitionDateTime(), false, null) + "'";
		w.add("where", query);
		XmlDoc.Element r = cxn.execute("asset.query", w.document());
		if (r==null) return null;
		if (subjectCID!=null) {
			return r.value("cid");
		} else {
			return r.value("id");
		}
	}

	/**
	 * Create the raw study as a DICOM or PSSD data model asset
	 * 
	 * @param cxn
	 * @param pm
	 * @param patientAssetID
	 * @param subjectCID
	 * @param domain
	 * @param user
	 * @return
	 * @throws Throwable
	 */
	private static String createRawStudy (ServerClient.Connection cxn, PETCTMetaData pm, String patientAssetID,
			String subjectCID, UserCredential cred) throws Throwable {

		// Create a study with siemens doc attached
		XmlStringWriter w = new XmlStringWriter();


		if (subjectCID!=null) {

			// Find the ExMethod executing the Method that created this Subject
			String exMethodCID = MBCRawUploadUtil.findExMethod (cxn, subjectCID);
			w.add("pid", exMethodCID);

			// FInd the step in the Method for this Study type
			Vector<String> studyTypes = new Vector<String>();
			studyTypes.add(PSSD_STUDY_TYPES[0]);   // COmbined PET/CT
			if (pm.getModality().equals("PT")) {
				studyTypes.add(PSSD_STUDY_TYPES[1]);
			} else if (pm.getModality().equals("CT")) {
				studyTypes.add(PSSD_STUDY_TYPES[2]);
			}
			studyTypes.add(PSSD_STUDY_TYPES[3]);   // Q/C
			studyTypes.add(PSSD_STUDY_TYPES[4]);   // Unspecified
			String step = MBCRawUploadUtil.getFirstMethodStep (cxn, exMethodCID, studyTypes);
			w.add("step", step);
		} else {
			w.push("related");
			w.add("to", new String[] {"relationship", "had-by"}, patientAssetID);
			w.pop();
			w.add("namespace", MF_NAMESPACE);	

			// Add the asset mime type. 
			w.add("type", MimeTypes.PETCT_RAW_STUDY_MIME_TYPE);
		}


		// Add the meta-data
		w.push("meta");
		w.push(RAW_STUDY_DOC_TYPE);
		//
		String date = DateUtil.formatDate(pm.getAcquisitionDateTime(), false, null);
		w.add("date", date);
		//
		w.push("ingest");
		w.add("date", "now");
		w.add("domain", cred.domain());
		w.add("user", cred.user());
		w.add("from-token", cred.fromToken());
		w.pop();
		w.pop();
		w.pop();
		//
		XmlDoc.Element r = null;
		if (subjectCID!=null) {
			w.add("description", "Raw Siemens data");
			r = cxn.execute("om.pssd.study.create", w.document());
		} else {
			r = cxn.execute("asset.create", w.document());
		}
		return r.value("id");
	}





	private static String findRawSeries (ServerClient.Connection cxn, PETCTMetaData pm, 
			String rawStudyID, String rawStudyCID) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		String query = null;
		if (rawStudyCID!=null) {
			query = "model='om.pssd.dataset' and cid starts with '" + rawStudyCID + "'";	
			w.add("action", "get-cid");
		} else {
			query = MBCRawUploadUtil.nameSpaceQuery(MF_NAMESPACE);
			query += " and (related to{container} (id=" + rawStudyID + "))";
		}
		String q1 = "xpath(" + RAW_SERIES_DOC_TYPE + "/date)='" +
				DateUtil.formatDate(pm.getAcquisitionDateTime(), true, false) + "'";
		String q3 = "xpath(" + RAW_SERIES_DOC_TYPE + "/modality)='" + pm.getModality() + "'";
		String q4 = "xpath(" + RAW_SERIES_DOC_TYPE + "/description)='" + pm.getDescription() + "'";
		String q5 = "xpath(" + RAW_SERIES_DOC_TYPE + "/series_number)='" + pm.getSeriesNumber() + "'";
		String q6 = "xpath(" + RAW_SERIES_DOC_TYPE + "/type)='" + pm.getAcquisitionType() + "'";
		String q7 = "xpath(" + RAW_SERIES_DOC_TYPE + "/uuid)='" + pm.getUUID() + "'";
		String q8 = "xpath(" + RAW_SERIES_DOC_TYPE + "/instance)='" + pm.getInstanceNumber() + "'";

		// We just establish that there is content and that the meta-data is the same
		w.add("where", "asset has content and " + query + " and " + q1 + " and " +
				q3 + " and " + q4 + " and " + q5 + " and " + q6 + " and " +
				q7 + " and " + q8);
		XmlDoc.Element r = cxn.execute("asset.query", w.document());
		if (r==null) return null;
		if (rawStudyCID!=null) {
			return r.value("cid");
		} else {
			return r.value("id");
		}
	}





	private static String createRawSeries (ServerClient.Connection cxn, File path,
			PETCTMetaData pm, String rawStudyID, String rawStudyCID, Boolean expire) throws Throwable {
		// Create a study with siemens doc attached
		XmlStringWriter w = new XmlStringWriter();
		if (rawStudyCID!=null) {
			w.add("pid", rawStudyCID);
			// Defaults to Method and Step of parent Study
		} else {
			w.add("namespace", MF_NAMESPACE);
			w.push("related");
			w.add("to", new String[] {"relationship", "container"}, rawStudyID);
			w.pop();
		}
		w.push("meta");
		w.push(RAW_SERIES_DOC_TYPE);
		//
		String date = DateUtil.formatDate(pm.getAcquisitionDateTime(), true, false);
		w.add("date", date);
		w.add("modality", pm.getModality());
		w.add("description", pm.getDescription());
		w.add("type", pm.getAcquisitionType());
		w.add("series_number", pm.getSeriesNumber());
		w.add("instance", pm.getInstanceNumber());
		w.add("uuid", pm.getUUID());
		w.add("date-export", pm.getExportDateTime());
		//
		if (expire) {
			Calendar c = Calendar.getInstance(); 
			Date t = pm.getAcquisitionDateTime();
			c.setTime(t); 
			c.add(Calendar.YEAR, 1);
			w.add("date-expire", c.getTime());
		}
		//
		w.pop();
		w.pop();

		// DataSet mime type
		if (pm.getModality().equals("CT")) {
			w.add("type", MimeTypes.CT_RAW_SERIES_MIME_TYPE); 
		} else if (pm.getModality().equals("PT")) {
			w.add("type", MimeTypes.PET_RAW_SERIES_MIME_TYPE); 
		}

		// Add content
		ServerClient.Input in = ServerClient.createInputFromURL("file:" + path.getAbsolutePath());

		// Upload
		XmlDoc.Element r = null;
		if (rawStudyCID!=null) {
			w.add("description", "Raw Siemens DataSet");
			w.add("filename", path.getName());          // Original filename
			r = cxn.execute("om.pssd.dataset.primary.create", w.document(), in, null);
		} else {
			r = cxn.execute("asset.create", w.document(), in, null);
		}
		return r.value("id");
	}


	/**
	 * 
	 * prints the help information for this command line tool.
	 * 
	 * @param os
	 * 
	 */
	private static void printHelp() {
		System.out.println("MBCPETUpload");
		System.out.println("");
		System.out.println("Synopsis:");
		System.out.println("   Uploads raw Siemens PET/CT files to Mediaflux.  Assets are associated");
		System.out.println("   with pre-existing DICOM patient assets holding the DICOM images.");
		System.out.println("");
		System.out.println("Usage:");
		System.out.println("   " + MBCPETCTUpload.class.getName() + " [options..]");
		System.out.println("");
		System.out.println("");
		System.out.println("Java Properties:");
		System.out.println("    -mf.host      [Required]: The name or IP address of the Mediaflux host.");
		System.out.println("    -mf.port      [Required]: The server port number.");
		System.out.println("    -mf.token     [Optional]: The security token to authenticate with (preferred).");
		System.out.println("    -mf.user      [Optional]: The logon user (if no token).");
		System.out.println("    -mf.domain    [Optional]: The logon domain (if no token).");
		System.out.println("    -mf.password  [Optional]: The logon user's (obfuscated) password (if no token).");
		System.out.println("    -mf.transport [Optional]: Required if the port number is non-standard.");
		System.out.println("                              One of [HTTP, HTTPS, TCPIP]. By default the");
		System.out.println("                              following transports are inferred from the port:");
		System.out.println("                                80    = HTTP");
		System.out.println("                                443   = HTTPS");
		System.out.println("                                other = TCPIP");
		System.out.println("");
		System.out.println("Options:");
		System.out.println("   " + HELP_ARG + "          Displays this help.");
		System.out.println("   " + PATH_ARG + "          The path for the directory holding the data or a single file. Default is " + DEFAULT_SRC_PATH);
		System.out.println("   " + NOLOG_ARG + "        Disables writing any log file.");
		System.out.println("   " + LOGPATH_ARG + "       Specify the directory for log files to be written in. Default is " + DEFAULT_LOGGER_PATH);
		System.out.println("   " + NOCHKSUM_ARG + "     Disables check sum validation of uploaded file");
		System.out.println("   " + NODELETE_ARG + "     Disables the deletion of the input file after check sum validation");
		System.out.println("   " + EXPIRE_ARG + "        Specifies that meta-data is to be attached to the file with an expiry data of 1 year after acquisition");
		System.out.println("   " + ID_ARG + "            Specifies the PSSD data model (DaRIS) should be used and that this is the citeable ID that the Study should be associated with. Can be depth 2 (the repository), 3 (a Project) or 4 (a Subject).");
		System.out.println("   " + NIG_META_ARG);
		System.out.println("                  Specifies the NIG-domain meta-data should be added to the PSSD Subject");
		System.out.println("   " + DECRYPT_ARG + "    Specifies the password should not be decrypted.");
		System.out.println("   " + SLEEP_ARG +   "         Specifies the amount of time (minutes) to wait before trying to upload data.Some early parsing/checking happens before the sleep is activated.");
		System.out.println("");
		System.out.println("");
	}
}

