package daris.client.util;

public class OSUtils {

    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase()
                .indexOf("linux") >= 0;
    }

}
