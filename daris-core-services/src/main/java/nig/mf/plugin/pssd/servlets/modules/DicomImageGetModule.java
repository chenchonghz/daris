package nig.mf.plugin.pssd.servlets.modules;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDocMaker;
import nig.mf.plugin.pssd.servlets.DicomServlet;
import nig.mf.plugin.pssd.servlets.Disposition;

public class DicomImageGetModule implements Module {

    public static final DicomImageGetModule INSTANCE = new DicomImageGetModule();

    public static final String NAME = DicomServlet.ModuleName.image.name();

    public static final String PNG_MIME_TYPE = "image/png";

    public static final String PNG_FILE_EXTENSION = "png";

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
            id = ServerUtils.idFromCid(server, sessionKey, cid);
        }
        // idx
        String idxStr = request.variableValue(DicomServlet.ARG_IDX);
        long idx = idxStr == null ? 1 : Long.parseLong(idxStr);
        // frame
        String frameStr = request.variableValue(DicomServlet.ARG_FRAME);
        long frame = frameStr == null ? 1 : Long.parseLong(frameStr);
        // disposition
        Disposition disposition = Disposition.parse(
                request.variableValue(DicomServlet.ARG_DISPOSITION),
                Disposition.attachment);
        // filename
        String fileName = request.variableValue(DicomServlet.ARG_FILENAME);
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", new String[] { "idx", String.valueOf(idx - 1), "frame",
                String.valueOf(frame - 1) }, id);
        dm.add("lossless", true);

        try {
            response.setHeaderField("Content-Type", PNG_MIME_TYPE);
            if (fileName == null) {
                fileName = String.format("%05d.%s", idx, PNG_FILE_EXTENSION);
            }
            response.setHeaderField("Content-Disposition",
                    disposition.name() + "; filename=\"" + fileName + "\"");
            server.execute(sessionKey, "dicom.image.get", dm.root(),
                    (HttpRequest) null, response);
        } catch (Throwable e) {
            StringBuilder error = new StringBuilder();
            error.append("<h3>");
            error.append("Error: Failed to retrieve DICOM image from asset ");
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
