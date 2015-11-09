package nig.mf.plugin.pssd.servlets.modules;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlStringWriter;
import nig.mf.plugin.pssd.servlets.DicomServlet;

public class DicomMetadataGetModule implements Module {
    public static final DicomMetadataGetModule INSTANCE = new DicomMetadataGetModule();

    public static final String NAME = DicomServlet.ModuleName.metadata.name();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey,
            HttpRequest request, HttpResponse response) throws Throwable {
        // id
        String id = request.variableValue(DicomServlet.ARG_ID);
        // cid
        String cid = request.variableValue(DicomServlet.ARG_CID);
        if (id == null) {
            id = server
                    .execute(sessionKey, "asset.get",
                            "<cid>" + cid + "</cid>", null, null)
                    .value("asset/@id");
        }
        // idx
        String idxStr = request.variableValue(DicomServlet.ARG_IDX);
        long idx = idxStr == null ? 1 : Long.parseLong(idxStr);
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", new String[] { "idx", String.valueOf(idx) }, id);
        dm.add("defn", true);
        try {
            XmlDoc.Element re = server.execute(sessionKey, "dicom.metadata.get",
                    dm.root());
            XmlStringWriter w = new XmlStringWriter();
            w.push("dicom", new String[] { "idx", String.valueOf(idx - 1) });
            w.add(re, false);
            w.pop();
            response.setContent(w.document(), "text/xml");
        } catch (Throwable e) {
            StringBuilder error = new StringBuilder();
            error.append("<h3>");
            error.append(
                    "Error: Failed to retrieve DICOM metadata from asset ");
            error.append(id != null ? id : cid);
            error.append("</h3><br/>");
            error.append("<pre>");
            error.append(e.getMessage());
            error.append("</pre>");
            response.setContent(error.toString(), "text/html");
            throw e;
        }
    }
}
