package nig.mf.plugin.pssd.dicom.series;

import java.io.File;
import java.io.FileInputStream;

import nig.mf.plugin.pssd.dicom.DICOMMetaUtil;
import nig.mf.plugin.pssd.dicom.DicomElements;
import nig.mf.plugin.pssd.dicom.study.PSSDStudyProxy;
import nig.mf.plugin.pssd.dicom.study.StudyMetadata;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dicom.SeriesProxy;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;




public class PSSDSeriesProxy extends SeriesProxy {

	private PSSDStudyProxy _study;
	private SeriesMetadata _sm;
	private boolean        _createdSeries;
	private String         _series;
	private StudyMetadata _studyMeta;


	public PSSDSeriesProxy(PSSDStudyProxy study,int id,SeriesMetadata sm) {
		super(sm.UID(), id);

		_study = study;
		_sm = sm;
		_createdSeries = false;
		_studyMeta = _study.metaData();
	}


	/**
	 * The asset will be a 'derived' DataSet. If there is a matching (by UID) Bruker DataSet
	 *  use that to establish the name of this DataSet and to set its derivation origins
	 *  
	 */
	public long createOrUpdateAsset(ServiceExecutor executor, long study, File data, String mimeType, int imin,int imax,int size) throws Throwable {
		String sid = _study.id();
		
		// Drop the SR modality if non-human and configured for dropping
		if (dropDoseReport (executor, sid, _sm.modality(), _study.dropDoseReports())) {
			System.out.println("Dropping Series of modality" + _sm.modality());
			return 0;
		}

		// If this DICOM series already exists, we are going to overwrite it.
		_series = findExistingDICOMSeries(executor,sid,_sm.UID());
		String prot = _sm.protocol();
		String desc = _sm.description();
		String id = _sm.id();

		// Do the data originate from a Bruker scanner. 
		boolean isBruker = DICOMMetaUtil.isBrukerScanner(_studyMeta);

		// Set some DataSet metadata and especially the name of the DataSet
		XmlDocMaker dm = new XmlDocMaker("args");
		String name = setDataSetName(isBruker, prot, desc, _sm.UID(), id);
		if (name != null) dm.add("name", name);

		// See what is available for the object description
		if ( desc != null ) {
			dm.add("description",desc);
		} else if ( prot != null) {
			dm.add("description", prot);
		}

		// If this DataSet is derived from a Bruker DataSet, add that information
		// FInd whether there is a pre-existing Bruker series for this Study
		// If there is we will get the 'vid' parameter (to use in derivation specification)
		if (isBruker) {
			String brukerCID = findExistingBrukerSeries(executor, sid, _sm.UID());
			if (brukerCID != null) {
				String brukerVid = getExistingBrukerDetails (executor, brukerCID);
				if (brukerVid!=null) {
					dm.add("input", new String[] {"vid", brukerVid}, brukerCID);
				}
			}
		}	

		// We can get the ExMethod and Step from the Study, so might as well supply it..
		dm.push("method");
		dm.add("id",_study.exMethod());
		String step = _study.methodStep();
		dm.add("step",step);
		if (step == null) step = "1";               // Shouldn't happen but seems to sometimes...
		dm.pop();

		// Add meta-data	
		dm.push("meta");

		// Add Mediaflux generic DICOM meta-data
		dm.push("mf-dicom-series",new String[] { "ns", "dicom" });

		// ds.add(new XmlDoc.Element("idx",String.valueOf(idx)));
		dm.add("uid",_sm.UID());
		dm.add("id",_sm.id());

		if ( desc != null ) {
			dm.add("description", desc);
		}

		if ( _sm.modality() != null ) {
			dm.add("modality",_sm.modality());
		}

		if ( prot != null ) {
			dm.add("protocol",prot);
		}

		if ( _sm.seriesDateTime() != null ) {
			dm.add("sdate",_sm.seriesDateTime());
		}
		
	    if ( _sm.acquisitionDateTime() != null ) {
	        dm.add("adate",_sm.acquisitionDateTime());
	    }

		if ( imin != -1 ) {
			// MF 3.6.052 allows imin,imax>=0
			dm.add("imin",imin);
			dm.add("imax",imax);
		}
		dm.add("size",size);
		
		double [] imagePositions = _sm.imagePosition();
		double [] imageOrientations = _sm.imageOrientation();
		if ( (imagePositions!=null && imagePositions.length==3)  ||
				(imageOrientations!=null && imageOrientations.length==6) ) {
			dm.push("image");	
			//
			if (imagePositions != null && imagePositions.length==3) {  
				dm.push("position");
				String[] loc = new String[]{"x", "y", "z"};
				for (int i=0; i<imagePositions.length; i++) {
					dm.add(loc[i], imagePositions[i]);
				}
				dm.pop();
			}
			//
			if (imageOrientations != null && imageOrientations.length==6) { 
				dm.push("orientation");
				for (int i=0; i<imageOrientations.length; i++) {
					dm.add("value", imageOrientations[i]);
				}
				dm.pop();
			}
			dm.pop();
		}
		dm.pop();
		dm.pop();

		// Create the DataSet.  DICOM DataSets are always derived (i.e. non-native 
		// acquisition format). 
		FileInputStream is = new FileInputStream(data);
		boolean processed = isProcessed (_sm.imageType());
		try {
			PluginService.Input in = new PluginService.Input(is,data.length(), mimeType,null);
			PluginService.Inputs ins = new PluginService.Inputs(in);

			if ( _series == null ) {
				dm.add("pid",sid);
				dm.add("type","dicom/series");
				dm.add("processed", processed);
				dm.add("fillin", false);
				XmlDoc.Element r = executor.execute("om.pssd.dataset.derivation.create",dm.root(),ins,null);
				_series = r.value("id");
				_createdSeries = true;
				
				// Extract additional meta-data that is not contained
				// in mf-dicom-series from the DICOM header
				try {
					addSpecificDICOMMeta(executor, _series);
				} catch (Throwable t) {
					// Don't fail the DICOM server if this service fails for some reason
				}
			} else {
				dm.add("id",_series);
				executor.execute("om.pssd.dataset.derivation.update",dm.root(),ins,null);
			}
		} finally {
			is.close();
		}

		// Interface says this should be the id of the asset...
		return 0;
	}
	
