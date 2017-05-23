package daris.util;

import java.net.URI;
import java.net.URL;

public class URLUtils {

    public static String encode(String str) throws Throwable {
        if (str == null || str.trim().isEmpty() || str.trim().equals('/')) {
            return str;
        }
        if (str.indexOf("://") != -1) {
            // full url
            URL url = new URL(str);
            return new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
                    url.getQuery(), url.getRef()).toASCIIString();
        } else {
            // path only
            return new URI(null, null, str, null, null).toASCIIString();
        }
    }

    public static void main(String[] args) throws Throwable {
        System.out.println(encode("/ttt9/www/Arcitecta Dots Grey.png"));
    }

}
