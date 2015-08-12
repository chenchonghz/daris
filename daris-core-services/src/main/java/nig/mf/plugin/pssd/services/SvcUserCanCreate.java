package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.user.Self;
import nig.mf.pssd.CiteableIdUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcUserCanCreate extends PluginService {

	public static final String SERVICE_NAME = "om.pssd.user.can.create";
	public static final String SERVICE_DESCRIPTION = "Returns whether the current user, based on its roles, is allowed to create specific PSSD objects on the local server.";
	public static final PSSDObject.Type[] SUPPORTED_TYPES = {
			PSSDObject.Type.project, PSSDObject.Type.subject,
			PSSDObject.Type.ex_method, PSSDObject.Type.study,
			PSSDObject.Type.dataset, PSSDObject.Type.data_object };

	private Interface _defn;

	public SvcUserCanCreate() {
		_defn = new Interface();
		_defn.add(new Interface.Element(
				"pid",
				CiteableIdType.DEFAULT,
				"The citeable identifier of the direct parent object when creating a 'subject', 'ex-method', 'study' or 'dataset'. Without this argument or supply 0 as the value when creating a 'project'.",
				0, 1));
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

	private static PSSDObject.Type getTypeFromParentId(String parentId)
			throws Throwable {
		if (parentId == null || "0".equals(parentId)) {
			return PSSDObject.Type.project;
		}
		if (CiteableIdUtil.isProjectId(parentId)) {
			return PSSDObject.Type.subject;
		}
		if (CiteableIdUtil.isSubjectId(parentId)) {
			return PSSDObject.Type.ex_method;
		}
		if (CiteableIdUtil.isExMethodId(parentId)) {
			return PSSDObject.Type.study;
		}
		if (CiteableIdUtil.isStudyId(parentId)) {
			return PSSDObject.Type.dataset;
		}
		if (CiteableIdUtil.isDataSetId(parentId)) {
			return PSSDObject.Type.data_object;
		}
		throw new IllegalArgumentException("Invalid parent id: " + parentId
				+ ". Could not identify its type.");
	}

	private static boolean isTypeSupported(PSSDObject.Type type) {
		for (PSSDObject.Type t : SUPPORTED_TYPES) {
			if (t == type) {
				return true;
			}
		}
		return false;
	}

	public static boolean canCreate(ServiceExecutor executor, String parentId)
			throws Throwable {
		String projectId = CiteableIdUtil.getProjectId(parentId);
		PSSDObject.Type objType = getTypeFromParentId(parentId);
		if (!isTypeSupported(objType)) {
			throw new Exception("Cannot create " + objType
					+ ". It is not supported.");
		}

		// We don't need the proute/actual project because roles are just
		// specified according to the Project CID. If we ever change to going
		// back to the parent Project then we would need proute as well.
		switch (objType) {
		case project:
			// creating project requires 'project.creator' role
			return Self.isProjectCreator(executor);
		case subject:
			// creating subject requires 'subject.admin' role
			return Self.isSubjectAdmin(executor, projectId);
		default:
			// creating ex-method, study or dataset requires 'project.member'
			// role
			return Self.isMember(executor, projectId);
		}
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String parentId = args.stringValue("pid");
		w.add("can", new String[] { "action", "create" },
				canCreate(executor(), parentId));
	}

}
