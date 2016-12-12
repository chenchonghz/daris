package nig.mf.plugin.pssd.servlets.modules;

import java.util.AbstractMap.SimpleEntry;

import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
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

        if (id == null && cid == null) {
            throw new Exception("Missing id or cid argument");
        }
        if (id == null) {
            id = ServerUtils.idFromCid(server, sessionKey, cid);
        }
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", id);
        dm.add("idx", idx);
        SimpleEntry<String, Long> entryInfo = getArchiveEntryInfo(server,
                sessionKey, id, idx);
        String entryFileName = extractFileName(entryInfo.getKey());
        String mimeType = getMimeTypeFromName(server, sessionKey,
                entryFileName);
        response.setHeaderField("Content-Type",
                mimeType == null ? "content/unknown" : mimeType);
        response.setHeaderField("Content-Disposition",
                disposition.name() + "; filename=\""
                        + (fileName == null ? entryFileName : fileName) + "\"");
        server.execute(sessionKey, "asset.archive.content.get", dm.root(),
                (HttpRequest) null, response);
    }

    public static SimpleEntry<String, Long> getArchiveEntryInfo(
            HttpServer server, SessionKey sessionKey, String id, long idx)
                    throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("id", id);
        dm.add("idx", idx);
        dm.add("size", 1);
        XmlDoc.Element ee = server.execute(sessionKey,
                "asset.archive.content.list", dm.root(), null, null)
                .element("entry");
        String name = ee.value();
        Long size = ee.longValue("size", -1);
        SimpleEntry<String, Long> entry = new SimpleEntry<String, Long>(name,
                size);
        return entry;
    }

    public static String extractFileName(String path) {
        int i = path.lastIndexOf('/');
        if (i >= 0) {
            return path.substring(i);
        } else {
            return path;
        }
    }

    private static String getMimeTypeFromName(HttpServer server,
            SessionKey sessionKey, String name) throws Throwable {
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
