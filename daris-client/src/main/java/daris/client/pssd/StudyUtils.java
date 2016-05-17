package daris.client.pssd;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;

public class StudyUtils {

    public static String findOrCreateStudy(ServerClient.Connection cxn,
            String pid, String step, String studyName, String studyDescription,
            PrintStream ps) throws Throwable {
        String studyCid = findStudy(cxn, pid, studyName);
        if (studyCid == null) {
            studyCid = createStudy(cxn, pid, step, studyName, studyDescription);
        } else {
            if (ps != null) {
                ps.println("study " + studyCid + " already exists.");
            }
        }
        return studyCid;
    }

    static String createStudy(ServerClient.Connection cxn, String pid,
            String step, String studyName, String studyDescription)
                    throws Throwable {
        String exMethodCid = findExMethod(cxn, pid);
        if (step == null) {
            List<String> exMethodSteps = findExMethodSteps(cxn, exMethodCid);
            if (exMethodSteps == null || exMethodSteps.isEmpty()) {
                throw new Exception(
                        "No step found in ex-method: '" + exMethodCid + "'");
            }
            if (exMethodSteps.size() > 1) {
                throw new Exception("More than one step found in ex-method: '"
                        + exMethodCid + "'");
            }
            step = exMethodSteps.get(0);
        }
        XmlStringWriter w = new XmlStringWriter();
        w.add("pid", exMethodCid);
        w.add("step", step);
        w.add("fillin", true);
        if (studyName != null) {
            w.add("name", studyName);
        }
        if (studyDescription != null) {
            w.add("description", studyDescription);
        }
        return cxn.execute("om.pssd.study.create", w.document(), null, null)
                .value("id");
    }

    private static String findExMethod(ServerClient.Connection cxn, String pid)
            throws Throwable {
        XmlDoc.Element ae = ObjectUtils.getAssetMeta(cxn, pid);
        String objectType = ae.value("meta/daris:pssd-object/type");
        if ("ex-method".equals(objectType)) {
            return pid;
        } else if ("subject".equals(objectType)) {
            XmlDoc.Element re = cxn
                    .execute("asset.query",
                            "<where>cid in '" + pid
                                    + "'</where><action>get-cid</action>",
                            null, null);
            if (re.count("cid") > 1) {
                throw new Exception("More than one ex-method found in subject '"
                        + pid + "'.");
            }
            return re.value("cid");
        } else {
            throw new Exception(
                    "Expect citable id of ex-method or subject. Found objec type: "
                            + objectType + " cid: " + pid);
        }
    }

    private static List<String> findExMethodSteps(ServerClient.Connection cxn,
            String exMethodCid) throws Throwable {
        Collection<String> steps = cxn
                .execute("om.pssd.ex-method.study.step.find",
                        "<id>" + exMethodCid + "</id>", null, null)
                .values("ex-method/step");
        if (steps == null || steps.isEmpty()) {
            return null;
        }
        return new ArrayList<String>(steps);
    }

    static String findStudy(ServerClient.Connection cxn, String pid,
            String studyName) throws Throwable {
        StringBuilder query = new StringBuilder();
        query.append("(model='om.pssd.study' and (cid starts with '" + pid
                + "' or cid='" + pid + "'))");
        if (studyName != null) {
            query.append(
                    " and (xpath(daris:pssd-object/name)='" + studyName + "')");
        }
        XmlStringWriter w = new XmlStringWriter();
        w.add("where", query.toString());
        w.add("action", "get-cid");
        XmlDoc.Element re = cxn.execute("asset.query", w.document());
        if (re.elementExists("cid")) {
            if (re.count("cid") == 1) {
                return re.value("cid");
            } else {
                throw new Exception("More than one study found in '" + pid
                        + "' with name: '" + studyName + "'");
            }
        }
        return null;
    }
}
