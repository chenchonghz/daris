package nig.mf.mr.client.upload;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import nig.compress.ZipUtil;
import nig.iio.siemens.MBCRawUploadUtil;
import nig.mf.MimeTypes;
import nig.mf.client.util.ClientConnection;
import nig.mf.client.util.AssetUtil;
import nig.mf.client.util.UserCredential;
import nig.mf.pssd.CiteableIdUtil;
import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.Connection;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;


public class MBCMRUpload {

	// The namespace for the original DICOM-based archive
	private static final String MF_NAMESPACE = "MBIC-Archive";
	//
	private static final String RAW_STUDY_DOC_TYPE = "daris:siemens-raw-mr-study";
	private static final String RAW_SERIES_DOC_TYPE = "daris:siemens-raw-mr-series";
	// This is the NAS host as seen from the NAS host as the MR client is run on that host
	private static final String DEFAULT_SRC_PATH = "/home/meduser/MR/Raw";
	private static final String DEFAULT_LOGGER_PATH = "/home/meduser/MR/Raw_Archive_Upload_Logs";

	// Only one study type as it may hold both PET and CT DataSets
	private static final String[] PSSD_STUDY_TYPES = {"Magnetic Resonance Imaging", "Quality Assurance", "Unspecified"};


	// The authenticating security token must be made with :app == to this string.
	// The token must also hold the appropriate permissions to allow it to access the
	// PSSD Project or namespace into which it is uploading data.
	private static final String TOKEN_APP = "MBIC-MR-Raw-Upload";


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
		public boolean decrypt = true;
		public boolean bundle = true;              // Bundle files in one Study
		public boolean skip = true;        // Skip files already uploaded
		public String subjectFindMethod = "id";

