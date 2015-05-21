package nig.util;

import java.util.Random;

public class PathUtil {
    private static final Random RANDOM = new Random();

    public static String getParentDirectory(String filePath) {
        return getParentDirectory(filePath, true);
    }

    public static String getParentDirectory(String filePath, boolean appendTrailingSlash) {
        if (filePath == null) {
            return null;
        }
        if (filePath.equals("/")) {
            return null;
        }
        if (filePath.endsWith("/")) {
            filePath = PathUtil.trimTrailingSlash(filePath);
        }
        int idx = filePath.lastIndexOf('/');
        if (idx == -1) {
            return appendTrailingSlash ? "./" : ".";
        } else if (idx == 0) {
            return "/";
        } else {
            return filePath.substring(0, appendTrailingSlash ? idx + 1 : idx);
        }
    }

    public static String getFileName(String filePath) {
        if (filePath == null) {
            return null;
        }
        int idx = filePath.lastIndexOf('/');
        if (idx == -1) {
            return filePath;
        } else {
            return filePath.substring(idx + 1);
        }
    }

    public static String trimLeadingSlash(String path) {
        return StringUtil.trimLeading(path, "/", true);
    }

    public static String trimTrailingSlash(String path) {
        return StringUtil.trimTrailing(path, "/", true);
    }

    public static String prependSlash(String path) {
        if (path.startsWith("/")) {
            return path;
        } else {
            return new StringBuilder("/").append(path).toString();
        }
    }

    public static String appendSlash(String path) {
        if (path.endsWith("/")) {
            return path;
        } else {
            return new StringBuilder(path).append("/").toString();
        }
    }

    public static String join(String path1, String path2) {
        StringBuilder sb = new StringBuilder();
        sb.append(trimTrailingSlash(path1));
        sb.append("/");
        sb.append(trimLeadingSlash(path2));
        return sb.toString();
    }

    public static String getRandomFileName(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int n = RANDOM.nextInt(62);
            if (n < 10) {
                // 0-9
                sb.append(Integer.toString(n));
            }
            if (n >= 10 && n < 36) {
                // A-Z
                n = n - 10 + 65;
                sb.append((char) n);
            }
            if (n >= 36 && n < 62) {
                // a-z
                n = n - 36 + 97;
                sb.append((char) n);
            }
        }
        return sb.toString();
    }

}
