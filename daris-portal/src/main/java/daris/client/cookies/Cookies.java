package daris.client.cookies;

import java.util.Date;

import arc.mf.client.util.ObjectUtil;

public class Cookies {

    private Cookies() {
    }

    public static final long MILLISECS_PER_DAY = 86400000L;

    public static void set(String name, String value, int days) {
        if (value == null) {
            com.google.gwt.user.client.Cookies.removeCookie(name);
            return;
        }
        String v = com.google.gwt.user.client.Cookies.getCookie(name);
        if (!ObjectUtil.equals(v, value)) {
            Date d = new Date();
            d.setTime(d.getTime() + days * MILLISECS_PER_DAY);
            com.google.gwt.user.client.Cookies.setCookie(name, value, d);
        }
    }

    public static String get(String name) {
        return com.google.gwt.user.client.Cookies.getCookie(name);
    }

    public static void remove(String name) {
        com.google.gwt.user.client.Cookies.removeCookie(name);
    }

}
