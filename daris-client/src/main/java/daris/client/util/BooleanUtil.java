package daris.client.util;

public class BooleanUtil {

    public static boolean parseBoolean(String s, boolean def) {
        if (s != null) {
            if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)
                    || "T".equalsIgnoreCase(s) || "Y".equalsIgnoreCase(s)
                    || "1".equals(s)) {
                return true;
            }
            if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)
                    || "F".equalsIgnoreCase(s) || "N".equalsIgnoreCase(s)
                    || "0".equals(s)) {
                return false;
            }
        }
        return def;
    }

}
