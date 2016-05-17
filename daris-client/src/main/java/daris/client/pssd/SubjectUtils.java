package daris.client.pssd;

import java.io.PrintStream;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class SubjectUtils {

    public static String findOrCreateSubject(ServerClient.Connection cxn,
            String projectCid, String methodCid, String subjectName,
            String subjectDescription, PrintStream ps) throws Throwable {
        String subjectCid = findSubject(cxn, projectCid, methodCid,
                subjectName);
        if (subjectCid == null) {
            subjectCid = createSubject(cxn, projectCid, methodCid, subjectName,
                    subjectDescription);
        } else {
            if (ps != null) {
                ps.println("subject " + subjectCid + " already exists.");
            }
        }
        return subjectCid;
    }

    static String createSubject(ServerClient.Connection cxn, String projectCid,
            String methodCid, String subjectName, String subjectDescription)
                    throws Throwable {

        XmlStringWriter w = new XmlStringWriter();
        w.add("pid", projectCid);
        if (methodCid == null) {
            methodCid = findMethod(cxn, projectCid);
        }
        w.add("method", methodCid);
        w.add("fillin", true);
        if (subjectName != null) {
            w.add("name", subjectName);
        }
        if (subjectDescription != null) {
            w.add("description", subjectDescription);
        }
        return cxn.execute("om.pssd.subject.create", w.document(), null, null)
                .value("id");
    }

    private static String findMethod(ServerClient.Connection cxn,
            String projectCid) throws Throwable {
        XmlDoc.Element pe = ObjectUtils.getAssetMeta(cxn, projectCid);
        if (!pe.elementExists("meta/daris:pssd-project/method/id")) {
            throw new Exception(
                    "No method is set on project '" + projectCid + "'");
        }
        if (pe.count("meta/daris:pssd-project/method/id") > 1) {
            throw new Exception("More than one methods are set on project '"
                    + projectCid + "'");
        }
        return pe.value("meta/daris:pssd-project/method/id");
    }

    static String findSubject(ServerClient.Connection cxn, String projectCid,
            String methodCid, String subjectName) throws Throwable {

        StringBuilder sb = new StringBuilder();
        sb.append("(cid in '" + projectCid + "')");
        if (methodCid != null) {
            sb.append(" and (xpath(daris:pssd-subject/method)='" + methodCid
                    + "')");
        }
        if (subjectName != null) {
            sb.append(" and (xpath(daris:pssd-object/name)='" + subjectName
                    + "')");
        }
        XmlStringWriter w = new XmlStringWriter();
        w.add("where", sb.toString());
        w.add("action", "get-cid");
        XmlDoc.Element re = cxn.execute("asset.query", w.document(), null,
                null);
        if (re.elementExists("cid")) {
            if (re.count("cid") == 1) {
                return re.value("cid");
            } else {
                throw new Exception("More than one subject found in project: '"
                        + projectCid + "' with name: '" + subjectName + "'");
            }
        }
        return null;
    }

}
