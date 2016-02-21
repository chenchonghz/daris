package nig.mf.plugin.pssd.servlets;

import arc.mf.plugin.http.HttpRequest;

public enum OutputFormat {
    xml, html, text;

    public static OutputFormat parse(String s) {
        if (s != null) {
            if (html.name().equalsIgnoreCase(s)) {
                return html;
            } else if (xml.name().equalsIgnoreCase(s)) {
                return xml;
            }
        }
        return null;
    }

    public static OutputFormat parse(HttpRequest request, OutputFormat defaultValue) {
        OutputFormat format = null;
        if (request != null) {
            format = parse(request.variableValue(ObjectServlet.ARG_FORMAT));
        }
        if (format == null) {
            return defaultValue;
        } else {
            return format;
        }
    }
}
