package daris.client.pssd;

public class CiteableIdUtils {

    /**
     * Depth of the dataset id. e.g. 1.5.1.1.1.1.1
     */
    public static final int DATASET_ID_DEPTH = 7;

    /**
     * Depth of the ex-method id. e.g. 1.5.1.1.1
     */
    public static final int EX_METHOD_ID_DEPTH = 5;

    public static final String METHOD_ID_ROOT_NAME = "pssd.method";

    /**
     * Depth of the project id. e.g. 1.5.1
     */
    public static final int PROJECT_ID_DEPTH = 3;

    /**
     * Depth of the project id root. e.g. 1.5
     */
    public static final int PROJECT_ID_ROOT_DEPTH = 2;

    public static final String PROJECT_ID_ROOT_NAME = "pssd.project";

    public static final String RSUBJECT_ID_ROOT_NAME = "pssd.r-subject";
    /**
     * Depth of the study id. e.g. 1.5.1.1.1.1
     */
    public static final int STUDY_ID_DEPTH = 6;
    /**
     * Depth of the subject id. e.g. 1.5.1.1
     */
    public static final int SUBJECT_ID_DEPTH = 4;
    public static final String TRANSFORM_ID_ROOT_NAME = "pssd.transform";

    public static int compare(String cid1, String cid2) {

        assert cid1 != null && cid2 != null;
        if (cid1.equals(cid2)) {
            return 0;
        }
        String[] parts1 = cid1.split("\\.");
        String[] parts2 = cid2.split("\\.");
        if (parts1.length < parts2.length) {
            return -1;
        }
        if (parts1.length > parts2.length) {
            return 1;
        }
        for (int i = 0; i < parts1.length; i++) {
            if (!parts1[i].equals(parts2[i])) {
                long n1 = Long.parseLong(parts1[i]);
                long n2 = Long.parseLong(parts2[i]);
                if (n1 < n2) {
                    return -1;
                }
                if (n1 > n2) {
                    return 1;
                }
            }
        }
        return 0;
    }

    public static String getChildTypeFromCID(String cid) {

        int depth = getDepth(cid);
        if (depth <= 1) {
            // must be a repository (id==null or id==uuid)
            return "project";
        }
        return getTypeFromDepth(getDepth(cid) + 1);
    }

    /**
     * Depth of the given identifier. This is the number of dots.
     * 
     * @param cid
     * @return
     */
    public static int getDepth(String cid) {

        if (cid == null || cid.length() == 0) {
            return 0;
        }
        int depth = 1;
        int idx = cid.indexOf('.');
        while (idx != -1) {
            depth++;
            idx = cid.indexOf('.', idx + 1);
        }
        return depth;

    }

    public static String getExMethodCID(String cid) {
        int depth = getDepth(cid);
        if (depth < EX_METHOD_ID_DEPTH || depth > DATASET_ID_DEPTH) {
            return null;
        }
        int diff = depth - EX_METHOD_ID_DEPTH;
        return getParentCID(cid, diff);
    }

    public static int getLastNumber(String cid) {

        if (!isCID(cid)) {
            return 0;
        } else {
            return Integer.parseInt(getLastPart(cid));
        }
    }

    /**
     * Return the last section of the specified citeable id. e.g. the last
     * section of 1.2.3 is 3
     * 
     * @param cid
     * @return
     */
    public static String getLastPart(String cid) {

        int idx = cid.lastIndexOf('.');
        if (idx == -1) {
            return cid;
        }
        return cid.substring(idx + 1);

    }

    /**
     * Returns the parent id.
     * 
     * @param cid
     * @return
     */
    public static String getParentCID(String cid) {

        if (cid == null) {
            return null;
        }
        int idx = cid.lastIndexOf('.');
        if (idx == -1) {
            return null;
        }
        return cid.substring(0, idx);

    }

    /**
     * Returns the parent/ancester id by specifying the levels.
     * 
     * @param cid
     * @param levels
     * @return
     */

    public static String getParentCID(String cid, int levels) {

        for (int i = 0; i < levels; i++) {
            cid = getParentCID(cid);
        }
        return cid;

    }

    /**
     * Returns the project id.
     * 
     * @param cid
     * @return
     */
    public static String getProjectCID(String cid) {

        int depth = getDepth(cid);
        if (depth < PROJECT_ID_DEPTH || depth > DATASET_ID_DEPTH) {
            return null;
        }
        int diff = depth - PROJECT_ID_DEPTH;
        return getParentCID(cid, diff);

    }

    public static String getStudyCID(String cid) {
        int depth = getDepth(cid);
        if (depth < STUDY_ID_DEPTH || depth > DATASET_ID_DEPTH) {
            return null;
        }
        int diff = depth - STUDY_ID_DEPTH;
        return getParentCID(cid, diff);
    }

    public static String getSubjectCID(String id) {
        int depth = getDepth(id);
        if (depth < SUBJECT_ID_DEPTH || depth > DATASET_ID_DEPTH) {
            return null;
        }
        int diff = depth - SUBJECT_ID_DEPTH;
        return getParentCID(id, diff);
    }

