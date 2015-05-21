package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.user.Self;
import nig.mf.pssd.CiteableIdUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcUserCanDestroy extends PluginService {

	public static final String SERVICE_NAME = "om.pssd.user.can.destroy";
	public static final String SERVICE_DESCRIPTION = "Returns whether the user, based on its roles, is allowed to destroy the specific PSSD object on the local server.";

	private Interface _defn;

	public SvcUserCanDestroy() {
		_defn = new Interface();
		_defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT,
				"The citeable identifier of the local object of interest.", 1,
				1));
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
	public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w)
			throws Throwable {
		String cid = args.value("cid");
		w.add("can", new String[] { "action", "destroy" },
				canDestroy(executor(), cid));
	}

	@Override
	public String name() {
		return SERVICE_NAME;
	}

	public static boolean canDestroy(ServiceExecutor executor, String cid)
			throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		String assetId = executor.execute("asset.get", dm.root()).value(
				"asset/@id");

		dm = new XmlDocMaker("args");
		dm.add("id", assetId);
		dm.add("metadata", "read-write");
		dm.add("content", "read-write");
		XmlDoc.Element r = executor.execute("asset.acl.have", dm.root());
		boolean canModifyMetadata = r.booleanValue("asset/metadata");
		boolean canModifyContent = r.booleanValue("asset/content");
		// first, the user need to have MODIFY acccess
		if (!canModifyMetadata || !canModifyContent) {
			return false;
		}
		PSSDObject.Type objType = PSSDObject.Type.fromId(cid);
		String projectId = CiteableIdUtil.getProjectId(cid);
		switch (objType) {
		case project:
			// You can only destroy a project if you are an system-administrator
			// and you have project.admin role.
			return Self.isProjectAdmin(executor, projectId)
					&& Self.isSystemAdministrator(executor);
		case subject:
			// project.admin and subject.admin can destroy subjects.
			return Self.isProjectAdmin(executor, projectId)
					|| Self.isSubjectAdmin(executor, projectId);
		case ex_method:
		case study:
		case dataset:
			// project.admin and subject.admin can destroy ex-methods, studies
			// and datasets; also member if he is the creator or modifier
			return Self.isProjectAdmin(executor, projectId)
					|| Self.isSubjectAdmin(executor, projectId)
					|| (Self.isMember(executor, projectId) && Self.isCreatorOrModifierOf(executor, cid));
		default:
			return false;
		}
	}

}
