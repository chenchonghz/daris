package nig.mf.plugin.pssd.servlets.modules;

import java.util.List;

import nig.mf.plugin.pssd.sc.Status;
import nig.mf.plugin.pssd.servlets.HtmlBuilder;
import nig.mf.plugin.pssd.servlets.OutputFormat;
import nig.mf.plugin.pssd.servlets.ShoppingCartServlet;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlStringWriter;

public class ShoppingCartDescribeModule implements Module {

    public static final ShoppingCartDescribeModule INSTANCE = new ShoppingCartDescribeModule();

    public static final String NAME = ShoppingCartServlet.ModuleName.describe.name();

    private ShoppingCartDescribeModule() {
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void execute(HttpServer server, SessionKey sessionKey, HttpRequest request,
            HttpResponse response) throws Throwable {
        String sid = request.variableValue(ShoppingCartServlet.ARG_SID);
        OutputFormat format = OutputFormat.parse(request, OutputFormat.html);
        describe(server, sessionKey, sid, format, request, response);
    }

    static void describe(HttpServer server, SessionKey sessionKey, String sid, OutputFormat format,
            HttpRequest request, HttpResponse response) throws Throwable {
        XmlDoc.Element ce = describe(server, sessionKey, sid);
        if (format == OutputFormat.html) {
            HtmlBuilder html = new HtmlBuilder(urlFor(sid));
            outputHtml(sessionKey, ce, html);
            response.setContent(html.buildHtml(), "text/html");
        } else {
            XmlStringWriter w = new XmlStringWriter();
            outputXml(sessionKey, ce, w);
            response.setContent(w.document(), "text/xml");
        }
    }

    static XmlDoc.Element describe(HttpServer server, SessionKey sessionKey, String sid)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("sid", sid);
        XmlDoc.Element ce = server.execute(sessionKey, "shopping.cart.describe", dm.root())
                .element("cart");
        if (ce != null) {
            Status status = Status.fromString(ce.value("status"));
            if (status == Status.processing) {
                XmlDoc.Element pe = server.execute(sessionKey, "shopping.cart.processing.describe",
                        dm.root()).element("process");
                if (pe != null) {
                    ce.add(pe);
                }
            }
        }
        return ce;
    }

