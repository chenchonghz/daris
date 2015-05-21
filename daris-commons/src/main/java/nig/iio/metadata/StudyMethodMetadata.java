package nig.iio.metadata;

import arc.xml.*;
import nig.mf.Executor;
import nig.mf.pssd.plugin.util.DistributedAsset;

import java.util.Collection;

/**
 * Transforms the "value" meta-data description of an ExMethod step into actual 
 * XML.    This can be utilised, for example, when creating Studies, to populate
 * all Method pre-specified meta-data
 * 
 * Because it is used by both client and plugin code, it makes use of the Executor class
 * that wraps these two environments.
 * 
 * @author nebk
 *
 */
public class StudyMethodMetadata  {

	/**
	 * This function takes the meta-data template definition from an ExMethod object for a specific
	 * Study step and converts the  'value' specifications into actual XML.
	 * If the specified step does not pertain to a STudy step, it does nothing
	 *
	 * @param executor The integrated plugin/client executor
	 * @param eid  The ExMethod object of relevance.
	 * @param step The step in the Method of interest
	 * @param dm   The meta-data will be added to this (in readiness for use in om.pssd.study.create) with
	 *   top-level "meta" entries with correct namespace
	 * @throws Throwable
	 */
	public static void addStudyMethodMeta (Executor executor, String eid, String step, XmlDocMaker dm) throws Throwable {

		DistributedAsset dEID = new DistributedAsset(null, eid);
		addStudyMethodMeta (executor, dEID, step, dm);
	}

	
	/**
	 * This function takes the meta-data template definition from an ExMethod object for a specific
	 * Study step and converts the  'value' specifications into actual XML.
	 * If the specified step does not pertain to a STudy step, it does nothing
	 *
	 * @param executor The integrated plugin/client executor
	 * @param dEID  The ExMethod object of relevance.
	 * @param step The step in the Method of interest
	 * @param dm   The meta-data will be added to this (in readiness for use in om.pssd.study.create) with
	 *   top-level "meta" entries with correct namespace
	 * @throws Throwable
	 */
	public static void addStudyMethodMeta (Executor executor, DistributedAsset dEID, String step, XmlDocMaker dm) throws Throwable {

		// Get Method Meta-data for this step
		XmlDocMaker dm2 = new XmlDocMaker("args");
		dm2.add(dEID.asXmlDoc("id"));
		dm2.add("step", step);
		XmlDoc.Element me = executor.execute("om.pssd.ex-method.step.describe", dm2);
		if (me==null) return;
		
		// Structure is, for example (for a single level element):
		// 
		// :ex-method
		//    :step
		//       :study
		//          :metadata
		//              :element
		//                 :value     
		//


		// From the description, find the Study component for this step
		XmlDoc.Element me2 = me.element("ex-method/step/study");
		if (me2==null) return;

		// Iterate through structure and convert pre-specified values to actual XML
		dm2 = new XmlDocMaker("args");
		addStudyMethodMetaTemplate (executor, dm2, me2);
		
		// Now set this in the provided XmlDocMaker and add the
		// namespace <exmethod>_<step> 
		Collection<XmlDoc.Element>els = dm2.root().elements();
		if (els!=null) {
			dm.push("meta");
			for (XmlDoc.Element el : els) {
				el.add(new XmlDoc.Attribute("ns", dEID.getCiteableID()+"_"+step));
				dm.add(el);
			}
			dm.pop();
		}
	}


	private static void addStudyMethodMetaTemplate (Executor executor, XmlDocMaker dm, XmlDoc.Element metaDef) throws Throwable {

		if (metaDef==null) return;
		
		// Find all children
		Collection<XmlDoc.Element> metas  = metaDef.elements("metadata");
		if (metas==null) return;

		// Iterate through document type specifications
		for (XmlDoc.Element meta : metas) {

			// Get DocType 
			String docType = meta.value("@type");

			Collection<XmlDoc.Element> els = meta.elements("definition/element");
			if (els!=null) {
				for (XmlDoc.Element el : els) {
					// Build a path string that will be used to build the XML for any elements
					// that have actual values
					String path = docType + "/" + el.value("@name");
					
					// Recursively look for values
					findValues (el, path, dm);
				}
			}
		}
	}

	private static void findValues (XmlDoc.Element el, String path, XmlDocMaker dm) throws Throwable {

		// See if this element has a value and if so, add it to the document
		// We don't look to see if values are constant as well as it doesn't
		// matter in the study creation context
		String value = el.value("value");
		if (value!=null) {
			// NB This overwrites any pre-existing path of the same name
			// This is an unlikely use case in Method specification
			XmlDoc.Element t = dm.root().create(path);
			t.setValue(value);
		}

		// Now recurse down the other elements
		Collection<XmlDoc.Element> els = el.elements("element");
		if (els!=null) {
			for (XmlDoc.Element el2 : els) {
				String path2 = new String(path+"/"+el2.value("@name"));
				findValues (el2, path2, dm);
			}
		}
	}
}
