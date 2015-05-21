package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.LongType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcDataSetProcessedInputList extends PluginService {

	public static final String SERVICE_NAME = "om.pssd.dataset.processed.input.list";
	public static final String SERVICE_DESCRIPTION = "List the input data sets of the given (set of) processed data set. (Note: it looks at daris:pssd-derivation/input.)";
	public static final long DEFAULT_PAGE_SIZE = 100;

	private Interface _defn;

	public SvcDataSetProcessedInputList() {
		_defn = new Interface();
		_defn.add(new Interface.Element(
				"cid",
				CiteableIdType.DEFAULT,
				"The citeable id of the project/subject/ex-method/study that contains processed data sets, or the citeable id of the specific processed data set.",
				1, 1));
		_defn.add(new Interface.Element(
				"idx",
				LongType.POSITIVE_ONE,
				"Absolute cursor position. If used, the cursor will be positioned starting at 'idx'",
				0, 1));
		_defn.add(new Interface.Element(
				"size",
				LongType.POSITIVE_ONE,
				"Number of results to return. Defaults to 100 for actions returning results.",
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
		String cid = args.value("cid");
		long idx = args.longValue("idx", 1);
		long size = args.longValue("size", DEFAULT_PAGE_SIZE);
		StringBuilder sb = new StringBuilder();
		sb.append("(cid starts with '").append(cid).append("' or cid='")
				.append(cid).append("')");
		sb.append(" and model='om.pssd.dataset'");
		sb.append(" and xpath(daris:pssd-derivation/processed)=true");
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", sb.toString());
		dm.add("action", "get-values");
		dm.add("xpath", new String[] { "ename", "cid" }, "cid");
		dm.add("xpath", new String[] { "ename", "input" },
				"meta/daris:pssd-derivation/input");
		if (idx > 1) {
			dm.add("idx", idx);
		}
		if (size != DEFAULT_PAGE_SIZE) {
			dm.add("size", size);
		}
		XmlDoc.Element re = executor().execute("asset.query", dm.root());
		List<XmlDoc.Element> aes = re.elements("asset");
		if (aes != null) {
			for (XmlDoc.Element ae : aes) {
				w.push("dataset", new String[] { "cid", ae.value("cid"),
						"processed", "true" });
				Collection<String> inputs = ae.values("input");
				if (inputs != null) {
					for (String input : inputs) {
						w.add("input", input);
					}
				}
				w.pop();
			}
		}
		XmlDoc.Element ce = re.element("cursor");
		if (ce != null) {
			w.add(ce, true);
		}
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

}
