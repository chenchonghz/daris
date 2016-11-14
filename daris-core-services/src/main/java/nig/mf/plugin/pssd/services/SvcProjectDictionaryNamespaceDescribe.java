package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import nig.mf.plugin.pssd.Project;

public class SvcProjectDictionaryNamespaceDescribe extends PluginService {
	public static final String SERVICE_NAME = "daris.project.dictionary.namespace.describe";

	private Interface _defn;

	public SvcProjectDictionaryNamespaceDescribe() {
		_defn = new Interface();
		_defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the DaRIS project.", 1, 1));
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
		return "Gets the project specific dictionary namespace.";
	}

	@Override
	public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
		String projectCid = args.value("cid");
		String dictNS = Project.projectSpecificDictionaryNamespaceOf(projectCid);
		boolean exists = SvcProjectDictionaryCreate.dictionaryNamespaceExists(executor(), projectCid);
		w.add("namespace", new String[] { "exists", Boolean.toString(exists) }, dictNS);
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

}
