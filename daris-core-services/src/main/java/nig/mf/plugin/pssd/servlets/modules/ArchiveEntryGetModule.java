package nig.mf.plugin.pssd.servlets.modules;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDocMaker;
import nig.mf.plugin.pssd.services.SvcArchiveContentGet;
import nig.mf.plugin.pssd.services.SvcArchiveContentList;
import nig.mf.plugin.pssd.servlets.ArchiveServlet;
import nig.mf.plugin.pssd.servlets.Disposition;

public class ArchiveEntryGetModule implements Module {

    public static final ArchiveEntryGetModule INSTANCE = new ArchiveEntryGetModule();

    public static final String NAME = ArchiveServlet.ModuleName.eget.name();

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

        XmlDocMaker dm = new XmlDocMaker("args");
        if (id != null) {
            dm.add("id", id);
        } else {
            dm.add("cid", cid);
        }
        dm.add("idx", idx);
        String entryFileName = getEntryFileName(server, sessionKey, id, cid,
                idx);
        String mimeType = getMimeTypeFromName(server, sessionKey,
                entryFileName);
        response.setHeaderField("Content-Type",
                mimeType == null ? "content/unknown" : mimeType);
        response.setHeaderField("Content-Disposition",
                disposition.name() + "; filename=\"" + (fileName == null
                        ? entryFileName : fileName) + "\"");
        server.execute(sessionKey, SvcArchiveContentGet.SERVICE_NAME, dm.root(),
                (HttpRequest) null, response);
    }

    static String getEntryFileName(HttpServer server, SessionKey sessionKey,
            String id, String cid, long idx) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        if (id != null) {
            dm.add("id", id);
        } else {
            dm.add("cid", cid);
        }
        dm.add("idx", idx);
        dm.add("size", 1);
        String name = server.execute(sessionKey,
                SvcArchiveContentList.SERVICE_NAME, dm.root(), null, null)
                .value("entry");
        int i2 = name.lastIndexOf('/');
        if (i2 >= 0) {
            return name.substring(i2);
        } else {
            return name;
        }
    }

    static String getMimeTypeFromName(HttpServer server, SessionKey sessionKey,
            String name) throws Throwable {
        String ext = getFileExtension(name);
        if (ext != null) {
            return getMimeTypeFromExtension(server, sessionKey, ext);
        }
        return null;
    }

    static String getMimeTypeFromExtension(HttpServer server,
            SessionKey sessionKey, String extension) throws Throwable {
        return server
                .execute(sessionKey, "type.ext.types",
                        "<extension>" + extension + "</extension>", null, null)
                .value("extension/type");
    }

    static String getFileExtension(String fileName) {
        if (fileName != null) {
            int idx = fileName.lastIndexOf('.');
            if (idx >= 0) {
                String ext = fileName.substring(idx + 1);
                if (ext != null && !ext.isEmpty()) {
                    return ext;
                }
            }
        }
        return null;
    }
}
