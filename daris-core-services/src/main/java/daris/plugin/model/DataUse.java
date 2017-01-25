package daris.plugin.model;

import java.util.Collection;

public enum DataUse {

    UNSPECIFIED, SPECIFIC, EXTENDED;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }

    public static DataUse fromString(String s) {
        if (s != null) {
            DataUse[] vs = values();
            for (DataUse v : vs) {
                if (v.toString().equalsIgnoreCase(s)) {
                    return v;
                }
            }
        }
        return null;
    }

    public static DataUse dataUseFrom(Collection<String> subjectUseRoles, String projectCid) {
        if (subjectUseRoles != null) {
            if (subjectUseRoles.contains(ProjectRole.subjectUseRoleName(DataUse.EXTENDED, projectCid))) {
                return DataUse.EXTENDED;
            }
            if (subjectUseRoles.contains(ProjectRole.subjectUseRoleName(DataUse.SPECIFIC, projectCid))) {
                return DataUse.SPECIFIC;
            }
            if (subjectUseRoles.contains(ProjectRole.subjectUseRoleName(DataUse.UNSPECIFIED, projectCid))) {
                return DataUse.UNSPECIFIED;
            }
        }
        return null;
    }

    public static String[] stringValues() {
        DataUse[] vs = values();
        String[] svs = new String[vs.length];
        for (int i = 0; i < vs.length; i++) {
            svs[i] = vs[i].toString();
        }
        return svs;
    }
}
