package nig.dicom.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.ContentItem;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.StructuredReport;
import com.pixelmed.dicom.TagFromName;

public class DicomSR2Html {

	public static void main(String[] args) throws Throwable {
		try {
			if (args == null || args.length < 1 || args.length > 2) {
				throw new IllegalArgumentException("Invalid arguments.");
			}
			File dcmFile = new File(args[0]);
			File htmlFile = new File(args.length == 2 ? args[1] : generateHtmlFileName(args[0]));
			dicomSR2Html(dcmFile, htmlFile);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.err.println("Error: " + e.getMessage());
			printUsage();
		}
	}

	private static String generateHtmlFileName(String dcmFileName) {
		String htmlFileName;
		if (dcmFileName.endsWith(".dcm") || dcmFileName.endsWith(".DCM")) {
			int idx = dcmFileName.lastIndexOf('.');
			htmlFileName = dcmFileName.substring(0, idx);
		} else {
			htmlFileName = dcmFileName;
		}
		return htmlFileName + ".html";
	}

	static void dicomSR2Html(File f, File of) throws Throwable {
		if (!f.exists()) {
			throw new FileNotFoundException(f.getPath() + " is not found.");
		}
		if (!DicomFileCheck.isDicomFile(f)) {
			throw new DicomException(f.getAbsolutePath() + " is not a dicom file.");
		}

		InputStream in = new BufferedInputStream(new FileInputStream(f));
		OutputStream out = new BufferedOutputStream(new FileOutputStream(of));
		try {
			dicomSR2Html(in, out);
		} finally {
			in.close();
			out.close();
		}
	}

	static void dicomSR2Html(InputStream in, OutputStream out) throws Throwable {
		if (!in.markSupported()) {
			in = new BufferedInputStream(in);
		}
		AttributeList al = new AttributeList();
		al.read(new DicomInputStream(in));
		Attribute a = al.get(TagFromName.Modality);
		if (a == null) {
			throw new DicomException("Modality attribute is not found.");
		}
		String modality = a.getSingleStringValueOrNull();
		if (!"SR".equalsIgnoreCase(modality)) {
			throw new DicomException("Modality 'SR' is expected. Found '" + modality + "'.");
		}
		StructuredReport sr = new StructuredReport(al);
		PrintStream ps = new PrintStream(out);
		try {
			ps.println("<html>");
			ps.println("<head>");
			ps.println("<title>DICOM Structured Report</title>");
			ps.println("<style>");
			ps.println("tr:nth-child(even) {background:#eee;}");
			ps.println("tr:nth-child(odd) {background:#fff;}");
			ps.println(
					"td {font-size: 9pt; line-height: 1.5em; font-family:Consolas,Monaco,Lucida Console,Liberation Mono,DejaVu Sans Mono,Bitstream Vera Sans Mono,Courier New,Monospace;}");
			ps.println("th {font-size: 9pt; line-height: 1.5em;}");
			ps.println("</style>");
			ps.println("</head>");
			ps.println("<body>");
			ps.println("<table style=\"width:100%;\">");
			ps.println("<thead>");
			ps.println("<tr style=\"background:#eee;\"><th>Item</th><th>Type</th><th>Value</th></tr>");
			ps.println("</thead>");
			ps.println("<tbody>");
			process((ContentItem) sr.getRoot(), ps, "1");
			ps.println("</tbody>");
			ps.println("</table>");
			ps.println("</body>");
			ps.println("</html>");
		} finally {
			ps.close();
		}

	}

	static void process(ContentItem node, PrintStream ps, String indent) {

		String conceptNameCodeMeaning = node.getConceptNameCodeMeaning();
		String conceptValue = node.getConceptValue();
		// String conceptNameCodeValue = node.getConceptNameCodeValue();
		String valueType = node.getValueType();
		// String relationshipType = node.getRelationshipType();
		ps.print("<tr><td nowrap>");
		ps.print(indent + ". ");
		ps.print(conceptNameCodeMeaning);
		ps.print("</td><td align=\"center\">");
		ps.print(valueType.toLowerCase());
		ps.print("</td><td align=\"center\">");
		ps.print(conceptValue == null ? "&nbsp;" : conceptValue);
		ps.println("</td></tr>");
		int n = node.getChildCount();
		for (int i = 0; i < n; i++) {
			process((ContentItem) (node.getChildAt(i)), ps, indent + "." + String.valueOf(i + 1));
		}
	}

	private static void printUsage() {
		System.out.println("");
		System.out.println("Usage: dcmsr2html <dicom-file> [html-file]");
		System.out.println("");
	}

}
