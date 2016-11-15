package nig.mf.plugin.pssd.servlets.modules;

import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;

public class ServerUtils {

    public static String cidFromId(HttpServer server, SessionKey sessionKey,
            String id) throws Throwable {
        return assetMetaFromId(server, sessionKey, id).value("cid");
    }

    public static String idFromCid(HttpServer server, SessionKey sessionKey,
            String cid) throws Throwable {
        return assetMetaFromCid(server, sessionKey, cid).value("@id");
    }

    public static XmlDoc.Element assetMetaFromId(HttpServer server,
            SessionKey sessionKey, String id) throws Throwable {
        return server.execute(sessionKey, "asset.get", "<id>" + id + "</id>",
                null, null).element("asset");
    }

    public static XmlDoc.Element assetMetaFromCid(HttpServer server,
            SessionKey sessionKey, String cid) throws Throwable {
        return server.execute(sessionKey, "asset.get", "<cid>" + cid + "</cid>",
                null, null).element("asset");
    }

    static String getMimeTypeFromExtension(HttpServer server,
            SessionKey sessionKey, String extension) throws Throwable {
        return server
                .execute(sessionKey, "type.ext.types",
                        "<extension>" + extension + "</extension>", null, null)
                .value("extension/type");
    }

}
