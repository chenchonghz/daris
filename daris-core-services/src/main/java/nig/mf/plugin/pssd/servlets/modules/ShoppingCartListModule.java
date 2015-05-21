package nig.mf.plugin.pssd.servlets.modules;

import java.util.List;

import nig.mf.plugin.pssd.servlets.HtmlBuilder;
import nig.mf.plugin.pssd.servlets.OutputFormat;
import nig.mf.plugin.pssd.servlets.ShoppingCartServlet;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlStringWriter;

public class ShoppingCartListModule implements Module {

    public static final ShoppingCartListModule INSTANCE = new ShoppingCartListModule();

    public static final String NAME = ShoppingCartServlet.ModuleName.list.name();

    private ShoppingCartListModule() {
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey, HttpRequest request,
            HttpResponse response) throws Throwable {
        OutputFormat format = OutputFormat.parse(request, OutputFormat.html);
        list(server, sessionKey, format, request, response);
    }

    public static void list(HttpServer server, SessionKey sessionKey, OutputFormat format,
            HttpRequest request, HttpResponse response) throws Throwable {
        List<XmlDoc.Element> ces = list(server, sessionKey);
        if (format == OutputFormat.html) {
            HtmlBuilder html = new HtmlBuilder(urlFor(null, null));
            outputHtml(ces, html);
            response.setContent(html.buildHtml(), "text/html");
        } else if (format == OutputFormat.xml) {
            XmlStringWriter w = new XmlStringWriter();
            w.push("result");
            if (ces != null) {
                for (XmlDoc.Element ce : ces) {
                    w.add(ce);
                }
            }
            w.pop();
            response.setContent(w.document(), "text/xml");
        }
    }

    private static void outputHtml(List<XmlDoc.Element> ces, HtmlBuilder html) throws Throwable {
        html.setTitle("DaRIS: Shopping-carts");

        // nav items
        html.addNavItem("DaRIS", ObjectListModule.urlFor(null));
        html.addNavItem("Shopping-carts", ShoppingCartListModule.urlFor(null, null));

        // tabs
        html.addTabItem("Shopping-carts", null);

        html.appendContent("<div id=\"content_div\" width=\"100%\" height=\"100%\" style=\"background-color:#eee;\">\n");
        if (ces == null || ces.isEmpty()) {
            html.appendContent("<br/><br/><br/><br/><br/><p align=\"center\" style=\"margin:0px; padding:20px;\">No shopping-cart found.</p><br/><br/><br/><br/><br/>");
            return;
        }
        html.appendContent("<table width=\"100%\">\n");
        html.appendContent("<thead><tr class=\"head\"><th width=\"80px\">id</th><th width=\"150px\">status</th><th width=\"200px\">name</th><th>description</th><th width=\"100px\">action</th></thead>\n");
        html.appendContent("<tbody>\n");
        for (XmlDoc.Element ce : ces) {
            html.appendContent("<tr>\n");
            String sid = ce.value("@id");
            String status = ce.value("@status");
            String name = ce.value("@name");
            String description = ce.value("@description");
            // sid
            html.appendContent("  <td align=\"center\">" + sid + "</td>\n");
            // status
            html.appendContent("  <td align=\"center\">" + status + "</td>\n");
            // name
            html.appendContent("  <td>" + name + "</td>\n");
            // description
            html.appendContent("  <td>" + description + "</td>\n");
            html.appendContent("  <td align=\"center\">\n");
            html.appendContent("<button onclick=\"window.location.href='"
                    + ShoppingCartDescribeModule.urlFor(sid) + "';\">view</button>");
            html.appendContent("</td>\n");
            html.appendContent("</tr>\n");
        }
        html.appendContent("</tbody>\n");
        html.appendContent("</table>\n");
        html.appendContent("</div>");

    }

    static List<XmlDoc.Element> list(HttpServer server, SessionKey sessionKey) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("size", "infinity");
        return server.execute(sessionKey, "shopping.cart.list", dm.root()).elements("cart");
    }

    static String urlFor(String sessionKey, String token) {
        return ShoppingCartServlet.urlFor(ShoppingCartServlet.ModuleName.list, (String) null,
                sessionKey, token, (String[]) null);
    }
}
