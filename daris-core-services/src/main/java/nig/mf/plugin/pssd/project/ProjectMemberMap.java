package nig.mf.plugin.pssd.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nig.mf.plugin.pssd.user.UserCredential;
import nig.mf.pssd.ProjectRole;
import nig.mf.pssd.Role;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class ProjectMemberMap {

	public static class Members {

		private String _projectId;

		private Map<UserCredential, UserMember> _userMembers;
		private Map<String, RoleMember> _roleMembers;

		private Members(String projectId) {

			_projectId = projectId;
			_userMembers = new HashMap<UserCredential, UserMember>();
			_roleMembers = new HashMap<String, RoleMember>();
		}

		public String projectId() {

			return _projectId;
		}

		public void addUserMember(UserMember um) {

			UserMember m = _userMembers.get(um.userCredential());
			if (m != null) {
				if (um.compareTo(m) < 0) {
					_userMembers.put(um.userCredential(), um);
				}
			} else {
				_userMembers.put(um.userCredential(), um);
			}
		}

		public void addRoleMember(RoleMember rm) {

			RoleMember m = _roleMembers.get(rm.member());
			if (m != null) {
				if (rm.compareTo(m) < 0) {
					_roleMembers.put(rm.member(), rm);
				}
			} else {
				_roleMembers.put(rm.member(), rm);
			}
		}

		public void describe(XmlWriter w) throws Throwable {

			if (!_userMembers.isEmpty()) {
				List<UserMember> ums = new ArrayList<UserMember>(_userMembers.values());
				Collections.sort(ums);
				for (UserMember um : ums) {
					um.describe(w);
				}
			}
			if (!_roleMembers.isEmpty()) {
				List<RoleMember> rms = new ArrayList<RoleMember>(_roleMembers.values());
				Collections.sort(rms);
				for (RoleMember rm : rms) {
					rm.describe(w);
				}
			}
		}

	}

	private Map<String, Members> _members;

	private ProjectMemberMap() {

		_members = new HashMap<String, Members>();
	}

	public Collection<String> projects() {

		return _members.keySet();
	}

	public Members members(String projectId) {

		return _members.get(projectId);
	}

	public void describeMembers(XmlWriter w, String projectId) throws Throwable {

		Members ms = members(projectId);
		if (ms != null) {
			ms.describe(w);
		}
	}

	public void addUserMember(String projectId, UserMember um) {

		Members ms = _members.get(projectId);
		if (ms == null) {
			ms = new Members(projectId);
		}
		ms.addUserMember(um);
		_members.put(projectId, ms);
	}

	public void addRoleMember(String projectId, RoleMember rm) {

		Members ms = _members.get(projectId);
		if (ms == null) {
			ms = new Members(projectId);
		}
		ms.addRoleMember(rm);
		_members.put(projectId, ms);
	}
	
	public static ProjectMemberMap load(ServiceExecutor executor, ServerRoute sroute, String projectId) throws Throwable {

		ProjectMemberMap map = new ProjectMemberMap();
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("permissions", true);
		dm.add("role", new String[] { "type", "role" }, Role.modelUserRoleName());
		XmlDoc.Element r = executor.execute(sroute, "user.describe", dm.root());

		List<XmlDoc.Element> ues = r.elements("user");
		if (ues != null) {
			for (XmlDoc.Element ue : ues) {
				UserCredential user = new UserCredential(ue.value("@authority"), ue.value("@protocol"),
						ue.value("@domain"), ue.value("@user"));
				String id = ue.value("@id");
				Collection<String> roles = ue.values("role");
				if (roles != null) {
					for (String role : roles) {
						if (role.equals(ProjectRole.roleNameOf(ProjectRole.Type.project_administrator, projectId))) {
							UserMember um = new UserMember(id, user, projectId, ProjectRole.Type.project_administrator,
									findDataUse(roles, projectId));
							map.addUserMember(projectId, um);
						} else if (role.equals(ProjectRole.roleNameOf(ProjectRole.Type.subject_administrator, projectId))) {
							UserMember um = new UserMember(id, user, projectId, ProjectRole.Type.subject_administrator,
									findDataUse(roles, projectId));
							map.addUserMember(projectId, um);
						} else if (role.equals(ProjectRole.roleNameOf(ProjectRole.Type.member, projectId))) {
							UserMember um = new UserMember(id, user, projectId, ProjectRole.Type.member, findDataUse(
									roles, projectId));
							map.addUserMember(projectId, um);
						} else if (role.equals(ProjectRole.roleNameOf(ProjectRole.Type.guest, projectId))) {
							UserMember um = new UserMember(id, user, projectId, ProjectRole.Type.guest, findDataUse(
									roles, projectId));
							map.addUserMember(projectId, um);
						}
					}
				}
			}
		}
		addRoleMembers(executor, map, projectId);
		return map;
	}

	public static ProjectMemberMap load(ServiceExecutor executor, ServerRoute sroute) throws Throwable {

		ProjectMemberMap map = new ProjectMemberMap();
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("permissions", true);
		dm.add("role", new String[] { "type", "role" }, Role.modelUserRoleName());
		XmlDoc.Element r = executor.execute(sroute, "user.describe", dm.root());

		List<XmlDoc.Element> ues = r.elements("user");
		if (ues != null) {
			for (XmlDoc.Element ue : ues) {
				UserCredential user = new UserCredential(ue.value("@authority"), ue.value("@protocol"),
						ue.value("@domain"), ue.value("@user"));
				String id = ue.value("@id");
				Collection<String> roles = ue.values("role");
				if (roles != null) {
					for (String role : roles) {
						if (role.startsWith(ProjectRole.PROJECT_ADMINISTRATOR_ROLE_PREFIX)) {
							String projectId = role
									.substring(ProjectRole.PROJECT_ADMINISTRATOR_ROLE_PREFIX.length() + 1);
							UserMember um = new UserMember(id, user, projectId, ProjectRole.Type.project_administrator,
									findDataUse(roles, projectId));
							map.addUserMember(projectId, um);
						} else if (role.startsWith(ProjectRole.SUBJECT_ADMINISTRATOR_ROLE_PREFIX)) {
							String projectId = role
									.substring(ProjectRole.SUBJECT_ADMINISTRATOR_ROLE_PREFIX.length() + 1);
							UserMember um = new UserMember(id, user, projectId, ProjectRole.Type.subject_administrator,
									findDataUse(roles, projectId));
							map.addUserMember(projectId, um);
						} else if (role.startsWith(ProjectRole.MEMBER_ROLE_PREFIX)) {
							String projectId = role.substring(ProjectRole.MEMBER_ROLE_PREFIX.length() + 1);
							UserMember um = new UserMember(id, user, projectId, ProjectRole.Type.member, findDataUse(
									roles, projectId));
							map.addUserMember(projectId, um);
						} else if (role.startsWith(ProjectRole.GUEST_ROLE_PREFIX)) {
							String projectId = role.substring(ProjectRole.GUEST_ROLE_PREFIX.length() + 1);
							UserMember um = new UserMember(id, user, projectId, ProjectRole.Type.guest, findDataUse(
									roles, projectId));
							map.addUserMember(projectId, um);
						}
					}
				}
			}
		}
		Collection<String> projects = map.projects();
		if (projects != null) {
			for (String projectId : map.projects()) {
				addRoleMembers(executor, map, projectId);
			}
		}
		return map;
	}

	private static DataUse findDataUse(Collection<String> roles, String projectId) {

		for (String role : roles) {
			if (role.endsWith(projectId) && role.startsWith(DataUseRole.ROLE_PREFIX)) {
				DataUseRole dur = null;
				try {
					dur = DataUseRole.parse(role);
				} catch (Throwable e) {
				}
				if (dur != null) {
					return dur.dataUse();
				}
			}
		}
		return null;
	}

	private static void addRoleMembers(ServiceExecutor executor, ProjectMemberMap map, String projectId)
			throws Throwable {

		addRoleMembers(executor, map, projectId, ProjectRole.Type.project_administrator);
		addRoleMembers(executor, map, projectId, ProjectRole.Type.subject_administrator);
		addRoleMembers(executor, map, projectId, ProjectRole.Type.member);
		addRoleMembers(executor, map, projectId, ProjectRole.Type.guest);

	}

	private static void addRoleMembers(ServiceExecutor executor, ProjectMemberMap map, String projectId,
			ProjectRole.Type type) throws Throwable {

		String role = ProjectRole.roleNameOf(type, projectId);
		Collection<String> rms = rolesHaveRole(executor, role);
		if (rms != null) {
			for (String rm : rms) {
				if (rm.startsWith("daris:pssd.project")) {
					continue;
				}
				XmlDocMaker dm = new XmlDocMaker("args");
				dm.add("type", "role");
				dm.add("name", rm);
				XmlDoc.Element r = executor.execute("actor.describe", dm.root());
				String id = r.value("actor/@id");
				Collection<String> roles = r.values("actor/role[@type='role']");
				DataUse dataUse = null;
				if (roles != null) {
					dataUse = findDataUse(roles, projectId);
				}
				map.addRoleMember(projectId, new RoleMember(id, rm, projectId, type, dataUse));
			}
		}
	}

	private static Collection<String> rolesHaveRole(ServiceExecutor executor, String role) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", "role");
		dm.add("role", new String[] { "type", "role" }, role);
		XmlDoc.Element r = executor.execute("actors.granted", dm.root());
		return r.values("actor/@name");
	}
}
