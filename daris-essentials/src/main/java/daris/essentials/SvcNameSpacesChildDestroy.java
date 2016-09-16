package daris.essentials;

import java.util.Collection;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcNameSpacesChildDestroy extends PluginService {


	private Interface _defn;

	public SvcNameSpacesChildDestroy() {
		_defn = new Interface();
		_defn.add(new Interface.Element("namespace", StringType.DEFAULT, "If true, (default false) assets will actually be moved to the correct namespace (one at a time; not efficient) rather than just listed.", 0, 1));
		_defn.add(new Interface.Element("destroy", BooleanType.DEFAULT, "Destroy or list (default).", 0, 1));
		_defn.add(new Interface.Element("assets", BooleanType.DEFAULT, "Include assets (default true).", 0, 1));
	}
	public String name() {
		return "nig.namespaces.child.destroy";
	}

	public String description() {
		return "Lists or destroys all child namespaces";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public int executeMode() {
		return EXECUTE_LOCAL;
	}

	public boolean canBeAborted() {

		return true;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String namespace = args.value("namespace");
		Boolean destroy = args.booleanValue("destroy", false);
		Boolean includeAssets = args.booleanValue("assets", true);

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("namespace", namespace);
		if (includeAssets) dm.add("assets", true);
		XmlDoc.Element r = executor().execute("asset.namespace.list", dm.root());
		if (r==null) return;
		Collection<String> nss = r.values("namespace/namespace");
		Collection<XmlDoc.Element> assets = null;
		if (includeAssets) assets = r.elements("namespace/asset");
		if (nss!=null) {
			for (String ns : nss) {
				String ns2 = namespace + "/" + ns;
				if (destroy) {
					dm = new XmlDocMaker("args");
					dm.add("namespace",ns2);
					executor().execute("asset.namespace.destroy", dm.root());
				}
				w.add("namespace", ns2);
			}
		}
		if (assets!=null) {
			for (XmlDoc.Element asset : assets) {
				String id = asset.value("@id");
				if (destroy) {
					dm = new XmlDocMaker("args");
					dm.add("id", id);
					executor().execute("asset.destroy", dm.root());
				}
				w.add("asset", id);
			}
		}
	}
}
