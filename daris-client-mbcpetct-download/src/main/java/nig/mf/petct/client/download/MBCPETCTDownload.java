package nig.mf.petct.client.download;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Vector;

import nig.compress.ZipUtil;
import nig.mf.MimeTypes;
import nig.mf.client.util.AssetUtil;
import nig.mf.client.util.ClientConnection;
import nig.mf.client.util.UserCredential;
import nig.util.DateUtil;

import arc.mf.client.ServerClient;
import arc.mf.client.ServerClient.Connection;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;


public class MBCPETCTDownload {

	private static final String MF_NAMESPACE = "MBIC-Archive";
	private static final String MF_PSSD_NAMESPACE = "MBIC-PSSD-Archive";
	//
	private static final String RAW_STUDY_DOC_TYPE = "daris:siemens-raw-petct-study";
	private static final String RAW_SERIES_DOC_TYPE = "daris:siemens-raw-petct-series ";
	//
	private static final String DEFAULT_SRC_PATH = "/Volumes/SiteData/PETRawData";

	// The authenticating security token must be made with :app == to this string.
	// The token must also hold the appropriate permissions to allow it to access the
	// PSSD Project or namespace into which it is uploading data.
	private static final String TOKEN_APP = "MBIC-PETCT-Raw-Download";


	// This class sets the defaults for arguments that can be passed in
	// to the main program
	private static class Options {

		//These all map on to meta-data parsed out of the raw file name. 
		// The basci file name structure is (see PETMetaData or details):
		//
		// <Last>_<First>.<Modality>.<Description>.<Series Number>.<Acquisition Type>.
		//          0          1         2              3                4
		// <Date Acquired>.<Time Acquired>.<Instance Number>.<DateExported>.<TimeExported>.
		//       5                6              7              8-10           11-13
		// <UUID>.<Extension>

		public String firstName = null;
		public String lastName = null;
		public String startDate = null;
		public String endDate = null;
		public String modality = null;
		public String path = null;
		public String description = null;
		public String type = null;
		public String uuid = null;
		public boolean chksum = true;
		public String id = null;
		//	
		public boolean list = false;
		public boolean decrypt = true;

		public void print () {
			System.out.println("list           = " + list);
			System.out.println("id             = " + id);
			System.out.println("first          = " + firstName);
			System.out.println("last           = " + lastName);
			System.out.println("start          = " + startDate);
			System.out.println("end            = " + endDate);
			System.out.println("desc           = " + description);
			System.out.println("modality       = " + modality);
			System.out.println("type           = " + type);
			System.out.println("uuid           = " + uuid);
			System.out.println("path           = " + path);
			System.out.println("no-chk         = " + !chksum);
			System.out.println("no-decrpyt     = " + !decrypt);
		}
	}

	public static final String HELP_ARG = "-help";
	public static final String ID_ARG = "-id";
	public static final String FIRSTNAME_ARG = "-first";
	public static final String LASTNAME_ARG = "-last";
	public static final String STARTDATE_ARG = "-start";
	public static final String ENDDATE_ARG = "-end";
	public static final String MODALITY_ARG = "-modality";
	public static final String DESCRIPTION_ARG = "-desc";
	public static final String PATH_ARG = "-path";
	public static final String TYPE_ARG = "-type";
	public static final String UUID_ARG = "-uuid";
	public static final String LIST_ARG = "-list";
	public static final String NOCHK_ARG = "-no-chk";
	public static final String DECRYPT_ARG = "-no-decrypt";

	public static final String DEST_ARG = "-dest";  // Consumed by wrapper