    private static void outputHtml(SessionKey sessionKey, Element ce, HtmlBuilder html)
            throws Throwable {

        String sid = ce.value("@id");
        Status status = Status.fromString(ce.value("status"));
        html.setTitle("DaRIS shopping-cart " + sid);
        if (status == Status.assigned || status == Status.await_processing
                || status == Status.editable || status == Status.fulfilled
                || status == Status.processing) {
            // if the cart is in a non-static state, set up a timer to reload the page.
            // reload the page every 10 seconds..
            html.prependToHead(" <meta http-equiv=\"refresh\" content=\"10\"/>");
        }
        if (status == Status.data_ready) {
            html.prependToHead("<meta http-equiv=\"refresh\" content=\"5;url="
                    + ShoppingCartDownloadModule.urlFor(sid) + "\">");
        }

        html.addStyle("tr:nth-child(even) {background:#eee;}");
        html.addStyle("tr:nth-child(odd) {background:#eee;}");
        html.addStyle("th {font-size:1em; line-height:1.5em; font-weight:bold;}");
        html.addStyle("td {font-size:1em; line-height:1.5em;}");
        html.addStyle("input {font-size:1em; line-height:1.5em; width:100px;}");

        html.addNavItem("DaRIS", ObjectListModule.urlFor(null));
        html.addNavItem("Shopping-carts", ShoppingCartListModule.urlFor(null, null));
        html.addNavItem("Shopping-cart " + sid, null);

        html.addTabItem("Shopping-cart " + sid, null);
        

        // content
        html.appendContent("<div id=\"content_div\" width=\"100%\" height=\"100%\" style=\"background-color:#eee;\">\n");
        html.appendContent("<table width=\"100%\" style=\"padding-top:20px;\">\n");
        html.appendContent("<tbody>\n");
        html.appendContent("<tr><th align=\"right\" width=\"50%\">Cart ID:</th><td align=\"left\" width=\"50%\">"
                + sid + "</td></tr>\n");
        String cartName = ce.value("name");
        if (cartName != null) {
            html.appendContent("<tr><th align=\"right\" width=\"50%\">Name:</th><td align=\"left\" width=\"50%\">"
                    + cartName + "</td></tr>\n");
        }
        String cartDescription = ce.value("description");
        if (cartDescription != null) {
            html.appendContent("<tr><th align=\"right\" width=\"50%\">Description:</th><td align=\"left\" width=\"50%\">"
                    + cartDescription + "</td></tr>\n");
        }
        html.appendContent("<tr><th align=\"right\" width=\"50%\">Status:</th><td align=\"left\" width=\"50%\">"
                + status + "</td></tr>\n");
        String itemCount = ce.value("content-statistics/item-count");
        if (itemCount != null) {
            html.appendContent("<tr><th align=\"right\" width=\"50%\">Item Count:</th><td align=\"left\" width=\"50%\">"
                    + itemCount + "</td></tr>\n");
        }
        String itemSize = ce.value("content-statistics/item-size");
        if (itemSize != null) {
            html.appendContent("<tr><th align=\"right\" width=\"50%\">Content Size:</th><td align=\"left\" width=\"50%\">"
                    + itemSize + " bytes</td></tr>\n");
        }
        List<XmlDoc.Element> mtes = ce.elements("content-statistics/content-mimetype/name");
        if (mtes != null && !mtes.isEmpty()) {
            for (XmlDoc.Element mte : mtes) {
                String mimeType = mte.value();
                int mimeTypeCount = mte.intValue("@count", 0);
                html.appendContent("<tr><th align=\"right\" width=\"50%\">MIME Type:</th><td align=\"left\" width=\"50%\">"
                        + mimeType + "(" + mimeTypeCount + ")</td></tr>\n");
            }
        }
        List<XmlDoc.Element> tes = ce.elements("data-transformation/transcode");
        if (tes != null && !tes.isEmpty()) {
            for (XmlDoc.Element te : tes) {
                String from = te.value("from");
                String to = te.value("to");
                if (from != null && to != null && !"none".equals(to)) {
                    html.appendContent("<tr><th align=\"right\" width=\"50%\">Transcode:</th><td align=\"left\" width=\"50%\">"
                            + from + " =&gt; " + to + "</td></tr>\n");
                }
            }
        }
        html.appendContent("<tr><th align=\"right\" width=\"50%\">Archive File Format:</th><td align=\"left\" width=\"50%\">"
                + ce.value("packaging") + "</td></tr>\n");
        XmlDoc.Element pe = ce.element("process");
        if (pe != null) {
            String started = pe.value("start-time");
            if (started != null) {
                html.appendContent("<tr><th align=\"right\" width=\"50%\">Started:</th><td align=\"left\" width=\"50%\">"
                        + started + "</td></tr>\n");
            }
            String duration = ce.value("duration");
            if (duration != null) {
                html.appendContent("<tr><th align=\"right\" width=\"50%\">Duration:</th><td align=\"left\" width=\"50%\">"
                        + duration + " " + ce.value("duration/@units") + "</td></tr>\n");
            }
            String completed = pe.value("completed");
            String total = pe.value("total");
            html.appendContent("<tr><th align=\"right\" width=\"50%\">Completed/Total:</th><td align=\"left\" width=\"50%\">"
                    + completed + "/" + total + "</td></tr>\n");
        }
        html.appendContent("<tr><td align=\"center\" colspan=\"2\">&nbsp;</td></tr>");
        html.appendContent("<tr><td align=\"center\" colspan=\"2\"><form id=\"module_form\" method=\"get\" action=\""
                + ShoppingCartServlet.URL_BASE + "\">\n");
        html.appendContent("<input type=\"hidden\" name=\"" + ShoppingCartServlet.ARG_SID
                + "\" value=\"" + sid + "\"/>\n");
        html.appendContent("<input type=\"hidden\" name=\"" + ShoppingCartServlet.ARG_MODULE
                + "\" id=\"module_input\"/>\n");
        if (status == Status.editable && ce.longValue("content-statistics/item-size", 0) > 0) {
            html.appendContent("<input type=\"button\" value=\"Order\" onclick=\"document.getElementById('module_input').value='"
                    + ShoppingCartServlet.ModuleName.order
                    + "'; document.getElementById('module_form').submit();\"/>\n");
        }
        if (status == Status.processing) {
            html.appendContent("<input type=\"button\" value=\"Abort\" onclick=\"document.getElementById('module_input').value='"
                    + ShoppingCartServlet.ModuleName.abort
                    + "'; document.getElementById('module_form').submit();\"/>\n");
        }
        if (status == Status.data_ready) {
            html.appendContent("<input type=\"button\" value=\"Download\" onclick=\"document.getElementById('module_input').value='"
                    + ShoppingCartServlet.ModuleName.download
                    + "'; document.getElementById('module_form').submit();\"/>\n");
        }
        if (status == Status.editable || status == Status.aborted || status == Status.data_ready
                || status == Status.error || status == Status.rejected
                || status == Status.withdrawn) {
            html.appendContent("<input type=\"button\" value=\"Destroy\" onclick=\"document.getElementById('module_input').value='"
                    + ShoppingCartServlet.ModuleName.destroy
                    + "'; document.getElementById('module_form').submit();\"/>\n");

        }
        html.appendContent("</td></tr>\n");

        html.appendContent("</tbody>\n");
        html.appendContent("</table>\n");
        html.appendContent("</div>\n");

    }

    private static void outputXml(SessionKey sessionKey, Element ce, XmlStringWriter w)
            throws Throwable {
        if (ce != null) {
            w.add(ce);
        }
    }

    public static String urlFor(String sid, String sessionKey, String token) {
        return ShoppingCartServlet.urlFor(ShoppingCartServlet.ModuleName.describe, sid, sessionKey,
                token, (String[]) null);
    }

    public static String urlFor(String sid) {
        return urlFor(sid, null, null);
    }
}
