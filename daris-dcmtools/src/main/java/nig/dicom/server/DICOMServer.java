package nig.dicom.server;


import com.pixelmed.network.StorageSOPClassSCPDispatcher;
import java.io.File;


public class DICOMServer  {

	public static void main(String arg[]) {
		try {
			StorageSOPClassSCPDispatcher dispatcher = null;
			if (arg.length!=4) {
				throw new Exception("There must be 4 arguments: port, aet, tmp, parent");
			}
			
			// Parse
			int port = Integer.parseInt(arg[0]);
			String calledAETitle = arg[1];
			File savedImagesFolder = new File(arg[2]);
			String parent = arg[3];
			int dbg = 0;

			// Start server with handler
			System.err.println("StorageSOPClassSCPDispatcher.main(): listening on port "+port+" AE "+calledAETitle+" storing into "+savedImagesFolder+" debugging level "+dbg);
			DICOMReceivedObjectHandler handler = new DICOMReceivedObjectHandler(parent, true, true);
			dispatcher = new StorageSOPClassSCPDispatcher(port, calledAETitle, savedImagesFolder,
					handler, dbg);
			new Thread(dispatcher).start();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(0);
		}
	}
}