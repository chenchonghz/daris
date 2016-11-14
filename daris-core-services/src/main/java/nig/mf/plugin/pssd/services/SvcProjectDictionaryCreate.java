package nig.mf.plugin.pssd.services;

import java.util.regex.Pattern;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.plugin.pssd.Project;

public class SvcProjectDictionaryCreate extends PluginService {

	public static final String SERVICE_NAME = "daris.project.dictionary.create";

	private Interface _defn;

	public SvcProjectDictionaryCreate() {
		_defn = new Interface();

		_defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citable id of the project.", 1, 1));

		_defn.add(new Interface.Element("name", new StringType(255, Pattern.compile("[a-zA-Z_0-9\\.\\-]+")),
				"The name of the dictionary.", 1, 1));

		_defn.add(new Interface.Element("description", StringType.DEFAULT, "The description about the dictionary.", 0,
				1));

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
		return "Creates project specific dictionary.";
	}

	@Override
	public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
		String cid = args.value("cid");
		String name = args.value("name");
		String description = args.value("description");
		String dictionaryNamespace = Project.projectSpecificDictionaryNamespaceOf(cid);
		String dictionaryName = dictionaryNamespace + ":" + name;

		if (!projectExists(executor(), cid)) {
			throw new Exception("DaRIS project " + cid + " does not exist.");
		}

		if (!dictionaryNamespaceExists(executor(), cid)) {
			throw new Exception("Dictionary namespace: " + dictionaryNamespace + " does not exist. Run "
					+ SvcProjectDictionaryNamespaceCreate.SERVICE_NAME + " to create the dictionary namespace.");
		}

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("name", dictionaryName);
		dm.addIfSet("description", description);
		executor().execute("dictionary.create", dm.root());

		w.add("dictionary", new String[] { "created", "true" }, dictionaryName);
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

	static boolean projectExists(ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", "cid='" + cid + "' and model='om.pssd.project'");
		dm.add("action", "count");
		return executor.execute("asset.query", dm.root()).longValue("value") > 0;
	}

	static boolean dictionaryNamespaceExists(ServiceExecutor executor, String cid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("namespace", Project.projectSpecificDictionaryNamespaceOf(cid));
		return executor.execute("dictionary.namespace.exists", dm.root()).booleanValue("exists", false);
	}

	// public static void main(String[] args) throws Throwable {
	// System.out.println("abc.a12".matches("[a-zA-Z_0-9\\.\\-]+"));
	// System.out.println("a-bc.a_12".matches("[a-zA-Z_0-9\\.\\-]+"));
	// }
}
