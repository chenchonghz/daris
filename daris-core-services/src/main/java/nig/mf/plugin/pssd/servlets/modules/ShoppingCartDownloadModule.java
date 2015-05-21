package nig.mf.plugin.pssd.servlets.modules;

import nig.mf.plugin.pssd.servlets.AbstractServlet;
import nig.mf.plugin.pssd.servlets.Disposition;
import nig.mf.plugin.pssd.servlets.ObjectServlet;
import nig.mf.plugin.pssd.servlets.ShoppingCartServlet;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class ShoppingCartDownloadModule implements Module {

    public static final ShoppingCartDownloadModule INSTANCE = new ShoppingCartDownloadModule();

    public static final String NAME = ShoppingCartServlet.ModuleName.download.name();

    private ShoppingCartDownloadModule() {
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey, HttpRequest request,
            HttpResponse response) throws Throwable {
        String sid = request.variableValue(ShoppingCartServlet.ARG_SID);
        Disposition disposition = Disposition.parse(request, Disposition.attachment);
        String filename = request.variableValue(ObjectServlet.ARG_FILENAME);
        XmlDoc.Element ce = ShoppingCartDescribeModule.describe(server, sessionKey, sid);
        String ext = ce.value("packaging");
        String archiveType = null;
        if (ext != null) {
            archiveType = typeFromExt(server, sessionKey, ext);
        }
        download(server, sessionKey, sid, disposition, filename, ext, archiveType, response);
    }

    static void download(HttpServer server, SessionKey sessionKey, String sid,
            Disposition disposition, String filename, String ext, String archiveType,
            HttpResponse response) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", sid);
        XmlDoc.Element ce = server.execute(sessionKey, "shopping.cart.describe", dm.root())
                .element("cart");
        server.execute(sessionKey, "shopping.cart.output.retrieve", dm.root(), (HttpRequest) null,
                response);
        if (archiveType != null) {
            response.setHeaderField("Content-Type", archiveType);
        }
        if (response.contentType() == null) {
            response.setHeaderField("Content-Type", AbstractServlet.CONTENT_UNKNOWN);
        }
        if (filename == null) {
            String cartName = ce.value("name");
            if (cartName != null) {
                filename = cartName;
            } else {
                filename = "shoppingcart_" + sid;
            }
        }
        if (ext != null && !filename.endsWith(ext)) {
            filename = filename + "." + ext;
        }
        response.setHeaderField("Content-Disposition", disposition.name() + "; filename=\""
                + filename + "\"");
    }

    private static String typeFromExt(HttpServer server, SessionKey sessionKey, String ext)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("extension", ext);
        return server.execute(sessionKey, "type.ext.types", dm.root()).value("extension/type");
    }

    public static String urlFor(String sid, String sessionKey, String token) {
        return ShoppingCartServlet.urlFor(ShoppingCartServlet.ModuleName.download, sid, sessionKey,
                token, (String[]) null);
    }

    public static String urlFor(String sid) {
        return urlFor(sid, null, null);
    }

    public static String completeUrlFor(String hostAddr, SessionKey sessionKey, String sid) {
        return hostAddr + urlFor(sid);
    }

}
