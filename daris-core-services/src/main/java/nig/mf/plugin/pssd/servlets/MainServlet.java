package nig.mf.plugin.pssd.servlets;

import nig.mf.plugin.pssd.servlets.modules.ObjectListModule;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.UrlType;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.mf.plugin.http.HttpServlet;
import arc.mf.plugin.http.HttpServletArguments;

public class MainServlet extends HttpServlet {
    public static final String APP = "daris";

    public static final String PATH = "main.mfjp";

    public static final String URL_BASE = AbstractServlet.ROOT + "/" + PATH;

    public static final String NAME = "daris.main";

    public static final String DESCRIPTION = "The entry point for logging on and off.";

    public static final String COOKIE_SKEY = "skey";

    public static final String ARG_MODULE = "module";

    public static final String ARG_DOMAIN = "mf.domain";

    public static final String ARG_USER = "mf.user";

    public static final String ARG_PASSWORD = "mf.password";

    public static final String ARG_SOURCE_URL = "source.url";

    public static enum ModuleName {

        logon, logoff;
        public static ModuleName parse(HttpRequest request, ModuleName defaultModuleName) {
            String name = request.variableValue(ARG_MODULE);
            ModuleName moduleName = parse(name);
            if (moduleName == null) {
                return defaultModuleName;
            }
            return moduleName;
        }

