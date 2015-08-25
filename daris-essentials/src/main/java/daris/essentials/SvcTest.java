package daris.essentials;

import arc.mf.plugin.PluginService;

import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;



public class SvcTest extends PluginService {
	private Interface _defn;

	public SvcTest() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("echo",XmlDocType.DEFAULT, "The ae", 0, 1));

	}

	@Override
	public String name() {

		return "nig.testing";
	}

	@Override
	public String description() {

		return "Test";
	}

	@Override
	public Interface definition() {

		return _defn;
	}

	@Override
	public Access access() {

		return ACCESS_ACCESS;
	}

	@Override
	public boolean canBeAborted() {

		return true;
	}

	@Override
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {


		XmlDoc.Element t = args.element("echo");
		System.out.println("echo="+t);

	}
}
