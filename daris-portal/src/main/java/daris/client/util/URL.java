package daris.client.util;

import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.Document;

public class URL {

    public static String getHost(String url) {
        return getProperty(url, "host");
    }

    public static String getProtocol(String url) {
        return getProperty(url, "protocol");
    }

    public static String getPath(String url) {
        return getProperty(url, "pathname");
    }

    private static String getProperty(String url, String property) {
        AnchorElement anchor = Document.get().createAnchorElement();
        anchor.setHref(url);
        String value = anchor.getPropertyString(property);
        anchor.removeFromParent();
        return value;
    }

}
