package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.PSSDObject;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcUserCanModify extends PluginService {

	public static final String SERVICE_NAME = "om.pssd.user.can.modify";
	public static final String SERVICE_DESCRIPTION = "Returns whether the user, based on its roles, is allowed to edit specific PSSD object on the local server.";

	private Interface _defn;

	public SvcUserCanModify() {
		_defn = new Interface();
		_defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
				"The citeable identifier of the local object of interest.", 1,
				1));
	}

	public String name() {
		return SERVICE_NAME;
	}

	public String description() {
		return SERVICE_DESCRIPTION;
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public int executeMode() {
		return EXECUTE_DISTRIBUTED_ALL;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String cid = args.value("cid");
		w.add("can", new String[] { "action", "modify" },
				canModify(executor(), cid));

	}

	/**
	 * Check if the current user can modify the given object.
	 * 
	 * @param executor
	 *            The ServiceExecutor object.
	 * @param cid
	 *            The citeable identifier of the object.
	 * @return
	 * @throws Throwable
	 */
	public static boolean canModify(ServiceExecutor executor, String cid)
			throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);	
		String assetId = executor.execute("asset.get", dm.root()).value(
				"asset/@id");

		dm = new XmlDocMaker("args");
		dm.add("id", assetId);
		dm.add("metadata", "read-write");
		dm.add("content", "read-write");
		boolean canModifyMetadata = executor.execute("asset.acl.have",
				dm.root()).booleanValue("asset/metadata");
		boolean canModifyContent = executor
				.execute("asset.acl.have", dm.root()).booleanValue(
						"asset/metadata");
		PSSDObject.Type type = PSSDObject.Type.fromId(cid);
		switch (type) {
		case ex_method:
			return false;
		default:
			return canModifyMetadata && canModifyContent;
		}
	}

}
