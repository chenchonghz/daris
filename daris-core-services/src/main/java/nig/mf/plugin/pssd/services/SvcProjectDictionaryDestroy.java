package nig.mf.plugin.pssd.services;

import java.util.regex.Pattern;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.plugin.pssd.Project;

public class SvcProjectDictionaryDestroy extends PluginService {

	public static final String SERVICE_NAME = "daris.project.dictionary.destroy";

	private Interface _defn;

	public SvcProjectDictionaryDestroy() {
		_defn = new Interface();

		_defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citable id of the project.", 1, 1));

		_defn.add(new Interface.Element("name", new StringType(255, Pattern.compile("[a-zA-Z_0-9\\.\\-]+")),
				"The name of the dictionary.", 1, 1));

	}

	@Override
	public Access access() {
		return ACCESS_MODIFY;
	}

	@Override
	public Interface definition() {
		return _defn;
	}

	@Override
	public String description() {
		return "Deletes a project specific dictionary.";
	}

	@Override
	public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
		String cid = args.value("cid");
		String name = args.value("name");

		String dictionaryNamespace = Project.projectSpecificDictionaryNamespaceOf(cid);
		String dictionaryName = dictionaryNamespace + ":" + name;

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("name", dictionaryName);
		executor().execute("dictionary.destroy", dm.root());

		w.add("dictionary", new String[] { "destroyed", "true" }, dictionaryName);

	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

}