        public static ModuleName parse(String name) {
            if (name != null) {
                ModuleName[] vs = values();
                for (ModuleName v : vs) {
                    if (v.name().equalsIgnoreCase(name)) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    private HttpServletArguments _args;

    public MainServlet() {
        _args = new HttpServletArguments();
        _args.add(ARG_MODULE, new EnumType(ModuleName.values()),
                "The module to execute. Can be logon or logoff. Defaults to logon.", 0);
        _args.add(ARG_SOURCE_URL, UrlType.DEFAULT, "The source url.", 0);
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public String name() {
        return NAME;
    }


    static String logonUrlFor(String sourceUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append(URL_BASE);
        sb.append("?module=" + ModuleName.logon);
        if (sourceUrl != null) {
            sb.append("&" + ARG_SOURCE_URL + "=%22");
            sb.append(sourceUrl);
            sb.append("%22");
        }
        return sb.toString();
    }

    public static String logoffUrlFor(String source) {
        StringBuilder sb = new StringBuilder();
        sb.append(URL_BASE);
        sb.append("?module=" + ModuleName.logoff);
        if (source != null) {
            sb.append("&" + ARG_SOURCE_URL + "=%22");
            sb.append(source);
            sb.append("%22");
        }
        return sb.toString();
    }

    @Override
    public HttpServletArguments arguments() {
        return _args;
    }

    @Override
    public void execute(HttpServer server, HttpRequest request, HttpResponse response)
            throws Throwable {
        ModuleName moduleName = ModuleName.parse(request, ModuleName.logon);
        switch (moduleName) {
        case logon:
            logon(server, request, response);
            break;
        case logoff:
            logoff(server, request, response);
            break;
        default:
            break;
        }
    }

    public static void logon(HttpServer server, HttpRequest request, HttpResponse response)
            throws Throwable {
        String skey = request.cookieValue(COOKIE_SKEY);
        String sourceUrl = request.variableValue(ARG_SOURCE_URL);
        if (server.sessionKeyValid(skey)) {
            response.setCookieValue(COOKIE_SKEY, skey);
            response.redirectTo(sourceUrl != null ? sourceUrl : ObjectListModule.urlFor(null));
            return;
        }
        String domain = request.variableValue(ARG_DOMAIN);
        String user = request.variableValue(ARG_USER);
        String password = request.variableValue(ARG_PASSWORD);
        if (domain != null && user != null && password != null) {
            SessionKey sessionKey = server.createSession(request, APP, domain, user, password);
            if (sessionKey != null) {
                response.setCookieValue(COOKIE_SKEY, sessionKey.key());
                server.storeSessionData(ARG_DOMAIN, domain);
                server.storeSessionData(ARG_USER, user);
                response.redirectTo(sourceUrl != null ? sourceUrl : ObjectListModule.urlFor(null));
                return;
            }
        }

        String error = null;
        if (domain != null || user != null || password != null) {
            error = "Invalid credentials. Try again.";
        } else if (request.variableValue(COOKIE_SKEY) != null) {
            error = "Session expired.";
        }

        HtmlBuilder html = new HtmlBuilder(null);
        outputLogonHtml(domain, user, sourceUrl, error, html);
        response.setContent(html.buildHtml(), "text/html");
    }

    private static void outputLogonHtml(String domain, String user, String sourceUrl, String error,
            HtmlBuilder html) {
        html.setTitle("DaRIS logon");
        html.addStyle("div.logon {position:absolute; top:50%; left:50%; margin-top:-150px; margin-left: -200px; width:400px; height:300px; background-color:rgba(0,0,0,0.6);}");
        html.addStyle("tr:nth-child(even) {background: none}");
        html.addStyle("tr:nth-child(odd) {background: none}");
        html.addStyle("th {font-size:1em; color:#f0f0f0; line-height:1.5em; font-weight:bold;}");
        html.addStyle("td {font-size:1em; color:#f0f0f0; line-height:1.5em;}");
        html.addStyle("input {font-size:1em; line-height:1.5em;}");
        html.addStyle("button {font-size:1em; line-height:1.5em;}");
        html.appendContent("<div class=\"logon\"><br/>\n");
        html.appendContent("<form id=\"logon_form\" method=\"post\" action=\""
                + logonUrlFor((String) null) + "\" enctype=\"multipart/form-data\">\n");
        if (sourceUrl != null) {
            html.appendContent("<input type=\"hidden\" name=\"" + ARG_SOURCE_URL + "\" value=\""
                    + sourceUrl + "\"/>");
        }
        html.appendContent("<table align=\"center\">\n");
        html.appendContent("<thead>\n");
        html.appendContent("<tr><th colspan=\"2\" align=\"center\" style=\"text-align:center; font-size:1.5em;\">DaRIS</th></tr>");
        html.appendContent("</thead>\n");
        html.appendContent("<tbody>\n");
        html.appendContent("<tr><td colspan=\"2\">&nbsp;</td></tr>\n");
        html.appendContent("<tr><th align=\"right\">Domain:</th><td align=\"left\"><input type=\"text\" name=\""
                + ARG_DOMAIN + "\"");
        if (domain != null) {
            html.appendContent(" value=\"" + domain + "\"");
        }
        html.appendContent("/></td></tr>\n");
        html.appendContent("<tr><th align=\"right\">User:</th><td align=\"left\"><input type=\"text\" name=\""
                + ARG_USER + "\"");
        if (user != null) {
            html.appendContent(" value=\"" + user + "\"");
        }
        html.appendContent("/></td></tr>\n");
        html.appendContent("<tr><th align=\"right\">Password:</th><td align=\"left\"><input type=\"password\" name=\""
                + ARG_PASSWORD + "\"/></td></tr>\n");
        html.appendContent("<tr><td colspan=\"2\" align=\"center\" style=\"color:red; text-align:center; font-size:9pt;\">");
        html.appendContent(error == null ? "&nbsp;" : error);
        html.appendContent("</td></tr>\n");
        html.appendContent("<tr><td colspan=\"2\" align=\"center\">");
        html.appendContent("<button type=\"submit\" onclick=\"document.getElementById('logon_form').submit();\">Logon</button>");
        html.appendContent("</td></tr>\n");
        html.appendContent("</tbody>\n");
        html.appendContent("</table>\n");
        html.appendContent("</form>\n");
        html.appendContent("</div>\n");
    }

    public static void logoff(HttpServer server, HttpRequest request, HttpResponse response)
            throws Throwable {
        String skey = request.cookieValue(COOKIE_SKEY);
        String sourceUrl = request.variableValue(ARG_SOURCE_URL);
        server.destroySession(skey);
        server.clearSessionData();
        response.redirectTo(sourceUrl != null ? logonUrlFor(sourceUrl) : request.completeURL());
    }

}
