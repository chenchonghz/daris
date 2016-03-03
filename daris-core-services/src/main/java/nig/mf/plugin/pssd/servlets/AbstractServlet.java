package nig.mf.plugin.pssd.servlets;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.http.HttpRequest;
import arc.mf.plugin.http.HttpResponse;
import arc.mf.plugin.http.HttpResponse.Status;
import arc.mf.plugin.http.HttpServer;
import arc.mf.plugin.http.HttpServer.SessionKey;
import arc.mf.plugin.http.HttpServlet;
import arc.mf.plugin.http.HttpServletArguments;
import arc.xml.XmlDoc;
import arc.xml.XmlPrintStream;

public abstract class AbstractServlet extends HttpServlet {

    public static final String ROOT = "/daris";

    // private static final String MFLUX_PORTAL_URL_BASE = "/mflux/portal.mfjp";

    private static final String COOKIE_SKEY = "skey";

    public static final String ARG_SKEY = "_skey";

    public static final String ARG_TOKEN = "_token";

    public static final String CONTENT_UNKNOWN = "content/unknown";

    public static final Status STATUS_PARTIAL_CONTENT = new Status(206, "Partial Content");

    private static String sessionKey(HttpRequest request) {
        String skey = request.cookieValue(COOKIE_SKEY);
        if (skey != null) {
            return skey;
        } else {
            return request.variableValue(ARG_SKEY);
        }
    }

    private static String secureToken(HttpRequest request) {
        return request.variableValue(ARG_TOKEN);
    }

    public static String convertToIndentedText(XmlDoc.Element xe) throws Throwable {
        String text = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XmlPrintStream xos = new XmlPrintStream(new PrintStream(os));
        try {
            xos.println(xe);
            text = os.toString();
        } finally {
            os.close();
        }
        return text;
    }

    public static final String urlFor(String urlBase, Map<String, String> args) {
        StringBuilder sb = new StringBuilder();
        sb.append(urlBase);
        if (args != null) {
            Set<String> argNames = args.keySet();
            boolean first = true;
            for (String argName : argNames) {
                String argValue = args.get(argName);
                if (argName != null && argValue != null) {
                    if (first) {
                        sb.append("?");
                        first = false;
                    } else {
                        sb.append("&");
                    }
                    sb.append(argName + "=" + argValue);
                }
            }
        }
        return sb.toString();
    }

    public static final String urlFor(String urlBase, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(urlBase);
        if (args != null) {
            int size = args.length;
            if (size >= 2) {
                boolean first = true;
                for (int i = 0; i < size; i += 2) {
                    String argName = args[i];
                    String argValue = args[i + 1];
                    if (argName != null && argValue != null) {
                        if (first) {
                            sb.append('?');
                            first = false;
                        } else {
                            sb.append('&');
                        }
                        sb.append(argName + "=" + argValue);
                    }
                }
            }
        }
        return sb.toString();
    }

    private HttpServletArguments _args;

    AbstractServlet() {
        _args = new HttpServletArguments();
        _args.add("_skey", StringType.DEFAULT, "The session key if known.", 0);
        _args.add("_token", StringType.DEFAULT, "The secure identity token if known.", 0);
    }

    @Override
    public final HttpServletArguments arguments() {
        return _args;
    }

    @Override
    public final void execute(HttpServer server, HttpRequest request, HttpResponse response)
            throws Throwable {
        String skey = sessionKey(request);
        String token = secureToken(request);
        SessionKey sessionKey = null;
        if (skey == null && token == null) {
            // no session and token, redirect to logon page
            response.redirectTo(MainServlet.logonUrlFor(request.completeURL()));
            return;
        }
        if (skey != null) {
            if (server.sessionKeyValid(skey)) {
                sessionKey = new SessionKey(skey);
            } else {
                // session expired or invalid, redirect to logon page
                response.redirectTo(MainServlet.logonUrlFor(request.completeURL()));
                return;
            }
        } else {
            // token != null
            sessionKey = server.createSessionFromToken(request, path(), token);
            if (sessionKey == null || sessionKey.key() == null) {
                // failed create session from secure token
                throw new Exception("Failed to create session from _token: " + token);
            }
        }

        execute(server, sessionKey, request, response);

    }

    protected abstract void execute(HttpServer server, SessionKey sessionKey, HttpRequest request,
            HttpResponse response) throws Throwable;

    protected abstract String path();

}