		// This is the actual Study CID (it's not an input option, it's managed in this application)
		public String rawStudyCID = null;
		//
		public void print () {
			System.out.println("no-bundle     = " + !bundle);
			System.out.println("no-skip       = " + !skip);
			System.out.println("expire        = " + expire);
			System.out.println("find-method   = " + subjectFindMethod);
			System.out.println("id            = " + id);
			System.out.println("logpath       = " + logpath);
			System.out.println("no-chksum     = " + !chksum);
			System.out.println("no-delete     = " + !delete);
			System.out.println("no-log        = " + !logger);
			System.out.println("no-decrypt    = " + !decrypt);
			System.out.println("path          = " + path);
		}
		public void printToWriter (PrintWriter writer) {
			if (writer==null) return;
			writer.println("no-bundle     = " + !bundle);
			writer.println("no-skip       = " + !skip);
			writer.println("expire        = " + expire);
			writer.println("find-methodd  = " + subjectFindMethod);
			writer.println("id            = " + id);
			writer.println("logpath       = " + logpath);
			writer.println("no-chksum     = " + !chksum);
			writer.println("no-delete     = " + !delete);
			writer.println("no-log        = " + !logger);
			writer.println("no-decrypt    = " + !decrypt);
			writer.println("path          = " + path);
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
	public static final String DEST_ARG = "-dest";           // COnsumed in wrapper
	public static final String FIND_ARG = "-find-method";
	public static final String NOBUNDLE_ARG = "-no-bundle";
	public static final String NOSKIP_ARG = "-no-skip";


	/**
	 * 
	 * the main function of this command line tool.
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) throws Throwable {

		for (int i=0;i<args.length;i++) {
			//	System.out.println("arg="+args[i]);
		}
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
			} else if (args[i].equalsIgnoreCase(FIND_ARG)) {
				ops.subjectFindMethod = args[++i];
				if (!(ops.subjectFindMethod).equals("name") || !(ops.subjectFindMethod).equals("id") ||
						!(ops.subjectFindMethod).equals("name+id")) {
					throw new Exception ("Illegal value " + ops.subjectFindMethod + " for argument -find-method");	
				}
			} else if (args[i].equalsIgnoreCase(NOCHKSUM_ARG)) {
				ops.chksum = false;
			} else if (args[i].equalsIgnoreCase(NODELETE_ARG)) {
				ops.delete = false;
			} else if (args[i].equalsIgnoreCase(NOBUNDLE_ARG)) {
				ops.bundle = false;
			} else if (args[i].equalsIgnoreCase(NOSKIP_ARG)) {
				ops.skip = false;
			} else if (args[i].equalsIgnoreCase(EXPIRE_ARG)) {
				ops.expire = true;
			} else if (args[i].equalsIgnoreCase(NOLOG_ARG)) {
				ops.logger = false;
			} else if (args[i].equalsIgnoreCase(ID_ARG)) {
				ops.id = args[++i];
			} else if (args[i].equalsIgnoreCase(DECRYPT_ARG)) {
				ops.decrypt = false;
			} else if (args[i].equalsIgnoreCase(DEST_ARG)) {
				// This argument is consumed by the wrapper script
				i++;
			} else {
				System.err.println("MBCMRUpload: error: unexpected argument = " + args[i]);
				printHelp();
				System.exit(1);	 
			}
		}
		//
		if (ops.path == null) ops.path = DEFAULT_SRC_PATH;
		MBCRawUploadUtil.checkPath (ops.path);

		if (ops.id==null) {
			System.err.println("MBCMRUpload: you must specify the -id argument as usage is now only with the DaRIS data model");
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

		// Check CID is for this server
		nig.mf.pssd.client.util.CiteableIdUtil.checkCIDIsForThisServer(cxn, ops.id, true);

		// CHeck CID ois Subject or Study
		int depth = CiteableIdUtil.getIdDepth(ops.id);
		if (depth!=CiteableIdUtil.STUDY_ID_DEPTH &&
				depth!=CiteableIdUtil.SUBJECT_ID_DEPTH && 
				depth!=CiteableIdUtil.PROJECT_ID_DEPTH) {			
			throw new Exception ("The depth of the supplied id must be 2 (Repository), 3 (Project), 4 (Subject) or (6 Study)");
		}

		// Check is raw Study
		if (depth==CiteableIdUtil.STUDY_ID_DEPTH) {
			XmlDoc.Element meta = AssetUtil.getMeta(cxn, null, ops.id);
			XmlDoc.Element rawMeta = meta.element("asset/meta/daris:siemens-raw-mr-study");
			if (rawMeta==null) {
				throw new Exception ("The supplied destination Study does not appear to be a Siemens Raw MR Study (missing document)");
			}
		}


		// Iterate over all files in directory or upload given
		File path = new File(ops.path);
		if (path.isDirectory()) {
			IOFileFilter fileFilter = new WildcardFileFilter("meas*.dat");
			Collection<File> files = FileUtils.listFiles(path, fileFilter, null);
			if (files.size()>0) {
				for (File file : files) {
					uploadFile (cxn, file, ops, cred, logger);
				}
			} else {
				MBCRawUploadUtil.log (logger,"*** No files to upload in : " + path.toString());
			}
		} else {
			uploadFile (cxn, path, ops,  cred, logger);
		}

		// CLose connection
		cxn.close();
	}


	private static void uploadFile (ServerClient.Connection cxn, File file, Options ops, UserCredential cred, PrintWriter logger) throws Throwable {

		MBCRawUploadUtil.log (logger, "");
		MBCRawUploadUtil.log (logger, "Processing file " + file.toString());

		// Parse subject find method (values checked to be correct earlier)
		MBCRawUploadUtil.SUBJECT_FIND_METHOD method = null;
		if (ops.subjectFindMethod.equals("name")) {
			method = MBCRawUploadUtil.SUBJECT_FIND_METHOD.NAME;
		} else if (ops.subjectFindMethod.equals("id")) {
			method = MBCRawUploadUtil.SUBJECT_FIND_METHOD.ID;
		} else if (ops.subjectFindMethod.equals("name+id")) {
			method = MBCRawUploadUtil.SUBJECT_FIND_METHOD.NAME_ID;
		}

		// Parse file for patient details
		MRMetaData pm = new MRMetaData (file);
		try {
			pm.parse();
		} catch (Throwable t) {
			MBCRawUploadUtil.log (logger, "   *** Failed to parse file meta-data header - skipping file");
			return;
		}
		pm.print();
		pm.printToWriter(logger);

		// Branch for DICOM or PSSD data model
		if (ops.id==null) {

			// DICOM Data model

			// See if we can find the patient.
			String assetID = MBCRawUploadUtil.findPatientAssetFromDICOM (cxn, method, pm.getFirstName(), 
					pm.getLastName(), pm.getID(), null, MF_NAMESPACE, logger);

			// Upload
			if (assetID != null) {
				MBCRawUploadUtil.log (logger, "     Patient asset = " + assetID);
				createDICOMAssets (cxn, file, pm, assetID, cred, ops, logger);
			} else {
				// Skip uploading this one
			}
		} else {
			// PSSD Data model

			String subjectID = null;
			int depth = CiteableIdUtil.getIdDepth(ops.id);

			// Try and find the Subject pre-existing when the CID is for repos,project or subject
			if (depth<CiteableIdUtil.STUDY_ID_DEPTH) {

				// See if we can find the subject.  Null if we didn't find it or multiples
				subjectID = MBCRawUploadUtil.findSubjectAsset (cxn, MF_NAMESPACE, ops.id, false,
						method, pm.getFirstName(), pm.getLastName(), pm.getID(), logger);

				// Upload, creating Study and DataSets
				if (subjectID != null) {
					MBCRawUploadUtil.log (logger, "     Subject asset = " + subjectID);
					createPSSDAssets (cxn, file, pm, subjectID, cred, ops, logger);
				} else {
					// Skip uploading this one. A message will have been issued as to why
				}
			} else {

				// Handle the case where a Study is specified. We have already checked that it's a valid Raw Siemens Study
				// Look for this file already uploaded. 
				// We have no mechanism to find them apart from by name
				String rawSeriesCID = findRawSeries (cxn, file, ops.id);
				if (rawSeriesCID!=null) {
					MBCRawUploadUtil.log (logger, "     *** Found existing raw Series (by name) with ID = '" + rawSeriesCID + "' - skipping");
					return;
				}

				// Upload and create DataSet
				Boolean chkSumOK = false;
				if (rawSeriesCID==null) {
					ops.rawStudyCID = ops.id;
					chkSumOK = createPSSDDataSet (cxn, file, pm, ops, logger);
				} else {
					MBCRawUploadUtil.log (logger, "     *** Found existing raw DataSet ID (by name) = " + rawSeriesCID + " - skipping");
					chkSumOK = true;  // This will trigger a deletion since the file has already been successfully uploaded
				}
				
				// Clean up input file if upload successful
				if (ops.delete && chkSumOK) deleteFile(file, logger);
			}
		}
	}





	private static String createDICOMAssets (ServerClient.Connection cxn, File file, MRMetaData pm, String patientAssetID,
			UserCredential cred, Options ops, PrintWriter logger) throws Throwable {


		// Look for MR raw STudy associated with this Patient
		String rawStudyID = null;//findRawStudy  (cxn, pm, patientAssetID, null);

		// Create Study if needed
		if (rawStudyID==null) {
			rawStudyID = createRawStudy (cxn, pm, patientAssetID, null,  cred);
			MBCRawUploadUtil.log (logger, "     Created raw Study asset = " + rawStudyID);
		} else {
			MBCRawUploadUtil.log (logger, "     Study asset = " + rawStudyID);
		}

		// Look for MR raw Series
		String rawSeriesID = null;//findRawSeries  (cxn, pm, rawStudyID, null);

		// Create asset for raw MR data file. Skip if pre-exists
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
				String chkSumAsset = getCheckSum (cxn, rawSeriesID, null);

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
		if (ops.delete && chkSumOK) deleteFile(file, logger);
		//
		return null;
	}

	private static String createPSSDAssets (ServerClient.Connection cxn, File file, MRMetaData pm, String subjectCID,
			UserCredential cred, Options ops, PrintWriter logger) throws Throwable {

		// Look for MR raw DataSets
		// We have no mechanism to find them apart from by name
		String rawSeriesCID = findRawSeries (cxn, file, ops.id);
		if (rawSeriesCID!=null) {
			MBCRawUploadUtil.log (logger, "     *** Found existing raw Series (by name) with ID = '" + rawSeriesCID + "' - skipping");
			return null;
		}

		// Look for MR raw STudy associated with this Patient
		// There is no mechanism to find a pre-existing Study as there is no date we can
		// use in the meta-data to find one. So either the users chooses to bundle
		// all DataSets into one or not.
		if (ops.bundle){

			// Put all DataSets in one Study
			// Create Study if needed
			if (ops.rawStudyCID==null) {
				ops.rawStudyCID = createRawStudy (cxn, pm, null, subjectCID, cred);
				MBCRawUploadUtil.log (logger, "     Created raw PSSD Study asset = " + ops.rawStudyCID);
			} else {
				MBCRawUploadUtil.log (logger, "     PSSD Study asset = " + ops.rawStudyCID);
			}
		} else {
			// Always make new Study
			ops.rawStudyCID = createRawStudy (cxn, pm, null, subjectCID, cred);
			MBCRawUploadUtil.log (logger, "     Created raw PSSD Study asset = " + ops.rawStudyCID);
		}

		// Create the DataSet
		// Create asset for raw MR data file. Skip if pre-exists
		Boolean chkSumOK = false;
		if (rawSeriesCID==null) {
			chkSumOK = createPSSDDataSet (cxn, file, pm, ops, logger);
		} else {
			MBCRawUploadUtil.log (logger, "     *** Found existing raw DataSet ID (by name) = " + rawSeriesCID + " - skipping");
			chkSumOK = true;  // This will trigger a deletion since the file has already been successfully uploaded
		}
		//
		// Clean up
		if (ops.delete && chkSumOK) deleteFile(file, logger);
		//
		return null;
	}


	private static Boolean  createPSSDDataSet (ServerClient.Connection cxn, File file, MRMetaData pm, 
			Options ops, PrintWriter logger) throws Throwable {

		MBCRawUploadUtil.log (logger, "   Uploading file");
		long tsize = FileUtils.sizeOf(file);
		MBCRawUploadUtil.log (logger, "      File size = " + FileUtils.byteCountToDisplaySize(tsize));
		String rawSeriesCID = createRawSeries (cxn, file, pm, null, ops.rawStudyCID, ops.expire);
		if (rawSeriesCID==null) {
			throw new Exception ("Failed to create PSSD DataSet asset");
		}
		MBCRawUploadUtil.log (logger, "      Created raw PSSD DataSet asset = " + rawSeriesCID);
		Boolean chkSumOK = false;
		if (ops.chksum) {
			MBCRawUploadUtil.log (logger, "        Validating checksum");

			// Get chksum from disk
			MBCRawUploadUtil.log (logger, "           Computing disk file check sum");
			String chkSumDisk = ZipUtil.getCRC32(file, 16);

			// Get chksum from asset
			String chkSumAsset = getCheckSum (cxn, null, rawSeriesCID);

			if (chkSumDisk.equalsIgnoreCase(chkSumAsset)) {
				MBCRawUploadUtil.log(logger, "            Checksums match");	
				chkSumOK = true;
			} else {
				MBCRawUploadUtil.log (logger, "       Checksums do not match. Checksums are:");	
				MBCRawUploadUtil.log (logger, "           Input file      = " + chkSumDisk);
				MBCRawUploadUtil.log (logger, "           Mediaflux asset = " + chkSumAsset);
				//
				MBCRawUploadUtil.log (logger, "           Destroyed Mediaflux asset");
			}
		}
		return chkSumOK;
	}


	private static void deleteFile (File file, PrintWriter logger) throws Throwable {
		try {
			FileUtils.forceDelete(file);
			MBCRawUploadUtil.log (logger, "     Successfully deleted file " + file.getAbsolutePath());
		} catch (Exception e) {
			MBCRawUploadUtil.log (logger, "     *** Failed to delete file " + file.getPath() + " with error " + e.getMessage());
		}
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

	/*
	private static String findRawStudy (ServerClient.Connection cxn, MRMetaData pm, String patientAssetID, String subjectCID) throws Throwable {
		// FInds by date only as there is no study 'UID'. Only UIDs for Series (data sets).
		XmlStringWriter w = new XmlStringWriter();
		String query = null;
		if (subjectCID!=null) {
			query = "model='om.pssd.study' and cid starts with '" + subjectCID + "'";	
			w.add("action", "get-cid");
		} else {
			query = nameSpaceQuery(MF_NAMESPACE);
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
	 */
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

	private static String createRawStudy (ServerClient.Connection cxn, MRMetaData pm, String patientAssetID,
			String subjectCID, UserCredential cred) throws Throwable {

		// Create a study with siemens doc attached
		XmlStringWriter w = new XmlStringWriter();


		if (subjectCID!=null) {

			// Find the ExMethod executing the Method that created this Subject
			String exMethodCID = MBCRawUploadUtil.findExMethod (cxn, subjectCID);
			w.add("pid", exMethodCID);

			// FInd the step in the Method for this Study type
			Vector<String> studyTypes = new Vector<String>();
			for (int i=0; i<PSSD_STUDY_TYPES.length; i++) {
				studyTypes.add(PSSD_STUDY_TYPES[i]);
			}
			String step = MBCRawUploadUtil.getFirstMethodStep (cxn, exMethodCID, studyTypes);
			w.add("step", step);
		} else {
			w.push("related");
			w.add("to", new String[] {"relationship", "had-by"}, patientAssetID);
			w.pop();
			w.add("namespace", MF_NAMESPACE);	

			// Add the asset mime type. 
			w.add("type", MimeTypes.MR_RAW_STUDY_MIME_TYPE);
		}


		// Add the meta-data
		w.push("meta");
		w.push(RAW_STUDY_DOC_TYPE);
		//
		//		String date = DateUtil.formatDate(pm.getAcquisitionDateTime(), false, null);
		//		w.add("date", date);
		//
		w.push("ingest");
		w.add("date", "now");
		w.add("domain", cred.domain());
		w.add("user", cred.user());
		w.add("from-token", cred.fromToken());
		w.pop();
		w.pop();
		w.pop();
		w.add("name", "Raw Siemens MR");


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


	private static String findExMethod (ServerClient.Connection cxn, String subjectCID) throws Throwable {
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

	/**
	 * The only way we can find if the DataSet pre-exists is by its name. Not very robust.
	 * DaRIS data model only
	 * 
	 * @param cxn
	 * @param file
	 * @param sid supplied 
	 * @return
	 * @throws Throwable
	 */
	private static String findRawSeries (ServerClient.Connection cxn, File file,  String sid) throws Throwable {

		// The parent ID could be repository, project, subject or study
		// Look for DataSet with given file name
		XmlStringWriter w = new XmlStringWriter();
		String name = file.getName();                // This is the name of the asset.
		String query = null;
		query = "type='siemens-raw-mr/series' and model='om.pssd.dataset' and cid starts with '" + sid + "'" + 
				" and xpath(daris:pssd-object/name)='" + name + "' and asset has content";
		w.add("action", "get-cid");

		// We just establish that there is content and that the meta-data is the same
		w.add("where", query);
		w.add("pdist", "0");
		XmlDoc.Element r = cxn.execute("asset.query", w.document());
		if (r==null) return null;
		Collection<String> cids = r.values("cid");
		if (cids==null) return null;

		// What to do if we find multiples...  It probably means something has been uploaded
		// wrongly and needs to be remedied (could be ok in a test environment).
		if (cids.size()>1) {
			throw new Exception ("Multiple Raw Siemens DataSets with name '" + name + "' and parent '" + sid + "' were found in the repository. This is most likely an error and needs to be remedied.");
		}
		return r.value("cid");
	}


	private static String createRawSeries (ServerClient.Connection cxn, File path,
			MRMetaData pm, String rawStudyID, String rawStudyCID, Boolean expire) throws Throwable {
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

		/*
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

		w.pop();
		w.pop();
		 */

		// DataSet mime type
		w.add("type", MimeTypes.MR_RAW_SERIES_MIME_TYPE); 


		// Add content
		ServerClient.Input in = ServerClient.createInputFromURL("file:" + path.getAbsolutePath());

		// Upload
		XmlDoc.Element r = null;
		if (rawStudyCID!=null) {
			w.add("description", "Raw Siemens DataSet");
			w.add("filename", path.getName());          // Original filename
			w.add("name", path.getName());              // Original filename
			r = cxn.execute("om.pssd.dataset.primary.create", w.document(), in, null);
		} else {
			r = cxn.execute("asset.create", w.document(), in, null);
		}
		return r.value("id");
	}




	private static String getCheckSum (ServerClient.Connection cxn, String id, String cid) throws Throwable {
		XmlDoc.Element r = nig.mf.client.util.AssetUtil.getMeta(cxn, id, cid);
		return r.value("asset/content/csum");

	}


	private static String nameSpaceQuery (String ns) {
		return "namespace='" + ns + "'";
	}

	/**
	 * 
	 * prints the help information for this command line tool.
	 * 
	 * @param os
	 * 
	 */
	private static void printHelp() {
		System.out.println("MBCMRUpload");
		System.out.println("");
		System.out.println("Synopsis:");
		System.out.println("   Uploads raw Siemens MR 'meas.dat' files to DaRIS/Mediaflux.  Assets should be uploaded");
		System.out.println("   to pre-existing Subjects with extant DICOM images.");
		System.out.println("");
		System.out.println("Usage:");
		System.out.println("   " + MBCMRUpload.class.getName() + " [options..]");
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
		System.out.println("   " + PATH_ARG + "          The path for the directory holding the data (all files matching the pattern 'meas*.dat' will be upload) or a single file. Default is " + DEFAULT_SRC_PATH);
		System.out.println("   " + FIND_ARG + "   Method to find pre-existing subjects; one of 'name', 'id'(default) or 'name+id'");
		System.out.println("   " + NOLOG_ARG + "        Disables writing any log file.");
		System.out.println("   " + LOGPATH_ARG + "       Specify the directory for log files to be written in. Defaults is " + DEFAULT_LOGGER_PATH);
		System.out.println("   " + NOCHKSUM_ARG + "     Disables check sum validation of uploaded file");
		System.out.println("   " + NODELETE_ARG + "     Disables the deletion of the input file after check sum validation");
		System.out.println("   " + EXPIRE_ARG + "        Specifies that meta-data is to be attached to the file with an expiry data of 1 year after acquisition");
		System.out.println("   " + ID_ARG + "            Specifies the PSSD data model (DaRIS) should be used and that this is the citeable ID that the Study should be associated with. Can be depth 3 (a Project), 4 (a Subject) or 6 (a Study).");
		System.out.println("   " + NIG_META_ARG);
		System.out.println("                  Specifies the NIG-domain meta-data should be added to the PSSD Subject");
		System.out.println("   " + DECRYPT_ARG + "    Specifies the password should not be decrypted.");
		System.out.println("   " + NOBUNDLE_ARG + "     Specify that each file uploaded is to have its own parent Study. By default, all files uploaded are bundled under the same Study.");
		System.out.println("   " + NOSKIP_ARG + "       Do not attempt to find DataSets pre-existing (by name); if specified, then a new Study will always be created and all files uploaded to it");
		System.out.println("");
		System.out.println("");
	}
}

