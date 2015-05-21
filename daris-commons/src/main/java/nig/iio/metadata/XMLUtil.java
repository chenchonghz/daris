package nig.iio.metadata;

import java.util.Collection;
import java.util.List;

import nig.sec.encode.Base64Coder;
import nig.sec.encode.EncodingTypes;
import nig.sec.encode.ROT13;
import nig.sec.encode.EncodingTypes.EncodingType;

import java.util.Vector;

import arc.xml.XmlDoc;

/**
 * 
 * Mediaflux XML handling utilities
 * 
 * @author nebk
 */

public class XMLUtil {

	public static final int DEFAULT_INDENT_STEP = 4;

	
	/**
	 * Recursively replace any instances where the value of an element has the given old String.
	 * 
	 * @param doc The document to search
	 * @param oldValue  The value of the String to replace
	 * @param newValue  The new value of the replaced String
	 * @param exact If true, the String to be replaced must match exactly.  Otherwise, the 
	 * String to be replaced must just be contained and that contained SubString will be replaced.
	 * @return true if replacement occurred else false
	 * 
	 */
	public static boolean replaceString (XmlDoc.Element doc, String oldValue, String newValue, boolean exact) {
		if (doc==null) return false;
		List<XmlDoc.Element> els = doc.elements();
		if (els==null) return false;
		boolean doReplace = false;

		for (XmlDoc.Element el : els) {

			if (el.hasValue()) {
				String val = el.value();
				if (!exact) {
					if (val.contains(oldValue)) {
						val = val.replace(oldValue, newValue);
						el.setValue(val);
						doReplace = true;
					}
				} else {
					if (val.equals(oldValue)) {
						el.setValue(newValue);
						doReplace = true;
					}
				}
			}

			// Recurse down
			boolean doReplace2 = replaceString (el, oldValue, newValue, exact);
			if (doReplace2) doReplace = true;
		}

		return doReplace;
	}


	/**
	 * Remove the given attribute (by name) from all of the direct children  of the given node.
	 * 
	 * @param me
	 * @param tag
	 */
	public static void removeAttribute (XmlDoc.Element me, String attributeName) {

		Collection<XmlDoc.Element> eles = me.elements();
		if (eles == null) {
			return;
		}

		for (XmlDoc.Element el : eles) {
			XmlDoc.Attribute attr = el.attribute(attributeName);
			if (attr != null) {
				el.remove(attr);
			}
		}
	}


	/**
	 * Remove all occurrences of the named element from the document.  
	 * 
	 * @param doc
	 * @param el
	 * @throws Throwable
	 */
	public static void removeElement (XmlDoc.Element doc, String name) throws Throwable {
		Collection<XmlDoc.Element> els = doc.elements(name);
		if (els!=null) {
			for (XmlDoc.Element el : els) {
				doc.remove(el);
			}
		}
	}

	/**
	 * Find all children documents of the given type, and then for each, remove all instances
	 * of child elements of the given name
	 * 
	 * @param meta  Will be modified in-situ on output
	 * @param docType
	 * @param elementNames The names of the elements in this document type to remove
	 * @return A collection holding the documents that had an element removed.  Will always be non-null but may be of length 0
	 * @throws Throwable
	 */
	public static Collection<XmlDoc.Element> removeElements (XmlDoc.Element meta, String docType, String[] elementNames) throws Throwable {
		Collection<XmlDoc.Element> docOut = new Vector<XmlDoc.Element>();
		if (meta==null) return docOut;

		// Iterate over the documents 
		Collection<XmlDoc.Element> docs = meta.elements(docType);
		if (docs!=null) {
			for (XmlDoc.Element doc : docs) {

				// Now iterate through the elements we are interested in and remove them if found
				boolean some = false;
				for (int i=0; i<elementNames.length; i++) {
					Collection<XmlDoc.Element> els = doc.elements(elementNames[i]);
					if (els!=null) {
						for (XmlDoc.Element el : els) {
							if (doc.removeInstance(el)) some = true;
						}
					}
				}

				// Add the modified  document to the output if we actually changed it
				if (some) docOut.add(doc);
			}
		}
		return docOut;
	}


