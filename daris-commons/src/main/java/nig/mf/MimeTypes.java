package nig.mf;

public class MimeTypes {

	private MimeTypes() {
	};

	//
	// Asset mime types
	//
	
	// DICOM Image Series
	public static final String DICOM_SERIES = "dicom/series";

	// Analyze Image Series (Neurological)
	public static final String ANALYZE_SERIES_RL = "analyze/series/rl";

	// Analyze Image Series (Radiological)
	public static final String ANALYZE_SERIES_NL = "analyze/series/nl";

	// Analyze Image Series
	public static final String ANALYZE_SERIES = "analyze/series";

	// NIFTI Image Series
	public static final String NIFTI_SERIES = "nifti/series";

	// Bruker Image Series
	public static final String BRUKER_SERIES = "bruker/series";
	
	// MINC Image Series
	public static final String MINC_SERIES = "minc/series";

	// Bruker FID 
	public static final String BRUKER_FID = "bruker/fid";

	// Siemens RDA file
	public static final String SIEMENS_RDA = "siemens/rda";
	
	// Siemens raw data Study asset mime type (combines PET and CT)
	public static final String PETCT_RAW_STUDY_MIME_TYPE = "siemens-raw-petct/study";
	public static final String MR_RAW_STUDY_MIME_TYPE = "siemens-raw-mr/study";

	// Siemens  raw data Series asset mime types ; one for each of PET and CT
	public static final String PET_RAW_SERIES_MIME_TYPE = "siemens-raw-pet/series";
	public static final String CT_RAW_SERIES_MIME_TYPE = "siemens-raw-ct/series";
	public static final String MR_RAW_SERIES_MIME_TYPE = "siemens-raw-mr/series";

	//
	// Content Mime types
	//
	
	// Siemens raw data content type for PET and CT (not zipped)
	public static final String RAW_PET_CONTENT_MIME_TYPE = "application/siemens-raw-pet";
	public static final String RAW_CT_CONTENT_MIME_TYPE = "application/siemens-raw-ct";

	// ZIP file
	public static final String ZIP = "application/x-zip";
	
	// AAR file
	public static final String AAR = "application/arc-archive";
	
	// TAR file
	public static final String TAR = "application/x-tar";




}
