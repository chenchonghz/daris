package nig.iio.metadata;

import arc.xml.*;
import nig.mf.Executor;

import java.util.Collection;

/**
 * Transforms the meta-data template definition from a Method object and converts the 
 * subject-relevant (but not R-SUbject as these are deprecated) 'value' specifications into actual 
 * XML.    This can be utilised, for example, when auto-creating Subjects, to populate
 * all Method pre-specified meta-data
 * 
 * Because it is used by both client and plugin code, it makes use of the Executor class
 * that wraps these two environments.
 * 
 * @author nebk
 *
 */
public class SubjectMethodMetadata  {

	/**
	 * This function takes the meta-data template definition from a Method object and converts the 
	 * subject-relevant (but not R-SUbject as these are deprecated) 'value' specifications into actual 
	 * XML.  
	 *
	 * @param executor The integrated plugin/client executor
	 * @param mid  The Method object of relevance.
	 * @param dm   The meta-data will be added to this (in readiness for use in om.pssd.subject.create) with
	 *   top-level "public" and "private" entries. R-Subjects are not handled.
	 * @throws Throwable
	 */
	public static void addSubjectMethodMeta (Executor executor, String mid, XmlDocMaker dm) throws Throwable {

		// Get Method Meta-data
		XmlDocMaker dm2 = new XmlDocMaker("args");
		dm2.add("cid", mid);
		XmlDoc.Element me = executor.execute("asset.get", dm2);
		if (me==null) return;

		// Find the Subject-only meta-data definition
		// Method structure in daris:pssd-method-subject is
		// :public
		// :private
		XmlDoc.Element me2 = me.element("asset/meta/daris:pssd-method-subject");
		if (me2==null) return;

		// Now convert these template definitions into actual XML
		// This translates :value specifications into actual elements
		// We don't handle R-Subjects any more
		//
		// The meta-data structure in om.pssd.subject.create is
		//      :private
		//      :public
		XmlDoc.Element publicMeta = me2.element("public");
		if (publicMeta!=null) {
			dm.push("public");
			addSubjectMethodMetaTemplate (executor, dm, publicMeta);
			dm.pop();
		}
		//
		XmlDoc.Element privateMeta = me2.element("private");
		if (privateMeta!=null){
			dm.push("private");
			addSubjectMethodMetaTemplate (executor, dm, privateMeta);
			dm.pop();
		}
	}

	private static void addSubjectMethodMetaTemplate (Executor executor, XmlDocMaker dm, XmlDoc.Element metaDef) throws Throwable {

		if (metaDef==null) return;

		// Method structure in daris:pssd-method-subject is
		//   :metadata
		//     :definition
		//     :value
		Collection<XmlDoc.Element> metas  = metaDef.elements("metadata");
		if (metas==null) return;

		// Iterate through document type specifications
		for (XmlDoc.Element meta : metas) {

			// Get DocType and all values
			String docType = meta.value("definition");
			Collection<XmlDoc.Element> values = meta.elements("value");
			if (values!=null) {

				// Prepare arguments for template conversion
				XmlDocMaker dm2 = new XmlDocMaker("args");
				dm2.push("template", new String[]{"type", docType});
				for (XmlDoc.Element value : values) dm2.add(value);
				dm2.pop();
				//
				XmlDoc.Element r = executor.execute("asset.doc.template.as.xml", dm2);

				// Add this to the subject creation meta-data
				if (r!=null) {
					Collection<XmlDoc.Element> els = r.elements();
					if (els!=null) {
						for (XmlDoc.Element el : els) dm.add(el);
					}
				}
			}
		}
	}
}
