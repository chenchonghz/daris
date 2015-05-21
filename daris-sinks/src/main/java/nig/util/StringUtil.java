package nig.util;

public class StringUtil {

    public static String trimLeading(String str, String trim, boolean all) {
        if (str.startsWith(trim)) {
            str = str.substring(trim.length());
        }
        if (all) {
            while (str.startsWith(trim)) {
                str = str.substring(trim.length());
            }
        }
        return str;
    }

    public static String trimTrailing(String str, String trim, boolean all) {
        if (str.endsWith(trim)) {
            str = str.substring(0, str.length() - trim.length());
        }
        if (all) {
            while (str.endsWith(trim)) {
                str = str.substring(0, str.length() - trim.length());
            }
        }
        return str;
    }

}
