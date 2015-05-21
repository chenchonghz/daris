package nig.mf.pssd.client.dicom;


import java.io.File;
import java.io.PrintStream;

import nig.mf.pssd.CiteableIdUtil;


/**
 * Client to upload data to a DICOM server with optional editing of one DICOM meta-data
 * element and replacement by the citable ID (for use with the PSSD DICOM server in Mediaflux).
 * 
 * @author nebk
 *
 */

public class DicomSCU {



	public static void main(String[] args) throws Throwable {

		// Defaults
		String id = null;                       // Citable ID if editing meta-data
		Boolean fullID = true;                 // Is server expecting full CID
		String dicomElement = "0010,0010";     // DICOM element to edit
		Boolean addCaret = true;               // Add  leading caret to CID when editing
		String patientID = null;               // Replace patient ID with this string
		//
		File dir = null;                        // Directory holding data
		Boolean check = true;                   // Check supplied files are DICOM
		Boolean clean = true;                   // Clean up temporary directory
		Boolean inSitu = false;                 // Edit files in situ rather than copying and editing
		//
		String host = "localhost";              // Server host
		Integer port = 6667;                    // Server port
		String callingAET = "HFI-DICOM-TEST";
		String calledAET = "UM-DaRIS-1";
		
		String tempDir = "/tmp";                // Temporary directory when copying files

		// Parse Inputs
		if (args.length==0) {
			printHelp(System.out, callingAET, calledAET);
			System.exit(1);
		}
		//
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-help")) {
				printHelp(System.out, callingAET, calledAET);
				System.exit(0);
			} else if (args[i].equalsIgnoreCase("-id")) {
				if (++i == args.length) {
					System.err.println("DicomSCU: error: -id option must specify a CID");
					System.exit(1);
				}
				id = new String(args[i]);
			} else if (args[i].equalsIgnoreCase("-pid")) {
				if (++i == args.length) {
					System.err.println("DicomSCU: error: -pid option must specify a patient ID String");
					System.exit(1);
				}
				patientID = new String(args[i]);
			} else if (args[i].equalsIgnoreCase("-partial-id")) {
				fullID = false;
			} else if (args[i].equalsIgnoreCase("-dir")) {
				if (++i == args.length) {
					System.err.println("DicomSCU: error: -dir option must specify a directory String");
					System.exit(1);
				}
				dir = new File(args[i]);
			} else if (args[i].equalsIgnoreCase("-host")) {
				if (++i == args.length) {
					System.err.println("DicomSCU: error: -host option must specify a host name/ip String");
					System.exit(1);
				}
				host = new String(args[i]);
			} else if (args[i].equalsIgnoreCase("-port")) {
				if (++i == args.length) {
					System.err.println("DicomSCU: error: -port option must specify a host port String");
					System.exit(1);
				}
				port = Integer.parseInt(args[i]);
			} else if (args[i].equalsIgnoreCase("-de")) {
				if (++i == args.length) {
					System.err.println("DicomSCU: error: -de option must specify a DICOM element String '<group>,<element>' e.g. '0010,0020'");
					System.exit(1);
				}
				dicomElement = args[i];
				String[] s = dicomElement.split(",");
				if (s.length != 2) {
					System.err.println("DicomSCU: error: -de option must specify a DICOM element String '<group>,<element>' e.g. '0010,0020'");
				}
			} else if (args[i].equalsIgnoreCase("-nocaret")) {
				addCaret = false; 
			} else if (args[i].equalsIgnoreCase("-k")) {
				clean = false;
			} else if (args[i].equalsIgnoreCase("-nochk")) {
				check = false;
			} else if (args[i].equalsIgnoreCase("-insitu")) {
				inSitu = true;
			} else if (args[i].equalsIgnoreCase("-callingAET")) {
				if (++i == args.length) {
					System.err.println("DicomSCU: error: -callingAET option must specify a String");
					System.exit(1);
				}
				callingAET = new String(args[i]);
			} else if (args[i].equalsIgnoreCase("-calledAET")) {
				if (++i == args.length) {
					System.err.println("DicomSCU: error: -calledAET option must specify a String");
					System.exit(1);
				}
				calledAET = new String(args[i]);
			} else if (args[i].equalsIgnoreCase("-tmp")) {
				if (++i == args.length) {
					System.err.println("DicomSCU: error: -tmp option must specify a String");
					System.exit(1);
				}
				tempDir = new String(args[i]);
			} else {
				System.err.println("DicomSCU: error: unexpected argument " + args[i]);
				printHelp(System.err, callingAET, calledAET);
				System.exit(1);
			}
		}
		if (dir==null) throw new Exception ("no -dir supplied");
		System.out.println("Supplied Arguments:");
		System.out.println("  dir        = " + dir.getAbsolutePath());
		System.out.println("  id         = " + id);
		System.out.println("  pid        = " + patientID);
		System.out.println("  partial-id = " + !fullID);
		System.out.println("  nocaret    = " + !addCaret);
		System.out.println("  insitu     = " + inSitu);
		System.out.println("  de         = " + dicomElement);
		System.out.println("  host       = " + host);
		System.out.println("  port       = " + port);
		System.out.println("  calledAET  = " + calledAET);
		System.out.println("  callingAET = " + callingAET);
		System.out.println("  nochk      = " + !check);
		System.out.println("  k          = " + clean);
		System.out.println("  tmp        = " + tempDir);
		System.out.println("\n\n");

		// Prepare CID for edit if provided
		String id2 = id;
		if (id!=null) {
			int d = CiteableIdUtil.getIdDepth(id);
			if (d<2) {
				throw new Exception ("The depth of the supplied citable ID must be at least 2 e.g. P.S");
			}
			if (!fullID) {
				id2 = Dicom.stripCID(id);
			}
			//
			if (addCaret) {
				id2 = "^" + id2;                       // As this group / element is Patient Name and we want the first Name
				if (!dicomElement.equals("0010,0010")) {
					System.out.println("*** Warning - you requested a leading caret ('^') added to the DICOM element for edit");
					System.out.println("***           although that element is not '0010,0010', the Patient Name.");
					System.out.println("***           The CID string is now " + id2);
					System.out.println("***           use -nocaret to turn off adding a leading '^'");
				}
			}
		}

		// Upload via DICOM client to DICOM server.
		Dicom.uploadSCU (id2, dicomElement, patientID, host, port, callingAET, calledAET, inSitu, check, clean, dir, tempDir);

	}




	private static void printHelp (PrintStream os, String callingAET, String calledAET) {
		os.println("DicomClient");
		os.println();
		os.println("Synopsis:");
		os.println("     Uploads DICOM data to the specified DICOM server.  If the server is a Mediaflux");
		os.println("         DICOM server, it will ingest the data into the correct PSSD tree if a CID (-id) is given");
		os.println();
		os.println("Usage:");
		os.println("   " + DicomSCU.class.getName() + " [options..] ");
		os.println();
		os.println("Options:");
		os.println("   -help       Displays this help.");
		os.println("   -dir        Directory holding DICOM files. Is traversed recursively.");
		os.println("   -nochk      Don't check that specifed files are DICOM; speeds the process up");
		os.println("   -k          Don't clean up intermediary files");
		os.println("");
		os.println("   -id         Citeable ID of parent object to store the data under in a PSSD tree.");
		os.println("               Should ALWAYS be a full CID with leading UUID.NS components (regardless of -full-id)");
		os.println("               The DICOM meta-data are edited with this value");
		os.println("               If not given, then the data are uploaded with no meta-data edit");
		os.println("   -partial-id The DICOM server is expecting a partial CID (i.e. P.S... without the leading UUID.NS).");
		os.println("               By default, the server is expecting a full CID such as UUID.NS.P.Su");
		os.println("   -de         The DICOM element to edit and replace with the CID. Defaults to '0010,0010' which is Patient Name. REFERRING PHYSICIAN IS '0008,0090'");
		os.println("   -nocaret    Don't add a leading caret '^' to the CID when it is inserted in the metadata");
		os.println("   -pid        Replace the patient ID (0010,0020) with this string.  Don't set this if you are also pointing -de at the patient id !");
		os.println("   -insitu     When editin files with the id, do it in-situ rather than copying and editing - use with care.");
		os.println("");
		os.println("   -host       The DICOM server host name or IP; defaults to localhost");
		os.println("   -port       The DICOM server port; defaults to 6667");
		os.println("   -calledAET  The called (i.e. the server's) AETitle. Defaults to " + calledAET);
		os.println("   -callingAET the calling (i.e. the client's) AETitle. Defaults to " + callingAET);
		os.println("   -tmp        The parent directory for temporary files to be written to. Defaults to /tmp");
		os.println();
	}

}
