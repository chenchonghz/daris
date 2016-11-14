package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;
import nig.mf.plugin.pssd.Project;

public class SvcProjectDictionaryNamespaceCreate extends PluginService {

	public static final String SERVICE_NAME = "daris.project.dictionary.namespace.create";

	private Interface _defn;

	public SvcProjectDictionaryNamespaceCreate() {
		_defn = new Interface();
		_defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the DaRIS project.", 1, 1));
	}

	@Override
	public Access access() {
		return ACCESS_ADMINISTER;
	}

	@Override
	public Interface definition() {
		return _defn;
	}

	@Override
	public String description() {
		return "Creates project specific dictionary  namespace.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs arg2, XmlWriter w) throws Throwable {
		String projectCid = args.value("cid");
		Project.createProjectSpecificDictionaryNamespace(executor(), projectCid);
		Project.grantProjectSpecificDictionaryNamespacePermissions(executor(), projectCid);
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

}