	/**
	 * 
	 * the main function of this command line tool.
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) throws Throwable {

		Options ops = new Options();

		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase(HELP_ARG)) {
				printHelp(System.out);
				System.exit(0);
			} else if (args[i].equalsIgnoreCase(NOCHK_ARG)) {
				ops.chksum = false;
			} else if (args[i].equalsIgnoreCase(FIRSTNAME_ARG)) {
				i++;
				ops.firstName = args[i];
			} else if (args[i].equalsIgnoreCase(LASTNAME_ARG)) {
				i++;
				ops.lastName = args[i];
			} else if (args[i].equalsIgnoreCase(ID_ARG)) {
				i++;
				ops.id = args[i];
			} else if (args[i].equalsIgnoreCase(STARTDATE_ARG)) {
				i++;
				ops.startDate = args[i];
			} else if (args[i].equalsIgnoreCase(ENDDATE_ARG)) {
				i++;
				ops.endDate = args[i];
			} else if (args[i].equalsIgnoreCase(MODALITY_ARG)) {
				i++;
				ops.modality = args[i];
			} else if (args[i].equalsIgnoreCase(DESCRIPTION_ARG)) {
				i++;
				ops.description = args[i];
			} else if (args[i].equalsIgnoreCase(TYPE_ARG)) {
				i++;
				ops.type = args[i];
			} else if (args[i].equalsIgnoreCase(UUID_ARG)) {
				i++;
				ops.uuid = args[i];
			} else if (args[i].equalsIgnoreCase(PATH_ARG)) {
				i++;
				ops.path= args[i];
			} else if (args[i].equalsIgnoreCase(LIST_ARG)) {
				ops.list = true;
			} else if (args[i].equalsIgnoreCase(DECRYPT_ARG)) {
				ops.decrypt = false;
			} else if (args[i].equalsIgnoreCase(DEST_ARG)) {
				// Consumed by wrapper
				i++;
			} else {
				System.err.println("MBCPETDownload: error: unexpected argument = " + args[i]);
				printHelp(System.err);
				System.exit(1);	 
			}
		}
		ops.print();

		// Check inputs
		if (ops.path == null) ops.path = DEFAULT_SRC_PATH;

		// FOrce a full name if first name given
		if ( (ops.firstName!=null && ops.lastName==null) ) {
			throw new Exception ("You must specify the last name if you give the first name.");
		}

		// Force a date to be given
		if (ops.startDate==null && ops.endDate==null) {
			throw new Exception("You must give at least a start or end date");
		}
		// Validate date formats are in desired Mediaflux syntax
		if (ops.startDate!=null) {
			try {			
				DateUtil.dateFromString(ops.startDate, "dd-MMM-yyyy");
			} catch (Throwable t) {
				throw new Exception("Start date (" + ops.startDate + ") not in correct format: must be of the form dd-MMM-yyyy");
			}
		}
		if (ops.endDate!=null) {
			try {			
				DateUtil.dateFromString(ops.endDate, "dd-MMM-yyyy");
			} catch (Throwable t) {
				throw new Exception("End date (" + ops.endDate + ") not in correct format: must be of the form dd-MMM-yyyy");
			}
		}

		if (ops.modality!=null) {
			ops.modality = ops.modality.toUpperCase();
			if (!ops.modality.equals("PT") && !ops.modality.equals("CT")) {
				throw new Exception ("Modality must be one of 'PT' or 'CT'");
			}
		}
		if (ops.type!=null) {
			ops.type = ops.type.toUpperCase();
			if (!ops.type.equals("RAW") && !ops.type.equals("LM") && !ops.type.equals("NORM") && 
					!ops.type.equals("PROTOCOL") && !ops.type.equals("SINO")) {
				throw new Exception ("Acquisition type must be one of 'RAW', 'LM', 'NORM', 'PROTOCOL' or 'SINO'");
			}
		}

		System.out.println("");
		System.out.println("");
		System.out.println("");
		try {
			// Download data
			download(ops);
		} catch (Throwable t) {
			System.err.println("MBCPETDownload: error: " + t.getMessage());
			t.printStackTrace();
			System.exit(1);
		}
	}


	private static void download (Options ops) throws Throwable {


		// Make connection to MF server	
		Connection cxn = ClientConnection.createServerConnection();
		UserCredential cred = ClientConnection.connect (cxn, TOKEN_APP, ops.decrypt);
		if (cred==null) {
			throw new Exception ("Failed to extract user credential after authentication");
		}

		// Queries to find assets
		Collection<String> assets = new Vector<String>();

		// Query for assets
		if (ops.lastName!=null) {
			// Query includes patient details
			fetchSeriesAssets1 (cxn, ops, assets);
		} else {
			// Query does not include patient details (much simpler)
			fetchSeriesAssets2 (cxn, ops, assets);
		}


		// Download assets
		if (assets.size()>0) {
			if (ops.list) {
				System.out.println("*** Listing asset original file names");
			} else {
				System.out.println("*** Downloading asset contents");
			}
			for (String asset : assets) {
				downloadAsset (cxn, ops, asset);
			}
		} else {
			System.out.println("*** No assets were found to download");
		}

		// CLose connection
		cxn.close();
	}



	private static void downloadAsset (ServerClient.Connection cxn, Options ops, String asset) throws Throwable {

		// Fetch original file name of asset
		XmlDoc.Element r = AssetUtil.getMeta(cxn, asset, null);
		String cid = r.value("asset/cid");
		if (r!=null) {
			String fileName = r.value("asset/meta/daris:pssd-filename/original");
			if (fileName==null) {
				System.out.println("       Asset does not have source file name - skipping");
				return;
			}

			// Create full name
			File fPath = new File(ops.path);
			File outFile = new File(org.apache.commons.io.FilenameUtils.concat(fPath.getAbsolutePath(), fileName));

			// Save or list
			if (ops.list) {
				System.out.println("   " + cid + " : " + fileName);
			} else {
				System.out.println("   " + outFile.getAbsolutePath());
				nig.mf.client.util.AssetUtil.getContent(cxn, asset, outFile);

				if (ops.chksum){
					System.out.println("      Validating checksums");

					// Get checksum from disk
					System.out.println("         Computing disk file check sum");

					String chkSumDisk = ZipUtil.getCRC32(outFile, 16);

					// Get chksum from asset
					String chkSumAsset = r.value("asset/content/csum");

					if (chkSumDisk.equalsIgnoreCase(chkSumAsset)) {
						System.out.println("         Checksums match");	
					} else {
						System.out.println("         Checksums do not match. Checksums are:");	
						System.out.println("            Mediaflux asset = " + chkSumAsset);
						System.out.println("            Output file      = " + chkSumDisk);
						//
						outFile.delete();
						System.out.println("         Destroyed output file");
					}
				}
			}
		}
	}


	private static void fetchSeriesAssets2 (ServerClient.Connection cxn, Options ops, Collection<String> seriesAssets) throws Throwable {



		// FInd raw series
		XmlStringWriter w = new XmlStringWriter();
		String query = null;
		if (ops.id!=null) {
			// Must be PSSD
			query = "(cid starts with '" + ops.id + "' or cid='" + ops.id + "')";
		} else {
			// ID not given, could be old DICOM or new PSSD archive
			query = "(" + nameSpaceQuery(MF_NAMESPACE) + " or " + nameSpaceQuery(MF_PSSD_NAMESPACE) + ")";
		}

		//
		if (ops.modality!=null) {
			if (ops.modality.equals("PT")) {
				query += " and (type='" + MimeTypes.PET_RAW_SERIES_MIME_TYPE + "')";
			} else if (ops.modality.equals("CT")) {
				query += " and (type='" + MimeTypes.CT_RAW_SERIES_MIME_TYPE + "')";
			}
		}

		// Add extra ops queries
		query = addExtraQueries (query, ops);

		w.add("where", query);
		w.add("size", "infinity");
		XmlDoc.Element r = cxn.execute("asset.query", w.document());
		if (r!=null) {

			// Add series assets to list
			Collection<String> series = r.values("id");
			if (series!=null && series.size()>0) {		
				seriesAssets.addAll (series);
			}
		}
	}

	/**
	 * Fetch raw series assets given patient name based query
	 * 
	 * @param cxn
	 * @param ops
	 * @param seriesAssets
	 * @throws Throwable
	 */
	private static void fetchSeriesAssets1 (ServerClient.Connection cxn, Options ops, Collection<String> seriesAssets) throws Throwable {

		// Find assets with mf-dicom-patient document set in the correct namespace
		// May be DICOM or PSSD data model
		XmlStringWriter w = new XmlStringWriter();
		String query = null;
		w.add("pdist", 0);
		w.add("size", "infinity");

		if (ops.id!=null) {
			// Must be PSSD
			query = "(cid starts with '" + ops.id + "' or cid='" + ops.id + "')";
		} else {
			// ID not given, could be old DICOM or new PSSD archive
			query = "(" + nameSpaceQuery(MF_NAMESPACE) + " or " + nameSpaceQuery(MF_PSSD_NAMESPACE) + ")";
		}
		String lastNameQuery = "xpath(mf-dicom-patient/name[@type='last'])=ignore-case('" + ops.lastName + "')";
		query += " and " + lastNameQuery;
		if (ops.firstName!=null) {
			String firstNameQuery = "xpath(mf-dicom-patient/name[@type='first'])=ignore-case('" + ops.firstName + "')";
			query += " and " + firstNameQuery;
		}
		w.add("where", query);
		XmlDoc.Element r = cxn.execute("asset.query", w.document());
		Collection<String> patientAssets = r.values("id");
		if (patientAssets==null) return;
		//
		if (patientAssets!=null && patientAssets.size() > 1) {
			System.out.println("*** Warning found multiple patient assets for patient " + ops.firstName + " " + ops.lastName);
		}


		// Iterate over patient assets
		if (patientAssets.size()>0) {
			for (String patientAsset : patientAssets) {
				XmlDoc.Element meta = AssetUtil.getMeta(cxn, patientAsset, null);

				// Is this a DICOM model patient or PSSD Model subject
				String model = meta.value("asset/model");
				Boolean isPSSD = false;
				if (model!=null && model.equals("om.pssd.subject")) isPSSD = true;

				// FInd raw studies (DICOM or PSSD) - should combine these into a framework !
				w = new XmlStringWriter();
				w.add("pdist", 0);
				query = "(" + RAW_STUDY_DOC_TYPE + " has value)";

				if (isPSSD) {
					// We don't create Study mine types yet in PSSD so we can't filter
					// by Study type.  So just use asset.query to find the children
					String cid = meta.value("asset/cid");
					query += " and (cid starts with '" + cid + "')";
				} else {
					query += " and " + nameSpaceQuery(MF_NAMESPACE);
					query += " and (related to{had-by} (id=" + patientAsset + "))";
				}
				w.add("where", query);
				r = cxn.execute("asset.query", w.document());

				if (r!=null) {

					// Iterate over raw Studies
					Collection<String> studies = r.values("id");
					if (studies!=null && studies.size()>0) {
						for (String study : studies) {


							// FInd raw series
							w = new XmlStringWriter();
							query = "(" + RAW_SERIES_DOC_TYPE  + " has value)";

							// Add modality via type if supplied
							if (ops.modality!=null) {
								if (ops.modality.equals("PT")) {
									query += " and (type='" + MimeTypes.PET_RAW_SERIES_MIME_TYPE + "')";
								} else if (ops.modality.equals("CT")) {
									query += " and (type='" + MimeTypes.CT_RAW_SERIES_MIME_TYPE + "')";
								}
							}

							if (isPSSD) {
								XmlDoc.Element dataSetMeta = AssetUtil.getMeta(cxn, study, null);
								String cid = dataSetMeta.value("asset/cid");
								query += " and (cid starts with '" + cid + "')";

							} else {
								query += " and " + nameSpaceQuery (MF_NAMESPACE);
								query += " and (related to{container} (id=" + study + "))";
							}

							// Add extra ops queries
							query = addExtraQueries (query, ops);

							w.add("where", query);
							r = cxn.execute("asset.query", w.document());

							if (r!=null) {

								// Add series assets to list
								Collection<String> series = r.values("id");
								if (series !=null && series.size()>0) {		
									seriesAssets.addAll (series);
								}
							}
						}
					}
				}
			}
		}
	}



