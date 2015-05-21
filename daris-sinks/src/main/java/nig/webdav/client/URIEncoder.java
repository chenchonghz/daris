package nig.webdav.client;

import java.net.URI;
import java.net.URL;

public class URIEncoder {

    public static String encode(String url) throws Exception {
        URL urlObj = new URL(url);
        URI uriObj = new URI(urlObj.getProtocol(), urlObj.getUserInfo(), urlObj.getHost(), urlObj.getPort(), urlObj.getPath(),
                urlObj.getQuery(), urlObj.getRef());
        return uriObj.toString();
    }

}
