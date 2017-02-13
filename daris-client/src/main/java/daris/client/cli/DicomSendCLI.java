package daris.client.cli;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomFileUtilities;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.SetOfDicomFiles;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.network.MultipleInstanceTransferStatusHandler;
import com.pixelmed.network.StorageSOPClassSCU;

public class DicomSendCLI {

	public static final Action DEFAULT_ACTION = Action.REPLACE_OR_ADD;

	static enum Action {
		REPLACE_OR_IGNORE('&'), REPLACE_OR_ADD('+'), REPLACE_IF_EMPTY('#'), REMOVE('-');

		private char _op;

		Action(char op) {
			_op = op;
		}

		public char operator() {
			return _op;
		}

		public static Action parse(String token, Action defaultAction) {
			if (token != null) {
				char op = token.trim().charAt(0);
				Action[] vs = values();
				for (Action v : vs) {
					if (v.operator() == op) {
						return v;
					}
				}
			}
			return defaultAction;
		}

		public static Action parse(String token) {
			return parse(token, DEFAULT_ACTION);
		}

	}

	static class ElementArg {
		private AttributeTag _tag;
		private String _value;
		private Action _action;

		ElementArg(AttributeTag tag, String value, Action action) throws Throwable {
			_tag = tag;
			_value = value;
			_action = action;
		}

		public AttributeTag tag() {
			return _tag;
		}

		public String value() {
			return _value;
		}

		public Action action() {
			return _action;
		}
	}

