package nig.dicom.util;

import java.io.File;


public class DicomCopy {

	public static boolean backup = false;
	public static String Default_AETitle = "DCMTOOLS";

	public static void main(String[] args) throws Throwable {

		if (args.length != 2) {
			printUsage();
			System.exit(1);
		}

		File in = new File (args[0]);
		File out = new File(args[1]);
		
		copyFileAndFixHeader(in, out);

	}

	public static void copyFileAndFixHeader (File in, File out) throws Throwable {
		
		com.pixelmed.utils.CopyStream.copy(in,out);
		DicomModify.editFile(out, null, null);         // CLeans up header
	}

	public static void printUsage() {

		System.out.println("Usage:");
		System.out.println("\t DCMCOPY <input>  <output>\n");
	}
}
