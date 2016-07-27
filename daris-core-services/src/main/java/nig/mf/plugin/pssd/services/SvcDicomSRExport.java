package nig.mf.plugin.pssd.services;

import java.io.ByteArrayInputStream;
import java.util.AbstractMap.SimpleEntry;

import com.pixelmed.dicom.ContentItem;
import com.pixelmed.dicom.StructuredReport;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlStringWriter;
import arc.xml.XmlWriter;

public class SvcDicomSRExport extends PluginService {

	public static final String SERVICE_NAME = "daris.dicom.sr.export";

	private Interface _defn;

	public SvcDicomSRExport() {
		_defn = new Interface();
		_defn.add(new Interface.Element("format", new EnumType(new String[] { "html", "csv", "xml" }),
				"Output file format. Defaults to html.", 0, 1));
		SvcDicomSRGet.addToDefinition(_defn);
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
		return "Export DICOM structured report.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		String format = args.stringValue("format", "html");
		SimpleEntry<XmlDoc.Element, StructuredReport> entry = SvcDicomSRGet.read(executor(), args);
		StructuredReport sr = entry.getValue();
		XmlDoc.Element ae = entry.getKey();
		String title = titleFor(ae);
		PluginService.Output output = outputs.output(0);
		String os;
		String type;
		if ("html".equals(format)) {
			StringBuilder sb = new StringBuilder();
			exportHtml(title, sr, sb);
			os = sb.toString();
			type = "text/html";
		} else if ("csv".equalsIgnoreCase(format)) {
			StringBuilder sb = new StringBuilder();
			exportCsv(title, sr, sb);
			os = sb.toString();
			type = "text/csv";
		} else {
			XmlStringWriter xw = new XmlStringWriter();
			SvcDicomSRGet.describe(ae, sr, xw);
			os = xw.document();
			type = "text/xml";
		}
		byte[] b = os.getBytes("UTF-8");
		output.setData(new ByteArrayInputStream(b), b.length, type);
	}

	static String titleFor(XmlDoc.Element ae) throws Throwable {
		StringBuilder sb = new StringBuilder();
		if (ae.elementExists("cid")) {
			sb.append(ae.value("cid"));
		} else {
			sb.append(ae.value("@id"));
		}
		String name = SvcDicomSRGet.nameFor(ae);
		if (name != null) {
			sb.append(": ").append(name);
		}
		return sb.toString();
	}

	static void exportHtml(String title, StructuredReport sr, StringBuilder sb) throws Throwable {
		sb.append("<html>\n");
		sb.append("<head>\n");
		sb.append("<title>DICOM Structured Report - " + title + "</title>\n");
		sb.append("<style>\n");
		sb.append("tr:nth-child(even) {background:#eee;}\n");
		sb.append("tr:nth-child(odd) {background:#fff;}\n");
		sb.append(
				"td {font-size: 9pt; line-height: 1.5em; font-family:Consolas,Monaco,Lucida Console,Liberation Mono,DejaVu Sans Mono,Bitstream Vera Sans Mono,Courier New,Monospace;}\n");
		sb.append("th {font-size: 9pt; line-height: 1.5em;}\n");
		sb.append("</style>\n");
		sb.append("</head>\n");
		sb.append("<body>\n");
		sb.append("<table style=\"width:100%;\">\n");
		sb.append("<thead>\n");
		sb.append("<tr style=\"background:#eee;\"><th colspan=\"3\">" + title + "</th></tr>\n");
		sb.append("<tr style=\"background:#eee;\"><th>Item</th><th>Type</th><th>Value</th></tr>\n");
		sb.append("</thead>\n");
		sb.append("<tbody>\n");
		exportHtml((ContentItem) sr.getRoot(), sb, "1");
		sb.append("</tbody>\n");
		sb.append("</table>\n");
		sb.append("</body>\n");
		sb.append("</html>");
	}

	static void exportHtml(ContentItem item, StringBuilder sb, String indent) throws Throwable {
		String conceptNameCodeMeaning = item.getConceptNameCodeMeaning();
		String conceptValue = item.getConceptValue();
		// String conceptNameCodeValue = item.getConceptNameCodeValue();
		String valueType = item.getValueType();
		// String relationshipType = item.getRelationshipType();
		sb.append("<tr><td nowrap>");
		sb.append(indent + ". ");
		sb.append(conceptNameCodeMeaning);
		sb.append("</td><td align=\"center\">");
		sb.append(valueType.toLowerCase());
		sb.append("</td><td align=\"center\">");
		sb.append(conceptValue == null ? "&nbsp;" : conceptValue);
		sb.append("</td></tr>\n");
		int n = item.getChildCount();
		for (int i = 0; i < n; i++) {
			exportHtml((ContentItem) (item.getChildAt(i)), sb, indent + "." + String.valueOf(i + 1));
		}
	}

	static void exportCsv(String title, StructuredReport sr, StringBuilder sb) throws Throwable {
		sb.append('"').append(title).append('"').append(",,,,,,\n");
		sb.append("ID,Name,Type,Value,Code,Relationship,\n");
		exportCsv((ContentItem) sr.getRoot(), sb, "1");
	}

	static void exportCsv(ContentItem item, StringBuilder sb, String indent) throws Throwable {
		String conceptNameCodeMeaning = item.getConceptNameCodeMeaning();
		String conceptValue = item.getConceptValue();
		String conceptNameCodeValue = item.getConceptNameCodeValue();
		String valueType = item.getValueType();
		String relationshipType = item.getRelationshipType();
		sb.append('"').append(indent).append('"').append(",");
		sb.append('"').append(conceptNameCodeMeaning).append('"').append(",");
		sb.append('"').append(valueType.toLowerCase()).append('"').append(",");
		if (conceptValue == null) {
			sb.append(",");
		} else {
			sb.append('"').append(conceptValue).append('"').append(",");
		}
		sb.append('"').append(conceptNameCodeValue).append('"').append(",");
		if (relationshipType == null) {
			sb.append(",");
		} else {
			sb.append('"').append(relationshipType).append('"').append(",");
		}
		sb.append("\n");
		int n = item.getChildCount();
		for (int i = 0; i < n; i++) {
			exportCsv((ContentItem) (item.getChildAt(i)), sb, indent + "." + String.valueOf(i + 1));
		}
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

	@Override
	public int minNumberOfOutputs() {
		return 1;
	}

	@Override
	public int maxNumberOfOutputs() {
		return 1;
	}

}
