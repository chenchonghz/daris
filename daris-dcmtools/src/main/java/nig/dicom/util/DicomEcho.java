package nig.dicom.util;

import com.pixelmed.network.VerificationSOPClassSCU;

public class DicomEcho {

	public static void main(String[] args) {

		String host = null;
		int port = -1;
		String calledAETitle = null;
		String callingAETitle = null;
		int debugLevel = 0;
		for (int i = 0; i < args.length;) {
			if (args[i].equals("-host")) {
				host = args[i + 1];
				i += 2;
			} else if (args[i].equals("-port")) {
				try {
					port = Integer.parseInt(args[i + 1]);
				} catch (NumberFormatException e) {
					System.err.println("Error: " + args[i + 1] + " is not a valid port number.");
					printUsage();
					System.exit(1);
				}
				i += 2;

			} else if (args[i].equals("-calledAETitle")) {
				calledAETitle = args[i + 1];
				i += 2;
			} else if (args[i].equals("-callingAETitle")) {
				callingAETitle = args[i + 1];
				i += 2;
			} else if (args[i].equals("-debug") || args[i].equals("--debug-level")) {
				try {
					debugLevel = Integer.parseInt(args[i + 1]);
				} catch (NumberFormatException e) {
					System.err.println("Error: " + args[i + 1] + " is not a valid number for setting debug level.");
					printUsage();
					System.exit(1);
				}
				i += 2;
			} else {
				System.err.println("Error: invalid argument: " + args[i] + ". Ignored.");
				i++;
			}
		}
		if (host == null) {
			System.err.println("Error: no host specified.");
			printUsage();
			System.exit(1);
		}
		if (port == -1) {
			System.err.println("Error: no port specified.");
			printUsage();
			System.exit(1);
		}
		if (calledAETitle == null) {
			System.err.println("Error: no calledAETitle specified.");
			printUsage();
			System.exit(1);
		}
		if (callingAETitle == null) {
			System.err.println("Error: no callingAETitle specified.");
			printUsage();
			System.exit(1);
		}
		try {
			new VerificationSOPClassSCU(host, port, calledAETitle, callingAETitle, false, debugLevel);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
		}
	}

	private static void printUsage() {

		System.out.println("Usage:");
		System.out.println("\t DCMECHO   [options]   <DICOM Files>");
		System.out.println("Options:");
		System.out.println("    -host <host>                    Their hostname or IP Address.");
		System.out.println("    -port <port>                    Their port number.");
		System.out.println("    -calledAETitle <AETitle>        Their AE Title.");
		System.out.println("    -callingAETitle <AETitle>       Our AE Title.");
		System.out.println("    -debug <debugLevel>             Debug level. Defaults to 0.");
	}
}