	/**
	 * Builds a new (copy) of the XmlDoc.Element with just the parent name and attributes
	 * 
	 * @param el
	 * @return
	 * @throws Throwable
	 */
	public static XmlDoc.Element copyParentOnly (XmlDoc.Element el) throws Throwable {

		// Old
		Collection<XmlDoc.Attribute> atts = el.attributes();
		String name = el.qname();

		// Prepare new
		XmlDoc.Element el2 = new XmlDoc.Element(new String(name));

		// Copy the attributes; damn Java references
		if (atts!=null) {
			Vector<XmlDoc.Attribute> atts2 = new Vector<XmlDoc.Attribute>(); 
			for (XmlDoc.Attribute att : atts) {
				atts2.add(new XmlDoc.Attribute (new String(att.qname()), new String(att.value())));
			}
			el2.addAllAttributes(atts2);
		}
		return el2;
	}




	/**
	 * Encode String.  The caller must ensure the String is not null
	 * as no check is made
	 * 
	 * @param type
	 * @param str
	 * @return
	 */
	private static String encodeString (EncodingType type, String str) {
		switch (type) {
		case BASE_64: return Base64Coder.encodeString(str); 
		case ROT13: return ROT13.encodeString(str);
		}
		return null;
	}

	/**
	 * Decode String.  The caller must ensure the String is not null
	 * as no check is made
	 * 
	 * @param type
	 * @param str
	 * @return
	 */
	private static String decodeString (EncodingType type, String str) {
		switch (type) {
		case BASE_64: return Base64Coder.decodeString(str);
		case ROT13: return ROT13.decodeString(str);
		}
		return null;
	}


/**
 * Convert document to nicely laid out text (like aterm)
 */
	public static String toText(XmlDoc.Element e) throws Throwable {
		StringBuilder sb = new StringBuilder();
		toText(e, sb);
		return sb.toString();
	}
	
/**
 * Convert document to HTML laid out nicely
 * 
 */
 	public static String toHTML(XmlDoc.Element e) throws Throwable {
		StringBuilder sb = new StringBuilder();
		toHTML(e, sb);
		return sb.toString();
	}

    private static void toText(XmlDoc.Element e, StringBuilder sb) {
        toText(e, 0, DEFAULT_INDENT_STEP, sb);
    }

    private static void toText(XmlDoc.Element e, int indent, int indentStep, StringBuilder sb) {
        for (int i = 0; i < indent; i++) {
            sb.append(" ");
        }
        sb.append(":" + e.qname());
        if (e.hasAttributes()) {
            List<XmlDoc.Attribute> attrs = e.attributes();
            for (XmlDoc.Attribute attr : attrs) {
                sb.append(" -" + attr.qname());
                sb.append(" " + attr.value());
            }
        }
        if (e.hasValue()) {
            sb.append(" " + e.value());
        }
        sb.append("\n");
        if (e.hasSubElements()) {
            List<XmlDoc.Element> ses = e.elements();
            for (XmlDoc.Element se : ses) {
                toText(se, indent + indentStep, indentStep, sb);
            }
        }
    }
    
    private static void toHTML(XmlDoc.Element e, StringBuilder sb) {
        toHTML(e, 0, DEFAULT_INDENT_STEP, sb);
    }

    private static void toHTML(XmlDoc.Element e, int indent, int indentStep, StringBuilder sb) {
        for (int i = 0; i < indent; i++) {
            sb.append("&nbsp;");
        }
        sb.append(":" + e.qname());
        if (e.hasAttributes()) {
            List<XmlDoc.Attribute> attrs = e.attributes();
            for (XmlDoc.Attribute attr : attrs) {
                sb.append(" -" + attr.qname());
                sb.append(" " + attr.value());
            }
        }
        if (e.hasValue()) {
            sb.append(" " + e.value());
        }
        sb.append("<br/>");
        if (e.hasSubElements()) {
            List<XmlDoc.Element> ses = e.elements();
            for (XmlDoc.Element se : ses) {
                toHTML(se, indent + indentStep, indentStep, sb);
            }
        }
    }
}
