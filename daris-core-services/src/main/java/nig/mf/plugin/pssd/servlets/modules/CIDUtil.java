package nig.mf.plugin.pssd.servlets.modules;

import nig.mf.pssd.CiteableIdUtil;

public class CIDUtil extends nig.mf.pssd.CiteableIdUtil {

    public static String getParentType(String cid) {
        if (cid == null) {
            return null;
        }
        if (CiteableIdUtil.isProjectId(cid)) {
            return "repository";
        }
        if (CiteableIdUtil.isSubjectId(cid)) {
            return "project";
        }
        if (CiteableIdUtil.isExMethodId(cid)) {
            return "subject";
        }
        if (CiteableIdUtil.isStudyId(cid)) {
            return "ex-method";
        }
        if (CiteableIdUtil.isDataSetId(cid)) {
            return "study";
        }
        if (CiteableIdUtil.isDataObjectId(cid)) {
            return "dataset";
        }
        return null;
    }

    public static String getType(String cid) {
        if (cid == null) {
            return "repository";
        }
        if (CiteableIdUtil.isProjectId(cid)) {
            return "project";
        }
        if (CiteableIdUtil.isSubjectId(cid)) {
            return "subject";
        }
        if (CiteableIdUtil.isExMethodId(cid)) {
            return "ex-method";
        }
        if (CiteableIdUtil.isStudyId(cid)) {
            return "study";
        }
        if (CiteableIdUtil.isDataSetId(cid)) {
            return "dataset";
        }
        if (CiteableIdUtil.isDataObjectId(cid)) {
            return "data-object";
        }
        return null;
    }

    public static String getChildType(String cid) {
        if (cid == null) {
            return "project";
        }
        if (CiteableIdUtil.isProjectId(cid)) {
            return "subject";
        }
        if (CiteableIdUtil.isSubjectId(cid)) {
            return "ex-method";
        }
        if (CiteableIdUtil.isExMethodId(cid)) {
            return "study";
        }
        if (CiteableIdUtil.isStudyId(cid)) {
            return "dataset";
        }
        if (CiteableIdUtil.isDataSetId(cid)) {
            return "data-object";
        }
        return null;
    }

    public static String getChildType(String cid, boolean plural) {
        String ct = getChildType(cid);
        if (!plural) {
            return ct;
        } else {
            if (ct == null) {
                return null;
            }
            if ("study".equals(ct)) {
                return "studies";
            }
            return ct + "s";
        }
    }
}
