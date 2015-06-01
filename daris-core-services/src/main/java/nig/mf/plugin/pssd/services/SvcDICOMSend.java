package nig.mf.plugin.pssd.services;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import nig.dicom.util.DicomFileCheck;
import nig.io.FileUtils;
import nig.mf.dicom.plugin.util.DICOMPatient;
import nig.mf.plugin.util.ArchiveUtil;
import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.FileMetaInformation;
import com.pixelmed.dicom.SetOfDicomFiles;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.dicom.TransferSyntax;
import com.pixelmed.network.StorageSOPClassSCU;

public class SvcDICOMSend extends PluginService {

	public static final String SERVICE_NAME = "om.pssd.dicom.send";
	public static final String SERVICE_DESCRIPTION = "Send DICOM DataSets to a remote DICOM Application Entity(server).  Each DataSet is sent in a separate DICOM client call.";
	public static final int DEFAULT_DICOM_PORT = 104;

	public static enum ElementAction {
		unchanged("unchanged"), set("set"), anonymize("anonymize"), use_subject_cid(
				"use-subject-cid"), use_mf_dicom_patient_name(
				"use-mf-dicom-patient-name"), use_mf_dicom_patient_id(
				"use-mf-dicom-patient-id");
		private String _stringValue;

		ElementAction(String stringValue) {
			_stringValue = stringValue;
		}

		public final String stringValue() {
			return _stringValue;
		}

		public final String toString() {
			return stringValue();
		}

		public static final String[] stringValues() {
			ElementAction[] vs = values();
			String[] svs = new String[vs.length];
			for (int i = 0; i < vs.length; i++) {
				svs[i] = vs[i].stringValue();
			}
			return svs;
		}

		public static final ElementAction fromString(String action,
				ElementAction defaultValue) {
			if (action != null) {
				ElementAction[] vs = values();
				for (ElementAction v : vs) {
					if (v.stringValue().equalsIgnoreCase(action)) {
						return v;
					}
				}
			}
			return defaultValue;
		}
	}

	private static class ApplicationEntity {
		public final String title;
		public final String host;
		public final int port;

		ApplicationEntity(String host, int port, String title) {
			this.host = host;
			this.port = port;
			this.title = title;
		}
	}

	private static interface DicomFileEditor {
		void edit(File f) throws Throwable;
	}

	private Interface _defn;

