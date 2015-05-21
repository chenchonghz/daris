package nig.mf.plugin.pssd.servlets.modules;

import java.util.List;

import nig.mf.plugin.pssd.servlets.HtmlBuilder;
import nig.mf.plugin.pssd.servlets.ObjectServlet;
import nig.mf.plugin.pssd.servlets.OutputFormat;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlStringWriter;
import arc.xml.XmlWriter;

public class ObjectListModule implements Module {

    public static final ObjectListModule INSTANCE = new ObjectListModule();

    public static final String NAME = ObjectServlet.ModuleName.list.name();

    private ObjectListModule() {
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey, HttpRequest request,
            HttpResponse response) throws Throwable {
        String cid = request.variableValue(ObjectServlet.ARG_CID);
        boolean sort = Boolean.parseBoolean(request.variableValue(ObjectServlet.ARG_SORT,
                Boolean.toString(true)));
        OutputFormat format = OutputFormat.parse(request, OutputFormat.html);

        // call object.describe service to get the object metadata
        XmlDoc.Element oe = null;
        if (cid != null) {
            oe = ObjectDescribeModule.describe(server, sessionKey, cid);
        }
        // call collection.member.list service to get the children objects
        List<XmlDoc.Element> coes = list(server, sessionKey, cid, sort);

        // set response content
        if (format == OutputFormat.html) {
            HtmlBuilder html = new HtmlBuilder(urlFor(cid));
            outputHtml(sessionKey, oe, coes, html);
            response.setContent(html.buildHtml(), "text/html");
        } else {
            XmlStringWriter w = new XmlStringWriter();
            outputXml(sessionKey, oe, coes, w);
            response.setContent(w.document(), "text/xml");
        }
    }

    private static void outputHtml(SessionKey sessionKey, XmlDoc.Element oe,
            List<XmlDoc.Element> coes, HtmlBuilder html) throws Throwable {
        String cid = oe == null ? null : oe.value("id");
        String type = oe == null ? CIDUtil.getType(null) : oe.value("@type");
        html.setTitle("DaRIS: " + type + (cid == null ? "" : (" " + cid)) + " members");
        html.addStyle("button {width:80px; font-size:9pt; line-height:1.5em;}");

        // nav bar items.
        boolean isRepository = cid == null;
        html.addNavItem("DaRIS", isRepository ? null : urlFor(null));
        if (!isRepository) {
            String projectId = CIDUtil.getProjectId(cid);
            boolean isProject = CIDUtil.isProjectId(cid);
            html.addNavItem("Project " + projectId, isProject ? null : urlFor(projectId));
            if (!isProject) {
                String subjectId = CIDUtil.getSubjectId(cid);
                boolean isSubject = CIDUtil.isSubjectId(cid);
                html.addNavItem("Subject " + subjectId, isSubject ? null : urlFor(subjectId));
                if (!isSubject) {
                    String exMethodId = CIDUtil.getExMethodId(cid);
                    boolean isExMethod = CIDUtil.isExMethodId(cid);
                    html.addNavItem("Ex-method " + exMethodId, isSubject ? null
                            : urlFor(exMethodId));
                    if (!isExMethod) {
                        String studyId = CIDUtil.getStudyId(cid);
                        boolean isStudy = CIDUtil.isStudyId(cid);
                        html.addNavItem("Study " + studyId, isStudy ? null : urlFor(studyId));
                        if (!isStudy) {
                            String dataSetId = CIDUtil.getDataSetId(cid);
                            boolean isDataSet = CIDUtil.isDataSetId(cid);
                            html.addNavItem("Dataset " + dataSetId, isDataSet ? null
                                    : urlFor(dataSetId));
                        }
                    }
                }
            }
        }

        // tab items
        html.addTabItem("Members", null);
        html.addTabItem("Details", cid == null ? "#" : ObjectDescribeModule.urlFor(cid));
        html.addTabItem("Download", cid == null ? "#" : ObjectDownloadModule.urlFor(cid, false));

        // contents
        if (coes != null && !coes.isEmpty()) {
            html.appendContent("<table width=\"100%\">\n");
            html.appendContent("<thead><tr class=\"head\"><th width=\"15%\">type</th><th width=\"15%\">cid</th><th width=\"30%\">name</th><th width=\"30%\">action</th></tr></thead>\n");
            html.appendContent("<tbody>\n");
            for (XmlDoc.Element coe : coes) {
                html.appendContent("<tr>\n");
                String cCid = coe.value("id");
                String cDetailsUrl = ObjectDescribeModule.urlFor(cCid);
                String cMembersUrl = ObjectListModule.urlFor(cCid);
                String cDownloadUrl = ObjectDownloadModule.urlFor(cCid, false);
                // type
                html.appendContent("  <td align=\"center\">" + coe.value("@type") + "</td>\n");
                // cid
                html.appendContent("  <td align=\"center\">" + cCid + "</td>\n");
                // name
                String name = coe.value("name");
                html.appendContent("  <td>" + (name==null?"&nbsp;":name) + "</td>\n");
                // action
                html.appendContent("  <td align=\"center\">\n");
                html.appendContent("<button onclick=\"window.location.href='" + cMembersUrl
                        + "';\"" + (CIDUtil.isDataSetId(cid) ? " disabled" : "")
                        + ">members</button>");
                html.appendContent("<button onclick=\"window.location.href='" + cDetailsUrl
                        + "';\">details</button>");
                html.appendContent("<button onclick=\"window.location.href='" + cDownloadUrl
                        + "';\">download</button>");
                html.appendContent("</td>\n");
                html.appendContent("</tr>\n");
            }
            html.appendContent("</tbody>\n");
            html.appendContent("</table>\n");
        }

    }

    public static String urlFor(String cid, SessionKey sessionKey, String token) {
        String url = ObjectServlet.urlFor(ObjectServlet.ModuleName.list, cid, sessionKey == null ? null
                : sessionKey.key(), token, (String[]) null);
        return url;
    }

    public static String urlFor(String cid) {
        return urlFor(cid, null, null);
    }

    private static void outputXml(SessionKey sessionKey, XmlDoc.Element oe,
            List<XmlDoc.Element> coes, XmlWriter w) throws Throwable {
        if (oe != null) {
            w.push(oe.value("@type"),
                    new String[] { "cid", oe.value("id"), "id", oe.value("id/@asset"), "name",
                            oe.value("name") });
            if (coes != null) {
                for (XmlDoc.Element coe : coes) {
                    w.add(coe.value("@type"),
                            new String[] { "cid", coe.value("id"), "id", coe.value("id/@asset"),
                                    "name", coe.value("name") });
                }
            }
            w.pop();
        }
    }

    public static List<XmlDoc.Element> list(HttpServer server, SessionKey sessionKey, String pid,
            boolean sort) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        if (pid != null) {
            dm.add("id", pid);
        }
        dm.add("sort", sort);
        return server.execute(sessionKey, "om.pssd.collection.member.list", dm.root()).elements(
                "object");
    }

    @Override
    public String name() {
        return NAME;
    }

}
