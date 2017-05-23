package daris.util;

import java.util.ArrayList;
import java.util.List;

public class PathUtils {
    public static String join(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.size(); i++) {
            if (i == 0) {
                sb.append(trimTrailingSlash(paths.get(i)));
            } else {
                sb.append("/").append(trimSlash(paths.get(i)));
            }
        }
        return sb.toString();
    }

    public static String join(String... paths) {
        if (paths == null || paths.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            if (i == 0) {
                sb.append(trimTrailingSlash(paths[i]));
            } else {
                sb.append("/");
                sb.append(trimSlash(paths[i]));
            }
        }
        return sb.toString();
    }

    public static String join(String[] paths, int offset, int length) {
        if (paths == null || paths.length == 0 || offset >= paths.length) {
            return null;
        }
        int end = offset + length;
        if (end > paths.length) {
            end = paths.length;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < end; i++) {
            if (i == 0) {
                sb.append(trimTrailingSlash(paths[i]));
            } else {
                sb.append("/").append(trimSlash(paths[i]));
            }
        }
        return sb.toString();
    }

    public static String trimSlash(String path) {
        return trimTrailingSlash(trimLeadingSlash(path));
    }

    public static String trimTrailingSlash(String path) {

        if (path == null || path.isEmpty()) {
            return path;
        }
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static String trimLeadingSlash(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    public static String[] split(String path) {
        if (path == null || path.isEmpty() || path.indexOf("/") == -1) {
            return new String[] { path };
        }
        return trimSlash(path).split("/");
    }

    public static List<String> getParents(String path, boolean ascending) {
        if (path == null) {
            return null;
        }
        path = path.trim();
        if (path.isEmpty() || path.indexOf("/") == -1) {
            return null;
        }
        List<String> parents = new ArrayList<String>();
        String[] components = split(path);
        if (ascending) {
            for (int i = 0; i < components.length - 1; i++) {
                parents.add(join(components, 0, i + 1));
            }
        } else {
            for (int i = components.length - 1; i >= 0; i--) {
                parents.add(join(components, 0, i));
            }
        }
        return parents;
    }

    public static String getParent(String path, String defaultIfNull) {
        String parent = getParent(path);
        if (parent == null) {
            return defaultIfNull;
        }
        return parent;
    }

    public static String getParent(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        boolean leadingSlash = path.startsWith("/");
        String p = trimSlash(path);
        int idx = p.lastIndexOf('/');
        if (idx >= 0) {
            StringBuilder sb = new StringBuilder(leadingSlash ? "/" : "");
            sb.append(p.substring(0, idx));
            return sb.toString();
        }
        return null;
    }

    public static String getLastComponent(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        path = trimTrailingSlash(path);
        int idx = path.lastIndexOf('/');
        if (idx < 0) {
            return path;
        }
        return path.substring(idx + 1);
    }

    public static boolean hasParents(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        String p = trimSlash(path);
        return p.indexOf('/') >= 0;
    }

    public static String removeFileExtension(String path, String ext) {
        if (path != null && ext != null && path.endsWith("." + ext)) {
            return path.substring(0, path.length() - 1 - ext.length());
        }
        return path;
    }

    public static void main(String[] args) throws Throwable {
        List<String> parents = getParents("/a/b/c/d", false);
        if (parents != null) {
            for (String p : parents) {
                System.out.println(p);
            }
        }
    }

}
