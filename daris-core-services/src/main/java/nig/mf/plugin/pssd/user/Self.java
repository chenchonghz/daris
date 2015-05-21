package nig.mf.plugin.pssd.user;

import nig.mf.pssd.ProjectRole;
import nig.mf.pssd.Role;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDocMaker;

public class Self {

	public static boolean hasRole(ServiceExecutor executor, String role)
			throws Throwable {
		return executor.execute("actor.self.have",
				"<args><role type=\"role\">" + role + "</role></args>", null,
				null).booleanValue("role");
	}

	public static boolean hasRole(String role) throws Throwable {
		return hasRole(PluginThread.serviceExecutor(), role);
	}

	public static boolean isProjectAdmin(ServiceExecutor executor,
			String projectCid) throws Throwable {
		return hasRole(executor,
				ProjectRole.projectAdministratorRoleNameOf(projectCid));
	}

	public static boolean isProjectAdmin(String projectCid) throws Throwable {
		return hasRole(ProjectRole.projectAdministratorRoleNameOf(projectCid));
	}

	public static boolean isSubjectAdmin(ServiceExecutor executor,
			String projectCid) throws Throwable {
		return hasRole(executor,
				ProjectRole.subjectAdministratorRoleNameOf(projectCid));
	}

	public static boolean isSubjectAdmin(String projectCid) throws Throwable {
		return hasRole(ProjectRole.subjectAdministratorRoleNameOf(projectCid));
	}

	public static boolean isMember(ServiceExecutor executor, String projectCid)
			throws Throwable {
		return hasRole(executor, ProjectRole.memberRoleNameOf(projectCid));
	}

	public static boolean isMember(String projectCid) throws Throwable {
		return hasRole(ProjectRole.memberRoleNameOf(projectCid));
	}

	public static boolean isGuest(ServiceExecutor executor, String projectCid)
			throws Throwable {
		return hasRole(executor, ProjectRole.guestRoleNameOf(projectCid));
	}

	public static boolean isGuest(String projectCid) throws Throwable {
		return hasRole(ProjectRole.guestRoleNameOf(projectCid));
	}

	public static boolean isSystemAdministrator(ServiceExecutor executor)
			throws Throwable {
		return hasRole(executor, SystemRoles.SYSTEM_ADMINISTRATOR);
	}

	public static boolean isSystemAdministrator() throws Throwable {
		return hasRole(SystemRoles.SYSTEM_ADMINISTRATOR);
	}

	public static boolean isObjectAdmin() throws Throwable {
		return hasRole(Role.objectAdminRoleName());
	}

	public static boolean isPowerUser() throws Throwable {
		return hasRole(Role.powerModelUserRoleName());
	}

	/**
	 * Determines if the caller holds one of : system-administrator,
	 * pssd.object.admin, pssd.project.admin.<pid>,
	 * pssd.project.subject.admin.<pid> for the project that owns the given CID.
	 * If not, an exception is thrown.
	 * 
	 * 
	 * @param cid
	 *            CID of object; must be at least of depth 2. Can be for any
	 *            object type.
	 * 
	 * @throws Throwable
	 */
	public static void isAdmin(String cid, Boolean includePowerUser)
			throws Throwable {
		if (isSystemAdministrator())
			return;
		if (isObjectAdmin())
			return;
		if (includePowerUser && isPowerUser())
			return;
		//
		String pid = nig.mf.pssd.CiteableIdUtil.getProjectId(cid);
		if (isProjectAdmin(pid))
			return;
		if (isSubjectAdmin(pid))
			return;
		//
		throw new Exception(
				"Caller does not hold system-administrator,pssd.object.admin, project-admin or subject-admin for this project ("
						+ pid + ")");
	}

	public static boolean isProjectCreator(ServiceExecutor executor)
			throws Throwable {
		return hasRole(executor, Role.projectCreatorRoleName());
	}

	public static boolean isCreatorOf(ServiceExecutor executor, String cid)
			throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("action", "count");
		dm.add("where", "created by me and cid='" + cid + "'");
		return executor.execute("asset.query", dm.root()).intValue("value", 0) > 0;
	}

	public static boolean isModifierOf(ServiceExecutor executor, String cid)
			throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("action", "count");
		dm.add("where", "modified by me and cid='" + cid + "'");
		return executor.execute("asset.query", dm.root()).intValue("value", 0) > 0;
	}

	public static boolean isCreatorOrModifierOf(ServiceExecutor executor, String cid)
			throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("action", "count");
		dm.add("where", "(modified by me or created by me) and cid='" + cid + "'");
		return executor.execute("asset.query", dm.root()).intValue("value", 0) > 0;
	}

}
