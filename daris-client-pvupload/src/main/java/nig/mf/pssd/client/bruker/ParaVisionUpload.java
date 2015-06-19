package nig.mf.pssd.client.bruker;

import java.io.File;
import java.io.PrintStream;


import nig.iio.bruker.BrukerFileUtils;
import nig.iio.bruker.BrukerMeta;
import nig.mf.MimeTypes;
import nig.mf.client.util.ClientConnection;
import nig.mf.client.util.LogUtil;
import nig.mf.client.util.UserCredential;
import nig.mf.pssd.client.util.CiteableIdUtil;
import nig.util.DateUtil;
import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.Connection;
import arc.mf.client.archive.Archive;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;



public class ParaVisionUpload {

	// This class sets the defaults for arguments that can be passed in
	// to the main program
	public static class Options {

		public boolean verbose = false;
		public long wait = 60000;	
		public int image = 1;                          // Upload image file (0->no, 1->yes)
		public int fid = 2;                            // Upload FID file (0->no, 1->yes in own DataSet, 2->yes with Image DataSet)
		public int clevel = 0;                         // ZipArchiveOutputStream.DEFAULT_COMPRESSION;   Compression level
		// Takes a lot of time to compress data with not much transmission benefit.
		public String cid_delimiter = "_";             // We parse CIDs from <String><delim><cid> in the "SUBJECT_id" field of the SUbject meta-data file.
		// The delimiter, if used, nust be a single character only.
		// If no delimiter is required, set to the string "null". The string is then assume to hold just the CID
		public boolean cid_is_full = false;            // Are we supplying a full cid S.N.P.S or P.S only 
		public boolean auto_subject_create = false;    // Will we allow auto-subject creation ?
		public boolean clone_first_subject = false;    // If auto-creating subjects, make new ones by cloning the first one (if it exists)
		// Otherwise, create with Method specified meta-data
		public boolean nig_subject_meta_add = false;   // Using the aMRIF convention for the "SUBJECT_id", parse the subject identifier and 
		// locate meta data on projects, subjects and studies 
		public String cid = null;                      // Citable ID of destination Subject. If supplied, over-rides that embedded in Subject file.
		public String ctype = "zip";                   // Container type (4GB limit)


		public void print () {
			System.out.println("verbose              = " + verbose);
			System.out.println("wait                 = " + wait);
			System.out.println("image                = " + image);
			System.out.println("fid                  = " + fid);
			System.out.println("clevel               = " + clevel);
			System.out.println("cid                  = " + cid);
			System.out.println("cid_delimiter        = " + cid_delimiter);
			System.out.println("cid_is_full          = " + cid_is_full);
			System.out.println("auto_subject_create  = " + auto_subject_create);
			System.out.println("clone_first_subject  = " + clone_first_subject);
			System.out.println("nig_subject_meta_add = " + nig_subject_meta_add);
			System.out.println("ctype                = " + ctype);
		}
	}

	public static final String HELP_ARG = "--help";
	public static final String VERBOSE_ARG = "-verbose";
	public static final String WAIT_ARG = "-wait";
	public static final String UPLOAD_IMAGE_ARG = "-image";
	public static final String UPLOAD_FID_ARG = "-fid";
	public static final String COMPRESSION_LEVEL_ARG = "-clevel";
	public static final String FID_WITH_IMAGE_ARG = "-fid-with-image";
	public static final String CID_DELIMITER_ARG = "-cid-delimiter";
	public static final String CID_IS_FULL_ARG = "-cid-is-full";
	public static final String AUTO_SUBJECT_CREATE_ARG = "-auto-subject-create";
	public static final String CLONE_FIRST_SUBJECT_ARG = "-clone-first-subject";
	public static final String NIG_SUBJECT_META_ADD_ARG = "-nig-subject-meta-add";
	public static final String CITABLE_ID_ARG = "-id";
	public static final String CTYPE_ARG = "-ctype";

	// User credential after authentication
	private static UserCredential cred_ = null;

	// The authenticating security token must be made with :app == to this string.
	// The token must also hold the appropriate permissions to allow it to access the
	// PSSD Project or namespace into which it is uploading data.
	private static final String TOKEN_APP = "ParaVision-Upload";



	/**
	 * Creates a connection to a remote Mediaflux server.
	 * 
	 * @return the connection
	 * 
	 */
	private static ServerClient.Connection createServerConnection() throws Throwable {

		// Make connection to MF server 	
		Connection cxn = ClientConnection.createServerConnection();
		cred_ = ClientConnection.connect (cxn, TOKEN_APP, false);

		return cxn;
	}

