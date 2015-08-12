package nig.mf.plugin.pssd.services;

import nig.mf.pssd.CiteableIdUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;


public class SvcTesting extends PluginService {
	private Interface _defn;

	public SvcTesting() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id", StringType.DEFAULT, "The identity of the PSSD object. If not supplied, returns root level object types.", 0, 1);
		_defn.add(me);
	}

	public String name() {
		return "om.pssd.test";
	}

	public String description() {
		return "Test stuff.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String id = args.value("id");
		Boolean b1 = CiteableIdUtil.isCiteableId(id);
		w.add("b1", b1);
		
	    w.add("n1", CiteableIdUtil.isNumeric(id));
	}
}
