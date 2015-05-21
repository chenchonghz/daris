package nig.mf.plugin.pssd.dicom;

import java.util.*;


/**
 * Ingestion controls for the NIG.DICOM engine
 * These controls needs to match what is in the DicomAssetHandlerFactory
 * 
 * @author Jason Lohrey
 *
 */
public class DicomIngestControls {

	/**
	 * Don't identify.
	 */
	public static final int ID_NONE = 0;

	/**
	 * Uniquely identify patient by DICOM element (0x0010,0x0020).
	 */
	public static final int ID_BY_PATIENT_ID = 1;

	/**
	 * Uniquely identify patient by DICOM element (0x0010,0x0010).
	 */
	public static final int ID_BY_PATIENT_FULL_NAME = 2;

	/**
	 * Uniquely identify patient by first name from DICOM element (0x0010,0x0010).
	 */
	public static final int ID_BY_PATIENT_FIRST_NAME = 3;

	/**
	 * Uniquely identify patient by last name from DICOM element (0x0010,0x0010).
	 */
	public static final int ID_BY_PATIENT_LAST_NAME = 4;

	/**
	 * Uniquely identify by last name from DICOM element (0x0020,0x0010).
	 */
	public static final int ID_BY_STUDY_ID = 5;

	/**
	 * Uniquely identify by  DICOM element ReferringPhysicianName (0x0008,0x0090).
	 */
	public static final int ID_BY_REFERRING_PHYSICIAN_NAME = 6;

	/**
	 * Uniquely identify by  DICOM element Performing Physician (0x0008,0x1050).
	 */
	public static final int ID_BY_PERFORMING_PHYSICIAN = 7;

	/**
	 * Uniquely identify by  DICOM element ReferringPhysicianPhone (0x0008,0x0094).
	 */
	public static final int ID_BY_REFERRING_PHYSICIAN_PHONE = 8;

	
	
	public static class ExInvalidSetting extends Throwable {
		public ExInvalidSetting(String setting,String error) {
			super("NIG.DICOM: invalid argument [" + setting + "]: " + error);
		}
	}

	private int[]     _cidElements;
	private String    _cidPrefix;
	private int       _minCidDepth;
	private Boolean   _ignoreNonDigits;
	private String    _ignoreBeforeLastDelim;
	private String    _ignoreAfterLastDelim;
	private String 	  _citableID;
	private String    _findSubjectMethod;
	private Boolean   _autoSubjectCreate;
	private Boolean   _cloneFirstSubject; 
	private String    _subjectMetaService;
	private Boolean   _ignoreModality; 
	
	// Controls set subject name from DICOM Patient Name or ID
	private Boolean   _setSubjectNameFromFirst;         // Set from first name
	private Boolean   _setSubjectNameFromLast;          // Set from last name
	private Boolean   _setSubjectNameFromFull;          // Set from full name
	private Boolean   _setSubjectNameFromID;            // Set from ID
	private String    _setSubjectNameFromIgnoreAfterLastDelim;  // Ignore chars after (and incl) last delim
	private String    _setSubjectNameFromIndexRange;    // Select chars only in given range of indices (a start and end pair such as "0,11")

	//
	private String    _projectSelector;
	private Boolean   _writeDICOMPatient;
	private Boolean   _dropDoseReports;         // Not needed for non-humans

	public DicomIngestControls() {
		_cidElements     = null;
		_citableID = null;
		_autoSubjectCreate = false;
		_cloneFirstSubject = false;
		_findSubjectMethod = "name";
		_setSubjectNameFromFirst = false;
		_setSubjectNameFromLast = false;
		_setSubjectNameFromFull = false;
	    _setSubjectNameFromIgnoreAfterLastDelim = null;
		_setSubjectNameFromID = false;
		_setSubjectNameFromIndexRange = null;

		// Minimum CID depth to be considered a CID..
		_minCidDepth  = 3;
		_ignoreNonDigits = false;
		_ignoreBeforeLastDelim = null;
		_ignoreAfterLastDelim = null;
		_cidPrefix = null;

		_ignoreModality = false;
		_projectSelector = null;
		_writeDICOMPatient = false;
		_dropDoseReports = false;
	}

	

	public String cidPrefix() {
		return _cidPrefix;
	}

	public int minCidDepth() {
		return _minCidDepth;
	}

	public int[] cidElements() {
		return _cidElements;
	}

	public Boolean ignoreNonDigits() {
		return _ignoreNonDigits;
	}
	public String ignoreBeforeLastDelim () {
		return _ignoreBeforeLastDelim;
	}
	public String ignoreAfterLastDelim () {
		return _ignoreAfterLastDelim;
	}
	public String citableID () {
		return _citableID;
	}
	
