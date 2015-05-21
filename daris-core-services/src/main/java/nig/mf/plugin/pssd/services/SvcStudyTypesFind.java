package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


public class SvcStudyTypesFind extends PluginService {


	private Interface _defn;

	public SvcStudyTypesFind() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The citeable asset id of the local Project to harvest. Default means all.",
				1, 1));
	}

	public String name() {

		return "om.pssd.study.types.find";
	}

	public String description() {

		return "Finds and returns a unique list of Study types for the given parent object.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}




	public void execute(XmlDoc.Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

		String id = args.stringValue("id");
		findStudyTypes (executor(), id, w);

	}

	/**
	 * Returns a collection of unique Study Types for the given parent.
	 * @param executor
	 * @param id
	 * @param w
	 * @return Vector - will be empty if none, never null
	 * @throws Throwable
	 */
	public static Collection<String> findStudyTypes (ServiceExecutor executor, String id, XmlWriter w) throws Throwable {

		// Find all children studies of this project
		XmlDocMaker dm = new XmlDocMaker("args");
		String query = "cid starts with '" + id
				+ "' and xpath(daris:pssd-object/type)='study'";
		dm.add("where", query);
		dm.add("pdist", 0);
		dm.add("action", "get-value");
		dm.add("xpath", "meta/daris:pssd-study/type");
		XmlDoc.Element r = executor.execute("asset.query", dm.root());
		if (r==null) return null;
		Collection<String> studyTypes = r.values("asset/value");
		if (studyTypes==null || studyTypes.size()==0) return null;

		// Now find unique list of Study types
		HashMap<String,String> types = new HashMap<String,String>();
		Vector<String> cTypes = new Vector<String>();
		for (String studyType : studyTypes) { 
			if (!types.containsKey(studyType)) {
				types.put(studyType, "XXX");   // Dummy value
				w.add("type", studyType);
				cTypes.add(studyType);
			}	
		}
		return cTypes;
	}
}
