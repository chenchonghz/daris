package nig.mf.plugin.pssd.servlets.modules;

import nig.mf.plugin.pssd.servlets.HtmlBuilder;
import nig.mf.plugin.pssd.servlets.ShoppingCartServlet;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDocMaker;

public class ShoppingCartDestroyModule implements Module {

    public static final ShoppingCartDestroyModule INSTANCE = new ShoppingCartDestroyModule();

    public static final String NAME = ShoppingCartServlet.ModuleName.destroy.name();

    private ShoppingCartDestroyModule() {
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey, HttpRequest request,
            HttpResponse response) throws Throwable {
        String sid = request.variableValue(ShoppingCartServlet.ARG_SID);
        destroy(server, sessionKey, sid);
        HtmlBuilder html = new HtmlBuilder(ObjectListModule.urlFor(null));
        html.setTitle("DaRIS: deleted shopping-cart " + sid + "");
        html.addNavItem("DaRIS", ObjectListModule.urlFor(null));
        html.addTabItem("Shopping-cart " + sid, null);
        html.appendContent("<div id=\"content_div\" width=\"100%\" height=\"100%\" style=\"background-color:#eee;\">\n");
        html.appendContent("<br/><br/><br/><br/><br/><p align=\"center\" style=\"margin:0px; padding:20px;\">Shopping-cart "
                + sid
                + " has been deleted. Go back to <a href=\""
                + ObjectListModule.urlFor(null)
                + "\">DaRIS</a></p><br/><br/><br/><br/><br/>");

        html.appendContent("</div>");
        response.setContent(html.buildHtml(), "text/html");
    }

    static void destroy(HttpServer server, SessionKey sessionKey, String sid) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", sid);
        server.execute(sessionKey, "shopping.cart.destroy", dm.root());
    }

    public static String urlFor(String sid, String sessionKey, String token) {
        return ShoppingCartServlet.urlFor(ShoppingCartServlet.ModuleName.destroy, sid, sessionKey,
                token, (String[]) null);
    }

    public static String urlFor(String sid) {
        return urlFor(sid, null, null);
    }

}
