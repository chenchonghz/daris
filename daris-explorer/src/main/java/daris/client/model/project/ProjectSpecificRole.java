package daris.client.model.project;

public enum ProjectSpecificRole {
    PROJECT_ADMIN("project-administrator",
            "daris:pssd.project.admin"), SUBJECT_ADMIN("subject-administrator",
                    "daris:pssd.project.subject.admin"), MEMBER("member",
                            "daris:pssd.project.member"), GUEST("guest",
                                    "daris:pssd.project.guest");
    public final String typeName;
    public final String roleNamePrefix;

    ProjectSpecificRole(String typeName, String roleNamePrefix) {
        this.typeName = typeName;
        this.roleNamePrefix = roleNamePrefix;
    }

    @Override
    public final String toString() {
        return this.typeName;
    }

    public static ProjectSpecificRole fromString(String s) {
        if (s != null) {
            ProjectSpecificRole[] vs = values();
            for (ProjectSpecificRole v : vs) {
                if (v.typeName.equalsIgnoreCase(s)) {
                    return v;
                }
            }
        }
        return null;
    }

    public String roleNameForProject(String projectCid) {
        return this.roleNamePrefix + "." + projectCid;
    }
}
