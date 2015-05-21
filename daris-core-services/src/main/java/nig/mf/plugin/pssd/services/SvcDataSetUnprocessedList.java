package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDataSetUnprocessedList extends PluginService {

	public static final String SERVICE_NAME = "om.pssd.dataset.unprocessed.list";
	public static final String SERVICE_DESCRIPTION = "List the unproessed data sets in the parent project/subject/ex-method/study.";

	private Interface _defn;

	public SvcDataSetUnprocessedList() {
		_defn = new Interface();
		_defn.add(new Interface.Element(
				"pid",
				CiteableIdType.DEFAULT,
				"The citeable id of the parent project/subject/ex-method/study.",
				1, 1));
		_defn.add(new Interface.Element(
				"where",
				StringType.DEFAULT,
				"Selection query to identify the unprocessed data sets, if any.",
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
		return SERVICE_DESCRIPTION;
	}

	@Override
	public void execute(Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
		String pid = args.value("pid");
		String where = args.value("where");
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", "(cid starts with '" + pid + "' or cid='" + pid
				+ "') and model='om.pssd.study'");
		dm.add("size", "infinity");
		dm.add("action", "get-cid");
		Collection<String> studyCids = executor().execute("asset.query",
				dm.root()).values("cid");
		if (studyCids != null) {
			for (String studyCid : studyCids) {
				Set<String> unprocessed = listUnprocessedDataSets(executor(),
						studyCid, where);
				if (unprocessed != null) {
					for (String dataset : unprocessed) {
						w.add("dataset",
								new String[] { "unprocessed", "true" }, dataset);
					}
				}
			}
		}
	}

	private static Set<String> listUnprocessedDataSets(
			ServiceExecutor executor, String studyCid, String where)
			throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		StringBuilder sb = new StringBuilder();
		sb.append("cid in '"
				+ studyCid
				+ "' and model='om.pssd.dataset' and (not (xpath(daris:pssd-derivation/processed)=true))");
		if (where != null) {
			sb.append(" and (" + where + ")");
		}
		dm.add("where", sb.toString());
		dm.add("size", "infinity");
		dm.add("action", "get-cid");
		Collection<String> datasets = executor
				.execute("asset.query", dm.root()).values("cid");
		if (datasets == null || datasets.isEmpty()) {
			return null;
		}

		Set<String> unprocessed = new HashSet<String>();
		unprocessed.addAll(datasets);

		dm = new XmlDocMaker("args");
		dm.add("cid", studyCid);
		dm.add("size", "infinity");
		Collection<String> inputs = executor.execute(
				SvcDataSetProcessedInputList.SERVICE_NAME, dm.root()).values(
				"dataset/input");
		if (inputs != null && !inputs.isEmpty()) {
			Set<String> inputSet = new HashSet<String>();
			inputSet.addAll(inputs);
			unprocessed.removeAll(inputSet);
		}
		return unprocessed;
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

}
