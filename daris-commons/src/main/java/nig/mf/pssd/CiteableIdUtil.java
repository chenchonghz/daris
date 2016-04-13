package nig.mf.pssd;

import nig.util.ObjectUtil;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public class CiteableIdUtil {

	// Standard citeable ID named roots
	public static final String PROJECT_ID_ROOT_NAME = "pssd.project";
	public static final String RSUBJECT_ID_ROOT_NAME = "pssd.r-subject";
	public static final String METHOD_ID_ROOT_NAME = "pssd.method";
	public static final String TRANSFORM_ID_ROOT_NAME = "pssd.transform";

	/**
	 * Depth of the repository id. e.g. 1.5
	 */
	private static final int REPOSITORY_ID_DEPTH = 2;


	/**
	 * Depth of the project id. e.g. 1.5.1
	 */
	private static final int PROJECT_ID_DEPTH = 3;

	/**
	 * Depth of the subject id. e.g. 1.5.1.1
	 */
	private static final int SUBJECT_ID_DEPTH = 4;
	/**
	 * Depth of the ex-method id. e.g. 1.5.1.1.1
	 */
	private static final int EX_METHOD_ID_DEPTH = 5;
	/**
	 * Depth of the study id. e.g. 1.5.1.1.1.1
	 */
	private static final int STUDY_ID_DEPTH = 6;
	/**
	 * Depth of the dataset id. e.g. 1.5.1.1.1.1.1
	 */
	private static final int DATASET_ID_DEPTH = 7;
	/**
	 * Depth of the data-object id. e.g. 1.5.1.1.1.1.1.1
	 */
	private static final int DATA_OBJECT_ID_DEPTH = 8;


	/**
	 * Expected depth of repository CID
	 * @return
	 */
	public static int repositoryDepth () {
		return REPOSITORY_ID_DEPTH ;
	}
	/**
	 * Expected depth of project CID
	 * @return
	 */
	public static int projectDepth () {
		return PROJECT_ID_DEPTH ;
	}
	/**
	 * Expected depth of subject CID
	 * @return
	 */
	public static int subjectDepth () {
		return SUBJECT_ID_DEPTH ;
	}
	/**
	 * Expected depth of ExMethod CID
	 * @return
	 */
	public static int exMethodDepth () {
		return EX_METHOD_ID_DEPTH ;
	}
	/**
	 * Expected depth of Study CID
	 * @return
	 */
	public static int studyDepth () {
		return STUDY_ID_DEPTH ;
	}
	/**
	 * Expected depth of DataSet CID
	 * @return
	 */
	public static int dataSetDepth () {
		return DATASET_ID_DEPTH ;
	}
	/**
	 * Expected depth of DataObject CID
	 * @return
	 */
	public static int dataObjectDepth () {
		return DATA_OBJECT_ID_DEPTH ;
	}

	/**
	 * Returns the server's root citeable identifier.
	 * 
	 * @param executor
	 * @param proute
	 *            Route to remote server. If null use local
	 * @return
	 * @throws Throwable
	 */
	public static String citeableIDRoot(ServiceExecutor executor, String proute) throws Throwable {

		//
		XmlDoc.Element r = null;
		if (proute == null) {
			r = executor.execute("citeable.root.get");
		} else {
			r = executor.execute(new ServerRoute(proute), "citeable.root.get");
		}
		return r.value("cid");
	}

	/**
	 * Check if the string is a citeable id.
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isCiteableId(String s) {

		if (s == null) {
			return false;
		}

		return s.matches("^\\d+(\\d*.)*\\d+$");

	}

	/**
	 * Returns the project id from a given CID
	 * 
	 * @param id citeable ID
	 * @return
	 */
	public static String getProjectId(String id) {

		int depth = getIdDepth(id);
		if (depth < projectDepth()  || depth > dataObjectDepth()) {
			return null;
		}
		int diff = depth - projectDepth();
		return getParentId(id, diff);

	}

	/**
	 * Returns the subject id from a given CID
	 * 
	 * @param cid
	 * @return
	 */
	public static String getSubjectId(String cid) {

		int depth = getIdDepth(cid);
		if (depth < subjectDepth()  || depth > dataObjectDepth()) {

			return null;
		}
		int diff = depth - subjectDepth();
		return getParentId(cid, diff);
	}

	/**
	 * Returns the ex-method id from a given CID
	 * 
	 * @param cid
	 * @return
	 */
	public static String getExMethodId(String cid) {

		int depth = getIdDepth(cid);
	    if (depth < exMethodDepth()  || depth > dataObjectDepth()) {

			return null;
		}
		int diff = depth - exMethodDepth();
		return getParentId(cid, diff);
	}

	/**
	 * Returns the study id from a given CID
	 * 
	 * @param cid
	 * @return
	 */
	public static String getStudyId(String cid) {

		int depth = getIdDepth(cid);
		if (depth < studyDepth() || depth > dataObjectDepth()) {
			return null;
		}
		int diff = depth - studyDepth();
		return getParentId(cid, diff);
	}

	/**
	 * Returns the dataset id from a given CID
	 * 
	 * @param cid
	 * @return
	 */
	public static String getDataSetId(String cid) {

		int depth = getIdDepth(cid);
		if (depth < dataSetDepth() || depth > dataObjectDepth()) {

			return null;
		}
		int diff = depth - dataSetDepth();
		return getParentId(cid, diff);
	}


	/**
	 * Returns the immediate parent id.
	 * 
	 * @param id CIteable ID
	 * @return
	 */
	public static String getParentId(String id) {

		int idx = id.lastIndexOf('.');
		if (idx == -1) {
			return null;
		}
		return id.substring(0, idx);

	}

	/**
	 * Returns the parent/ancester id by specifying the levels.
	 * 
	 * @param id
	 * @param levels
	 * @return
	 */

	public static String getParentId(String id, int levels) {

		for (int i = 0; i < levels; i++) {
			id = getParentId(id);
		}
		return id;
	}

	/**
	 * Returns the root parent/ancester id 
	 * E.g.  101.2.3 -> 101
	 * 
	 * @param id
	 * @param levels
	 * @return
	 */   
	public static String getRootParentId (String id) {
		int idx = id.indexOf(".");
		if (idx == -1) {
			return null;
		}
		return id.substring(0,idx);
	}

	/**
	 * Get the depth of the given identifier. This is the number of dots + 1
	 * 
	 * @param id
	 * @return  If id is null returns 0
	 */
	public static int getIdDepth(String id) {

		if (id == null || id.length() == 0) {
			return 0;
		}
		int depth = 1;
		int idx = id.indexOf('.');
		while (idx != -1) {
			depth++;
			idx = id.indexOf('.', idx + 1);
		}
		return depth;

	}

	/**
	 * Return the last section of the specified citeable id. e.g. the last section of 1.2.3 is 3
	 * 
	 * @param id
	 * @return
	 */
	public static String getLastSection(String id) {

		int idx = id.lastIndexOf('.');
		if (idx == -1) {
			return null;
		}
		return id.substring(idx + 1);

	}

	/**
	 * Replace left sections of the citeable id with the specified string.
	 * 
	 * @param id
	 * @param leftSections
	 * @return
	 */
	public static String replaceLeftSections(String id, String leftSections) {

		if (getIdDepth(id) < getIdDepth(leftSections)) {
			return null;
		}
		if (getIdDepth(id) == getIdDepth(leftSections)) {
			return leftSections;
		}
		String[] parts1 = id.split("\\.");
		String[] parts2 = leftSections.split("\\.");

		for (int i = 0; i < parts2.length; i++) {
			parts1[i] = parts2[i];
		}
		String newCid = parts1[0];
		for (int j = 1; j < parts1.length; j++) {
			newCid = newCid + "." + parts1[j];
		}
		return newCid;

	}

	public static boolean isProjectId(String id) {

		if (!isCiteableId(id)) {
			return false;
		}
		if (getIdDepth(id) == projectDepth()) {
			return true;
		}
		return false;

	}

	public static boolean isSubjectId(String id) {

		if (!isCiteableId(id)) {
			return false;
		}
		if (getIdDepth(id) == subjectDepth()) {
			return true;
		}
		return false;

	}

	public static boolean isExMethodId(String id) {

		if (!isCiteableId(id)) {
			return false;
		}
		if (getIdDepth(id) == exMethodDepth()) {
			return true;
		}
		return false;

	}

	public static boolean isStudyId(String id) {

		if (!isCiteableId(id)) {
			return false;
		}
		if (getIdDepth(id) == studyDepth()) {
			return true;
		}
		return false;

	}

	public static boolean isDataSetId(String id) {

		if (!isCiteableId(id)) {
			return false;
		}
		if (getIdDepth(id) == dataSetDepth()) {
			return true;
		}
		return false;

	}

	public static boolean isDataObjectId(String id) {

		if (!isCiteableId(id)) {
			return false;
		}
		if (getIdDepth(id) == dataObjectDepth()) {
			return true;
		}
		return false;

	}

	public static boolean isProjectIdSegment(String s) {

		if (!isCiteableId(s)) {
			return false;
		}
		if (getIdDepth(s) == projectDepth() - repositoryDepth()) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean isSubjectIdSegment(String s) {

		if (!isCiteableId(s)) {
			return false;
		}
		if (getIdDepth(s) == subjectDepth() - repositoryDepth()) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean isExMethodIdSegment(String s) {

		if (!isCiteableId(s)) {
			return false;
		}
		if (getIdDepth(s) == exMethodDepth() - repositoryDepth()) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean isStudyIdSegment(String s) {

		if (!isCiteableId(s)) {
			return false;
		}
		if (getIdDepth(s) == studyDepth() - repositoryDepth()) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean isDataSetIdSegment(String s) {

		if (!isCiteableId(s)) {
			return false;
		}
		if (getIdDepth(s) == dataSetDepth() - repositoryDepth()) {
			return true;
		} else {
			return false;
		}

	}

	public static boolean isDataObjectIdSegment(String s) {

		if (!isCiteableId(s)) {
			return false;
		}
		if (getIdDepth(s) == dataObjectDepth() - repositoryDepth()) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Returns the given string with leading non-digits removed. Will remove all prefix that are not numbers.
	 * 
	 * For instance,
	 * 
	 * AB9.1.2 -> 9.1.2 AB.8.2 -> 8.2
	 * 
	 * @param sid
	 * @return
	 */
	public static String removeLeadingNonDigits(String sid) {

		if (sid == null) {
			return null;
		}

		// Run through until we find a number..
		for (int i = 0; i < sid.length(); i++) {
			char ch = sid.charAt(i);
			if (Character.isDigit(ch)) {
				return sid.substring(i);
			}
		}

		return null;
	}

	/**
	 * Returns the given string with leading characters before (and including) the last instance of the delimiter
	 * removed For instance,
	 * 
	 * XYZ_ABC_1.2 -> 1.2 with the delimiter as '_'
	 * 
	 * @param sid
	 * @paqram delim
	 * @return
	 */
	public static String removeBeforeLastDelim(String sid, String delim) {

		if (sid == null) {
			return null;
		}
		if (delim == null)
			return sid;

		String[] t = sid.split(delim);
		int n = t.length;
		if (n >= 1) {
			sid = t[n - 1];
		}
		return sid;
	}

	/**
	 * Returns the given string with trailing characters after (and including) the last instance of the delimiter
	 * removed.  For instance,
	 * 
	 * XYZ_ABC_1.2 -> XYZ_ABC with the delimiter as '_'
	 * 
	 * @param sid
	 * @paqram delim
	 * @return
	 */
	public static String removeAfterLastDelim(String sid, String delim) {

		if (sid == null) {
			return null;
		}
		if (delim == null) {
			return sid;
		}

		int idx = sid.lastIndexOf(delim);
		if (idx>=0) {
			return sid.substring(0,idx);
		} else {
			return sid;
		}
	}


	/**
	 * Find out if a string is numeric. Useful to find out if a String is a CID
	 * 
	 * @param s
	 * @return true or false
	 * @throws Throwable
	 */
	public static boolean isNumeric(String s) throws Throwable {

		return s.matches("\\d+");
	}

	public static int compare(String id1, String id2) {

		if (ObjectUtil.equals(id1, id2)) {
			return 0;
		}
		if (id1 == null) {
			return -1;
		}
		if (id2 == null) {
			return 1;
		}
		String[] parts1 = id1.split("\\.");
		String[] parts2 = id2.split("\\.");
		if (parts1.length < parts2.length) {
			return -1;
		}
		if (parts1.length > parts2.length) {
			return 1;
		}
		for (int i = 0; i < parts1.length; i++) {
			if (!parts1[i].equals(parts2[i])) {
				long n1 = 0;
				long n2 = 0;
				try {
					n1 = Long.parseLong(parts1[i]);
					n2 = Long.parseLong(parts2[i]);
				} catch (Throwable e) {

				}
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

	/**
	 * Check if cid1 starts with cid2.
	 * 
	 * @param cid1
	 * @param cid2
	 * @return
	 */
	public static boolean startsWith(String cid1, String cid2) {
		if (cid1==null && cid2==null) {
			return true;
		}
		if(cid1==null || cid2==null){
			return false;
		}
		if (cid1.startsWith(cid2) && cid1.charAt(cid2.length()) == '.') {
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
	}

}