	public String findSubjectMethod () {
		return _findSubjectMethod;
	}
	public Boolean setSubjectNameFromID () {
		return _setSubjectNameFromID;
	}
	public Boolean setSubjectNameFromFirst () {
		return _setSubjectNameFromFirst;
	}
	public Boolean setSubjectNameFromLast () {
		return _setSubjectNameFromLast;
	}
	public Boolean setSubjectNameFromFull () {
		return _setSubjectNameFromFull;
	}
	public String setSubjectNameFromIgnoreAfterLastDelim () {
	    return _setSubjectNameFromIgnoreAfterLastDelim;
	}
	public String setSubjectNameFromIndexRange () {
	   return _setSubjectNameFromIndexRange;  
	}
	public Boolean autoSubjectCreate () {
		return _autoSubjectCreate;
	}
	public Boolean cloneFirstSubject () {
		return _cloneFirstSubject;
	}
	public String subjectMetaService () {
		return _subjectMetaService;
	}

	public Boolean ignoreModality () {
		return _ignoreModality;
	}
	public String projectSelector () {
		return _projectSelector;
	}
	public Boolean writeDICOMPatient () {
		return _writeDICOMPatient;
	}
	public Boolean dropSR () {
		return _dropDoseReports;
	}

	/**
	 * Configure controls by reading either directly from the command line (e.g. dicom.ingest :arg -name nig.dicom.id.citable 1.2.3.4)
	 * or from the network configuration
	 * 
	 * @param args
	 * @throws Throwable
	 */
	protected void configure(Map<String,String> args) throws Throwable {
			
		// Root namespace for storing data.
		//_ns = (String)args.get("nig.dicom.asset.namespace.root");

		// Either the Citable ID is directly specified by the caller or it is
		// extracted from the DICOM metadata.  This control is not passed on to the
		// DICOM server when using a DICOM client.
		_citableID = (String)args.get("nig.dicom.id.citable");

		if (_citableID == null) {
			String idBy = (String)args.get("nig.dicom.id.by");
			if ( idBy != null ) {
				StringTokenizer st = new StringTokenizer(idBy,",");
				_cidElements = new int[st.countTokens()];

				int i = 0;
				while ( st.hasMoreTokens() ) {
					String tok = st.nextToken();

					if ( tok.equalsIgnoreCase("patient.id") ) {
						_cidElements[i] = ID_BY_PATIENT_ID;
					} else if ( tok.equalsIgnoreCase("patient.name") ) {
						_cidElements[i] = ID_BY_PATIENT_FULL_NAME;
					} else if ( tok.equalsIgnoreCase("patient.name.first") ) {
						_cidElements[i] = ID_BY_PATIENT_FIRST_NAME;
					} else if ( tok.equalsIgnoreCase("patient.name.last") ) {
						_cidElements[i] = ID_BY_PATIENT_LAST_NAME;
					} else if ( tok.equalsIgnoreCase("study.id") ) {
						_cidElements[i] = ID_BY_STUDY_ID;
					} else if ( tok.equalsIgnoreCase("referring.physician.name") ) {
						_cidElements[i] = ID_BY_REFERRING_PHYSICIAN_NAME;
					} else if ( tok.equalsIgnoreCase("referring.physician.phone") ) {
						_cidElements[i] = ID_BY_REFERRING_PHYSICIAN_PHONE;
					} else if ( tok.equalsIgnoreCase("performing.physician.name") ) {
						_cidElements[i] = ID_BY_PERFORMING_PHYSICIAN;
					} else {
						throw new ExInvalidSetting("nig.dicom.id.by","expected one of [patient.id, patient.name, patient.name.first, patient.name.last, study.id, performing.physician.name, referring.physician.phone] for id.patient.by - found: " + idBy);
					}

					i++;
				}
			}

			String ignoreChars = (String)args.get("nig.dicom.id.ignore-non-digits");
			if ( ignoreChars != null ) {
				if ( ignoreChars.equalsIgnoreCase("true") ) {
					_ignoreNonDigits = true;
				} else if ( ignoreChars.equalsIgnoreCase("false") ) {
					_ignoreNonDigits = false;
				} else {
					throw new ExInvalidSetting("nig.dicom.id.ignore-non-digits","expected one of [true,false] - found: " + ignoreChars);
				}
			}	
			
			_ignoreBeforeLastDelim = (String)args.get("nig.dicom.id.ignore-before-last-delim");
			_ignoreAfterLastDelim = (String)args.get("nig.dicom.id.ignore-after-last-delim");
		}

		_cidPrefix = (String)args.get("nig.dicom.id.prefix");
		
		// Method for finding Subjects
		String findSubject = (String)args.get("nig.dicom.subject.find.method");
		if (findSubject != null) {
			if ( findSubject.equalsIgnoreCase("id") ) {
				_findSubjectMethod = "id"; 
			} else if (findSubject.equalsIgnoreCase("name") ) {
				_findSubjectMethod = "name";
			} else if (findSubject.equalsIgnoreCase("name+") ) {
				_findSubjectMethod = "name+";
			} else if (findSubject.equalsIgnoreCase("all") ) {
				_findSubjectMethod = "all";
			} else {
				throw new ExInvalidSetting("nig.dicom.subject.find.method","expected one of [id,name,name+,all] - found: " + findSubject);
			}		
		}

		// Auto SUbject creation
		String createSubject = (String)args.get("nig.dicom.subject.create");
		if ( createSubject != null ) {
			if ( createSubject.equalsIgnoreCase("true") ) {
				_autoSubjectCreate = true;
			} else if ( createSubject.equalsIgnoreCase("false") ) {
				_autoSubjectCreate = false;
			} else {
				throw new ExInvalidSetting("nig.dicom.subject.create","expected one of [true,false] - found: " + createSubject);
			}
		}	
		
		// Name the Subject object from the DICOM name or ID
		String setName = (String)args.get("nig.dicom.subject.name.from.last");
		if (setName != null) {
			if ( setName.equalsIgnoreCase("true") ) {
				_setSubjectNameFromLast = true;
			} else if ( setName.equalsIgnoreCase("false") ) {
				_setSubjectNameFromLast = false;
			} else {
				throw new ExInvalidSetting("nig.dicom.subject.name.from.last","expected one of [true,false] - found: " + setName);
			}
		}
		setName = (String)args.get("nig.dicom.subject.name.from.first");
		if (setName != null) {
			if ( setName.equalsIgnoreCase("true") ) {
				_setSubjectNameFromFirst = true;
			} else if ( setName.equalsIgnoreCase("false") ) {
				_setSubjectNameFromFirst = false;
			} else {
				throw new ExInvalidSetting("nig.dicom.subject.name.from.first","expected one of [true,false] - found: " + setName);
			}
		}
		setName = (String)args.get("nig.dicom.subject.name.from.full");
		if (setName != null) {
			if ( setName.equalsIgnoreCase("true") ) {
				_setSubjectNameFromFull = true;
			} else if ( setName.equalsIgnoreCase("false") ) {
				_setSubjectNameFromFull = false;
			} else {
				throw new ExInvalidSetting("nig.dicom.subject.name.from.full","expected one of [true,false] - found: " + setName);
			}
		}
		//
		setName = (String)args.get("nig.dicom.subject.name.from.id");
		if (setName != null) {
			if ( setName.equalsIgnoreCase("true") ) {
				_setSubjectNameFromID = true;
			} else if ( setName.equalsIgnoreCase("false") ) {
				_setSubjectNameFromID = false;
			} else {
				throw new ExInvalidSetting("nig.dicom.subject.name.from.id","expected one of [true,false] - found: " + setName);
			}
		}
		_setSubjectNameFromIgnoreAfterLastDelim = (String)args.get("nig.dicom.subject.name.from.ignore-after-last-delim");;
		_setSubjectNameFromIndexRange = (String)args.get("nig.dicom.subject.name.from.index.range");;
		//
		
		// Clone SUbject 
		String cloneSubject = (String)args.get("nig.dicom.subject.clone_first");
		if ( cloneSubject != null ) {
			if ( cloneSubject.equalsIgnoreCase("true") ) {
				_cloneFirstSubject = true;
			} else if ( cloneSubject.equalsIgnoreCase("false") ) {
				_cloneFirstSubject = false;
			} else {
				throw new ExInvalidSetting("nig.dicom.subject.clone_first","expected one of [true,false] - found: " + cloneSubject);
			}
		}	

		// Service to update meta-data by parsing the DICOM meta-data in a domain-specific way
		_subjectMetaService = (String)args.get("nig.dicom.subject.meta.set-service");

		//
		String ignoreModality = (String)args.get("nig.dicom.modality.ignore");
		if ( ignoreModality != null ) {
			if ( ignoreModality.equalsIgnoreCase("true") ) {
				_ignoreModality = true;
			} else if ( ignoreModality.equalsIgnoreCase("false") ) {
				_ignoreModality = false;
			} else {
				throw new ExInvalidSetting("nig.dicom.modality.ignore","expected one of [true,false] - found: " + ignoreModality);
			}
		}	
		
		// Place constraints on certain users so that they can only access certain projects
		_projectSelector = (String)args.get("nig.dicom.project.selector");
		
		// Tell the server to write mf-dicom-patient on the Subject
		String writePatient = (String)args.get("nig.dicom.write.mf-dicom-patient");
		if ( writePatient != null ) {
			if ( writePatient.equalsIgnoreCase("true") ) {
				_writeDICOMPatient = true;
			} else if ( writePatient.equalsIgnoreCase("false") ) {
				_writeDICOMPatient = false;
			} else {
				throw new ExInvalidSetting("nig.dicom.write.mf-dicom-patient","expected one of [true,false] - found: " + writePatient);
			}
		}	
		
		
		// Autoamted processes at CT scanners may upload automatics structured dose reports
		// Sometimes on their own requiring special step/modality. Allow the server
		// to drop these for non-humans (not required)
		String dropDoseReports = (String)args.get("nig.dicom.dose-reports.drop");
		if (dropDoseReports!=null) {
			if (dropDoseReports.equalsIgnoreCase("true") ) {
				_dropDoseReports = true;
			} else if (dropDoseReports.equalsIgnoreCase("false")) {
				_dropDoseReports = false;
			} else {
				throw new ExInvalidSetting("nig.dicom.dose-reports.drop","expected one of [true,false] - found: " + dropDoseReports);
			}

		}
	}
}
