package nig.mf.plugin.pssd.dicom.study;

import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dicom.StudyProxy;

/**
 * The PSSD study occurs under an ex-method. The incoming configuration
 * can connect to either:
 * 
 *  1. The subject, in which case the ex-method and study may need to 
 *     be created. If existing method, then or directly to the study.
 *     
 *  2. The study, which has already been created. In that case, there
 *     is nothing to do.
 *     
 * @author Jason
 *
 */
public class PSSStudyProxy extends StudyProxy {

	private String _ns;
	private String _patient;
	private String _study;
	private StudyMetadata _sm;
	
	/**
	 * Constructor.
	 * 
	 * @param studyId
	 */
	public PSSStudyProxy(String namespace,String studyId,String patientCID,String studyCID,StudyMetadata sm) {
		super(studyId);
		
		_ns      = namespace;
		_patient = patientCID;
		_study   = studyCID;
		_sm      = sm;
	}

	public long createOrUpdateAsset(ServiceExecutor executor) throws Throwable {
		// TODO Auto-generated method stub
		return 0;
	}

	public void destroyAsset(ServiceExecutor executor) throws Throwable {
		// TODO Auto-generated method stub
		
	}
	
	public boolean assetExists (ServiceExecutor executor) throws Throwable {
		// TODO Auto-generated method stub
		return false;
	}


}
