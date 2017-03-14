package nig.mf.plugin.pssd.servlets.modules;

import java.util.List;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlStringWriter;
import nig.mf.plugin.pssd.servlets.DicomServlet;
import nig.mf.plugin.pssd.servlets.OutputFormat;
import nig.mf.plugin.pssd.util.StringUtil;

public class DicomMetadataGetModule implements Module {
    public static final DicomMetadataGetModule INSTANCE = new DicomMetadataGetModule();

    public static final String NAME = DicomServlet.ModuleName.metadata.name();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey, HttpRequest request, HttpResponse response)
            throws Throwable {
        // id
        String id = request.variableValue(DicomServlet.ARG_ID);
        // cid
        String cid = request.variableValue(DicomServlet.ARG_CID);
        if (id == null) {
            id = server.execute(sessionKey, "asset.get", "<cid>" + cid + "</cid>", null, null).value("asset/@id");
        }
        // idx
        String idxStr = request.variableValue(DicomServlet.ARG_IDX);
        int idx = idxStr == null ? 1 : Integer.parseInt(idxStr);
        // output format
        OutputFormat format = OutputFormat.parse(request, OutputFormat.xml);
        try {
            XmlDoc.Element dcmMetadata = getDicomMetadata(server, sessionKey, id, idx, true);
            switch (format) {
            case xml:
                response.setContent(toXmlString(idx, dcmMetadata), "text/xml");
                break;
            case html:
                response.setContent(toHtmlString(dcmMetadata, false), "text/html");
                break;
            case text:
                response.setContent(toTextString(dcmMetadata), "plain/text");
                break;
            default:
                break;
            }
        } catch (Throwable e) {
            StringBuilder error = new StringBuilder();
            error.append("<h3>");
            error.append("Error: Failed to retrieve DICOM metadata from asset ");
            error.append(id != null ? id : cid);
            error.append("</h3><br/>");
            error.append("<pre>");
            error.append(e.getMessage());
            error.append("</pre>");
            response.setContent(error.toString(), "text/html");
            throw e;
        }
    }

    static XmlDoc.Element getDicomMetadata(HttpServer server, SessionKey sessionKey, String id, int idx, boolean defn)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", new String[] { "idx", String.valueOf(idx - 1) }, id);
        dm.add("defn", true);
        return server.execute(sessionKey, "dicom.metadata.get", dm.root());
    }

    private static String toXmlString(int idx, XmlDoc.Element dcmMetadata) throws Throwable {
        XmlStringWriter w = new XmlStringWriter();
        w.push("dicom", new String[] { "idx", String.valueOf(idx) });
        w.add(dcmMetadata, false);
        w.pop();
        return w.document();
    }

    static String toHtmlString(XmlDoc.Element dcmMetadata, boolean tableOnly) throws Throwable {
        if (dcmMetadata == null) {
            return null;
        }
        List<XmlDoc.Element> des = dcmMetadata.elements("de");
        StringBuilder sb = new StringBuilder();
        if (!tableOnly) {
            sb.append("<html>");
        }
        sb.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"1\">");
        sb.append("<thead><tr><th>Tag</th><th>VR</th><th>Definition</th><th>Value</th></tr></thead>");
        sb.append("<tbody>");
        if (des != null) {
            boolean odd = true;
            for (XmlDoc.Element de : des) {
                String bgColor = odd? "#ffffff":"#f1f0ee";
                sb.append("<tr>");
                sb.append("<td style=\"background-color:" + bgColor + ";\">").append(de.value("@grp"))
                        .append(",").append(de.value("@ele")).append("</td>");
                sb.append("<td style=\"background-color:" + bgColor + ";\">").append(de.value("@type")).append("</td>");
                sb.append("<td style=\"background-color:" + bgColor + ";\">").append(de.stringValue("defn", "&nbsp;")).append("</td>");
                sb.append("<td style=\"background-color:" + bgColor + ";\">").append(StringUtil.join(de.values("value"), ',')).append("</td>");
                sb.append("</tr>");
                odd = !odd;
            }
        }
        sb.append("</tbody>");
        sb.append("</table>");
        if (!tableOnly) {
            sb.append("</html>");
        }
        return sb.toString();
    }

    static String toTextString(XmlDoc.Element dcmMetadata) throws Throwable {
        if (dcmMetadata == null) {
            return null;
        }
        List<XmlDoc.Element> des = dcmMetadata.elements("de");
        StringBuilder sb = new StringBuilder();
        sb.append("Tag\t\tVR\tValue\t\t\tDefinition\n");
        if (des != null) {
            for (XmlDoc.Element de : des) {
                sb.append(de.value("@grp")).append(",").append(de.value("@ele")).append("\t\t");
                sb.append(de.stringValue("@type", "")).append("\t");
                sb.append(StringUtil.join(de.values("value"), ',')).append("\t\t\t");
                sb.append(de.stringValue("defn", "")).append("\n");
            }
        }
        return sb.toString();
    }
}
