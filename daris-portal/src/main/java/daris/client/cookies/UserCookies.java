package daris.client.cookies;

public class UserCookies {

    private UserCookies() {
    }

    private static final String DOMAIN = "mflux.domain";
    private static final int EXPIRE_DAYS = 30;
    private static final String USER = "mflux.user";

    public static void setDomain(String domain) {
        Cookies.set(DOMAIN, domain, EXPIRE_DAYS);
    }

    public static String domain() {
        return Cookies.get(DOMAIN);
    }

    public static void setUser(String user) {
        Cookies.set(USER, user, EXPIRE_DAYS);
    }

    public static String user() {
        return Cookies.get(USER);
    }

}
