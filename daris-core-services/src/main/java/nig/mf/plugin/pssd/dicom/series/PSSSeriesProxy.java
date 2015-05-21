package nig.mf.plugin.pssd.dicom.series;

import java.io.File;

import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dicom.*;
import nig.mf.plugin.pssd.dicom.study.PSSStudyProxy;

public class PSSSeriesProxy extends SeriesProxy {

	private PSSStudyProxy _study = null;
	
	public PSSSeriesProxy(PSSStudyProxy study,String uid, int id) {
		super(uid, id);
		
		_study = study;
	}

	public long createOrUpdateAsset(ServiceExecutor executor, long study, File data, String mimeType, int imin,int imax, int size) throws Throwable {
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
