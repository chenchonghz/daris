package daris.client.util;

import com.google.gwt.user.client.Window.Navigator;

public class BrowserUtil {

    public static boolean isIE() {
        String ua = Navigator.getUserAgent().toLowerCase();
        return ua.indexOf("msie") != -1;
    }

    public static boolean isChromeFrame() {
        String ua = Navigator.getUserAgent().toLowerCase();
        return ua.indexOf("chromeframe") != -1;
    }

    public static native String versionOfIE()/*-{
        return navigator.appVersion.match(/MSIE ([\d.]+)/)[1];
    }-*/;

}
