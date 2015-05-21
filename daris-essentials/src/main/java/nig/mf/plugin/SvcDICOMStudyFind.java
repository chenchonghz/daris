package nig.mf.plugin;

import java.util.Collection;
import java.util.HashMap;

import nig.mf.dicom.plugin.util.DICOMModelUtil;
import nig.util.DateUtil;


import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDICOMStudyFind extends PluginService {
	private Interface _defn;

	public SvcDICOMStudyFind() {

		_defn = new Interface();

		_defn.add(new Element("namespace", StringType.DEFAULT, "The namespace to find Studies in (defaults to 'dicom')", 0, 1));
		_defn.add(new Element("age", IntegerType.DEFAULT, "Find Studies younger (created/modified) than this in days (defaults to 7).", 0, 1));
		_defn.add(new Element("email", StringType.DEFAULT, "An email address to send the report to.", 0, Integer.MAX_VALUE));
		_defn.add(new Element("series", BooleanType.DEFAULT, "Show the Series names as well (defaults to false).", 0, 1)); 
		_defn.add(new Element("include-siemens-raw", BooleanType.DEFAULT, "Include raw Siemens data (associated with DICOM patient records) in the listing, defaults to false.", 0, 1));
	}

	public String name() {
		return "dicom.study.find";
	}

	public String description() {
		return "Finds local Studies created in the DICOM data model (by the PSS Engine) that were created/modified since a specified number of days ago.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		// Args
		String ns = args.stringValue("namespace", "dicom");
		String age = args.stringValue("age", "7");
		Collection<String> emails = args.values("email");
		Boolean showSeries = args.booleanValue("series", false);
		Boolean showRawSiemens = args.booleanValue("include-siemens-raw", false);

		// Find the Studies
		XmlDocMaker doc = new XmlDocMaker("args");
		String query = "namespace='" + ns + "' and mtime>='TODAY-" + age + "DAY'";
		if (showRawSiemens) {
			query += " and (type='dicom/study' or type='siemens-raw-petct/study')";
		} else {
			query += " and type='dicom/study'";
		}
		doc.add("action", "get-meta");
		doc.add("where", query);
		doc.add("pdist", 0);       // FOrce local
		doc.add("size", "infinity");
		XmlDoc.Element r = executor().execute("asset.query", doc.root());

		// Hash map has the patient name as the key
		// We will build up XML for each Study for each patient
		HashMap<String, XmlDocMaker> map = new HashMap<String, XmlDocMaker>();

		// Today's date
		String today = DateUtil.todaysDate(2);
		String msgSubject = today + " : data uploaded in the DICOM data model (namespace=" + ns + ") in the last " + age + " days";
		String msg = "";
		if (r==null) {
			msg = "No new studies were found";
		} else {

			// Iterate over Studies 
			Collection<XmlDoc.Element> assets = r.elements("asset");
			if (assets!=null) {
				for (XmlDoc.Element asset : assets) {
					// This will hold the meta-data for this Study
					XmlDocMaker dm = new XmlDocMaker("top");

					// Get meta-data
					String id = asset.value("@id");
					dm.push("study", new String[]{"id", id});

					// Get patient name from related asset
					String patientName = getPatientName (executor(), asset);

					// Get number of Series
					int nSeries = getNumberSeries (asset);

					// FIsh out STudy info
					XmlDoc.Element dicomStudyMeta  = asset.element("meta/mf-dicom-study");
					XmlDoc.Element siemensStudyMeta = asset.element("meta/daris:siemens-raw-petct-study");
					if (dicomStudyMeta!=null) {
						// Get size of study (from its series)
						XmlDoc.Element size = DICOMModelUtil.sizeOfStudy(executor(), id, 
								DICOMModelUtil.FormatType.DICOM);
						//
						dm.add("type", "dicom/study");
						String ingest = dicomStudyMeta.value("ingest/date");
						String acqDate = dicomStudyMeta.value("sdate");
						String description = dicomStudyMeta.value("description");
						dm.add("description", description);
						dm.add("date", acqDate);
						dm.add("ingest", ingest);
						formatSize (dm, size);
						dm.add("number-series", nSeries);
					} else if (siemensStudyMeta!=null) {
						// Get size of study (from its series)
						XmlDoc.Element size = DICOMModelUtil.sizeOfStudy(executor(), id, 
								DICOMModelUtil.FormatType.SIEMENS_RAW);
						//
						dm.add("type", "raw-siemens-petct/study");
						String ingest = siemensStudyMeta.value("ingest/date");
						String acqDate = siemensStudyMeta.value("date");
						dm.add("date", acqDate);
						dm.add("ingest", ingest);
						formatSize (dm, size);
						dm.add("number-series", nSeries);
					}

					// Add Series descriptions
					if (nSeries > 0 && showSeries) addSeries (executor(), asset, dm);
					dm.pop();

					// Add the patient record to the hash map
					if (!map.containsKey(patientName)) {
						XmlDocMaker t = new XmlDocMaker("subject", new String[]{"name", patientName});
						t.add(dm.root().element("study"));
						map.put(patientName, t);
					} else {
						XmlDocMaker t = map.get(patientName);
						t.add(dm.root().element("study"));
					}			
				}
			} else {
				msg = "No new studies were found";
			}
		}

		// Populate the output XMlWriter and text message
		Collection<XmlDocMaker> values = map.values();
		for (XmlDocMaker value : values) {

			// The root of each value is for one patient
			XmlDoc.Element patient = value.root();
			w.add(patient);

			// Parse and form text message
			msg += ":subject " + patient.value("@name") + "\n";

			// Iterate through Studies
			Collection<XmlDoc.Element> studies = patient.elements("study");
			for (XmlDoc.Element study : studies) {
				msg += "   :study -id " + study.value("@id") + "\n";
				msg += "      :type          " + study.value("type") + "\n";
				//
				String d = study.value("description");
				if (d!=null) {
					msg += "      :description   " + d + "\n";
				}
				msg += "      :date          " + study.value("date") + "\n";
				msg += "      :ingest        " + study.value("ingest") + "\n";
				Collection<XmlDoc.Element> sizes = study.elements("size");
				for (XmlDoc.Element size : sizes) {
					msg += "      :size        " + size.value() + " " + size.value ("@units") +  "\n";
				}
				msg += "      :number-series " + study.value("number-series") + "\n";
				if (showSeries) {
					XmlDoc.Element series = study.element("series");
					msg += "         :series -id " + series.value("@id") + "\n";
					d = series.value("description");
					if (d!=null) msg += "            :description " + d + "\n";
					d = series.value("protocol");
					if (d!=null) msg += "            :protocol    " + d + "\n";
					d = series.value("modality");
					if (d!=null) msg += "            :modality    " + d + "\n";
					d = series.value("type");
					if (d!=null) msg += "            :type        " + d + "\n";
				}
			}
		}	

		// Send email
		if (emails!=null) {
			for (String email : emails) {
				doc = new XmlDocMaker("args");
				doc.add("subject", msgSubject);
				doc.add ("to", email);
				doc.add("async", true);
				doc.add("body", msg);
				executor().execute("mail.send", doc.root());
			}
		}
	}

	private void addSeries (ServiceExecutor executor, XmlDoc.Element asset, XmlDocMaker w) throws Throwable {
		// Fish out id of Series
		Collection<String> seriesIDs = asset.values("related[@type='contains']/to");
		if (seriesIDs==null) return;
		for (String seriesID : seriesIDs) {
			w.push("series", new String[]{"id", seriesID});
			//
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", seriesID);
			XmlDoc.Element r = executor.execute("asset.get", dm.root());
			XmlDoc.Element dicom = r.element("asset/meta/mf-dicom-series");
			XmlDoc.Element siemens = r.element("asset/meta/daris:siemens-raw-petct-series ");
			//
			if (dicom!=null) {
				String description = dicom.value("description");
				if (description!=null) w.add("description", description);
				//
				String protocol= dicom.value("protocol");
				if (protocol!=null) w.add("protocol", protocol);
				//
				String modality = dicom.value("modality");
				if (modality!=null) w.add("modality", modality);
			} else if (siemens!=null) {
				String description = siemens.value("description");
				if (description!=null) w.add("description", description);
				//
				String type = siemens.value("type");
				if (type!=null) w.add("type", type);
				//
				String modality = siemens.value("modality");
				if (modality!=null) w.add("modality", modality);
			}
			//
			w.pop();
		}
	}
	private String getPatientName (ServiceExecutor executor, XmlDoc.Element asset) throws Throwable {

		// Fish out id of patient assset
		String patientAssetID = asset.value("related[@type='had-by']/to");
		if (patientAssetID==null) return "not-found";

		// Get the asset
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", patientAssetID);
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		if (r==null) return "not-found";
		String firstName = r.value("asset/meta/mf-dicom-patient/name[@type='first']");
		String lastName = r.value("asset/meta/mf-dicom-patient/name[@type='last']");
		String name = null;
		if (firstName!=null) name = firstName;
		if (lastName!=null) {
			if (name==null) {
				name = lastName;
			} else{
				name += " " + lastName;
			}
		}
		if (name==null) name = "not-found";
		return name;
	}

	private void formatSize (XmlDocMaker dm, XmlDoc.Element sizeAndNumber) throws Throwable {
		String sizeInBytes = sizeAndNumber.value("value");   // bytes
		Long s =Long.parseLong(sizeInBytes);
		dm.add("size", new String[]{"units", "bytes"}, s);
		if (s > 10000L && s <= 10000000L) {
		    s = s / 1000L;
			dm.add("size", new String[]{"units", "KBytes"}, s);
		} else if (s > 10000000L && s <= 10000000000L) {
			s = s / 1000000L;
			dm.add("size", new String[]{"units", "MBytes"}, s);
		} else {
			s = s / 1000000000L;
			dm.add("size", new String[]{"units", "GBytes"}, s);
		}
	}

	private int getNumberSeries (XmlDoc.Element asset) throws Throwable {
		Collection<String> seriesIDs = asset.values("related[@type='contains']/to");
		if (seriesIDs==null) return 0;
		return seriesIDs.size();
	}
}