	/**
	 * 
	 * the main function of this command line tool.
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {


		String srcPath = null;
		Options ops = new Options();

		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase(HELP_ARG)) {
				printHelp(System.out);
				System.exit(0);
			} else if (args[i].equalsIgnoreCase(VERBOSE_ARG)) {
				ops.verbose = true;
			} else if (args[i].equalsIgnoreCase(WAIT_ARG)) {
				if (++i == args.length) {
					System.err.println("ParaVisionUpload: error: -wait option must specify a positive integer.");
					System.exit(1);
				}
				try {
					ops.wait = Long.parseLong(args[i]) * 1000;
				} catch (Throwable t) {
					System.err.println("ParaVisionUpload: error: -wait option must specify a positive integer.");
					System.exit(1);
				}
				if (ops.wait < 0) {
					System.err.println("ParaVisionUpload: error: -wait option must specify a positive integer.");
					System.exit(1);
				}
			} else if (args[i].equalsIgnoreCase(UPLOAD_IMAGE_ARG)) {
				if (++i == args.length) {
					System.err.println("ParaVisionUpload: error: -image option must specify a positive integer.");
					System.exit(1);
				}
				try {
					ops.image = Integer.parseInt(args[i]);
				} catch (Throwable t) {
					System.err.println("ParaVisionUpload: error: -image option must specify a positive integer (0 or 1).");
					System.exit(1);
				}
				if (ops.image < 0 || ops.image > 1 ) {
					System.err.println("ParaVisionUpload: error: -image option must specify a positive integer (0 or 1).");
					System.exit(1);
				}
			} else if (args[i].equalsIgnoreCase(UPLOAD_FID_ARG)) {
				if (++i == args.length) {
					System.err.println("ParaVisionUpload: error: -fid option must specify a positive integer.");
					System.exit(1);
				}
				try {
					ops.fid = Integer.parseInt(args[i]);
				} catch (Throwable t) {
					System.err.println("ParaVisionUpload: error: -fid option must specify a positive integer (0, 1 or 2).");
					System.exit(1);
				}
				if (ops.fid < 0 || ops.fid > 2 ) {
					System.err.println("ParaVisionUpload: error: -fid option must specify a positive integer (0, 1, or 2).");
					System.exit(1);
				}
			} else if (args[i].equalsIgnoreCase(COMPRESSION_LEVEL_ARG)) {
				if (++i == args.length) {
					System.err.println("ParaVisionUpload: error: -clevel option must specify a non-negative integer.");
					System.exit(1);
				}
				try {
					ops.clevel = Integer.parseInt(args[i]);
				} catch (Throwable t) {
					System.err.println("ParaVisionUpload: error: -clevel option must specify a positive integer");
					System.exit(1);
				}
			} else if (args[i].equalsIgnoreCase(CITABLE_ID_ARG)) {
				if (++i == args.length) {
					System.err.println("ParaVisionUpload: error: -id option must specify a CID string.");
					System.exit(1);
				}
				ops.cid = args[i];
			} else if (args[i].equalsIgnoreCase(CID_DELIMITER_ARG)) {
				ops.cid_delimiter = args[++i];
				if (ops.cid_delimiter.equals("null")) {
					ops.cid_delimiter = null;
				} else {
					int l = ops.cid_delimiter.length();
					if (l!=1) {
						System.err.println("ParaVisionUpload: error: -cid-delimiter option must specify a single character.");
						System.exit(1);		    		
					}
				}
			} else if (args[i].equalsIgnoreCase(CID_IS_FULL_ARG)) {
				ops.cid_is_full = true;
			} else if (args[i].equalsIgnoreCase(AUTO_SUBJECT_CREATE_ARG)) {
				ops.auto_subject_create= true;
			} else if (args[i].equalsIgnoreCase(CLONE_FIRST_SUBJECT_ARG)) {
				ops.clone_first_subject = true;
			} else if (args[i].equalsIgnoreCase(NIG_SUBJECT_META_ADD_ARG)) {
				ops.nig_subject_meta_add= true;
			} else if (args[i].equalsIgnoreCase(CTYPE_ARG)) {
				ops.ctype = args[++i];
				if (!ops.ctype.equals("zip") && !ops.ctype.equals("tar") && !ops.ctype.equals("aar")) {
					System.err.print("ctype must be one of zip, tar, aar");
					System.exit(1);
				}
			} else if (srcPath == null) {
				srcPath = args[i];
			} else {
				/*
				System.err.println("ParaVisionUpload: error: unexpected argument = " + args[i]);
				printHelp(System.err);
				System.exit(1);
				 */
			}
		}

		// CHeck on fid upload method
		if (ops.fid==2) {
			if (!(ops.image==1)) {
				System.err.println("To upload the fid file into the image DataSet you must also set -image to 1");
				printHelp(System.err);
				System.exit(1);
			}
		}

		if (ops.verbose) ops.print();
		//
		if (srcPath == null) {
			System.err.println("ParaVisionUpload: error: missing argument: src-path");
			printHelp(System.err);
			System.exit(1);
		}

		try {

			// Upload
			File src = new File (srcPath);
			if (!src.exists()) {
				throw new Exception ("The source directory '" + srcPath + "' does not exist");
			}

			// Declare mime types for Archive classes
			Archive.declareSupportForAllTypes();

			// There are a lot of assumptions about the directory structure...
			upload(src.getAbsoluteFile(), ops);

		} catch (Throwable t) {
			System.err.println("ParaVisionUpload: error: " + t.getMessage());
			t.printStackTrace();
			System.exit(1);
		}
	}




	/**
	 * Bruker Directory Hierarchy is
	 * nmr
	 *    <Study (Session) Directory >
	 *      Subject
	 *       <Acquisition Directories>
	 *          imnd
	 *          acqp
	 *          fid
	 *          pdata 
	 *             <Reconstruction Directories>
	 *                 2dseq
	 *                 reco
	 *                 meta
	 * 
	 * @param cxn
	 * @param dir
	 * @param ops
	 * @throws Throwable
	 */

	private static void upload(File dir, Options ops) throws Throwable {

		if (!dir.exists()) {
			throw new Exception("Directory does not exist: " + dir.getAbsolutePath());
		}
		if (BrukerFileUtils.isStudyDir(dir)) {
			if (ops.verbose) System.out.println("Input is study level");
			uploadStudy(dir, ops);
		} else if (BrukerFileUtils.isAcqDir(dir)) {
			if (ops.verbose) System.out.println("Input is acquisition level");
			if (ops.image==1) {
				File[] recoDirs = BrukerFileUtils.getRecoDirs(dir);
				uploadSeries(recoDirs, ops);                         // Handles fid=2
			}
			if (ops.fid==1) {
				File studyDir = BrukerFileUtils.getParentStudyDir(dir);
				File fidFile = BrukerFileUtils.getFidFile(dir);
				uploadFidFile (studyDir, dir, fidFile, ops);				
			}
		} else if (BrukerFileUtils.isPdataDir(dir)) {
			if (ops.verbose) System.out.println("Input is Pdata level");
			if (ops.image==1) {
				File[] recoDirs = BrukerFileUtils.getRecoDirs(dir);
				uploadSeries(recoDirs, ops);
			}
			// Cannot upload fid file from this level
		} else if (BrukerFileUtils.isRecoDir(dir)) {
			System.out.println("Input is reconstruction level");
			if (ops.image==1) {
				uploadSeries(dir, ops);
			}
			// Cannot upload fid file from this level
		} else {
			printHelp(System.out);
			throw new Exception(dir.getAbsolutePath() + " is not a valid Bruker data directory path.");
		}
	}

	/**
	 * Iterate over the acquisition directories (one per 'image' acquisition)
	 * and upload all of the reconstructions for that acquisition
	 * 
	 * @param cxn
	 * @param studyDir
	 * @param ops
	 * @throws Throwable
	 */
	private static void uploadStudy(File studyDir, Options ops) throws Throwable {
		File[] acqDirs = BrukerFileUtils.getAcqDirs(studyDir);
		if (acqDirs != null) {
			for (int i = 0; i < acqDirs.length; i++) {
				if (ops.verbose) {
					System.out.println("   Acquisition " + acqDirs[i].toString());
				}

				// Upload image, possibly with FID as well
				if (ops.image==1) {
					File[] recoDirs = BrukerFileUtils.getRecoDirs(acqDirs[i]);
					uploadSeries(recoDirs, ops);
				}

				// Upload FID file into own DataSet
				if (ops.fid==1) {
					File fidFile = BrukerFileUtils.getFidFile(acqDirs[i]);
					if (fidFile!=null) {
						// Sometimes acquisutions have no data
						uploadFidFile (studyDir, acqDirs[i], fidFile, ops);
					}
				}
			}
		}
	}



	/**
	 * Upload one fid file (the Primary raw data before it is reconstructed) into a Primary DataSet
	 * 
	 * @param cxn
	 * @param studyDir
	 * @param acqDir
	 * @param fidFile
	 * @param ops
	 * 
	 * @throws Throwable
	 */
	private static void uploadFidFile (File studyDir, File acqDir, File fidFile, Options ops) throws Throwable {
		if (ops.verbose) {
			System.out.print("Uploading " + fidFile.getAbsolutePath() + "...");
		}

		// Create a containerfile of the fid file. This is the whole tree but stopping at the "pdata" level
		// as this holds the reconstruction images 
		File container = BrukerFileUtils.createFidContainer(acqDir, ops.clevel, ops.ctype);
		if (ops.verbose){
			System.out.println("Size of fid file = " + container.length());
		}

		// Create the connection to MF. We defer it to now because the creation of large zip
		// files was triggering an MF timeout. So each fid is uploaded with a new connection
		ServerClient.Connection cxn = createServerConnection();


		try {
			//
			// Find and read the Bruker meta-data files that identify the Series (PSSD DataSet) and the Study
			File subjectFile = BrukerFileUtils.getFile(studyDir, "subject");
			if (subjectFile == null) {
				throw new Exception("Could not find subject file.");
			}
			BrukerMeta subjectMeta = new BrukerMeta(subjectFile);

			File acqpFile = BrukerFileUtils.getFile(acqDir, "acqp");
			if (acqpFile == null) {
				throw new Exception("Could not find acqp file.");
			}
			BrukerMeta acqpMeta = new BrukerMeta(acqpFile);

			// Extract the required P or P.S citable ID from the Bruker SUBJECT_id meta-data in the  Bruker subject file
			String objectCID = getObjectCID (cxn, subjectMeta, ops);

			// Parse if the NIG domain meta-data if desired
			NIGBrukerIdentifierMetaData NIGSubjectMeta = null;
			if (ops.nig_subject_meta_add) {
				NIGSubjectMeta = parseNIGSubjectMeta (cxn, objectCID, subjectMeta, ops);
			}

			// Create or find the Subject
			String subjectCID = checkAndCreateSubject (cxn, objectCID, subjectMeta, NIGSubjectMeta, ops);

			// Now see if the user wants to locate any meta-data on the Subject by parsing the
			// Subject identifier further.  This is domain specific and currently only implemented for
			// the FNI small animal facility convention
			if (ops.nig_subject_meta_add) {
				LogUtil.logInfo(cxn, PSSDUtil.BRUKER_LOG_FILE, "Adding nig domain subject meta");
				setNIGDomainMetaData (cxn, subjectCID, NIGSubjectMeta);	


				// Add the Subject ID as the name
				if (NIGSubjectMeta!=null) {
					String name = NIGSubjectMeta.animalID();
					if (name!=null) {
						XmlStringWriter w = new XmlStringWriter();
						w.add("id", subjectCID);
						w.add("name", name);
						w.add("action", "merge");  
						cxn.execute("om.pssd.subject.update", w.document());
					}
				}
			}	

			// Fetch the Project CID and locate any project meta-data on it from
			// the domain-specific meta-data if desired
			String projectCID = nig.mf.pssd.CiteableIdUtil.getProjectId(subjectCID);
			if (ops.nig_subject_meta_add && NIGSubjectMeta!=null) {
				setNIGDomainMetaData (cxn,  projectCID, NIGSubjectMeta);
			}



			// Extract optional P.S.ExM or P.S.ExM.St from the SUBJECT_study_name meta-data in the Bruker subject file 
			String exMethodCID = null;
			String studyCID = null;
			getStudyExMethodCIDs (cxn, subjectMeta, subjectCID, exMethodCID, studyCID, ops);		


			// Search for a pre-existing Study in Mediaflux by the studyUID. We only look for the Study
			// in the CID tree of interest.  If the Study already exists elsewhere, that's fine.
			//
			// (1) If study is found in Mediaflux, update it; 
			// (2) If study is not found in Mediaflux then
			//    (2.1) no studyCID found in SUBJECT_study_name field of Bruker subject file, create a new study.
			//    (2.2) studyCID found in SUBJECT_study_name field of Bruker subject file, it is a pre-created study
			//           (without UID), try update it.
			// Extract some identifying meta-data from the Bruker meta
			Study study = updateOrCreateStudy (cxn, subjectMeta, subjectCID, exMethodCID, studyCID);
			if (studyCID==null) studyCID = study.id();

			// Locate any domain-specific Study meta-data on the STudy
			if (ops.nig_subject_meta_add && NIGSubjectMeta!=null) {
				setNIGDomainMetaData (cxn, studyCID, NIGSubjectMeta);
			}		

			// We have now dealt with the Study.  Move on to the DataSets (Series)
			// Acquisition level meta-data

			// NB the time is in format : e.g. <09:56:25  8 Apr 2010>
			// Convert to standard MF time
			String acqTime = convertAcqTime (acqpMeta);
			String protocol = acqpMeta.getValueAsString("ACQ_protocol_name");
			String seriesDescription = acqpMeta.getValueAsString("ACQ_scan_name");

			// Reconstruction level meta-data. We don't have these for the fid file
			String seriesUID = null;
			String seriesID = null;
			String seriesName = protocol;

			// Search for the Bruker series/DataSet in Mediaflux by seriesUID
			// How can we find a pre-existing fid file without meta-data ???
			//PrimaryDataSet brukerSeries = PrimaryDataSet.find(cxn, studyCID, seriesUID, seriesID);
			PrimaryDataSet brukerSeries = null;

			// Set correct content mime type
			String contentMimeType = setContentMimeType(ops.ctype);

			// Update/upload
			boolean isImage = false;
			if (brukerSeries != null) {

				// if found, update it
				PrimaryDataSet.update(cxn, brukerSeries.id(), seriesName, seriesDescription, seriesUID, seriesID,
						protocol, acqTime, container, contentMimeType);
				LogUtil.logInfo(cxn, PSSDUtil.BRUKER_LOG_FILE, "Bruker primary dataset " + brukerSeries.id() + " has been updated.");
			} else {
				// if not found, create it
				brukerSeries = PrimaryDataSet.create(cxn, isImage, studyCID, seriesName, seriesDescription, seriesUID,
						seriesID, protocol, acqTime, container, contentMimeType);
				LogUtil.logInfo(cxn, PSSDUtil.BRUKER_LOG_FILE, "Bruker series/dataset " + brukerSeries.id() + " has been created.");
			}

			// Search for DICOM derivation series/dataset in Mediaflux.
			// The chain should be Primary (fid) -> Derived (bruker) -> Derived (DICOM)

			//DerivationDataSet dicomSeries = DerivationDataSet.find(cxn, studyCID, seriesUID, seriesID);
			//if (dicomSeries != null) {

			// if found, set/update primary on the DICOM derivation series/dataset.
			//DerivationDataSet.setPrimary(cxn, dicomSeries.id(), brukerSeries.id());
			//PSSDUtil.logInfo(cxn, "DICOM derivation dataset/series " + dicomSeries.id()
			//		+ " has set primary to Bruker primary dataset/series " + brukerSeries.id());
			//}
			if (ops.verbose) {
				System.out.println("done.");
			}
		} finally {
			container.delete();
			cxn.close();
		}
	}


	private static String setContentMimeType (String ctype) {
		if (ctype.equals("zip")) {
			return MimeTypes.ZIP;
		} else if (ctype.equals("tar")) {
			return MimeTypes.TAR;
		} else if (ctype.equals("aar")) {
			return MimeTypes.AAR;
		} else {
			return null;
		}
	}

	/**
	 * Upload Series (volumes) in the given reconstruction directories. One volume per reconstruction directory.
	 * 
	 * @param cxn
	 * @param recoDirs
	 * @param ops
	 * @parma doImage If true upload the Bruker image reconstruction
	 * @throws Throwable
	 */
	private static void uploadSeries(File[] recoDirs, Options ops) throws Throwable {
		if (recoDirs != null) {
			for (int j = 0; j < recoDirs.length; j++) {
				File recoDir = recoDirs[j];
				uploadSeries(recoDir, ops);
			}
		}
	}

	/**
	 * Upload one Series (Volume) from  one reconstruction directory into one DataSet
	 * 
	 * @param cxn
	 * @param recoDir
	 * @param ops
	 * 
	 * @throws Throwable
	 */

	private static void uploadSeries(File recoDir, Options ops) throws Throwable {
		if (ops.verbose) {
			System.out.print("Uploading " + recoDir.getAbsolutePath() + "...");
		}

		// Create a container file that includes the image volume and required meta files.
		boolean fidWithImage = (ops.fid==2);
		//
		File container = BrukerFileUtils.createImageSeriesContainer(recoDir, fidWithImage, ops.clevel, ops.ctype);
		if (ops.verbose) {
			System.out.println("Finished creating container file of size " + container.length());
		}

		// Create the connection to MF. We defer it to now because the creation of large zip
		// files was triggering an MF timeout. So each Series is uploaded with a new connection
		ServerClient.Connection cxn = createServerConnection();


		// Read Bruker meta-data files	
		try {

			// Find and read the Bruker meta-data files that identify the Series (PSSD DataSet) and the Study
			File subjectFile = BrukerFileUtils.getSubjectFile(recoDir);
			if (subjectFile == null) {
				throw new Exception("Could not find subject file.");
			}
			BrukerMeta subjectMeta = new BrukerMeta(subjectFile);
			//System.out.println(subjectMeta.toString());

			File acqpFile = BrukerFileUtils.getAcqpFile(recoDir);
			if (acqpFile == null) {
				throw new Exception("Could not find acqp file.");
			}
			BrukerMeta acqpMeta = new BrukerMeta(acqpFile);

			// Sometimes the reco file (reconstruction parameters is missing).  We only get the UID out of it
			// so can live without it.
			File recoFile = BrukerFileUtils.getRecoFile(recoDir);
			BrukerMeta recoMeta = null;
			if (recoFile == null) {
				LogUtil.logInfo(cxn, PSSDUtil.BRUKER_LOG_FILE, "Skipping reconstruction as could not find reco file in " + recoDir.getAbsolutePath());
				container.delete();
				cxn.close();
				return;
				// throw new Exception("Could not find reco file in " + recoDir.getAbsolutePath());
			} else {
				recoMeta = new BrukerMeta(recoFile);
			}

			// Extract the required P.S or P citable ID from the Bruker SUBJECT_id meta-data in the  Bruker subject file
			String objectCID = getObjectCID (cxn, subjectMeta, ops);

			// Parse if the NIG domain meta-data if desired
			NIGBrukerIdentifierMetaData NIGSubjectMeta = null;
			if (ops.nig_subject_meta_add) {
				NIGSubjectMeta = parseNIGSubjectMeta (cxn, objectCID, subjectMeta, ops);
			}

			// See if the Subject already exists.  If not and if requested, auto-create
			String subjectCID = checkAndCreateSubject (cxn, objectCID, subjectMeta, NIGSubjectMeta, ops);

			// Now see if the user wants to locate any meta-data on the Subject by parsing the
			// Subject identifier further.  This is domain specific and currently only implemented for
			// the FNI small animal facility SUBJECT_ID coded string convention
			if (ops.nig_subject_meta_add) {
				LogUtil.logInfo(cxn,PSSDUtil.BRUKER_LOG_FILE, "Adding nig domain subject meta");
				setNIGDomainMetaData (cxn, subjectCID, NIGSubjectMeta);	

				// Add the Subject ID as the name
				if (NIGSubjectMeta!=null) {
					String name = NIGSubjectMeta.animalID();
					if (name!=null) {
						XmlStringWriter w = new XmlStringWriter();
						w.add("id", subjectCID);
						w.add("name", name);
						w.add("action", "merge");  
						cxn.execute("om.pssd.subject.update", w.document());
					}
				}
			}

			// Fetch the Project CID and locate any project meta-data on it from
			// the domain-specific meta-data if desired
			String projectCID = nig.mf.pssd.CiteableIdUtil.getProjectId(objectCID);
			if (ops.nig_subject_meta_add && NIGSubjectMeta!=null) {
				setNIGDomainMetaData (cxn,  projectCID, NIGSubjectMeta);
			}

			// Extract optional P.S.ExM or P.S.ExM.St from the SUBJECT_study_name meta-data in the Bruker subject file 
			String exMethodCID = null;
			String studyCID = null;
			getStudyExMethodCIDs (cxn, subjectMeta, subjectCID, exMethodCID, studyCID, ops);		


			// Search for a pre-existing Study in Mediaflux by the studyUID. We only look for the Study
			// in the CID tree of interest.  If the Study already exists elsewhere, that's fine.
			//
			// (1) If study is found in Mediaflux, update it; 
			// (2) If study is not found in Mediaflux then
			//    (2.1) no studyCID found in SUBJECT_study_name field of Bruker subject file, create a new study.
			//    (2.2) studyCID found in SUBJECT_study_name field of Bruker subject file, it is a pre-created study
			//           (without UID), try update it.
			// Extract some identifying meta-data from the Bruker meta
			Study study = updateOrCreateStudy (cxn, subjectMeta, subjectCID, exMethodCID, studyCID);
			if (studyCID==null) studyCID = study.id();

			// Locate any domain-specific Study meta-data on the STudy
			if (ops.nig_subject_meta_add && NIGSubjectMeta!=null) {
				setNIGDomainMetaData (cxn,  studyCID, NIGSubjectMeta);
			}		

			// We have now dealt with the Study.  Move on to the DataSets (Series)
			// Acquisition level meta-data

			// NB the time is in format : e.g. <09:56:25  8 Apr 2010>
			// Convert to standard MF time
			String acqTime = convertAcqTime (acqpMeta);
			String protocol = acqpMeta.getValueAsString("ACQ_protocol_name");
			String seriesDescription = acqpMeta.getValueAsString("ACQ_scan_name");

			// Reconstruction level meta-data
			String seriesID = "" + BrukerFileUtils.getSeriesId(recoDir);
			String seriesUID = null;
			if (recoMeta!=null) {
				seriesUID = recoMeta.getValueAsString("RECO_base_image_uid");
			}

			// Combination meta-data
			String seriesName = BrukerFileUtils.getParentAcqDir(recoDir).getName() + "_" + protocol;

			// Search for the Bruker series/DataSet in Mediaflux by seriesUID
			PrimaryDataSet brukerSeries = PrimaryDataSet.find(cxn, studyCID, seriesUID, seriesID);

			// Set correct content mime type
			String contentMimeType = setContentMimeType(ops.ctype);


			// Update/upload
			boolean isImage = true;
			if (brukerSeries != null) {
				if (ops.verbose) {
					System.out.println("Updating DataSet " + brukerSeries.id());
				}

				// if found, update it
				PrimaryDataSet.update(cxn, brukerSeries.id(), seriesName, seriesDescription, seriesUID, seriesID,
						protocol, acqTime, container, contentMimeType);
				LogUtil.logInfo(cxn, PSSDUtil.BRUKER_LOG_FILE, "Bruker primary FID dataset " + brukerSeries.id() + " has been updated.");
			} else {
				if (ops.verbose) {
					System.out.println("Creating DataSet ");
				}

				// if not found, create it
				brukerSeries = PrimaryDataSet.create(cxn, isImage, studyCID, seriesName, seriesDescription, seriesUID,
						seriesID, protocol, acqTime, container, contentMimeType);
				LogUtil.logInfo(cxn, PSSDUtil.BRUKER_LOG_FILE, "Bruker primary FID dataset " + brukerSeries.id() + " has been created.");
			}
			if (ops.verbose) {
				System.out.println("Finished creating/updating DataSet " + brukerSeries.id());
			}



			// The chain should be Primary (fid) -> Derived (bruker) -> Derived (DICOM)
			// Search for DICOM derivation series/dataset in Mediaflux.
			DerivationDataSet dicomSeries = DerivationDataSet.find(cxn, studyCID, seriesUID, seriesID);
			if (dicomSeries != null) {

				// if found, set/update primary on the DICOM derivation series/dataset.
				DerivationDataSet.setPrimary(cxn, dicomSeries.id(), brukerSeries.id());
				LogUtil.logInfo(cxn, PSSDUtil.BRUKER_LOG_FILE, "DICOM derivation dataset/series " + dicomSeries.id()
						+ " has set primary to Bruker primary dataset/series " + brukerSeries.id());
			}
			if (ops.verbose) {
				System.out.println("done.");
			}
		} finally {
			container.delete();
			cxn.close();
		}
	}


	private static String checkAndCreateSubject (ServerClient.Connection cxn, String objectCID, BrukerMeta subjectMeta, 
			NIGBrukerIdentifierMetaData NIGSubjectMeta, Options ops) throws Throwable {

		if (CiteableIdUtil.isProjectId(objectCID)) {
			// The CID provided is a Project.

			// Try to locate the subject pre-existing. We can only find the Subject is pre-existing
			// by its meta-data. The only useful meta-data to do this with is if there is domain
			// specific meta-data like for the NIG definition (this specification of domain specific
			// meta-data needs to be abstracted into  framework)

			String subjectCID = null;
			if (ops.nig_subject_meta_add) {
				subjectCID = findSubject (cxn, NIGSubjectMeta, objectCID);
				if (subjectCID!=null) {
					LogUtil.logInfo(cxn, PSSDUtil.BRUKER_LOG_FILE, "Found pre-existing Subject " + subjectCID);
					return subjectCID;
				}
			}

			// If not found (we may have no means to find it)  create new under the parent if allowed
			return createSubject (cxn, objectCID, subjectMeta, ops);	
		} else {
			// The CID provided is a Subject.  
			if (!Subject.exists(cxn, objectCID)) {
				// Create if does not exist
				createSubject (cxn, objectCID, subjectMeta, ops);	
				return objectCID;
			} else {
				return objectCID;
			}
		}
	}


	private static String findSubject (ServerClient.Connection cxn, NIGBrukerIdentifierMetaData NIGSubjectMeta, String projectCID) throws Throwable {

		// The only way we can find subjects is via the domain specific meta-data that
		// is parsed from the Subject_ID string in the subject file.  For the NIG
		// this pattern is specified, parsed and located on the Subject

		String subjectID = NIGSubjectMeta.animalID();
		XmlStringWriter dm = new XmlStringWriter();
		String query = "cid starts with '" + projectCID + "'";
		query += " and model='om.pssd.subject' and xpath(nig-daris:pssd-amrif-subject/id)='" + subjectID + "'";
		dm.add("where", query);
		dm.add("action", "get-cid");
		dm.add("pdist", "0");
		XmlDoc.Element r = cxn.execute("asset.query", dm.document());
		String cid = r.value("cid");     // First if many :-(
		return cid;	
	}

	private static String createSubject (ServerClient.Connection cxn, String objectCID, BrukerMeta subjectMeta, Options ops) throws Throwable {
		if (ops.auto_subject_create) {

			// Auto-create Subject

			boolean isSubject = true;
			if (CiteableIdUtil.isSubjectId(objectCID)) {
				// If the CID is for a subject, try to clone from the first into the CID (and allocate it)
				if (ops.clone_first_subject) {
					String subjectCID = cloneFirstSubject (cxn, objectCID);
					if (subjectCID!=null) return subjectCID;
				}
			} else {
				isSubject = false;
			}

			// We didn't clone.  Try to create the Subject either under the given Project or
			// importing the given Subject CID
			String subjectCID = nig.mf.pssd.client.util.PSSDUtil.createSubject (cxn, PSSDUtil.BRUKER_LOG_FILE, objectCID);
			if (subjectCID==null) {
				if (isSubject) {
					String errMsg = "Failed to auto-create the Subject with CID " + objectCID + 
							" (SUBJECT_id=" + subjectMeta.getValueAsString("SUBJECT_id") + ")";
					LogUtil.logError(cxn, PSSDUtil.BRUKER_LOG_FILE, errMsg);
					throw new Exception(errMsg);	
				} else {
					String errMsg = "Failed to auto-create the Subject under project CID " + objectCID + 
							" (SUBJECT_id=" + subjectMeta.getValueAsString("SUBJECT_id") + ")";
					LogUtil.logError(cxn, PSSDUtil.BRUKER_LOG_FILE, errMsg);
					throw new Exception(errMsg);	

				}
			} else {
				return subjectCID;
			}
		} else {
			// Does not exist and not allowed to auto-create
			String errMsg = "No existing Subject object found for the citable ID " + objectCID;
			LogUtil.logError(cxn, PSSDUtil.BRUKER_LOG_FILE, errMsg);
			throw new Exception(errMsg);
		}
	}


	/**
	 * Get object CID. If does not exist, optionally try to create
	 * 
	 * @param cxn
	 * @param subjectMeta
	 * @param ops
	 * @return
	 * @throws Throwable
	 */
	private static String getObjectCID (ServerClient.Connection cxn, BrukerMeta subjectMeta, Options ops) throws Throwable {

		// Externally supplied SUbject ID over-rides values found in meta-data
		String objectCID = null;
		if (ops.cid != null) {
			objectCID = ops.cid;
		} else {
			// Get the Subject CID from the SUBJECT_ID expected to be of the form <String><delim><CID>
			// Strip off leading characters ahead of the delimiter if requested
			objectCID = subjectMeta.getValueAsString("SUBJECT_id");

			// If the cid delimiter is null, we expect that SUBJECT_id holds the CID directly.
			if (ops.cid_delimiter!=null) {
				objectCID = extractCID (ops, objectCID);

				// Check what we extracted is a CID
				if (!CiteableIdUtil.isCiteableId(objectCID)) {
					throw new Exception ("The string '" + objectCID + "' parsed from the subject file is not a valid Citable Identifier");
				}
			}
		}

		// If the CID is not full, add on the root.
		String projectIDRoot = CiteableIdUtil.getProjectIdRoot(cxn);
		if (!ops.cid_is_full) objectCID = projectIDRoot + "." + objectCID;			
		//
		System.out.println("cid = " + objectCID);

		// We can handle a Project or a Subject CID.
		if (! (CiteableIdUtil.isSubjectId(objectCID) || CiteableIdUtil.isProjectId(objectCID) )) {
			String errMsg = null;
			if (ops.cid!=null) {
				errMsg = "The citable ID " + objectCID + " is not a valid Project or Subject citable ID";
			} else {
				errMsg = "No valid Project or Subject citeable id found in SUBJECT_id field of the Bruker subject file."
						+ " (SUBJECT_id=" + subjectMeta.getValueAsString("SUBJECT_id") + ")";
			}
			//			LogUtil.logError(cxn, PSSDUtil.BRUKER_LOG_FILE, errMsg);
			throw new Exception(errMsg);
		}



		return objectCID;
	}


	private static NIGBrukerIdentifierMetaData parseNIGSubjectMeta  (ServerClient.Connection cxn, String subjectCID, BrukerMeta subjectMeta, Options ops) throws Throwable {

		NIGBrukerIdentifierMetaData brukerMeta = null;

		// Get the full subject identifier
		String fullID = subjectMeta.getValueAsString("SUBJECT_id");

		// Parse into NIG bits
		// TODO: reimplement with a pattern specification like the Shopping Cart
		// <Project Description>_<Coil>_<Animal ID>_<Gender>_<Experiment Group>_<Invivo/exvivo>_<date><delim><cid>

		// We have already handled the CID - either it was supplied directly or extracted
		// as the last token of the SUBJECT_id string.  So now remove the CID, if it exists
		// from the SUbject ID value. The only safe way to do this  is by working backwards
		// since the delimiter may be embedded elsewhere in the strings
		String id = fullID;

		// If the cid was not supplied externally then it must be in the Subject_ID field.
		// If it is and a delimiter (<string><delim><cid>) was supplied then pull the trailing CID off.
		// If a delimiter was not supplied, it means that the SUBJECT_id holds only the CID so there
		// is no meta-data to parse.
		if (ops.cid==null) {
			if (ops.cid_delimiter!=null) {  
				id = removeCID (ops.cid_delimiter, fullID);
			} else {
				String errMsg = "No CID delimiter was supplied and therefore the SUBJECT_id field holds only a CID and therefore cannot hold any parseable meta-data";
				LogUtil.logError(cxn, PSSDUtil.BRUKER_LOG_FILE, errMsg);
				return null;
			}
		}

		// OK we think we have some parseable meta-data
		// Split into bits around the "_" delimiter (not necessarily the same as ops.cid_delimiter)
		String[] parts = id.split("_");
		if (parts.length==7) {

			// Parse the identifier meta-data into a container
			brukerMeta = new NIGBrukerIdentifierMetaData(parts);
		} else {
			// Not the correct format
			String errMsg = "The subject identifier was not of the correct form to extract the NIG meta-data";
			LogUtil.logError(cxn, PSSDUtil.BRUKER_LOG_FILE, errMsg);
		}	

		return brukerMeta;
	}


	private static void setNIGDomainMetaData (ServerClient.Connection cxn, String cid, NIGBrukerIdentifierMetaData brukerMeta) throws Throwable {
		if (brukerMeta==null) return;

		String subjectMetaService = "nig.pssd.subject.meta.set";
		XmlStringWriter dm = new XmlStringWriter();
		dm.add("id", cid);

		// Convert container to XML.  The parent is "bruker" ready to be added to the "bruker" element of the service.
		XmlDoc.Element m = brukerMeta.toXML();
		if (m!=null) {
			dm.add(m);
			try {
				cxn.execute(subjectMetaService, dm.document());
			} catch (Throwable t) {
				// If it fails, we don't want to throw an exception. Just write to logfile
				System.out.println("Failed to set domain-specific subject meta-data with service " + subjectMetaService + " : " + t.getMessage());
			}
		}
	}

	/**
	 * Function to try to auto-create a Subject of the given CID, if it is of the correct depth
	 * 
	 * 
	 * @param cid
	 * @param sm
	 * @return
	 * @throws Throwable
	 */
	private static String cloneFirstSubject (ServerClient.Connection cxn, String subjectCID) throws Throwable {

		// CHeck CID depth
		if (!nig.mf.pssd.CiteableIdUtil.isSubjectId(subjectCID)) return null;

		// Get Project
		String projectCID = nig.mf.pssd.CiteableIdUtil.getProjectId(subjectCID);

		// Find the first subject; if none, return for auto-create from Method meta-data
		String firstSubject = findFirstSubject (cxn, projectCID);
		if (firstSubject==null) {
			return null;
		}

		// CLone it
		String subjectNumber = nig.mf.pssd.CiteableIdUtil.getLastSection(subjectCID);
		XmlStringWriter w = new XmlStringWriter();
		w.add("id", firstSubject);
		w.add("subject-number", subjectNumber);
		cxn.execute("om.pssd.subject.clone", w.document());


		// Remove any meta-data from the clone that the domain-specific layer might have set 
		// The correct domain-specific data will be set subsequently when the data are actually
		// uploaded.  These meta-data are subject specific so it's not correct that they
		// be cloned
		w = new XmlStringWriter();
		w.add("id", subjectCID);
		w.add("remove", "true");
		w.push("bruker");
		w.pop();
		try {
			cxn.execute("nig.pssd.subject.meta.set", w.document());
		} catch (Throwable t) {
			System.err.print(t.getMessage());
			// Do nothing if it bombs
		}
		return subjectCID;
	}


	private static String findFirstSubject (ServerClient.Connection cxn, String pid) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("id", pid);
		XmlDoc.Element r = cxn.execute("om.pssd.collection.member.list", w.document());
		if (r==null) return null;
		//
		return r.value("object/id");     // Returns the first one
	}




	private static void getStudyExMethodCIDs (ServerClient.Connection cxn, BrukerMeta subjectMeta, 
			String subjectCID, String exMethodCID, String studyCID, Options ops) throws Throwable  {

		String projectIDRoot = CiteableIdUtil.getProjectIdRoot(cxn);
		//String cid2 = projectIDRoot + "." + subjectMeta.getValueAsString("SUBJECT_study_name");

		String cid2 =  subjectMeta.getValueAsString("SUBJECT_study_name");
		if (ops.cid_delimiter!=null) cid2 = extractCID(ops, cid2);
		if (!ops.cid_is_full) cid2 = projectIDRoot + "." + cid2;
		if (CiteableIdUtil.isCiteableId(cid2) && cid2.startsWith(subjectCID)) {
			if (CiteableIdUtil.isStudyId(cid2)) {
				studyCID = cid2;
				exMethodCID = nig.mf.pssd.CiteableIdUtil.getParentId(studyCID);
			} else if (CiteableIdUtil.isExMethodId(cid2)) {
				exMethodCID = cid2;
			}
		}
	}

	/**
	 * Parse <String><delim><cid> and return <cid>
	 * The <String> may contain the delimiter as well of course
	 * 
	 * @param ops
	 * @param cid
	 * @return
	 * @throws Throwable
	 */



	private static String extractCID (Options ops, String cid) throws Throwable {
		String[] t = cid.split(ops.cid_delimiter);  
		int n = t.length;
		if (n>=1) cid = t[n-1];
		return cid;
	}

	// Pull of the CID
	// Delimiter already checked to be a single character
	private static String removeCID (String delimiter, String cid) throws Throwable {
		int idx = cid.lastIndexOf(delimiter);
		//
		String id = cid;
		if (idx>=0) {
			id = cid.substring(0, idx);
		}
		return id;
	}



	private static Study updateOrCreateStudy (ServerClient.Connection cxn, BrukerMeta subjectMeta, String subjectCID, String exMethodCID, String studyCID) throws Throwable {

		// Search for a pre-existing Study in Mediaflux by the studyUID. We only look for the Study
		// in the CID tree of interest.  If the Study already exists elsewhere, that's fine.
		//
		// (1) If study is found in Mediaflux, update it; 
		// (2) If study is not found in Mediaflux then
		//    (2.1) no studyCID found in SUBJECT_study_name field of Bruker subject file, create a new study.
		//    (2.2) studyCID found in SUBJECT_study_name field of Bruker subject file, it is a pre-created study
		//           (without UID), try update it.
		// Extract some identifying meta-data from the Bruker meta
		String studyID = subjectMeta.getValueAsString("SUBJECT_study_nr");
		String studyUID = subjectMeta.getValueAsString("SUBJECT_study_instance_uid");
		String studyName = PSSDUtil.BRUKER_STUDY_TYPE;
		Study study = Study.find(cxn, subjectCID, exMethodCID, studyUID);       // Restrict query to CID tree
		if (study == null) {
			// Can't find the Bruker study pre-existing
			if (studyCID != null) {

				// There is a pre-created Study in MF but since we did not find the Bruker study by 
				// UID we just update the Study with the Bruker meta-data
				study = Study.update(cxn, studyCID, studyName, null, studyUID, studyID, cred_);
				LogUtil.logInfo(cxn, PSSDUtil.BRUKER_LOG_FILE, "Bruker study " + study.id() + " has been updated.");
			} else if (exMethodCID != null) {

				// Auto-create Study with given parent ExMethod and set Bruker meta-data
				study = Study.create(cxn, exMethodCID, studyName, null, studyUID, studyID, cred_);
				LogUtil.logInfo(cxn, PSSDUtil.BRUKER_LOG_FILE, "Bruker study " + study.id() + " has been created.");
			} else {

				// Auto-create Study with first ExMethod of Subject and set Bruker meta-data
				study = Study.createFromSubjectCID(cxn, subjectCID, studyName, null, studyUID, studyID, cred_);
				LogUtil.logInfo(cxn, PSSDUtil.BRUKER_LOG_FILE, "Bruker study " + study.id() + " has been created.");
			}
		} else {

			// Found the Study UID pre-existing in MF in the desired CID tree.  
			// Update it with Bruker meta-data
			study = Study.update(cxn, study.id(), study.name(), study.description(), studyUID, studyID, cred_);
			LogUtil.logInfo(cxn, PSSDUtil.BRUKER_LOG_FILE, "Bruker study " + studyCID + " has been updated.");
		}
		return study;
	}

	private static String convertAcqTime (BrukerMeta acqpMeta) throws Throwable {

		String t = acqpMeta.getValueAsString("ACQ_time"); 
		String acqTime = null;
		try {
			acqTime = DateUtil.convertDateString(t, "HH:mm:ss dd MMM yyyy", "dd-MMM-yyyy HH:mm:ss");
		} catch (Exception e) {
			// Better to have no time than wrong time
			acqTime = null;
		}
		return acqTime;
	}

	/**
	 * 
	 * prints the help information for this command line tool.
	 * 
	 * @param os
	 * 
	 */

	public static void printHelp(PrintStream os) {
		os.println("ParaVisionUpload");
		os.println();
		os.println("Synopsis:");
		os.println("   Packages up ParaVision study, experiment or processed experiment");
		os.println("   data into a ZIP archive and associates with the corresponding DICOM");
		os.println("   series in a Mediaflux server.");
		os.println();
		os.println("Usage:");
		os.println("   " + ParaVisionUpload.class.getName() + " [options..] <src-path>");
		os.println();
		os.println("     src-path is a file path (absolute or relative) for ParaVision, of the following form:");
		os.println("       study      - <path>/nmr/<name>");
		os.println("       experiment - <path>/nmr/<name>/<expno>");
		os.println("       processed  - <path>/nmr/<name>/<expno>/pdata/<procno>");
		os.println();
		os.println("Java Properties:");
		os.println("    -mf.host      [Required]: The name or IP address of the Mediaflux host.");
		os.println("    -mf.port      [Required]: The server port number.");
		os.println("    -mf.domain    [Required]: The logon domain.");
		os.println("    -mf.user      [Required]: The logon user.");
		os.println("    -mf.password  [Required]: The logon user's password.");
		os.println("    -mf.transport [Optional]: Required if the port number is non-standard.");
		os.println("                              One of [HTTP, HTTPS, TCPIP]. By default the");
		os.println("                              following transports are inferred from the port:");
		os.println("                                80    = HTTP");
		os.println("                                443   = HTTPS");
		os.println("                                other = TCPIP");
		os.println();
		os.println("Options:");
		os.println("   " + HELP_ARG + "                  Displays this help. <src-path> not required in when");
		os.println("                                       requesting help.");
		os.println("   " + VERBOSE_ARG + "                Enables tracing. By default, no tracing.");
		os.println();
		os.println("   " + WAIT_ARG + "                   Specifies the amount of time to try to find a matching Series: seconds, [default=60]");
		os.println("   " + UPLOAD_IMAGE_ARG + "                  Indicates whether to upload the Image Series or not:  0  [do not load] or 1 (default)");
		os.println("   " + UPLOAD_FID_ARG + "                    Indicates whether to upload the fid file:  1 [own DataSet], 2 [with image; default]");
		os.println("   " + COMPRESSION_LEVEL_ARG + "                 Gives the compression level (default 0): zip and tar (0 [none],1 [gz],2 [bz2]), aar (0-9) ");
		os.println("   " + CID_DELIMITER_ARG + "          We parse the CID from the 'SUBJECT_id' field of the SUbject meta-data file");
		os.println("                                This gives a delimiter to separate names from the CID in the form: <string><delim><cid>.");
		os.println("                                Defaults to '_'. Use the string 'null' if none required in which case the name string should hold just the CID");
		os.println("   " + CID_IS_FULL_ARG + "            If supplied indicates that the Server.Namespace CID prefix should not be added.");
		os.println("                                When not supplied, the CID is assumed to be partial starting with the Project.");
		os.println("   " + AUTO_SUBJECT_CREATE_ARG + "    If supplied indicates that subjects can be auto-created from the CID.");
		os.println("   " + CLONE_FIRST_SUBJECT_ARG + "    If supplied indicates that auto-created subjects will be cloned from the first pre-existing subject. If none, then standard auto-create occurs.");
		os.println("   " + CITABLE_ID_ARG + "                     If supplied then this gives the Subject's citable ID. Over-rides extraction from the Subject meta-data");
		os.println("   " + NIG_SUBJECT_META_ADD_ARG + "   If supplied indicates that the 'SUBJECT_id' field is configured with the NIG layout.");
		os.println("                                The layout is <Proj id>_<Coil>_<Animal ID>_<Gender>_<Exp Group>_<Vivo>_<Date>_<CID>");
		os.println("                                Parse this and extract extra meta-data and locate on the Project, Subject and Study");
		os.println("   " + CTYPE_ARG + "                  Container type. Select from 'zip,tar,aar. Defaults to zip.  zip is limited to 4GB, tar to 8GB and aar unlimited.");
		os.println();
	}
}
