package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.pssd.plugin.util.CiteableIdUtil;
import nig.util.ObjectUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectCidChange extends PluginService {

	public static final String SERVICE_NAME = "om.pssd.object.cid.change";

	private Interface _defn;

	public SvcObjectCidChange() {
		_defn = new Interface();
		_defn.add(new Interface.Element("from", CiteableIdType.DEFAULT,
				"The citeable id of the object", 1, 1));
		_defn.add(new Interface.Element("to", CiteableIdType.DEFAULT,
				"The citeable id to change to.", 1, 1));
	}

	@Override
	public Access access() {
		return ACCESS_ADMINISTER;
	}

	@Override
	public Interface definition() {
		return _defn;
	}

	@Override
	public String description() {
		return "Change the citeable id of the given object. The destined citeable id must be within the same parent(container).\nCaution: This service does not go through all the metadata to update the values refers to the old citeable ids. Therefore, it may causes problems. Use this only if you know what you are doing.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs,
			XmlWriter w) throws Throwable {

		String from = args.value("from");
		// check if src object exists
		if (!assetExists(executor(), from)) {
			throw new IllegalArgumentException("Asset " + from
					+ " does not exist.");
		}
		String to = args.value("to");
		// check if dst object exists
		if (assetExists(executor(), to)) {
			throw new IllegalArgumentException("Asset " + to
					+ " already exists.");
		}
		// check if dst object has descendants
		if (executor().execute(
				"asset.query",
				"<args><where>cid starts with '" + to
						+ "'</where><action>count</action></args>", null, null)
				.intValue("value", 0) > 0) {

		}
		// check if src and dst have the same parent
		if (!ObjectUtil.equals(CiteableIdUtil.getParentId(from),
				CiteableIdUtil.getParentId(to))) {
			throw new IllegalArgumentException(
					"The 'from' and 'to' citeable ids must be within the same parent/container.");
		}
		// check if the cid is for subject, ex-method, study or dataset.
		int depth = CiteableIdUtil.getIdDepth(from);
		if (depth < CiteableIdUtil.PROJECT_ID_DEPTH
				|| depth > CiteableIdUtil.DATASET_ID_DEPTH) {
			throw new IllegalArgumentException(
					"The citeable id must be an id of subject/ex-method/study/dataset.");
		}
		// change cid of a project is not allowed.
		if (CiteableIdUtil.isProjectId(from)) {
			throw new IllegalArgumentException(
					"You can not change the citeable id of a project.");
		}
		// get the object type
		PSSDObject.Type type = PSSDObject.typeOf(from);
		switch (type) {
		case subject:
			changeSubjectCid(executor(), from, to);
			break;
		case ex_method:
			changeExMethodCid(executor(), from, to);
			break;
		case study:
			changeStudyCid(executor(), from, to);
			break;
		case dataset:
			changeDataSetCid(executor(), from, to);
			break;
		default:
			break;
		}
	}

	static void changeSubjectCid(ServiceExecutor executor, String from,
			String to) throws Throwable {
		XmlDoc.Element ae = executor.execute("asset.get",
				"<args><cid>" + from + "</cid></args>", null, null).element(
				"asset");
		String id = ae.value("@id");
		/*
		 * find children (ex-methods)
		 */
		Collection<String> children = executor.execute(
				"asset.query",
				"<args><where>cid in '" + from
						+ "'</where><action>get-cid</action></args>", null,
				null).values("cid");
		/*
		 * set cid
		 */
		executor.execute("asset.cid.set", "<args><id>" + id + "</id><cid>" + to
				+ "</cid></args>", null, null);

		/*
		 * update metadata
		 */
		// None

		/*
		 * recursively update cids of the children (studies)
		 */
		if (children != null) {
			for (String exMethod : children) {
				String exMethodTo = to + "."
						+ CiteableIdUtil.getLastSection(exMethod);
				changeExMethodCid(executor, exMethod, exMethodTo);
			}
		}
	}

	static void changeExMethodCid(ServiceExecutor executor, String from,
			String to) throws Throwable {
		XmlDoc.Element ae = executor.execute("asset.get",
				"<args><cid>" + from + "</cid></args>", null, null).element(
				"asset");
		String id = ae.value("@id");
		/*
		 * find children (studies)
		 */
		Collection<String> children = executor.execute(
				"asset.query",
				"<args><where>cid in '" + from
						+ "'</where><action>get-cid</action></args>", null,
				null).values("cid");
		/*
		 * set cid
		 */
		executor.execute("asset.cid.set", "<args><id>" + id + "</id><cid>" + to
				+ "</cid></args>", null, null);

		/*
		 * update metadata
		 */
		// None

		/*
		 * recursively update cids of the children (studies)
		 */
		if (children != null) {
			for (String study : children) {
				String studyTo = to + "."
						+ CiteableIdUtil.getLastSection(study);
				changeStudyCid(executor, study, studyTo);
			}
		}

	}

	static void changeStudyCid(ServiceExecutor executor, String from, String to)
			throws Throwable {
		XmlDoc.Element ae = executor.execute("asset.get",
				"<args><cid>" + from + "</cid></args>", null, null).element(
				"asset");
		String id = ae.value("@id");
		/*
		 * find children (datasets)
		 */
		Collection<String> children = executor.execute(
				"asset.query",
				"<args><where>cid in '" + from
						+ "'</where><action>get-cid</action></args>", null,
				null).values("cid");
		/*
		 * set cid
		 */
		executor.execute("asset.cid.set", "<args><id>" + id + "</id><cid>" + to
				+ "</cid></args>", null, null);
		/*
		 * update metadata
		 */
		// daris:pssd-study/method
		String oldExMethodCid = ae.value("meta/daris:pssd-study/method");
		String step = ae.value("meta/daris:pssd-study/method/@step");
		String newExMethodCid = CiteableIdUtil.getExMethodId(to);
		if (oldExMethodCid != null && !oldExMethodCid.equals(newExMethodCid)) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", id);
			dm.push("meta", new String[] { "action", "merge" });
			dm.push("daris:pssd-study");
			dm.add("method", new String[] { "step", step }, newExMethodCid);
			dm.pop();
			dm.pop();
			executor.execute("asset.set", dm.root());
		}
		/*
		 * recursively update cids of the children (datasets)
		 */
		if (children != null) {
			for (String dataset : children) {
				String datasetTo = to + "."
						+ CiteableIdUtil.getLastSection(dataset);
				changeDataSetCid(executor, dataset, datasetTo);
			}
		}

	}

	static void changeDataSetCid(ServiceExecutor executor, String from,
			String to) throws Throwable {
		XmlDoc.Element ae = executor.execute("asset.get",
				"<args><cid>" + from + "</cid></args>", null, null).element(
				"asset");
		String id = ae.value("@id");
		// dataset should not have children
		if (executor.execute(
				"asset.query",
				"<args><where>cid in '" + from
						+ "'</where><action>count</action></args>", null, null)
				.intValue("value", 0) > 0) {
			throw new Exception("Unexpected children found for dataset: "
					+ from);
		}
		/*
		 * set cid
		 */
		executor.execute("asset.cid.set", "<args><id>" + id + "</id><cid>" + to
				+ "</cid></args>", null, null);
		/*
		 * update cid in metadata
		 */
		// daris:pssd-derivation/method
		String oldExMethodCid = ae.value("meta/daris:pssd-derivation/method");
		String step = ae.value("meta/daris:pssd-derivation/method/@step");
		String newExMethodCid = CiteableIdUtil.getExMethodId(to);
		if (oldExMethodCid != null && !oldExMethodCid.equals(newExMethodCid)) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", id);
			dm.push("meta", new String[] { "action", "merge" });
			dm.push("daris:pssd-derivation");
			dm.add("method", new String[] { "step", step }, newExMethodCid);
			dm.pop();
			dm.pop();
			executor.execute("asset.set", dm.root());
		}

	}

	private static boolean assetExists(ServiceExecutor executor, String cid)
			throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		return executor.execute("asset.exists", dm.root()).booleanValue(
				"exists");
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

}
