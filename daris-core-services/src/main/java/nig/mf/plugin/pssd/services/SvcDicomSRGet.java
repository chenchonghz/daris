package nig.mf.plugin.pssd.services;

import java.util.AbstractMap.SimpleEntry;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.ContentItem;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.StructuredReport;
import com.pixelmed.dicom.TagFromName;

import arc.archive.ArchiveInput;
import arc.archive.ArchiveInput.Entry;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mime.NamedMimeType;
import arc.streams.LongInputStream;
import arc.streams.SizedInputStream;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDicomSRGet extends PluginService {

	public static final String SERVICE_NAME = "daris.dicom.sr.get";

	private Interface _defn;

	public SvcDicomSRGet() {
		_defn = new Interface();
		addToDefinition(_defn);
	}

	static void addToDefinition(Interface defn) {
		defn.add(new Interface.Element("id", AssetType.DEFAULT, "Asset id of the DICOM series/dataset.", 0, 1));
		defn.add(
				new Interface.Element("cid", CiteableIdType.DEFAULT, "Citeable id of the DICOM series/dataset.", 0, 1));
	}

	@Override
	public Access access() {
		return ACCESS_ACCESS;
	}

	@Override
	public Interface definition() {
		return _defn;
	}

	@Override
	public String description() {
		return "Extracts DICOM structured report.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs ouputs, XmlWriter w) throws Throwable {
		SimpleEntry<XmlDoc.Element, StructuredReport> entry = read(executor(), args);
		XmlDoc.Element ae = entry.getKey();
		StructuredReport sr = entry.getValue();
		describe(ae, sr, w);
	}

	static SimpleEntry<XmlDoc.Element, StructuredReport> read(ServiceExecutor executor, XmlDoc.Element args)
			throws Throwable {
		String cid = args.value("cid");
		String id = args.value("id");
		/*
		 * validate args
		 */
		if (cid == null && id == null) {
			throw new IllegalArgumentException("Either cid or id argument must be specified.");
		}
		if (cid != null && id != null) {
			throw new IllegalArgumentException("Expects either cid or id argument. Found both.");
		}
		/*
		 * resolve asset meta
		 */
		XmlDocMaker dm = new XmlDocMaker("args");
		if (cid != null) {
			dm.add("cid", cid);
		} else {
			dm.add("id", id);
		}
		PluginService.Outputs outputs = new PluginService.Outputs(1);
		XmlDoc.Element ae = executor.execute("asset.get", dm.root(), null, outputs).element("asset");
		if (id == null) {
			id = ae.value("@id");
		}
		Output output = outputs.output(0);
		try {
			/*
			 * validate asset meta
			 */
			if (!ae.elementExists("meta/mf-dicom-series")) {
				// All dicom series in Mediaflux must have mf-dicom-series doc.
				throw new Exception("No mf-dicom-series meta data document is found. Not a valid DICOM series.");
			}
			String modality = ae.value("meta/mf-dicom-series/modality");
			if (!"SR".equals(modality)) {
				throw new Exception(
						"Not a structured report. mf-dicom-series/modality must be 'SR'. Found '" + modality + "'.");
			}
			String ctype = ae.value("content/type");
			String cext = ae.value("content/type/@ext");
			long csize = ae.longValue("content/size", 0);
			if (ctype == null) {
				throw new Exception("No content type is found in asset meta data.");
			}
			if (cext == null) {
				throw new Exception("No content extension is found in asset meta data.");
			}
			if (output == null) {
				throw new Exception("Failed to get asset content stream.");
			}
			if (csize == 0) {
				throw new Exception("Asset content size is 0.");
			}
			ArchiveInput ai = ArchiveRegistry.createInput(new SizedInputStream(output.stream(), csize),
					new NamedMimeType(ctype));
			try {
				Entry e = ai.next();
				StructuredReport sr = read(e.stream(), e.size());
				if (ai.next() != null) {
					throw new Exception("Expects only one entry in the content archive. Found multiple.");
				}
				return new SimpleEntry<>(ae, sr);
			} finally {
				ai.close();
			}
		} finally {
			if (output != null) {
				output.close();
			}
		}

	}

	static StructuredReport read(LongInputStream in, long length) throws Throwable {
		DicomInputStream dis;
		if (!in.isSizedStream() && length > 0) {
			dis = new DicomInputStream(new SizedInputStream(in, length));
		} else {
			dis = new DicomInputStream(in);
		}
		try {
			AttributeList al = new AttributeList();
			al.read(dis);
			Attribute a = al.get(TagFromName.Modality);
			if (a == null) {
				throw new DicomException("Modality attribute is not found.");
			}
			String modality = a.getSingleStringValueOrNull();
			if (!"SR".equalsIgnoreCase(modality)) {
				throw new DicomException("Modality 'SR' is expected. Found '" + modality + "'.");
			}
			StructuredReport sr = new StructuredReport(al);
			return sr;
		} finally {
			dis.close();
			in.close();
		}
	}

	static String nameFor(XmlDoc.Element ae) throws Throwable {
		String name = null;
		if (ae.elementExists("name")) {
			name = ae.value("name");
		} else if (ae.elementExists("meta/daris:pssd-object/name")) {
			name = ae.value("meta/daris:pssd-object/name");
		} else if (ae.elementExists("meta/mf-dicom-series/protocol")
				&& ae.elementExists("meta/mf-dicom-series/description")) {
			name = ae.value("meta/mf-dicom-series/protocol") + "_" + ae.value("meta/mf-dicom-series/description");
		} else if (ae.elementExists("meta/mf-dicom-series/description")) {
			name = ae.value("meta/mf-dicom-series/description");
		} else if (ae.elementExists("meta/mf-dicom-series/protocol")) {
			name = ae.value("meta/mf-dicom-series/protocol");
		}
		return name;
	}

	static void describe(XmlDoc.Element ae, StructuredReport sr, XmlWriter w) throws Throwable {
		w.push("dicom-structured-report",
				new String[] { "id", ae.value("@id"), "cid", ae.value("cid"), "name", nameFor(ae) });
		describe((ContentItem) sr.getRoot(), w);
		w.pop();
	}

	static void describe(ContentItem item, XmlWriter w) throws Throwable {
		if (item.getConceptValue() != null) {
			w.push("item",
					new String[] { "name", item.getConceptNameCodeMeaning(), "relationship", item.getRelationshipType(),
							"code", item.getConceptNameCodeValue(), "type", item.getValueType() },
					item.getConceptValue());
		} else {
			w.push("item", new String[] { "name", item.getConceptNameCodeMeaning(), "relationship",
					item.getRelationshipType(), "code", item.getConceptNameCodeValue(), "type", item.getValueType() });
		}
		int n = item.getChildCount();
		for (int i = 0; i < n; i++) {
			describe((ContentItem) item.getChildAt(i), w);
		}
		w.pop();
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

}
