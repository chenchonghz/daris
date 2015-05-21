package nig.mf.plugin.pssd.servlets;

import arc.mf.plugin.http.HttpRequest;

public enum Disposition {
    attachment, inline;
    public static Disposition parse(String value) {
        if (value != null) {
            if (attachment.name().equalsIgnoreCase(value)) {
                return attachment;
            } else if (inline.name().equalsIgnoreCase(value)) {
                return inline;
            }
        }
        return null;
    }

    public static Disposition parse(HttpRequest request, Disposition defaultValue) {
        Disposition value = parse(request.variableValue(ObjectServlet.ARG_DISPOSITION));
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

}
