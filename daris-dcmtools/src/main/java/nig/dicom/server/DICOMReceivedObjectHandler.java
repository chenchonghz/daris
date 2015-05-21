package nig.dicom.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.network.ReceivedObjectHandler;
import com.pixelmed.utils.FileUtilities;


/**
 * Simple handler for a pixelmed server that organizes by patient, study, series
 * 
 * @author nebk
 *
 */
public class DICOMReceivedObjectHandler extends ReceivedObjectHandler {
	private String parent_;
	private boolean move_;
	private boolean verbose_;
	public DICOMReceivedObjectHandler (String parent, boolean move, boolean verbose) {
		parent_ = parent;
		move_ = move;
		verbose_ = verbose;
	}


	public void sendReceivedObjectIndication(String dicomFileName,String transferSyntax,String callingAETitle) throws DicomNetworkException, DicomException, IOException {
		if (dicomFileName != null) {
			if (verbose_) {
				System.err.println("Received: "+dicomFileName+" from "+callingAETitle+" in "+transferSyntax);
			}
			try {
				DicomInputStream i = new DicomInputStream(new BufferedInputStream(new FileInputStream(dicomFileName)));
				AttributeList list = new AttributeList();
				list.read(i,TagFromName.PixelData); 
				i.close();

				File f = new File(dicomFileName);
				process (f, list, parent_, move_, verbose_);
			} catch (Exception e) {
				System.out.println("Exception for file" + dicomFileName);
				e.printStackTrace(System.err);
			}
		}
	}

	private void process (File f, AttributeList al, String parent, boolean move, boolean verbose) throws IOException {
		int dupID = 0;
		try {
			StringBuffer path = new StringBuffer(parent);
			path.append("/").append(Attribute.getSingleStringValueOrNull(al, TagFromName.PatientName));
			path.append("/").append(Attribute.getSingleStringValueOrNull(al, TagFromName.StudyDescription));
			path.append("/").append(Attribute.getSingleStringValueOrNull(al, TagFromName.SeriesDescription));
			path.append("_").append(Attribute.getSingleStringValueOrNull(al, TagFromName.SeriesInstanceUID));

			File newPath = new File(path.toString());	
			newPath.mkdirs();
			//
			String sopInstanceUID = Attribute.getSingleStringValueOrNull(al, TagFromName.SOPInstanceUID);
			String instanceNumber = Attribute.getSingleStringValueOrNull(al, TagFromName.InstanceNumber);
			String fileName = path+"/"+ instanceNumber + "_"  + sopInstanceUID + ".dcm";
			if (verbose) {		
				System.out.print(f);
				System.out.print(" -> ");
				System.out.println("/" + fileName);
			}
			if (move) {
				FileUtilities.renameElseCopyTo(f, new File(fileName));
				if (f.exists()) f.delete();
			} else {
				File t = new File(fileName);
				if (t.exists()) {
					long chkSumIn = org.apache.commons.io.FileUtils.checksumCRC32(f);
					long chkSumDup = org.apache.commons.io.FileUtils.checksumCRC32(t);
					if (chkSumIn==chkSumDup) {
						if (verbose) {
							System.out.println("*** File " + fileName + " already exists with identical check sum; dropping this instance");
						}
					} else {
						String degTag = "-duplicate" + dupID;
						if (verbose) {
							System.out.println("*** File " + fileName + 
									" already exists with differing checksum; appending degeneracy tag '" + degTag + "' and saving.");
						}
						fileName += degTag;
						org.apache.commons.io.FileUtils.copyFile(f, new File(fileName));
					}
				} else {
					org.apache.commons.io.FileUtils.copyFile(f, new File(fileName));
				}
			}

		} catch (Exception e) {
			System.out.println("**** Exception found with error " + e.getMessage());
		}
	}
};