	public SvcDICOMSend() throws Throwable {
		_defn = new Interface();
		/*
		 * pid
		 */
		Interface.Element me = new Interface.Element(
				"pid",
				CiteableIdType.DEFAULT,
				"The identity of the parent object; can be a Project, Subject, ExMethod, Study or DataSet.  All child DataSets (and in a federation children will be found on all peers in the federsation) containing DICOM data will be found and sent.",
				0, Integer.MAX_VALUE);
		_defn.add(me);
		/*
		 * where
		 */
		_defn.add(new Interface.Element(
				"where",
				StringType.DEFAULT,
				"A query to find the objects to send. All the DICOM datasets contained by the result objects will be sent.",
				0, 1));
		/*
		 * asset-type
		 */
		_defn.add(new Interface.Element("asset-type", new EnumType(
				DistributedQuery.ResultAssetType.stringValues()),
				"Specify type of asset to send. Defaults to all.", 0, 1));
		/*
		 * local application entity
		 */
		me = new Interface.Element("local", XmlDocType.DEFAULT,
				"Settings of the local application entity.", 1, 1);
		me.add(new Interface.Element("aet", StringType.DEFAULT,
				"Local/Calling AET(application entity title).", 1, 1));
		_defn.add(me);
		/*
		 * remote application entity
		 */
		me = new Interface.Element("remote", XmlDocType.DEFAULT,
				"Settings of the remote application entity.", 1, 1);
		me.add(new Interface.Attribute(
				"name",
				StringType.DEFAULT,
				new StringBuilder()
						.append("A convenience name that the remote application entity may be referred to by. ")
						.append("In this case, the application entity is looked up in the DICOM AE registry and the children elements host, port, aet are ignored. ")
						.append("Don't specify the name if you want to specify these children elements directly.")
						.toString(), 0));
		me.add(new Interface.Element("aet", StringType.DEFAULT,
				"Remote/Called AET(application entity title).", 0, 1));
		me.add(new Interface.Element("host", StringType.DEFAULT,
				"The host address of the remote/called application entity.", 0,
				1));
		me.add(new Interface.Element("port", IntegerType.DEFAULT,
				"The port number of the remote/called application entity.", 0,
				1));
		_defn.add(me);
		/*
		 * overridden meta
		 */
		me = new Interface.Element("override", XmlDocType.DEFAULT,
				"The DICOM elements to be overridden(modify/anonymize).", 0, 1);
		// generic element
		Interface.Element ee = new Interface.Element("element",
				XmlDocType.DEFAULT,
				"A generic DICOM element to be overridden.", 0,
				Integer.MAX_VALUE);
		ee.add(new Interface.Attribute("group", new StringType(Pattern
				.compile("[0-9a-fA-F]{4}")),
				"The group tag of the DICOM element.", 1));
		ee.add(new Interface.Attribute("element", new StringType(Pattern
				.compile("[0-9a-fA-F]{4}")),
				"The element tag of the DICOM element.", 1));
		ee.add(new Interface.Attribute(
				"action",
				new EnumType(ElementAction.stringValues()),
				"The action performed on the element before sending. Defaults to set.",
				0));
		ee.add(new Interface.Element(
				"value",
				StringType.DEFAULT,
				"The value of the DICOM element. Only required if the action is 'set'.",
				0, 1));
		me.add(ee);
		/*
		 * pre-selected elements
		 */
		// patient name
		Interface.Element pne = new Interface.Element(
				"patient-name",
				XmlDocType.DEFAULT,
				"The DICOM patient name element (0010,0010) will be overridden.",
				0, 1);
		pne.add(new Interface.Attribute(
				"action",
				new EnumType(ElementAction.stringValues()),
				"The action performed on the element before sending. Defaults to set.",
				0));
		pne.add(new Interface.Element(
				"value",
				StringType.DEFAULT,
				"The value of the DICOM element. Only required if the action is 'set'.",
				0, 1));
		me.add(pne);
		// patient id
		Interface.Element pie = new Interface.Element("patient-id",
				XmlDocType.DEFAULT,
				"The DICOM patient id element (0010,0020) will be overridden.",
				0, 1);
		pie.add(new Interface.Attribute(
				"action",
				new EnumType(ElementAction.stringValues()),
				"The action performed on the element before sending. Defaults to set.",
				0));
		pie.add(new Interface.Element(
				"value",
				StringType.DEFAULT,
				"The value of the DICOM element. Only required if the action is 'set'.",
				0, 1));
		me.add(pie);
		// study id
		Interface.Element sie = new Interface.Element("study-id",
				XmlDocType.DEFAULT,
				"The DICOM study id element (0020,0010) will be overridden.",
				0, 1);
		sie.add(new Interface.Attribute(
				"action",
				new EnumType(ElementAction.stringValues()),
				"The action performed on the element before sending. Defaults to set.",
				0));
		sie.add(new Interface.Element(
				"value",
				StringType.DEFAULT,
				"The value of the DICOM element. Only required if the action is 'set'.",
				0, 1));
		me.add(sie);
		// performing/attending physician's name
		Interface.Element ppne = new Interface.Element(
				"performing-physician-name",
				XmlDocType.DEFAULT,
				"The DICOM element (0008,1050), performing physician's name, will be overridden.",
				0, 1);
		ppne.add(new Interface.Attribute(
				"action",
				new EnumType(ElementAction.stringValues()),
				"The action performed on the element before sending. Defaults to set.",
				0));
		ppne.add(new Interface.Element(
				"value",
				StringType.DEFAULT,
				"The value of the DICOM element. Only required if the action is 'set'.",
				0, 1));
		me.add(ppne);
		// referring physician's name
		Interface.Element rpne = new Interface.Element(
				"referring-physician-name",
				XmlDocType.DEFAULT,
				"The DICOM element (0008,0090), referring physician's name, will be overridden.",
				0, 1);
		rpne.add(new Interface.Attribute(
				"action",
				new EnumType(ElementAction.stringValues()),
				"The action performed on the element before sending. Defaults to set.",
				0));
		rpne.add(new Interface.Element(
				"value",
				StringType.DEFAULT,
				"The value of the DICOM element. Only required if the action is 'set'.",
				0, 1));
		me.add(rpne);
		// referring physician's phone
		Interface.Element rppe = new Interface.Element(
				"referring-physician-phone",
				XmlDocType.DEFAULT,
				"The DICOM element (0008,0094), referring physician's telphone number, will be overridden.",
				0, 1);
		rppe.add(new Interface.Attribute(
				"action",
				new EnumType(ElementAction.stringValues()),
				"The action performed on the element before sending. Defaults to set.",
				0));
		rppe.add(new Interface.Element(
				"value",
				StringType.DEFAULT,
				"The value of the DICOM element. Only required if the action is 'set'.",
				0, 1));
		me.add(rppe);
		_defn.add(me);
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

	@Override
	public String description() {
		return SERVICE_DESCRIPTION;
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
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
		// Parse and check
		if (!args.elementExists("pid") && !args.elementExists("where")) {
			throw new Exception(
					"Expecting either pid or where element. Found none.");
		}
		if (args.elementExists("pid") && args.elementExists("where")) {
			throw new Exception(
					"Expecting either pid or where element. Found both.");
		}
		String type = args.stringValue("asset-type", "all");
		Collection<String> pids = args.values("pid");
		String where = args.value("where");
		if (where != null) {
			XmlDocMaker dm = new XmlDocMaker("args");
			StringBuilder sb = new StringBuilder(where);
			DistributedQuery.appendResultAssetTypePredicate(sb,
					DistributedQuery.ResultAssetType.instantiate(type));
			dm.add("where", sb.toString());
			dm.add("size", "infinity");
			dm.add("action", "get-cid");
			XmlDoc.Element r = executor().execute("asset.query", dm.root());
			pids = r.values("cid");
		}
		Boolean exceptionOnFail = args.booleanValue("exception-on-fail", true);
		// remote ae
		ApplicationEntity remoteAE = resolveRemoteAE(args.element("remote"),
				executor());
		String remoteAET = remoteAE.title;
		String remoteAEHost = remoteAE.host;
		int remoteAEPort = remoteAE.port;
		// local ae
		String localAET = args.value("local/aet");
		//
		int compressionLevel = 0;
		int debugLevel = 0;
		PluginTask.checkIfThreadTaskAborted();
		// Find the DataSets with DICOM content. Do a distributed query.
		// Iterate over all input pid
		ArrayList<String> dataSets = new ArrayList<String>();
		if (pids != null) {
			for (String pid : pids) {
				XmlDocMaker dm = new XmlDocMaker("args");
				StringBuilder query = new StringBuilder("(cid='" + pid
						+ "' or cid starts with '" + pid + "')");
				query.append(" and model='om.pssd.dataset' and type='dicom/series'");
				DistributedQuery.appendResultAssetTypePredicate(query,
						DistributedQuery.ResultAssetType.instantiate(type));
				dm.add("where", query.toString());
				dm.add("size", "infinity");
				XmlDoc.Element r = executor().execute("asset.query", dm.root());
				Collection<String> ids = r.values("id");
				if (ids != null) {
					dataSets.addAll(ids);
				}
			}
		}
		// Iterate over DataSets. An option could be added to copy all files
		// from all DataSets to a temporary folder and then sent in one giant
		// push.
		if (dataSets == null || dataSets.isEmpty()) {
			return;
		}
		int nDataSets = dataSets.size();
		PluginTask.threadTaskBeginSetOf(nDataSets);
		for (String id : dataSets) {
			PluginTask.checkIfThreadTaskAborted();
			String cid = nig.mf.pssd.plugin.util.CiteableIdUtil.idToCid(
					executor(), id);
			String subjectCid = nig.mf.pssd.CiteableIdUtil.getSubjectId(cid);
			DICOMPatient dicomPatient = null;
			XmlDoc.Element tm = AssetUtil
					.getAsset(executor(), subjectCid, null);
			if (tm != null) {
				XmlDoc.Element tm2 = tm.element("asset/meta/mf-dicom-patient");
				if (tm2 != null) {
					dicomPatient = new DICOMPatient(tm2);
				}
			}
			// Get the content into a stream
			PluginTask.setCurrentThreadActivity("Getting content of " + cid);
			InputStream is = AssetUtil.getContentInStream(executor(), id);
			if (is == null) {
				continue;
			}
			// Unpack content into temporary directory
			// TODO: clean up the temp files if aborted?
			File tempDir = createTemporaryDirectory();
			if (args.elementExists("override")) {
				PluginTask
						.setCurrentThreadActivity("Extracting and editing DICOM files in dataset"
								+ cid);
			} else {
				PluginTask
						.setCurrentThreadActivity("Extracting DICOM files in dataset"
								+ cid);
			}
			PluginTask.checkIfThreadTaskAborted();
			//
			ArchiveUtil.unpackContent(executor(), id, tempDir);
			File[] dicomFiles = tempDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return DicomFileCheck.isDicomFile(f);
				}
			});
			// Edit files if required and put into DICOM files container
			if (args.elementExists("override")) {
				editDicomFiles(tempDir, subjectCid, dicomPatient,
						args.element("override"), localAET);
			}
			SetOfDicomFiles dcmFiles = new SetOfDicomFiles();
			for (File f : dicomFiles) {
				dcmFiles.add(f);
			}
			PluginTask.checkIfThreadTaskAborted();
			// TODO: clean up the temp files if aborted?
			DICOMHandler dh = new DICOMHandler();
			try {
				PluginTask.setCurrentThreadActivity("Sending DICOM dataset "
						+ cid);
				new StorageSOPClassSCU(remoteAEHost, remoteAEPort, remoteAET,
						localAET, dcmFiles, compressionLevel, dh, null, 0,
						debugLevel);
				if (dh.getNumberFailed() > 0) {
					if (exceptionOnFail) {
						throw new Exception(
								"Failed to send DICOM data for cid " + cid);
					}
					w.add("id",
							new String[] { "status", "fail", "completed",
									"reason", "transfer failed",
									"" + dh.getNumberCompleted(), "failed",
									"" + dh.getNumberFailed() }, cid);
				} else {
					w.add("id", new String[] { "status", "pass", "completed",
							"" + dh.getNumberCompleted() }, cid);
				}
			} catch (Throwable t) {
				if (exceptionOnFail) {
					throw t;
				} else {
					// Logging
					t.printStackTrace(System.out);
					w.add("id", new String[] { "status", "fail", "reason",
							"association failed" }, cid);
				}
			}
			// Clean up
			FileUtils.delete(tempDir);
			PluginTask.threadTaskCompleted();
		}
	}

	private static ApplicationEntity resolveRemoteAE(XmlDoc.Element re,
			ServiceExecutor executor) throws Throwable {
		String name = re.value("@name");
		String host = re.value("host");
		int port = re.intValue("port", DEFAULT_DICOM_PORT);
		String aet = re.value("aet");
		if (host == null || aet == null) {
			if (name == null) {
				throw new Exception(
						"You did not supply the name of remote application entity (remote/@name), so you must give the host, port and aet of the remote application entity (remote/host, remote/port, remote/aet).");
			} else {
				// Get the registry for remote DICOM application entities
				XmlDoc.Element raee = executor.execute(
						SvcDICOMAERegList.SERVICE_NAME).element(
						"ae[@name='" + name + "']");
				if (raee == null) {
					throw new Exception(
							"Failed to look up the remote application entity name '"
									+ name
									+ "'. Run service '"
									+ SvcDICOMAERegList.SERVICE_NAME
									+ "' to see all the available remote AE names.");
				}
				host = raee.value("host");
				port = raee.intValue("port");
				aet = raee.value("aet");
				if (host == null || aet == null) {
					throw new Exception("Failed to retrieve AE:'" + name
							+ "' information from the AE registry.");
				}
			}
		}
		return new ApplicationEntity(host, port, aet);
	}

	private static void editDicomFile(File f, Map<AttributeTag, String> values,
			String localAET) throws Throwable {
		// System.out.print("Modifying " + f.getPath() + "... ");
		AttributeList list = new AttributeList();
		list.read(f);
		Attribute mediaStorageSOPClassUIDAttr = list
				.get(TagFromName.MediaStorageSOPClassUID);
		String mediaStorageSOPClassUID = null;
		if (mediaStorageSOPClassUIDAttr != null) {
			mediaStorageSOPClassUID = mediaStorageSOPClassUIDAttr
					.getSingleStringValueOrNull();
		}
		Attribute mediaStorageSOPInstanceUIDAttr = list
				.get(TagFromName.MediaStorageSOPInstanceUID);
		String mediaStorageSOPInstanceUID = null;
		if (mediaStorageSOPInstanceUIDAttr != null) {
			mediaStorageSOPInstanceUID = mediaStorageSOPInstanceUIDAttr
					.getSingleStringValueOrNull();
		}
		// String implementationClassUID =
		// list.get(TagFromName.ImplementationClassUID).getSingleStringValueOrNull();
		// String implementationVersionName =
		// list.get(TagFromName.ImplementationVersionName).getSingleStringValueOrNull();
		/*
		 * Cleanup
		 */
		list.removeGroupLengthAttributes();
		list.removeMetaInformationHeaderAttributes();
		list.remove(TagFromName.DataSetTrailingPadding);
		list.correctDecompressedImagePixelModule();
		list.insertLossyImageCompressionHistoryIfDecompressed();
		if (mediaStorageSOPClassUID != null
				&& mediaStorageSOPInstanceUID != null) {
			FileMetaInformation.addFileMetaInformation(list,
					mediaStorageSOPClassUID, mediaStorageSOPInstanceUID,
					TransferSyntax.ExplicitVRLittleEndian, localAET);
		} else {
			FileMetaInformation.addFileMetaInformation(list,
					TransferSyntax.ExplicitVRLittleEndian, localAET);
		}
		// Put the new tag in place
		if (values != null && !values.isEmpty()) {
			for (AttributeTag aTag : values.keySet()) {
				String aValue = values.get(aTag);
				Attribute attr = list.get(aTag);
				if (attr != null) {
					attr.setValue(values.get(aTag));
				} else {
					list.putNewAttribute(aTag).addValue(aValue);
				}
			}
		}
		list.write(new FileOutputStream(f),
				TransferSyntax.ExplicitVRLittleEndian, true, true);
	}

	private static void editDicomElementValue(Map<AttributeTag, String> values,
			AttributeTag tag, ElementAction action, String value,
			String subjectCid, DICOMPatient patient) {
		switch (action) {
		case unchanged:
			return;
		case anonymize:
			value = " ";
			break;
		case use_subject_cid:
			value = subjectCid;
			break;
		case use_mf_dicom_patient_name:
			value = patient.nameForDICOMFile();
			break;
		case use_mf_dicom_patient_id:
			value = patient.getID();
			break;
		default:
			break;
		}
		values.put(tag, value);
	}

	private static SetOfDicomFiles listDicomFiles(File dir,
			final DicomFileEditor editor) throws Throwable {
		SetOfDicomFiles dcmFiles = new SetOfDicomFiles();
		File[] files = dir.listFiles();
		for (File f : files) {
			if (DicomFileCheck.isDicomFile(f)) {
				if (editor != null) {
					editor.edit(f);
				}
				dcmFiles.add(f);
			}
		}
		return dcmFiles;
	}

	private static SetOfDicomFiles editDicomFiles(File dir, String subjectCid,
			DICOMPatient patient, XmlDoc.Element override, final String localAET)
			throws Throwable {
		final Map<AttributeTag, String> values = new LinkedHashMap<AttributeTag, String>();
		/*
		 * generic elements
		 */
		List<XmlDoc.Element> genericElements = override.elements("element");
		if (genericElements != null) {
			for (XmlDoc.Element ge : genericElements) {
				AttributeTag tag = new AttributeTag(
						ge.intValue("@group", 0, 16), ge.intValue("@element",
								0, 16));
				String value = ge.value("value");
				ElementAction action = ElementAction.fromString(
						ge.value("@action"), ElementAction.set);
				editDicomElementValue(values, tag, action, value, subjectCid,
						patient);
			}
		}
		/*
		 * pre-selected elements
		 */
		if (override.elementExists("patient-name")) {
			XmlDoc.Element pne = override.element("patient-name");
			ElementAction action = ElementAction.fromString(
					pne.value("@action"), ElementAction.set);
			String value = pne.value("value");
			editDicomElementValue(values, TagFromName.PatientName, action,
					value, subjectCid, patient);
		}
		if (override.elementExists("patient-id")) {
			XmlDoc.Element pie = override.element("patient-id");
			ElementAction action = ElementAction.fromString(
					pie.value("@action"), ElementAction.set);
			String value = pie.value("value");
			editDicomElementValue(values, TagFromName.PatientID, action, value,
					subjectCid, patient);
		}
		if (override.elementExists("study-id")) {
			XmlDoc.Element sie = override.element("study-id");
			ElementAction action = ElementAction.fromString(
					sie.value("@action"), ElementAction.set);
			String value = sie.value("value");
			editDicomElementValue(values, TagFromName.StudyID, action, value,
					subjectCid, patient);
		}
		if (override.elementExists("performing-physician-name")) {
			XmlDoc.Element ppne = override.element("performing-physician-name");
			ElementAction action = ElementAction.fromString(
					ppne.value("@action"), ElementAction.set);
			String value = ppne.value("value");
			editDicomElementValue(values, TagFromName.PerformingPhysicianName,
					action, value, subjectCid, patient);
		}
		if (override.elementExists("referring-physician-name")) {
			XmlDoc.Element rpne = override.element("referring-physician-name");
			ElementAction action = ElementAction.fromString(
					rpne.value("@action"), ElementAction.set);
			String value = rpne.value("value");
			editDicomElementValue(values, TagFromName.ReferringPhysicianName,
					action, value, subjectCid, patient);
		}
		if (override.elementExists("referring-physician-phone")) {
			XmlDoc.Element rppe = override.element("referring-physician-phone");
			ElementAction action = ElementAction.fromString(
					rppe.value("@action"), ElementAction.set);
			String value = rppe.value("value");
			editDicomElementValue(values,
					TagFromName.ReferringPhysicianTelephoneNumbers, action,
					value, subjectCid, patient);
		}
		SetOfDicomFiles dcmFiles = listDicomFiles(dir, values.isEmpty() ? null
				: new DicomFileEditor() {
					@Override
					public void edit(File f) throws Throwable {
						editDicomFile(f, values, localAET);
					}
				});
		return dcmFiles;
	}

	public class DICOMHandler extends
			com.pixelmed.network.MultipleInstanceTransferStatusHandler {
		private int _nFailed = 0;
		private int _nCompleted = 0;
		private int _nRemaining = 0;
		private int _nWarning = 0;
		String _instanceUID = null;

		private DICOMHandler() {
		};

		public void updateStatus(int nRemaining, int nCompleted, int nFailed,
				int nWarning, String sopInstanceUID) {
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
			return _instanceUID + ":" + _nCompleted + "/" + +_nFailed + "/"
					+ _nWarning + "/" + _nRemaining;
		}
	}
}
