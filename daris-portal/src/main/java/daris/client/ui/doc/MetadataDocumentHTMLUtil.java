package daris.client.ui.doc;

import java.util.List;

import arc.mf.model.asset.document.MetadataDocument;
import arc.mf.xml.defn.Attribute;
import arc.mf.xml.defn.Element;

public class MetadataDocumentHTMLUtil {

	public static String toHTMLString(MetadataDocument doc) {
		StringBuilder sb = new StringBuilder();

		sb.append("<div style=\"font-family: sans-serif; font-size: 11px; margin: 4px; position: absolute;\">");
		sb.append("<div style=\"margin-bottom: 4px;\">");
		sb.append("<div style=\"font-weight: bold;\"><span>" + doc.type() + "</span></div>");
		if (doc.description() != null) {
			sb.append("<div style=\"font-family: sans-serif; font-size: 11px; padding: 4px; font-style: italic;\"><span>"
					+ doc.description() + "</span></div>");
		}
		sb.append("</div>");
		sb.append("<div>");
		List<Element> des = doc.definition().root().elements();
		for (Element de : des) {
			toHTMLString(de, true, sb);
		}
		sb.append("</div>");
		return sb.toString();
	}

	private static void toHTMLString(Element de, boolean top, StringBuilder sb) {
		sb.append("<div style=\"font-family: sans-serif; font-size: 11px; margin-left: " + (top ? 2 : 8)
				+ "px; padding: " + (top ? "8px 0px 0px" : "4px 0px 4px 4px")
				+ "; border-left: 1px solid rgb(221, 221, 221);\">");
		sb.append("<div>");
		sb.append("<span style=\"cursor: default; font-weight: bold; padding: 4px; margin-left: 4px;\">");
		sb.append(de.name());
		sb.append("</span>");
		sb.append("<span style=\"font-style: italic;\">");
		sb.append("[");
		sb.append(de.minOccurs());
		sb.append("..");
		sb.append(de.maxOccurs() == Integer.MAX_VALUE ? "&#8734;" : Integer.toString(de.maxOccurs()));
		sb.append("] ");
		sb.append(de.type().name());
		sb.append("</span>");
		sb.append("</div>");
		if (de.description() != null) {
			sb.append("<div style=\"font-family: sans-serif; font-size: 11px; margin-top: 2px; margin-left: 12px; margin-bottom: 2px; padding: 4px; font-style: italic;\">");
			sb.append("<span>");
			sb.append(de.description());
			sb.append("</span>");
			sb.append("</div>");
		}
		List<Attribute> attrs = de.attributes();
		if (attrs != null) {
			for (Attribute attr : attrs) {
				toHTMLString(attr, sb);
			}
		}
		List<Element> elems = de.elements();
		if (elems != null) {
			for (Element elem : elems) {
				toHTMLString(elem, false, sb);
			}
		}
		sb.append("</div>");
	}

	private static void toHTMLString(Attribute attr, StringBuilder sb) {
		sb.append("<div style=\"font-family: sans-serif; font-size: 11px; margin-left: 8px; padding: 4px 0px 0px 0px; border-left: 1px solid rgb(221, 221, 221);\">");
		sb.append("<div>");
		sb.append("<span style=\"cursor: default; font-weight: bold; padding: 4px; margin-left: 4px;\">");
		sb.append(attr.name());
		sb.append("</span>");
		sb.append("<span style=\"font-style: italic;\">");
		sb.append("[");
		sb.append(attr.minOccurs());
		sb.append("..");
		sb.append(attr.maxOccurs() == Integer.MAX_VALUE ? "&#8734;" : Integer.toString(attr.maxOccurs()));
		sb.append("] ");
		sb.append(attr.type().name());
		sb.append("</span>");
		sb.append("</div>");
		if (attr.description() != null) {
			sb.append("<div style=\"font-family: sans-serif; font-size: 11px; margin-top: 2px; margin-left: 12px; margin-bottom: 2px; padding: 4px; font-style: italic;\">");
			sb.append("<span>");
			sb.append(attr.description());
			sb.append("</span>");
			sb.append("</div>");
		}
		sb.append("</div>");
	}
}
