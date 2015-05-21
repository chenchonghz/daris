package daris.client.model.exmethod;

import arc.mf.client.xml.XmlElement;
import daris.client.model.study.Study;

public class StepItem {

	private String _path;
	private String _type;
	private String _modality;

	public StepItem(XmlElement se) {
		this(se.value(), se.value("@type"), se.value("@dicom-modality"));
	}
	
	public StepItem(String path, String type, String modality) {
		_path = path;
		_type = type;
		_modality = modality;
	}

	@Override
	public String toString() {
		return _path + ": " + _type;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof StepItem){
			StepItem so = (StepItem)o;
			return _path.equals(so.path()) && _type.equals(so.type());
		}
		return false;
	}

	/**
	 * The step path.
	 * 
	 * @return
	 */
	public String path() {
		return _path;
	}

	/**
	 * The study type.
	 * 
	 * @return
	 */
	public String type() {
		return _type;
	}

	/**
	 * The DICOM modality. Returns null if it is not a DICOM study.
	 * 
	 * @return
	 */
	public String modality() {
		return _modality;
	}

	public static StepItem fromStudy(Study study) {
		if(study!=null){
			if(study.stepPath()!=null && study.studyType()!=null){
				return new StepItem(study.stepPath(), study.studyType(), null);
			}
		}
		return null;
	}
}