    public static String getTypeFromCID(String cid) {

        int depth = getDepth(cid);
        return getTypeFromDepth(depth);
    }

    public static String getTypeFromDepth(int depth) {

        switch (depth) {
        case PROJECT_ID_DEPTH:
            return "project";
        case SUBJECT_ID_DEPTH:
            return "subject";
        case EX_METHOD_ID_DEPTH:
            return "ex-method";
        case STUDY_ID_DEPTH:
            return "study";
        case DATASET_ID_DEPTH:
            return "dataset";
        default:
            return null;
        }
    }

    public static boolean isAncestor(String cid1, String cid2) {
        if (cid1 == null || cid2 == null) {
            return false;
        }
        return cid2.startsWith(cid1 + '.');
    }

    /**
     * Check if the string is a citeable id.
     * 
     * @param s
     * @return
     */
    public static boolean isCID(String s) {
        if (s == null) {
            return true;
        }
        return s.matches("^\\d+(\\d*.)*\\d+$");
    }

    public static boolean isDataSetCID(String cid) {

        if (!isCID(cid)) {
            return false;
        }
        if (getDepth(cid) == DATASET_ID_DEPTH) {
            return true;
        }
        return false;

    }

    public static boolean isDataSetCIDSegment(String s) {

        if (!isCID(s)) {
            return false;
        }
        if (getDepth(s) == DATASET_ID_DEPTH - PROJECT_ID_ROOT_DEPTH) {
            return true;
        } else {
            return false;
        }

    }

    public static boolean isDescendant(String cid1, String cid2) {
        if (cid1 == null || cid2 == null) {
            return false;
        }
        return cid1.startsWith(cid2 + '.');
    }

    public static boolean isDirectChild(String child, String parent) {
        return isDirectParent(parent, child);
    }

    public static boolean isDirectParent(String parent, String child) {
        if (parent == null && isProjectCID(child)) {
            return true;
        }
        if (child != null && parent != null) {
            if (getDepth(parent) + 1 == getDepth(child)
                    && child.startsWith(parent)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isExMethodCID(String cid) {

        if (!isCID(cid)) {
            return false;
        }
        if (getDepth(cid) == EX_METHOD_ID_DEPTH) {
            return true;
        }
        return false;

    }

    public static boolean isExMethodCIDSegment(String s) {

        if (!isCID(s)) {
            return false;
        }
        if (getDepth(s) == EX_METHOD_ID_DEPTH - PROJECT_ID_ROOT_DEPTH) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Check if the specified pid is the parent of the specified id.
     * 
     * @param parnetCid
     * @param cid
     * @return
     */

    public static boolean isParent(String parnetCid, String cid) {

        if (cid.startsWith(parnetCid + '.')) {
            if (getDepth(parnetCid) + 1 == getDepth(cid)) {
                return true;
            }
        }
        return false;

    }

    public static boolean isProjectCID(String cid) {

        if (!isCID(cid)) {
            return false;
        }
        if (getDepth(cid) == PROJECT_ID_DEPTH) {
            return true;
        }
        return false;

    }

    public static boolean isProjectCIDSegment(String s) {

        if (!isCID(s)) {
            return false;
        }
        if (getDepth(s) == PROJECT_ID_DEPTH - PROJECT_ID_ROOT_DEPTH) {
            return true;
        } else {
            return false;
        }

    }

    public static boolean isStudyCID(String cid) {

        if (!isCID(cid)) {
            return false;
        }
        if (getDepth(cid) == STUDY_ID_DEPTH) {
            return true;
        }
        return false;

    }

    public static boolean isStudyCIDSegment(String s) {

        if (!isCID(s)) {
            return false;
        }
        if (getDepth(s) == STUDY_ID_DEPTH - PROJECT_ID_ROOT_DEPTH) {
            return true;
        } else {
            return false;
        }

    }

    public static boolean isSubjectCID(String cid) {

        if (!isCID(cid)) {
            return false;
        }
        if (getDepth(cid) == SUBJECT_ID_DEPTH) {
            return true;
        }
        return false;

    }

    public static boolean isSubjectCIDSegment(String s) {

        if (!isCID(s)) {
            return false;
        }
        if (getDepth(s) == SUBJECT_ID_DEPTH - PROJECT_ID_ROOT_DEPTH) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Replace left sections of the citeable id with the specified string.
     * 
     * @param cid
     * @param newParent
     * @return
     */
    public static String replaceParent(String cid, String newParent) {

        if (getDepth(cid) < getDepth(newParent)) {
            return null;
        }
        if (getDepth(cid) == getDepth(newParent)) {
            return newParent;
        }
        String[] parts1 = cid.split("\\.");
        String[] parts2 = newParent.split("\\.");

        for (int i = 0; i < parts2.length; i++) {
            parts1[i] = parts2[i];
        }
        String newCid = parts1[0];
        for (int j = 1; j < parts1.length; j++) {
            newCid = newCid + "." + parts1[j];
        }
        return newCid;

    }
}
