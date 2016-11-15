package nig.mf.plugin.pssd.servlets.modules;

import java.util.AbstractMap.SimpleEntry;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDocMaker;
import nig.mf.plugin.pssd.services.SvcArchiveContentImageGet;
import nig.mf.plugin.pssd.servlets.ArchiveServlet;
import nig.mf.plugin.pssd.servlets.Disposition;

public class ArchiveEntryImageGetModule implements Module {

    public static final ArchiveEntryImageGetModule INSTANCE = new ArchiveEntryImageGetModule();

    public static final String NAME = ArchiveServlet.ModuleName.iget.name();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey,
            HttpRequest request, HttpResponse response) throws Throwable {
        // id
        String id = request.variableValue(ArchiveServlet.ARG_ID);
        // cid
        String cid = request.variableValue(ArchiveServlet.ARG_CID);
        // idx
        String idxStr = request.variableValue(ArchiveServlet.ARG_IDX);
        if (idxStr == null) {
            throw new Exception("Missing idx argument.");
        }
        long idx = idxStr == null ? 1 : Long.parseLong(idxStr);
        // disposition
        Disposition disposition = Disposition.parse(
                request.variableValue(ArchiveServlet.ARG_DISPOSITION),
                Disposition.attachment);
        // filename
        String fileName = request.variableValue(ArchiveServlet.ARG_FILENAME);

        if (id == null && cid == null) {
            throw new Exception("Missing id or cid argument");
        }
        if (id == null) {
            id = ServerUtils.idFromCid(server, sessionKey, cid);
        }

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", id);
        dm.add("idx", idx);
        SimpleEntry<String, Long> entryInfo = ArchiveEntryGetModule
                .getArchiveEntryInfo(server, sessionKey, id, idx);
        String entryFileName = ArchiveEntryGetModule
                .extractFileName(entryInfo.getKey());
        String outputMimeType = getOutputMimeType(server, sessionKey,
                entryFileName);
        String outputFileName = getOutputFileName(
                fileName != null ? fileName : entryFileName);
        response.setHeaderField("Content-Type",
                outputMimeType == null ? "content/unknown" : outputMimeType);
        response.setHeaderField("Content-Disposition",
                disposition.name() + "; filename=\"" + outputFileName + "\"");
        server.execute(sessionKey, SvcArchiveContentImageGet.SERVICE_NAME,
                dm.root(), (HttpRequest) null, response);

    }

    private static String getOutputFileName(String fileName) {
        String ext = ArchiveEntryGetModule.getFileExtension(fileName);
        if ("jpg".equalsIgnoreCase(ext) || "png".equalsIgnoreCase(ext)
                || "jpeg".equalsIgnoreCase(ext)
                || "gif".equalsIgnoreCase(ext)) {
            return fileName;
        } else {
            return fileName + ".png";
        }
    }

    private static String getOutputMimeType(HttpServer server,
            SessionKey sessionKey, String fileName) throws Throwable {
        String ext = ArchiveEntryGetModule.getFileExtension(fileName);
        if ("jpg".equalsIgnoreCase(ext) || "png".equalsIgnoreCase(ext)
                || "jpeg".equalsIgnoreCase(ext)
                || "gif".equalsIgnoreCase(ext)) {
            return ArchiveEntryGetModule.getMimeTypeFromExtension(server,
                    sessionKey, ext);
        } else {
            return "image/png";
        }
    }
}
