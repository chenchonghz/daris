package nig.mf.plugin.pssd.servlets.modules;

import nig.mf.plugin.pssd.servlets.AbstractServlet;
import nig.mf.plugin.pssd.servlets.HtmlBuilder;
import nig.mf.plugin.pssd.servlets.ObjectServlet;
import nig.mf.plugin.pssd.servlets.OutputFormat;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlStringWriter;

public class ObjectDescribeModule implements Module {

    public static final ObjectDescribeModule INSTANCE = new ObjectDescribeModule();

    public static final String NAME = ObjectServlet.ModuleName.describe.name();

    private ObjectServlet _servlet;

    private ObjectDescribeModule() {
    }

    public ObjectServlet servlet() {
        return _servlet;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey, HttpRequest request,
            HttpResponse response) throws Throwable {
        String cid = request.variableValue(ObjectServlet.ARG_CID);
        OutputFormat format = OutputFormat.parse(request, OutputFormat.html);
        describe(server, sessionKey, cid, format, response);
    }

    @Override
    public final String name() {
        return NAME;
    }

    public static String urlFor(String cid, SessionKey skey, String token) {
        return ObjectServlet.urlFor(ObjectServlet.ModuleName.describe, cid, skey == null ? null
                : skey.key(), token, (String[]) null);
    }

    public static String urlFor(String cid) {
        return urlFor(cid, null, null);
    }

    static void describe(HttpServer server, SessionKey sessionKey, String cid, OutputFormat format,
            HttpResponse response) throws Throwable {
        XmlDoc.Element oe = describe(server, sessionKey, cid);
        if (format == OutputFormat.html) {
            HtmlBuilder html = new HtmlBuilder(urlFor(cid));
            outputHtml(sessionKey, oe, html);
            response.setContent(html.buildHtml(), "text/html");
        } else {
            XmlStringWriter w = new XmlStringWriter();
            outputXml(sessionKey, oe, w);
            response.setContent(w.document(), "text/xml");
        }
    }

    public static XmlDoc.Element describe(HttpServer server, SessionKey sessionKey, String cid)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", cid);
        return server.execute(sessionKey, "om.pssd.object.describe", dm.root()).element("object");
    }

    private static void outputHtml(SessionKey sessionKey, Element oe, HtmlBuilder html)
            throws Throwable {
        String cid = oe.value("id");
        String type = oe.value("@type");
        html.setTitle("DaRIS: " + type + " " + cid + " details");

        // nav bar items
        boolean isRepository = cid == null;
        html.addNavItem("DaRIS", isRepository ? null : ObjectListModule.urlFor(null));
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
                    html.addNavItem("Ex-method " + exMethodId, isExMethod ? null
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

        // tab bar items
        html.addTabItem("Members", CIDUtil.isDataSetId(cid) ? "#" : ObjectListModule.urlFor(cid));
        html.addTabItem("Details", null);
        html.addTabItem("Download", cid == null ? "#" : ObjectDownloadModule.urlFor(cid, false));

        // content
        html.appendContent("<pre>\n");
        html.appendContent(AbstractServlet.convertToIndentedText(oe));
        html.appendContent("\n</pre>\n");
    }

    private static void outputXml(SessionKey sessionKey, Element oe, XmlStringWriter w)
            throws Throwable {
        if (oe != null) {
            w.add(oe);
        }
    }

}
