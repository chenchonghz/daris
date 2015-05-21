package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.method.Method;
import nig.mf.pssd.plugin.util.DistributedQuery;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.DictionaryEnumType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.LongType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcMethodFind extends PluginService {
	private Interface _defn;

	public SvcMethodFind() throws Throwable {

		_defn = new Interface();
		_defn.add(new Interface.Element("for", new EnumType(new String[] { "subject" }),
				"The type of entity to which this method applies. If not specified, returns all methods.", 0, 1));
		_defn.add(new Interface.Element("study", new DictionaryEnumType("pssd.study.types"), "The type of the study.",
				0, 1));
		_defn.add(new Interface.Element("name", StringType.DEFAULT, "Name of Method looking for", 0, 1));
		_defn.add(new Interface.Element("text", StringType.DEFAULT,
				"Arbitrary literal (exact match) string for query of indexed meta-data text strings.", 0, 1));
		_defn.add(new Interface.Element("method", CiteableIdType.DEFAULT, "Method utilized.", 0, 1));
		_defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE, "Cursor position. Defaults to 1", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE, "Cursor size. Defaults to 100", 0, 1));
		_defn.add(new Interface.Element("action", new EnumType(
				new String[] { "list", "summarize", "describe", "expand" }),
				"The type of display. Defaults to 'list'.", 0, 1));
		_defn.add(new Interface.Element("asset-type", new EnumType(new String[] { "primary", "replica", "all" }),
				"Specify type of asset to find. Defaults to all.", 0, 1));
		_defn.add(new Interface.Element(
				"pdist",
				IntegerType.DEFAULT,
				"Specifies the peer distance for a distributed query. Defaults to all servers in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",
				0, 1));
	}

	public String name() {

		return "om.pssd.method.find";
	}

	public String description() {

		return "Returns methods that match the given search criteria.  The query is distributed in a federation.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String study = args.value("study");
		String text = args.value("text");
		String method = args.value("method");
		String target = args.value("for");
		String pdist = args.stringValue("pdist");
		String assetType = args.stringValue("asset-type", "all");
		String name = args.value("name");

		// Setup query
		StringBuilder query = new StringBuilder();
		if (target != null) {
			if (target.equalsIgnoreCase("subject")) {
				query.append("(daris:pssd-method-subject has value or daris:pssd-method-rsubject has value)");
			}
		}

		if (study != null) {
			if (query.length()>0) {
				query.append(" and ");
			}

			query.append("xpath(daris:pssd-method/step/study/type)='" + study + "'");
		}

		if (method != null) {
			String sq = "(xpath(daris:pssd-method/step/method/id)='" + method + "'";
			sq += " or xpath(daris:pssd-method/step/branch/method/id)='" + method + "')";

			if (query.length()<=0) {
				query.append(sq);
			} else {
				query.append(" and " + sq);
			}
		}

		if (text != null) {
			String sq = "mtext contains literal ('" + text + "')";
			if (query.length()<=0) {
				query.append(sq);
			} else {
				query.append(" and " + sq);
			}
		}

		if (name != null) {
			String sq = "xpath(daris:pssd-object/name)='" + name + "'";
			if (query.length()<=0) {
				query.append(sq);
			} else {
				query.append(" and " + sq);
			}
		}

		// Specify Method object
		query.append(" and xpath(daris:pssd-object/type)='method'");

		// Primary/replica/both (default)
		DistributedQuery.appendResultAssetTypePredicate(query, DistributedQuery.ResultAssetType.instantiate(assetType));

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", query.toString());
		dm.add("action", "get-meta");
		dm.add("idx", args.longValue("idx", 1));
		dm.add("size", args.intValue("size", 100));
		if (pdist != null)
			dm.add("pdist", pdist);

		// Do the query; distributed in federation
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		List<XmlDoc.Element> aes = r.elements("asset");
		if (aes == null) {
			return;
		}
		String action = args.stringValue("action", "list");
		if (action.equalsIgnoreCase("list")) {
			listMethods(w, aes);
		} else if (action.equalsIgnoreCase("summarize")) {
			summarizeMethods(w, aes);
		} else if (action.equalsIgnoreCase("describe")) {
			describeMethods(executor(), w, aes, false);
		} else if (action.equalsIgnoreCase("expand")) {
			describeMethods(executor(), w, aes, true);
		}
	}

	public static void listMethods(XmlWriter w, List<XmlDoc.Element> aes) throws Throwable {

		if (aes == null) {
			return;
		}
		for (XmlDoc.Element ae : aes) {
			String proute = ae.value("@proute");
			String id = ae.value("cid");
			w.add("id", new String[] { "proute", proute }, id);
		}
	}

	public static void summarizeMethods(XmlWriter w, List<XmlDoc.Element> aes) throws Throwable {

		if (aes != null) {
			for (XmlDoc.Element ae : aes) {
				w.push("method", new String[] { "proute", ae.value("@proute"), "id", ae.value("cid") });

				String name = ae.value("meta/daris:pssd-object/name");
				String desc = ae.value("meta/daris:pssd-object/description");

				w.add("name", name);

				if (desc != null) {
					w.add("description", desc);
				}
				w.pop();
			}
		}
	}

	public static void describeMethods(ServiceExecutor executor, XmlWriter w, List<XmlDoc.Element> aes, boolean expand)
			throws Throwable {

		if (aes != null) {
			for (XmlDoc.Element ae : aes) {
				Method.describe(executor, w, ae, expand);
			}
		}
	}

	public static void describeMethods(ServiceExecutor executor, XmlWriter w, XmlDoc.Element r, boolean expand)
			throws Throwable {

		describeMethods(executor, w, r.elements("asset"), expand);
	}

}
