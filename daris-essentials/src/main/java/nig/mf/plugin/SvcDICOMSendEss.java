package nig.mf.plugin;

import java.io.File;
import java.util.Collection;

import nig.dicom.util.DicomModify;
import nig.io.FileUtils;
import nig.mf.dicom.plugin.util.DICOMModelUtil;
import nig.mf.plugin.util.ArchiveUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.SetOfDicomFiles;
import com.pixelmed.network.StorageSOPClassSCU;


public class SvcDICOMSendEss extends PluginService {
	private Interface _defn;

	public SvcDICOMSendEss() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element(
				"patient",
				AssetType.DEFAULT,
				"The asset id of the Patient (mf-dicom-patient) record for which all associated data will be sent.",
				0, Integer.MAX_VALUE));
		_defn.add(new Interface.Element(
				"study",
				AssetType.DEFAULT,
				"The asset id of the Study (mf-dicom-study) record for which all associated data will be sent.",
				0, Integer.MAX_VALUE));
		_defn.add(new Interface.Element(
				"series",
				AssetType.DEFAULT,
				"The asset id of the Series (mf-dicom-series) record which will be sent.",
				0, Integer.MAX_VALUE));
		_defn.add(new Interface.Element(
				"where",
				StringType.DEFAULT,
				"A query to find assets (local to this server only) to send; use this rather than patient/study/series arguments. No checks are made that these are DICOM assets until attempting to send.",
				0, 1));
		//
		Interface.Element me = new Interface.Element("ae",XmlDocType.DEFAULT,
				"The DICOM Application Entity (e.g. a DICOM server).",0,1);
		me.add(new Interface.Element("host", StringType.DEFAULT, "The AE host name or IP address.", 1, 1));
		me.add(new Interface.Element("port", IntegerType.DEFAULT, "The port number of the AE", 1, 1));
		me.add(new Interface.Element("aet",StringType.DEFAULT,"The AET of the AE.", 1, 1));
		_defn.add(me);

		//
		_defn.add(new Interface.Element("aet", StringType.DEFAULT, "The calling (our) AET.", 1, 1));

		_defn.add(new Interface.Element(
				"patient-name-action",
				new EnumType(new String[] { "unchanged", "anonymize" }),
				"Sets the action performed on the patient name field (0010,0010) of the DICOM file header before sending. Defaults to unchanged. Note: it will not change the local objects but only the intermediate files extracted from the objects.",
				0, 1));
		
		me = new Interface.Element("cid", CiteableIdType.DEFAULT, "Citable ID to encode in temporary DICOM meta-data before sending.", 0, 1);
		me.add(new Interface.Attribute("group", StringType.DEFAULT,  "The DICOM group component to overload with the CID. Part of the form <group,element>."));
		me.add(new Interface.Attribute("element", StringType.DEFAULT,  "The DICOM element component to overload with the CID. Part of the form <group,element>"));
		_defn.add(me);
		//
		_defn.add(new Interface.Element(
				"exception-on-fail",
				BooleanType.DEFAULT,
				"Behaviour on failure; the default is to throw an exception.  Otherwise it will continue for each DataSet and wirte summaries to the output XMLWriter.",
				0, 1));
		_defn.add(new Interface.Element("log", BooleanType.DEFAULT, "Write an entry in the mediaflux log file as assets are sent (defaults to false).", 0, 1));
	}

	@Override
	public String name() {

		return "dicom.send";
	}

	@Override
	public String description() {

		return "Sends DICOM/Series assets (DICOM data model) to a DICOM Application Entity (server).";
	}

	@Override
	public Interface definition() {

		return _defn;
	}

	@Override
	public Access access() {

		return ACCESS_ACCESS;
	}

	@Override
	public boolean canBeAborted() {

		return true;
	}

	@Override
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// Parse
		Collection<String> patients = args.values("patient");
		Collection<String> studies = args.values("study");
		Collection<String> series = args.values("series");
		if (patients!=null) {
			if (studies!=null || series!=null) {
				throw new Exception ("You can only give one of patient/study/series");
			}
		} else {
			if (studies!=null && series !=null) {
				throw new Exception ("You can only give one of patient/study/series");
			}
		}
		//
		String where = args.value("where");
		if (where!=null) {
			if (patients !=null || studies!=null || series!=null) {
				throw new Exception ("You can only give one of 'where' and 'patient/study/series'");
			}
		}
		//
		if (patients==null && studies==null && series==null && where==null) {
			throw new Exception("One of 'where' and 'patient/study/series' must be supplied");
		}
		//
		String patientNameAction = args.stringValue("patient-name-action", "unchanged");
		boolean anonymise = false;
		if (patientNameAction.equalsIgnoreCase("anonymize")) anonymise = true;
		Boolean exceptionOnFail = args.booleanValue("exception-on-fail", true);
		Boolean writeLog = args.booleanValue("log", false);
		XmlDoc.Element cid = args.element("cid");
		if (cid!=null) {
			String group = cid.value("@group");
			String element = cid.value("@element");
			if (group==null || element==null) {
				throw new Exception ("You must give both the group and element to specify the DICOM meta-data element holding the CID.");
			}
			
			if (anonymise) {
				if (group.equals("0010") && element.equals("0010")) {
					throw new Exception ("You have asked to anonymize the patient name and overload it with a CID. This is not self consistent.");
				}
			}
		}
		//
		String host = args.value("ae/host");
		Integer port = args.intValue("ae/port");
		String calledAET = args.value("ae/aet");
		String callingAET = args.value("aet");
		int compressionLevel = 0;
		int debugLevel = 0;

		PluginTask.checkIfThreadTaskAborted();

		// Find the DataSets with DICOM content. 
		Collection<String> assets = DICOMModelUtil.findSeries (executor(), where, patients, studies, series);

		// Iterate over DataSets. An option could be added to copy all files
		// from all DataSets to a temporary folder and then sent in one giant
		// push.
		if (assets != null) {
			int nDataSets = assets.size();
			PluginTask.threadTaskBeginSetOf(nDataSets);
			for (String asset : assets) {
				PluginTask.checkIfThreadTaskAborted();

				// Unpack content  into temporary directory
				// TODO: clean up the temp files if aborted?
				File tempDir = createTemporaryDirectory();
				SetOfDicomFiles dcmFiles = new SetOfDicomFiles();
				//
				PluginTask.setCurrentThreadActivity("Extracting " + (anonymise ? "and anonymizing" : "")
						+ " DICOM files in asset " + asset);
				PluginTask.checkIfThreadTaskAborted();
				//
				ArchiveUtil.unpackContent (executor(), asset, tempDir);

				// Edit files if required and put into DICOM files container
				editDICOM (tempDir, anonymise, cid, dcmFiles);

				PluginTask.checkIfThreadTaskAborted();
				// TODO: clean up the temp files if aborted?
				DICOMHandler dh = new DICOMHandler();

				try {
					PluginTask.setCurrentThreadActivity("Sending DICOM dataset " + asset);
					new StorageSOPClassSCU(host, port, calledAET, callingAET, dcmFiles, compressionLevel, dh, null,
							0, debugLevel);
					if (dh.getNumberFailed() > 0) {
						if (exceptionOnFail) {
							throw new Exception("Failed to send DICOM data for cid " + asset);
						}
						w.add("asset", new String[] { "status", "fail", "completed", "reason", "transfer failed",
								"" + dh.getNumberCompleted(), "failed", "" + dh.getNumberFailed() }, asset);
						if (writeLog) System.out.println("Asset " + asset + " not successfully sent");
					} else {
						w.add("asset", new String[] { "status", "pass", "completed", "" + dh.getNumberCompleted() },
								asset);
						if (writeLog) System.out.println("Asset " + asset + " successfully sent");
					}
				} catch (Throwable t) {
					if (exceptionOnFail) {
						throw new Exception("Failed to make association with AE");
					}

					w.add("asset", new String[] { "status", "fail", "reason", "association failed" }, asset);
				}
				// Clean up
				FileUtils.delete(tempDir);
				PluginTask.threadTaskCompleted();
			}
		}
	}



	private static void editDICOM (File tempDir,  boolean anonymise, XmlDoc.Element cid, SetOfDicomFiles dcmFiles)
			throws Throwable {
		
		// Set tag for DICOM edit
		int group = Integer.parseInt("0010", 16);
		int element = Integer.parseInt("0010", 16);
		AttributeTag aTag = new AttributeTag(group, element);
		//
		AttributeTag bTag = null;
		if (cid!=null) {
			bTag = new AttributeTag (Integer.parseInt(cid.value("@group"), 16), 
									 Integer.parseInt(cid.value("@element"), 16));
		}

		// Get list of files
		File[] files = tempDir.listFiles();		
		for (int i=0; i<files.length; i++) {
			File f = files[i];
			if (anonymise) DicomModify.editFile(f, aTag, " ");
			if (bTag!=null) DicomModify.editFile(f, bTag, cid.value());
			
			dcmFiles.add(f);
		}
	}


	public class DICOMHandler extends com.pixelmed.network.MultipleInstanceTransferStatusHandler {
		private int _nFailed = 0;
		private int _nCompleted = 0;
		private int _nRemaining = 0;
		private int _nWarning = 0;
		String _instanceUID = null;

		private DICOMHandler() {

		};

		public void updateStatus(int nRemaining, int nCompleted, int nFailed, int nWarning, String sopInstanceUID) {

			_nFailed = nFailed;
			_nCompleted = nCompleted;
			_nRemaining = nRemaining;
			_nWarning = nWarning;
			_instanceUID = sopInstanceUID;
		}

		public int getNumberCompleted() {

			return _nCompleted;
		}

		public int getNumberFailed() {

			return _nFailed;
		}

		public int getNumberRemaining() {

			return _nRemaining;
		}

		public int getNumberWarning() {

			return _nWarning;
		}

		public String sopInstanceUID() {

			return _instanceUID;
		}

		public String toString() {

			return _instanceUID + ":" + _nCompleted + "/" + +_nFailed + "/" + _nWarning + "/" + _nRemaining;
		}

	}

}
