package nig.mf.plugin.pssd.project;

import nig.mf.pssd.ProjectRole;
import arc.xml.XmlWriter;

public abstract class ProjectMember implements Comparable<ProjectMember> {

	private String _id;
	private ProjectRole _role;
	private DataUse _dataUse;

	protected ProjectMember(String id, ProjectRole role, DataUse dataUse) {

		_id = id;
		_role = role;
		_dataUse = dataUse;
	}

	protected ProjectMember(String id, String projectId, ProjectRole.Type type, DataUse dataUse) {

		this(id, new ProjectRole(type, projectId), dataUse);
	}

	public DataUse dataUse() {

		return _dataUse;
	}

	public void setDataUse(DataUse dataUse) {

		_dataUse = dataUse;
	}

	public ProjectRole role() {

		return _role;
	}

	public String id() {

		return _id;
	}

	@Override
	public int compareTo(ProjectMember m) {

		if (m == null) {
			return 1;
		}
		return role().type().compareTo(m.role().type());
	}

	public abstract void describe(XmlWriter w) throws Throwable;

}