	public static void main(String[] args) {
		String callingAET = null;
		String calledHost = null;
		int calledPort = -1;
		String calledAET = null;
		SetOfDicomFiles dcmFiles = new SetOfDicomFiles();
		File tmpDir = null;
		boolean debug = true;
		Map<AttributeTag, ElementArg> elements = new TreeMap<AttributeTag, ElementArg>();
		try {
			for (int i = 0; i < args.length;) {
				if (args[i].equals("--help") || args[i].equals("-h")) {
					printUsage();
					System.exit(0);
				} else if (args[i].equals("--debug")) {
					debug = true;
					i++;
				} else if (args[i].equals("--calling-aet")) {
					if (callingAET != null) {
						throw new Exception("--calling-aet has already been specified.");
					}
					callingAET = args[i + 1];
					i += 2;
				} else if (args[i].equals("--host")) {
					if (calledHost != null) {
						throw new Exception("--host has already been specified.");
					}
					calledHost = args[i + 1];
					i += 2;
				} else if (args[i].equals("--port")) {
					if (calledPort > 0) {
						throw new Exception("--port has already been specified.");
					}
					calledPort = Integer.parseInt(args[i + 1]);
					i += 2;
				} else if (args[i].equals("--called-aet")) {
					if (calledAET != null) {
						throw new Exception("--called-aet has already been specified.");
					}
					calledAET = args[i + 1];
					i += 2;
				} else if (args[i].equals("--element")) {
					parseElement(elements, args[i + 1]);
					i += 2;
				} else if (args[i].equals("--tmp")) {
					if (tmpDir != null) {
						throw new Exception("--tmp directory has already been specified.");
					}
					tmpDir = new File(args[i + 1]);
					if (!tmpDir.exists()) {
						throw new Exception("The specified --tmp temporary directory " + tmpDir + " does not exist.");
					}
					if (!tmpDir.isDirectory()) {
						throw new Exception(
								"The specified --tmp temporary directory " + tmpDir + " is not a directory.");
					}
					i += 2;
				} else {
					File f = Paths.get(args[i]).toFile();
					if (!f.exists()) {
						throw new FileNotFoundException("File " + args[i] + " is not found.");
					}
					if (f.isFile()) {
						if (DicomFileUtilities.isDicomOrAcrNemaFile(f)) {
							dcmFiles.add(f);
						} else {
							throw new Exception("File " + args[i] + " is not a valid DICOM file.");
						}
					} else if (f.isDirectory()) {
						addDicomDirectory(dcmFiles, f);
					}
					i++;
				}
			}
			if (callingAET == null) {
				throw new Exception("--calling-aet is not specified.");
			}
			if (calledHost == null) {
				throw new Exception("--host is not specified.");
			}
			if (calledPort <= 0) {
				calledPort = 104;
			}
			if (calledAET == null) {
				throw new Exception("--called-aet is not specified.");
			}
			if (tmpDir == null) {
				tmpDir = new File(System.getProperty("user.home"));
			}
			if (elements.isEmpty()) {
				System.out.println("Sending dicom files to " + calledAET + "@" + calledHost + ":" + calledPort + "...");
				sendDicomFiles(dcmFiles, callingAET, calledHost, calledPort, calledAET);
			} else {
				tmpDir = new File(tmpDir,
						"daris-dicom-send-" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
				if (!tmpDir.mkdirs()) {
					throw new Exception("Failed to create temporary directory: " + tmpDir.getAbsolutePath());
				}
				try {
					System.out.println("Editting dicom files...");
					editDicomFiles(dcmFiles, elements, callingAET, tmpDir);
					System.out.println(
							"Sending dicom files to " + calledAET + "@" + calledHost + ":" + calledPort + "...");
					sendDicomFiles(tmpDir, callingAET, calledHost, calledPort, calledAET);
				} finally {
					System.out.print("Deleting temporary directory: " + tmpDir + "...");
					forceDelete(tmpDir);
					System.out.println("done.");
				}
			}
		} catch (Throwable e) {
			System.err.println("Error: " + e.getMessage());
			printUsage();
			if (debug) {
				e.printStackTrace(System.err);
			}
			System.exit(1);
		}
	}

	private static void forceDelete(File file) throws IOException {
		try {
			if (file.isDirectory()) {
				FileUtils.deleteDirectory(file);
			} else {
				FileUtils.forceDelete(file);
			}
		} catch (Throwable e) {
			FileUtils.forceDeleteOnExit(file);
		}
	}

	private static void sendDicomFiles(File tmpDir, String callingAET, String calleHost, int calledPort,
			String calledAET) throws Throwable {
		SetOfDicomFiles dcmFiles = new SetOfDicomFiles();
		addDicomDirectory(dcmFiles, tmpDir);
		sendDicomFiles(dcmFiles, callingAET, calleHost, calledPort, calledAET);
	}

	private static void sendDicomFiles(SetOfDicomFiles dcmFiles, String callingAET, String calledHost, int calledPort,
			String calledAET) throws Throwable {
		final int total = dcmFiles.size();
		new StorageSOPClassSCU(calledHost, calledPort, calledAET, callingAET, dcmFiles, 0,
				new MultipleInstanceTransferStatusHandler() {
					@Override
					public void updateStatus(int nRemaining, int nCompleted, int nFailed, int nWarning,
							String sopInstanceUID) {
						System.out.println("Sent " + nCompleted + " of " + total + " dicom files.");
					}
				}, null, 0, 0);
	}

	private static void editDicomFiles(SetOfDicomFiles dcmFiles, Map<AttributeTag, ElementArg> elements,
			String callingAET, File tmpDir) throws Throwable {
		int i = 1;
		for (SetOfDicomFiles.DicomFile dcmFile : dcmFiles) {
			String outFileName = String.format("%08d.dcm", i);
			editDicomFile(new File(dcmFile.getFileName()), elements, callingAET, new File(tmpDir, outFileName));
			i++;
		}
	}

	private static void editDicomFile(File in, Map<AttributeTag, ElementArg> elements, String callingAET, File out)
			throws Throwable {
		AttributeList list = new AttributeList();
		list.read(in);
		Attribute mediaStorageSOPClassUIDAttr = list.get(TagFromName.MediaStorageSOPClassUID);
		String mediaStorageSOPClassUID = null;
		if (mediaStorageSOPClassUIDAttr != null) {
			mediaStorageSOPClassUID = mediaStorageSOPClassUIDAttr.getSingleStringValueOrNull();
		}
		Attribute mediaStorageSOPInstanceUIDAttr = list.get(TagFromName.MediaStorageSOPInstanceUID);
		String mediaStorageSOPInstanceUID = null;
		if (mediaStorageSOPInstanceUIDAttr != null) {
			mediaStorageSOPInstanceUID = mediaStorageSOPInstanceUIDAttr.getSingleStringValueOrNull();
		}
		// String implementationClassUID =
		// list.get(TagFromName.ImplementationClassUID).getSingleStringValueOrNull();
		// String implementationVersionName =
		// list.get(TagFromName.ImplementationVersionName).getSingleStringValueOrNull();
		/*
		 * Cleanup
		 */
		list.removeGroupLengthAttributes();
		list.removeMetaInformationHeaderAttributes();
		list.remove(TagFromName.DataSetTrailingPadding);
		list.correctDecompressedImagePixelModule();
		list.insertLossyImageCompressionHistoryIfDecompressed();
		if (mediaStorageSOPClassUID != null && mediaStorageSOPInstanceUID != null) {
			FileMetaInformation.addFileMetaInformation(list, mediaStorageSOPClassUID, mediaStorageSOPInstanceUID,
					TransferSyntax.ExplicitVRLittleEndian, callingAET);
		} else {
			FileMetaInformation.addFileMetaInformation(list, TransferSyntax.ExplicitVRLittleEndian, callingAET);
		}
		// Put the new tag in place
		if (elements != null && !elements.isEmpty()) {
			for (AttributeTag aTag : elements.keySet()) {
				Attribute attr = list.get(aTag);
				ElementArg ea = elements.get(aTag);
				if (attr == null) {
					if (ea.action() == Action.REPLACE_OR_ADD) {
						list.putNewAttribute(aTag).addValue(ea.value());
					} else {
						// Ignore
					}
				} else {
					if (ea.action() == Action.REMOVE) {
						list.remove(aTag);
					} else if (ea.action() == Action.REPLACE_IF_EMPTY) {
						if ("".equals(attr.getSingleStringValueOrEmptyString())) {
							attr.setValue(ea.value());
						} else {
							// Ignore
						}
					} else {
						attr.setValue(ea.value());
					}
				}
			}
		}
		list.write(new FileOutputStream(out), TransferSyntax.ExplicitVRLittleEndian, true, true);
	}

	private static void addDicomDirectory(final SetOfDicomFiles dcmFiles, File dir) {
		dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					addDicomDirectory(dcmFiles, f);
				} else if (f.isFile()) {
					if (DicomFileUtilities.isDicomOrAcrNemaFile(f)) {
						try {
							dcmFiles.add(f);
						} catch (Throwable e) {
							return false;
						}
						return true;
					}
				}
				return false;
			}
		});
	}

	private static void printUsage() {
		System.out.println("");
		System.out.println(
				"Usage: dicom-send --calling-aet <ae-title> --host <host> --port <port> --called-aet <ae-title> [--element \"tag=value\"] <dicom-directories|dicom-files>");
		System.out.println("");
		System.out.println("Options:");
		System.out.println("    --calling-aet <ae-title>    Calling DICOM AE title.");
		System.out.println("    --called-aet  <ae-title>    Called DICOM AE title.");
		System.out.println("    --host <host>               Called DICOM server host address.");
		System.out.println("    --port <port>               Called DICOM server port. Defaults to 104.");
		System.out.println("    --element     <[a]tag=value>   The element value to override. Can occur more than once to override multiple elements. The usage of [a] symbol is described in 'Element Actions'. Example of [op]tag=value: \"+(0010,0020)=ABC123\"");
		System.out.println("    --tmp         <tmp-dir>     The temporary directory to save the modified dicom files. The files will be deleted after uploading. Defaults to user's home directory.");
		System.out.println("");
		System.out.println("Element Actions:");
		System.out.println("    +       Replace the element value or add the element if it does not exist. If action symbol is omitted, this is the default action.");
		System.out.println("    -       Remove the element if it exists.");
		System.out.println("    &       Replace the element value. Ignore if the element does not exist.");
		System.out.println("    #       Replace the element value if the current value is empty.");
		System.out.println("");
		System.out.println("Element Action Examples:");
		System.out.println("    --element \"+(0010,0020)=123\"    set patient id to 123. If the element does not exist, add the element and set the value to 123");
		System.out.println("    --element \"-(0019,0020)\"        remove element (0019,0020)");
		System.out.println("    --element \"&(0019,0020)\"        set the value of existing element (0019,0020). Ignore if the element does not exist.");
		System.out.println("    --element \"#(0020,0011)=1\"      set the value of (0020,0011) to 1 if the element pre-exists and its value is empty.");
		System.out.println("    --element \"(0010,0020)=123\"     same as \"+(0010,0020)=123\"");
		System.out.println("    --element \"&(0010,0010)=''\"     set the patient name to empty if the element pre-exists.");
		System.out.println("");
		
	}

	private static void parseElement(Map<AttributeTag, ElementArg> elements, String token) throws Throwable {
		token = token.trim();
		Action action = Action.parse(token);
		if (action != Action.REMOVE && token.indexOf('=') == -1) {
			throw new IllegalArgumentException(
					"Failed  to parse --element argument: " + token + ". Missing element value.");
		}
		if (action == Action.REMOVE && token.indexOf('=') >= 0) {
			throw new IllegalArgumentException("Failed  to parse --element argument: " + token
					+ ". Unexpected value for removing element action.");
		}
		String[] parts = token.split("=");
		String tagStr = parts[0].replaceAll("[^\\d]", "");
		if (tagStr.length() != 8) {
			throw new Exception("Invalid element tag: " + parts[0]);
		}
		int gggg = Integer.parseInt(tagStr.substring(0, 4), 16);
		int eeee = Integer.parseInt(tagStr.substring(4, 8), 16);
		AttributeTag tag = new AttributeTag(gggg, eeee);
		if (elements.containsKey(tag)) {
			throw new IllegalArgumentException("Element " + tag + " has already been specified.");
		}
		String value = null;
		if (parts.length > 1) {
			value = parts[1];
			if (value.startsWith("\"") || value.startsWith("'")) {
				value = value.substring(1);
			}
			if (value.endsWith("\"") || value.endsWith("'")) {
				value = value.substring(0, value.length() - 1);
			}
		}
		if (action == Action.REMOVE && value != null) {
			throw new IllegalArgumentException(
					"Failed  to parse --element argument: " + token + ". Unexpected value: " + value);
		}
		if (action != Action.REMOVE && value == null) {
			value = "";
		}
		elements.put(tag, new ElementArg(tag, value, action));
	}
}