	/**
	 * Fetch additional desired DICOM elements from the header into indexed meta-data
	 * @param executor
	 * @param cid
	 * @throws Throwable
	 */
	private void addSpecificDICOMMeta (ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);		
		// Accession Number
		dm.add("tag", "00080050");           // Accession Number
		executor.execute("dicom.metadata.populate", dm.root());
	}
	
	public void destroyAsset(ServiceExecutor executor) throws Throwable {
		if ( !_createdSeries ) {
			return;
		}

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",_series);

		executor.execute("om.pssd.object.destroy",dm.root());

		_createdSeries = false;	
	}

	public boolean assetExists (ServiceExecutor executor) throws Throwable {

		// If this DICOM series already exists, we are going to overwrite it.
		String seriesCID = findExistingDICOMSeries (executor, _study.id(), _sm.UID());
		if (seriesCID!=null) {
			System.out.println("    ***Series " + _sm.UID() + " already exists in Study " + _study.id() + " with CID " + seriesCID + " - overwriting");
		}
		return (seriesCID!=null);
	}

	/**
	 * Find out if this DICOM data already exists as a DICOM DataSet under this Study
	 * Use the UID to find it.
	 * 
	 * @param executor
	 * @param study
	 * @param uid
	 * @return citable ID of DICOM DataSet
	 * @throws Throwable
	 */
	private String findExistingDICOMSeries(ServiceExecutor executor,String studyCID,String uid) throws Throwable {
		// Native MF Doc Type
		String query = "cid in '" + studyCID + "' and xpath(mf-dicom-series/uid)='" + uid + "'";

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where",query);
		dm.add("action","get-cid");
		dm.add("pdist", 0);		
		XmlDoc.Element r = executor.execute("asset.query",dm.root());
		return r.value("cid");
	}

	/**
	 * Find out if this DICOM data already exists as a Bruker DataSet under this Study
	 * Use the UID to find it.
	 * 
	 * @param executor
	 * @param studyCID
	 * @param uid
	 * @return citable ID of primary Bruker DataSet
	 * @throws Throwable
	 */
	private String findExistingBrukerSeries(ServiceExecutor executor,String studyCID,String uid) throws Throwable {
		// NIG Doc Type 
		String docType = "daris:bruker-series";
		if (!nig.mf.pssd.plugin.util.PSSDUtil.checkDocTypeExists(executor, docType)) return null;

		String query = "cid in '" + studyCID + "' and xpath(" + docType + "/uid)='" + uid + "'";		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where",query);
		dm.add("action","get-cid");
		dm.add("pdist", 0);
		XmlDoc.Element r = executor.execute("asset.query",dm.root());
		return r.value("cid");
	}

	/**
	 * Fish out the "vid" from the existing matched Bruker DataSet
	 * @param executor
	 * @param cid
	 * @return 
	 * @throws Throwable
	 */
	private String getExistingBrukerDetails(ServiceExecutor executor, String cid) throws Throwable {
		if (cid==null) return null;
		//
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", cid);
		XmlDoc.Element el = executor.execute("om.pssd.object.describe", dm.root());
		return el.value("object/vid");
	}

	/**
	 * Set the meta-data that describes the primary DataSet that this one will
	 * be derived from
	 * 
	 * @param dm
	 * @param brukerCID
	 * @throws Throwable
	 */
	private void setInputDerivation (ServiceExecutor executor, XmlDocMaker dm, String brukerCID, String vid) throws Throwable {			
		dm.add("input", new String[] {"vid", vid}, brukerCID);
	}





	private String setDataSetName (boolean isBruker, String prot, String desc, String uid, String id) {

		String name = null;
		if (isBruker) {

			// The data originate from a Bruker scanner.
			// The bit shifted id gives back the integer by which the folders
			// are named in the ParaVision console machine and by which the aMRIF staff 
			// identify data.
			name = "" + (Integer.parseInt(id)>>16);
			if (prot!=null) name += "_" + prot;
		} else {

			// Name the DataSet, based on the combination of protocol
			// and description.  Generally the protocol may stay fixed
			// and the description varies (e.g. describes online processing).
			if (prot != null && desc != null) {
				if (prot.equals(desc)) {
					name = prot;
				} else {
					name = prot + "_" + desc;
				}
			} else if (prot != null) {
				name = prot;
			} else if (desc != null) {
				name = desc;	
			}	
			// Fall back on Series number or UID if really needed for Series name
			if (name == null) {
				if (id != null) {
					name = id;
				}else if (uid != null) {
					name = uid;
				}
			}
		}
		return name;	
	}
	
	/**
	 * the ImageType will be of the form "[<A>, <B>, PRIMARY, <X>, <Y>< <Z>]" where
	 * the first two parts are required
	 * 
	 * @param imageType
	 * @return
	 * @throws Throwable
	 */
	private Boolean isProcessed (String imageType) throws Throwable {
		if (imageType==null) return false;
		String t = imageType.substring(1, imageType.length()-1);
		String[] parts = t.split(",");
		if (parts.length>=2) {
			if (parts[0].equalsIgnoreCase("DERIVED") || parts[1].equalsIgnoreCase("SECONDARY")) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	private Boolean dropDoseReport (ServiceExecutor executor, String sid, String modality, Boolean dropSR) throws Throwable {
		if (!dropSR) return false;
		if (!modality.equalsIgnoreCase("SR")) return false;
		
		// FInd the ExMethod from the Study
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", sid);
		XmlDoc.Element r = executor.execute("om.pssd.object.describe", dm.root());
		String eid = r.value("object/method/id");
		if (eid==null) return false;
		
		// Find the Method from the ExMethod
		dm = new XmlDocMaker("args");
		dm.add("id", eid);
		r = executor.execute("om.pssd.object.describe", dm.root());
		String mid = r.value("object/method/id");
		
		// Now see if it's a Human Method
		dm = new XmlDocMaker("args");
		dm.add("id", mid);
		r = executor.execute("om.pssd.object.describe", dm.root());
		XmlDoc.Element t = r.element("object/method/subject/project/human");       // RSubjects no longer used 
		if (t==null) return false;
		return !(t.booleanValue());    	
	}
}
