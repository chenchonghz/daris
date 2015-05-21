package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcMethodForSubjectUpdate extends PluginService {
	private Interface _defn;

	public SvcMethodForSubjectUpdate() throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of the method. Must be managed by the local server. If not goven, Method will be created.",0,1));
		SvcMethodForSubjectCreate.addInterface(_defn);
		_defn.add(new Interface.Element("replace", BooleanType.DEFAULT, "Replace (default) or merge the meta-data", 0, 1));
	}

	public String name() {
		return "om.pssd.method.for.subject.update";
	}

	public String description() {
		return "Updates or creates a research method. replace=true fully replaces all internals.  replace=false merges, but you can only add steps to the end of the existing list (you can't edit a pre-existing step).";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
			SvcMethodUpdate.updateMethod (executor(), args, in, out, w);
	}
}