	private static String nameSpaceQuery (String namespace) {
		return "namespace='" + namespace + "'";
	}

	private static String	addExtraQueries (String query, Options ops) {	

		// Add date query
		if (ops.startDate!=null) {
			query += " and xpath(" + RAW_SERIES_DOC_TYPE + "/date)>='" + ops.startDate + "'";
		}
		if (ops.endDate!=null) {
			query += " and xpath(" + RAW_SERIES_DOC_TYPE + "/date)<='" + ops.endDate + "'";
		}
		/*
		 * Handled via asset mime type now
		if (ops.modality!=null) {
			query += " and  xpath(" + RAW_SERIES_DOC_TYPE + "/modality)='" + ops.modality + "'";
		}
		 */
		if (ops.description!=null) {
			query += " and xpath(" + RAW_SERIES_DOC_TYPE + "/description) contains literal ('" + ops.description + "')";
		}
		if (ops.type!=null) {
			query += " and xpath(" + RAW_SERIES_DOC_TYPE + "/type) contains literal ('" + ops.type + "')";
		}
		if (ops.uuid!=null) {
			query += " and xpath(" + RAW_SERIES_DOC_TYPE + "/uuid)='" + ops.uuid + "'";

		}
		return query;
	}



	/**
	 * 
	 * prints the help information for this command line tool.
	 * 
	 * @param os
	 * 
	 */
	private static void printHelp(PrintStream os) {
		os.println("MBCPETDownload");
		os.println();
		os.println("Synopsis:");
		os.println("   Downloads raw Siemens PET/CT files from Mediaflux.  Raw Assets are associated");
		os.println("   with pre-existing DICOM patient assets holding the DICOM images. ");
		os.println();
		os.println("Usage:");
		os.println("   " + MBCPETCTDownload.class.getName() + " [options..]");
		os.println();
		os.println();
		os.println("Java Properties:");
		os.println("    -mf.host      [Required]: The name or IP address of the Mediaflux host.");
		os.println("    -mf.port      [Required]: The server port number.");
		os.println("    -mf.token     [Optional]: The security token to authenticate with (preferred).");
		os.println("    -mf.user      [Optional]: The logon user (if no token).");
		os.println("    -mf.domain    [Optional]: The logon domain (if no token).");
		os.println("    -mf.password  [Optional]: The logon user's (obfuscated) password (if no token).");
		os.println("    -mf.transport [Optional]: Required if the port number is non-standard.");
		os.println("                              One of [HTTP, HTTPS, TCPIP]. By default the");
		os.println("                              following transports are inferred from the port:");
		os.println("                                80    = HTTP");
		os.println("                                443   = HTTPS");
		os.println("                                other = TCPIP");
		os.println();
		os.println("Options:");
		os.println("   " + HELP_ARG + "          Displays this help. <src-path> not required in when");
		os.println("                    requesting help.");
		os.println("   " + LIST_ARG +    "          Just lists found assets rather than downloading them");
		os.println("   " + ID_ARG +  "            Specify citable ID parent to find PSSD (DaRIS) subjects under. If not given looks in DICOM and PSSD archives.");
		os.println("   " + FIRSTNAME_ARG +    "         Specify patient first name (case insensitive; optional)");
		os.println("   " + LASTNAME_ARG + "          Specify patient last name (case insensitive; optional)");
		os.println("   " + STARTDATE_ARG + "         Specify start date (dd-MMM-yyyy; inclusive; one date required)");
		os.println("   " + ENDDATE_ARG + "           Specify end date (dd-MMM-yyyy; inclusive; one date required)");
		os.println("   " + MODALITY_ARG + "      Specify modality ('PT' or 'CT'; optional)");
		os.println("   " + DESCRIPTION_ARG + "          Specify description of series (free text; optional; partial matches allowed)");
		os.println("   " + TYPE_ARG + "          Specify acquisition type ('RAW', 'LM', 'NORM', 'PROTOCOL', 'SINO','PET_CALIBRATION', 'PET_COUNTRATE', 'PET_LISTMODE', 'PETCT_SPL'; optional)");
		os.println("   " + UUID_ARG + "          Specify UUID of acquisitionl optional");
		os.println("   " + PATH_ARG + "          Specify path of directory to put downloaded files in (optional; default is /Volumes/SiteData/PETRawData)");
		os.println("   " + NOCHK_ARG + "        Disable checksum comparison.");
		os.println("   " + DECRYPT_ARG + "    Specifies the password should not be decrypted.");
		os.println("   " + DEST_ARG + "          Specifies the destination host and network parameters (see wrapper script). The default is 'mbciu' out of the box, but could be changed.");
		os.println();
		os.println();
	}
}
