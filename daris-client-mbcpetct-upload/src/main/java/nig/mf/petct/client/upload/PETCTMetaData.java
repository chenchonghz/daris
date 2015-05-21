package nig.mf.petct.client.upload;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import nig.util.DateUtil;

/**
 * File produiced when exporting raw data from Siemens Biograph PET/CT system
 * @author nebk
 *
 */
public class PETCTMetaData {
	private File _path;
	private String _fileName;        // The file name part of the path
	private String _firstName;       // Patient name
	private String _lastName;
	private String _modality;        // PT or CT
	private String _description;     // SUpplied by operator
	private String _acquisitionType; // ACquisition type; RAW (PT&CT), LM (PT), NORM (PT), PROTOCOL (PT), SINO (PT)
	private String _seriesNumber;    // Series number supplied by scanner
	private String _instanceNumber;  // Instance number supplied by scanner
	private Date _dateTime;          // Date/time of acquisition
	private Date _dateTimeExported;  // Date/time exported from scanner
	private String _uuid;            // UUID generated at time of export.
	private String _ext;             // The file extension; .ptr (CT) or .ptd (PET)


	public PETCTMetaData (File path) {
		_path = path;
	}

	public void parse () throws Throwable {
		// Path must be a directory of the form
		// <Last>_<First>.<Modality>.<Description>.<Series Number>.<Acquisition Type>.
		//          0          1         2              3                4
		// <Date Acquired>.<Time Acquired>.<Instance Number>.<DateExported>.<TimeExported>.
		//       5                6              7              8-10           11-13
		// <UUID>.<Extension>
		//   14-15    16
		

		// Find child part of path
		_fileName = _path.getName();

		// Split
		String[] parts = _fileName.split("\\.");
		int nParts = parts.length;
		if (nParts != 17) {
			throw new Exception ("Could not parse meta-data; expecting 17 parts, found " + nParts);
		}

		// Names
		String name = parts[0];
		String[] nameParts = name.split("_");
		int nPartsName = nameParts.length;
		if (nPartsName > 2) {
			throw new Exception ("Could not parse meta-data; name field does not have 1 or 2 parts.");

		}

		// If only one name (like the phantom) we put it in the last name only
		// The downloader will be happy with that
		if (nPartsName==2) {
			_lastName = nameParts[0];
			_firstName = nameParts[1];
		} else {
			_lastName = name;
		}

		// Modality
		_modality = parts[1];

		// Description
		_description = parts[2];

		// File type 
		_seriesNumber = parts[3];
		_acquisitionType = parts[4];

		// Date/time of acquisition and export
		String date = parts[5];
		String time = parts[6];
		_dateTime = DateUtil.dateFromString (date+":"+time, "yyyyMMdd:HHmmss");
		//
		date = parts[8] + parts[9] + parts[10];
		time = parts[11] + parts[12] + parts[13];
		_dateTimeExported = DateUtil.dateFromString (date+":"+time, "yyyyMMdd:HHmmss");

		// Instance and UUID
		_instanceNumber = parts[7];
		_uuid = parts[14]+ "." + parts[15];

		// Extension
		_ext = parts[16];
	}

	public static String reconstituteFileName (String lastName, String firstName, String modality, String description,
			String seriesNumber, String acquisitionType, Date dateTimeAcquisition, String instanceNumber, 
			Date dateTimeExported, String uuid) throws Throwable {

		String fileName = null;
		if (firstName==null) {
			//                    0                 0                1                  2
			fileName = lastName + "." + modality + "." + description +
					//            3                       4
					"." + seriesNumber + "." + acquisitionType;

		} else {
			//                    0                 0                1                  2
			fileName = lastName + "_" + firstName + "." + modality + "." + description +
					//       3                       4
					"." + seriesNumber + "." + acquisitionType;
		}

		// Convert date/times  back to Strings in original format
		String ta = DateUtil.formatDate (dateTimeAcquisition, "yyyyMMdd.HHmmss");
		String te = DateUtil.formatDate (dateTimeExported, "yyyy.MM.dd.HH.mm.ss");
		//               5  6            7            8 9 10 11 12 13       14 15
		fileName += "." + ta +"." + instanceNumber + "." +  te + "."      + uuid;
		return fileName;
	}

	public File getPath () {return _path;};
	public String getFileName  () {return _fileName;};
	public String getFirstName() {return _firstName;};
	public String getName() {return _firstName + " " + _lastName;};
	public String getLastName() {return _lastName;};
	public String getModality () {return _modality;};
	public String getDescription () {return _description;};
	public String getAcquisitionType () {return _acquisitionType;};
	public String getSeriesNumber () {return _seriesNumber;};
	public String getInstanceNumber () {return _instanceNumber;};
	public String getUUID () {return _uuid;};
	public Date getAcquisitionDateTime () {return _dateTime;};
	public Date getExportDateTime () {return _dateTimeExported;};
	public String getExtension () {return _ext;};


	public void print () {
		System.out.println("  Path                = " + _path);
		System.out.println("  File name           = " + _fileName);
		System.out.println("  File ext.           = " + _ext);
		if (_firstName!=null && _lastName!=null) {
			System.out.println("  Patient name        = " + _firstName + " " + _lastName);
		} else if (_lastName!=null) {
			System.out.println("  Patient (last) name = " + _lastName);
		}
		System.out.println("  Modality            = " + _modality);
		System.out.println("  Description         = " + _description);
		System.out.println("  Series Number       = " + _seriesNumber);
		System.out.println("  Acquisition type    = " + _acquisitionType);
		System.out.println("  Instance number     = " + _instanceNumber);
		System.out.println("  UUID                = " + _uuid);
		System.out.println("  Date/time           = " + _dateTime);
	}


	public void printToWriter (PrintWriter writer) {
		if (writer==null) return;
		writer.println("  Path             = " + _path);
		writer.println("  File name        = " + _fileName);
		writer.println("  File ext.        = " + _ext);
		writer.println("  Patient name     = " + _firstName + " " + _lastName);
		writer.println("  Modality         = " + _modality);
		writer.println("  Description      = " + _description);
		writer.println("  Series Number    = " + _seriesNumber);
		writer.println("  Acquisition type = " + _acquisitionType);
		writer.println("  Instance number  = " + _instanceNumber);
		writer.println("  UUID             = " + _uuid);
		writer.println("  Date/time        = " + _dateTime);
	}
}
