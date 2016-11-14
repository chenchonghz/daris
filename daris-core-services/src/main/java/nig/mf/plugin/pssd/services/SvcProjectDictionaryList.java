package nig.mf.plugin.pssd.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.plugin.pssd.Project;

public class SvcProjectDictionaryList extends PluginService {

	public static final String SERVICE_NAME = "daris.project.dictionary.list";

	private Interface _defn;

	public SvcProjectDictionaryList() {
		_defn = new Interface();
		_defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citable id of the project.", 1, 1));
		_defn.add(new Interface.Element("count", BooleanType.DEFAULT,
				"If set returns the total number of dictionaries in the system as part of cursor information.", 0, 1));
		_defn.add(new Interface.Element("idx", LongType.DEFAULT,
				"Absolute cursor position. If used, the cursor will be positioned starting at 'idx'.", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE,
				"Number of results to return. Defaults to 100 for actions returning results.", 0, 1));
		_defn.add(new Interface.Element("starting-with", StringType.DEFAULT,
				"An optional filter for dictionaries, starting with the given characters.", 0, 1));

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
		return "List all project specific dictionaries.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs arg2, XmlWriter w) throws Throwable {
		String projectCid = args.value("cid");

		String dictNS = Project.projectSpecificDictionaryNamespaceOf(projectCid);

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("namespace", dictNS);
		if (args.elementExists("count")) {
			dm.add(args.element("count"), true);
		}
		if (args.elementExists("idx")) {
			dm.add(args.element("idx"), true);
		}
		if (args.elementExists("size")) {
			dm.add(args.element("size"), true);
		}
		if (args.elementExists("starting-with")) {
			dm.add(args.element("starting-with"), true);
		}
		XmlDoc.Element re = executor().execute("dictionary.list", dm.root());
		if (re != null && re.hasSubElements()) {
			w.add(re, false);
		}
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

}
