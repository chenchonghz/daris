package nig.mf.plugin.pssd.servlets.modules;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDocMaker;
import nig.mf.plugin.pssd.servlets.DicomServlet;
import nig.mf.plugin.pssd.servlets.Disposition;

public class DicomFileGetModule implements Module {

    public static final DicomFileGetModule INSTANCE = new DicomFileGetModule();

    public static final String NAME = DicomServlet.ModuleName.file.name();

    public static final String DICOM_FILE_MIME_TYPE = "application/dicom";

    public static final String DICOM_FILE_EXTENSION = "dcm";

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
        // idx
        String idxStr = request.variableValue(DicomServlet.ARG_IDX);
        long idx = idxStr == null ? 1 : Long.parseLong(idxStr);
        // disposition
        Disposition disposition = Disposition.parse(
                request.variableValue(DicomServlet.ARG_DISPOSITION),
                Disposition.attachment);
        // filename
        String fileName = request.variableValue(DicomServlet.ARG_FILENAME);

        if (id == null && cid == null) {
            throw new Exception("Missing id or cid argument");
        }
        if (id == null) {
            id = ServerUtils.idFromCid(server, sessionKey, cid);
        }
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", id);
        dm.add("idx", idx);
        try {
            response.setHeaderField("Content-Type", DICOM_FILE_MIME_TYPE);
            if (fileName == null) {
                fileName = String.format("%05d.%s", idx, DICOM_FILE_EXTENSION);
            }
            response.setHeaderField("Content-Disposition",
                    disposition.name() + "; filename=\"" + fileName + "\"");
            server.execute(sessionKey, "asset.archive.content.get", dm.root(),
                    (HttpRequest) null, response);
        } catch (Throwable e) {
            StringBuilder error = new StringBuilder();
            error.append("<h3>");
            error.append("Error: Failed to retrieve DICOM file from asset ");
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
