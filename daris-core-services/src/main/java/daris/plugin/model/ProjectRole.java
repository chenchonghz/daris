package daris.plugin.model;

import java.util.Collection;

public class ProjectRole {

    public static final String PROJECT_SPECIFIC_ROLE_PREFIX = "daris:pssd.project.";
    public static final String SUBJECT_DATA_USE_ROLE_PREFIX = PROJECT_SPECIFIC_ROLE_PREFIX + "subject.use.";
    public static final String PROJECT_ADMINISTRATOR_ROLE_PREFIX = PROJECT_SPECIFIC_ROLE_PREFIX + "admin.";
    public static final String SUBJECT_ADMINISTRATOR_ROLE_PREFIX = PROJECT_SPECIFIC_ROLE_PREFIX + "subject.admin.";
    public static final String MEMBER_ROLE_PREFIX = PROJECT_SPECIFIC_ROLE_PREFIX + "member.";
    public static final String GUEST_ROLE_PREFIX = PROJECT_SPECIFIC_ROLE_PREFIX + "guest.";

    public static enum Type {

        PROJECT_ADMINISTRATOR, SUBJECT_ADMINISTRATOR, MEMBER, GUEST;

        @Override
        public String toString() {
            return super.toString().toLowerCase().replace('_', '-');
        }

        public String roleNameOf(String projectCid) {
            StringBuilder sb = new StringBuilder(ProjectRole.PROJECT_SPECIFIC_ROLE_PREFIX);
            switch(this){
            case PROJECT_ADMINISTRATOR:
                sb.append("admin.");
                break;
            case SUBJECT_ADMINISTRATOR:
                sb.append("subject.admin.");
                break;
            default:
                sb.append(toString()).append(".");
                break;
            }
            sb.append(projectCid);
            return sb.toString();
        }

        public static Type fromString(String s) {
            if (s != null) {
                Type[] vs = values();
                for (Type v : vs) {
                    if (v.toString().equalsIgnoreCase(s)) {
                        return v;
                    }
                }
            }
            return null;
        }

        public static Type roleTypeFrom(Collection<String> projectSpecificRoles, String projectCid) {
            if (projectSpecificRoles != null) {
                if (projectSpecificRoles.contains(projectAdministratorRoleName(projectCid))) {
                    return Type.PROJECT_ADMINISTRATOR;
                } else if (projectSpecificRoles.contains(subjectAdministratorRoleName(projectCid))) {
                    return Type.SUBJECT_ADMINISTRATOR;
                } else if (projectSpecificRoles.contains(memberRoleName(projectCid))) {
                    return Type.MEMBER;
                } else if (projectSpecificRoles.contains(guestRoleName(projectCid))) {
                    return Type.GUEST;
                }
            }
            return null;
        }

        public static String[] stringValues() {
            Type[] vs = values();
            String[] svs = new String[vs.length];
            for (int i = 0; i < vs.length; i++) {
                svs[i] = vs[i].toString();
            }
            return svs;
        }
    }

    public static String roleNameOf(String projectCid, Type roleType) {
        return roleType.roleNameOf(projectCid);
    }

    public static String subjectDataUseRoleNameOf(String projectCid, DataUse dataUse) {
        return ProjectRole.SUBJECT_DATA_USE_ROLE_PREFIX + dataUse + "." + projectCid;
    }

    public static String projectAdministratorRoleName(String projectCid) {
        return PROJECT_ADMINISTRATOR_ROLE_PREFIX + projectCid;
    }

    public static String subjectAdministratorRoleName(String projectCid) {
        return SUBJECT_ADMINISTRATOR_ROLE_PREFIX + projectCid;
    }

    public static String memberRoleName(String projectCid) {
        return MEMBER_ROLE_PREFIX + projectCid;
    }

    public static String guestRoleName(String projectCid) {
        return GUEST_ROLE_PREFIX + projectCid;
    }

    public static String subjectUseRoleName(DataUse dataUse, String projectCid) {
        return SUBJECT_DATA_USE_ROLE_PREFIX + dataUse.toString() + "." + projectCid;
    }

    public static String roleTypeNameFrom(Collection<String> projectSpecificRoles, String projectCid) {
        Type type = Type.roleTypeFrom(projectSpecificRoles, projectCid);
        if (type != null) {
            return type.toString();
        }
        return null;
    }

    public static String subjectDataUseFrom(Collection<String> subjectUseRoles, String projectCid) {
        DataUse dataUse = DataUse.dataUseFrom(subjectUseRoles, projectCid);
        if (dataUse != null) {
            return dataUse.toString();
        }
        return null;
    }

}
