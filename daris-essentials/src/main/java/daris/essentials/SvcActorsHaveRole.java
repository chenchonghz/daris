package daris.essentials;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcActorsHaveRole extends PluginService {

	private Interface _defn;

	public SvcActorsHaveRole() {

		_defn = new Interface();
		_defn.add(new Element("role", StringType.DEFAULT, "The role of interest.", 1, 1));
		_defn.add(new Element("type", StringType.DEFAULT, "The type of actor to scrutinize", 1, 1));

	}

	public String name() {

		return "nig.actors.have.role";

	}

	public String description() {

		return "Finds which actors of the specified type hold the specified role.";

	}

	public Interface definition() {

		return _defn;

	}

	public Access access() {

		return ACCESS_ACCESS;

	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", args.stringValue("type"));
		dm.add("role", new String[]{"type", "role"}, args.stringValue("role"));
		XmlDoc.Element r = executor().execute("actor.have", dm.root());
		if (r!=null) {
			Collection<XmlDoc.Element> elements = r.elements("actor");
			if (elements!=null) {
				for (XmlDoc.Element element : elements) {
					Boolean hasRole = element.booleanValue("role");
					if (hasRole) {
						w.add(element);
					}
				}
			}			
		}
	}
}