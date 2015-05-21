package nig.mf.pssd.client.bruker;

import java.util.Date;

import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import nig.util.DateUtil;


/**
 * Simple container class for the Bruker meta-data parsed from the SUbject
 * identifier string by the Bruker client utilised at the Neuroimaging group. This is totally
 * not portable.
 * 
 * The pattern is
 * 
 *  <Project Description>_<Coil>_<Animal ID>_<Gender>_<Experiment Group>_<Invivo/exvivo>_<date><delim><cid>
 * 
 * @author nebk
 *
 */
public class NIGBrukerIdentifierMetaData {

	// These need to match nig-daris:pssd-amrif-subject
	String[] _coils = new String[] {"B4C", "U72"};
	String [] _genders = new String[] {"M", "F", "X"};
	String[] _vivos = new String[] {"I", "E"};

	private String _projectDescriptor;
	private String _coil;
	private String _animalID;
	private String _gender;
	private String _expGroup;
	private String _vivo;
	private Date _date;

	
	/**
	 * Create the meta-data from the array (project description, coil, animalID, gender, expGroup, vivo, date)
	 * 
	 * @param dem
	 * @return
	 * @throws Throwable
	 */
	public NIGBrukerIdentifierMetaData (String[] parts) throws Throwable {
		if (parts.length!=7) {
			String errMsg = "The subject identifier was not of the correct form to extract the NIG meta-data";
			throw new Exception(errMsg);
		}	

		init();
		restoreFromVector(parts);
	}
	
	public static NIGBrukerIdentifierMetaData createFrom(XmlDoc.Element meta) throws Throwable {
		NIGBrukerIdentifierMetaData sm = new NIGBrukerIdentifierMetaData();
		sm.fromXML(meta);
		return sm;
	}

	
	public String projectDescriptor () {
		return _projectDescriptor;
	}
	
	public String coil () {
		return _coil;
	}
	
	public String animalID () {
		return _animalID;
	}
	
	public String gender () {
		return _gender;
	}

	public String experimentalGroup () {
		return _expGroup;
	}
	
	
	public String vivo () {
		return _vivo;
	}
	
	public Date date () {
		return _date;
	}
	
	/**
	 * Format date in Mediaflux style, dd-MM-yyyy
	 * 
	 * @return
	 */
	public String formatDate () throws Throwable {
		return DateUtil.formatDate(_date, "dd-MMM-yyyy");

	}
	
	public String toString () {
		return _projectDescriptor + "_" +
		       _coil + "_" +
		       _animalID + "_" +
		       _gender + "_" +
		       _expGroup + "_" +
		       _vivo + "_" + 
		       _date;
	}
	
	/**
	 * Convert to an XmlDoc.Element for transmission to the service layer
	 * 
	 * @return
	 * @throws Throwable
	 */
	public XmlDoc.Element toXML () throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("bruker");
		if (_projectDescriptor!=null) dm.add("project_descriptor", _projectDescriptor);
		if (_coil!=null) dm.add("coil", _coil);
		if (_animalID!=null) dm.add("animal_id", _animalID);
		if (_date!=null) dm.add("date", _date);
		if (_gender!=null) dm.add("gender", _gender);
		if (_expGroup!=null) dm.add("exp_group", _expGroup);
		if (_vivo!=null) dm.add("vivo", _vivo);
		//
		return dm.root();
	}

	/**
	 * Convert from an XmlDoc.Element from  the service layer interface
	 * 
	 * @return
	 * @throws Throwable
	 */
	private void fromXML (XmlDoc.Element meta) throws Throwable {
		
		// It could be that only a subset of DICOM elements are provided
		_projectDescriptor =  meta.value("project_descriptor");
		_coil = meta.value("coil");
		_animalID = meta.value("animal_id");
		_date = meta.element("date").hasValue() ? meta.dateValue("date") : null;
		_gender = meta.value("gender");
		_expGroup = meta.value("exp_group");
		_vivo = meta.value("vivo");
	}

	
	//
	private void restoreFromVector (String[] parts) throws Throwable {
		
		_projectDescriptor = parts[0];
		if (check(_coils, parts[1])) _coil = parts[1];
		_animalID = parts[2];
		if (check(_genders, parts[3])) _gender = parts[3];
		_expGroup = parts[4];
		if (check(_vivos, parts[5])) _vivo = parts[5];

		// There is no century in the year string. Add on "20"
		Date d = DateUtil.dateFromString("20"+parts[6], "yyyyMMdd");
		_date = d;
	}
	
	private boolean check (String[] allowed, String value) {
		for (int i=0; i<allowed.length; i++) {
			if (allowed[i].equals(value)) return true;
		}
		return false;
	}
	
	
	private NIGBrukerIdentifierMetaData() {
		init();	
	}

	private void init () {
		_projectDescriptor = null;
		_coil = null;
		_animalID = null;
		_gender = null;
		_expGroup = null;
		_vivo = null;
		_date = null; 
	}

}
