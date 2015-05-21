package nig.mf.plugin.pssd.dicom.study;



public class CIDAndMethodStep {

	String _cid = null;
	String _methodStep = null;

	public CIDAndMethodStep (String cid, String methodStep) {
		_cid = cid;
		_methodStep = methodStep;
	}

	public CIDAndMethodStep () {
		_cid = null;
		_methodStep = null;
	}

	public void setMethodStep (String step) {
		_methodStep = step;
	}

	public void setCID (String cid) {
		_cid = cid;
	}

	public String methodStep () {
		return _methodStep;
	}

	public String cid () {
		return _cid;
	}

	public String toString  () {
		return _cid + "_" + _methodStep;
	}
}

