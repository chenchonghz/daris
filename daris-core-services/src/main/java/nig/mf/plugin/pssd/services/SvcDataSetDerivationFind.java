package nig.mf.plugin.pssd.services;

import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDataSetDerivationFind extends PluginService {

	private Interface _defn;

	public SvcDataSetDerivationFind() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id", CiteableIdType.DEFAULT,
				"The citeable id of the dataset that the datasets are derived from.", 1, 1);
		me.add(new Interface.Attribute(
				"proute",
				CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.",
				0));
		_defn.add(me);
		_defn.add(new Interface.Element(
				"processed",
				BooleanType.DEFAULT,
				"Sets to true to return only the processed datasets (which is the subclass of the derived data set). Defaults to false, returns all derived data sets.",
				0, 1));
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
		return "Finds the derived data sets of the specified data set.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		String cid = args.value("id");
		String proute = args.value("id/@proute");
		boolean processed = args.booleanValue("processed", false);
		StringBuilder sb = new StringBuilder();
		sb.append("xpath(daris:pssd-derivation/input) = '" + cid + "'");
		if (processed) {
			sb.append(" and (xpath(daris:pssd-derivation/processed)=true)");
		}
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("pdist", 0);
		dm.add("where", sb.toString());
		dm.add("size", "infinity");
		dm.add("action", "get-cid");
		XmlDoc.Element r = executor()
				.execute(proute == null ? null : new ServerRoute(proute), "asset.query", dm.root());
		List<XmlDoc.Element> ces = r.elements("cid");
		if (ces != null) {
			for (XmlDoc.Element ce : ces) {
				w.add("id", new String[] { "asset-id", ce.value("@id") }, ce.value());
			}
		}
	}

	@Override
	public String name() {
		return "om.pssd.dataset.derivation.find";
	}

}
