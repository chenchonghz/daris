package nig.mf.plugin.pssd.servlets.modules;

import nig.mf.plugin.pssd.servlets.OutputFormat;
import nig.mf.plugin.pssd.servlets.ShoppingCartServlet;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDocMaker;

public class ShoppingCartOrderModule implements Module {

    public static final ShoppingCartOrderModule INSTANCE = new ShoppingCartOrderModule();

    public static final String NAME = ShoppingCartServlet.ModuleName.order.name();

    private ShoppingCartOrderModule() {
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey, HttpRequest request,
            HttpResponse response) throws Throwable {
        String sid = request.variableValue(ShoppingCartServlet.ARG_SID);
        order(server, sessionKey, sid);
        ShoppingCartDescribeModule.describe(server, sessionKey, sid, OutputFormat.html, request,
                response);
    }

    static void order(HttpServer server, SessionKey sessionKey, String sid) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", sid);
        server.execute(sessionKey, "shopping.cart.order", dm.root());
    }

    public static String urlFor(String sid, String sessionKey, String token) {
        return ShoppingCartServlet.urlFor(ShoppingCartServlet.ModuleName.order, sid, sessionKey,
                token, (String[]) null);
    }

    public static String urlFor(String sid) {
        return urlFor(sid, null, null);
    }

}
