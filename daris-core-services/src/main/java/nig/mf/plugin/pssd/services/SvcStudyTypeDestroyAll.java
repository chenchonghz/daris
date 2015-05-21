package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.*;
import arc.xml.*;

public class SvcStudyTypeDestroyAll extends PluginService {
	private Interface _defn;

	public SvcStudyTypeDestroyAll() {
		_defn = new Interface();
	}

	public String name() {
		return "om.pssd.study.type.destroy.all";
	}

	public String description() {
		return "Removes all types of study on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
				
		XmlDoc.Element r = executor().execute("om.pssd.study.type.describe");
		if (r!=null) {
			Collection<String> types = r.values("type/name");
			if (types!=null) {
				for (String type : types) {
					XmlDocMaker dm = new XmlDocMaker("args");
					dm.add("type",type);
					executor().execute("om.pssd.study.type.destroy", dm.root());
					w.add("type", type);
				}
			}
		}
	}
	
}
