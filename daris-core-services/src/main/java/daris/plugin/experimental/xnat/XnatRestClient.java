package daris.plugin.experimental.xnat;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

public class XnatRestClient {

    public static final String PATH_JSESSION = "/data/JSESSION";
    public static final String COOKIE_JSESSIONID = "JSESSIONID";

    public static String login(String siteUrl, String username, String password)
            throws Throwable {
        URL url = new URL(siteUrl + PATH_JSESSION);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes()));
        conn.setRequestMethod("POST");
        conn.setAllowUserInteraction(false);
        conn.setDoInput(true);
        conn.setDoOutput(false);
        InputStream in = new BufferedInputStream(conn.getInputStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            copy(in, out);
        } finally {
            out.close();
            in.close();
        }
        return out.toString();
    }

    public static HttpURLConnection getDownloadConnection(String siteUrl,
            String path, String username, String password) throws IOException {
        URL url = new URL(joinPaths(siteUrl, path));
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        if (username != null && password != null) {
            conn.setRequestProperty("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString(
                            (username + ":" + password).getBytes()));
        }
        conn.setRequestMethod("GET");
        conn.setAllowUserInteraction(false);
        conn.setDoInput(true);
        conn.setDoOutput(false);
        return conn;
    }

    public static HttpURLConnection getDownloadConnection(String siteUrl,
            String path, String sessionId) throws Throwable {
        URL url = new URL(joinPaths(siteUrl, path));
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Cookie", COOKIE_JSESSIONID + "=" + sessionId);
        conn.setAllowUserInteraction(false);
        conn.setDoInput(true);
        conn.setDoOutput(false);
        return conn;
    }

    public static InputStream getDownloadStream(String siteUrl, String path,
            String username, String password, final boolean disconnectOnClose)
                    throws Throwable {
        final HttpURLConnection conn = getDownloadConnection(siteUrl, path,
                username, password);
        return new BufferedInputStream(conn.getInputStream()) {
            @Override
            public void close() throws IOException {
                super.close();
                if (disconnectOnClose) {
                    conn.disconnect();
                }
            }
        };
    }

    public static InputStream getDownloadStream(String siteUrl, String path,
            String sessionId, final boolean disconnectOnClose)
                    throws Throwable {
        final HttpURLConnection conn = getDownloadConnection(siteUrl, path,
                sessionId);
        return new BufferedInputStream(conn.getInputStream()) {
            @Override
            public void close() throws IOException {
                super.close();
                if (disconnectOnClose) {
                    conn.disconnect();
                }
            }
        };
    }

    public static void downloadExperiment(String siteUrl, String projectId,
            String subjectId, String experimentId, String sessionId,
            OutputStream out) throws Throwable {
        InputStream in = getDownloadStream(siteUrl,
                getExperimentDownloadPath(projectId, subjectId, experimentId),
                sessionId, true);
        try {
            copy(in, out);
        } finally {
            out.close();
            in.close();
        }
    }

    public static InputStream getStreamForDownloadExperiement(String siteUrl,
            String projectId, String subjectId, String experimentId,
            String sessionId) throws Throwable {
        return getDownloadStream(siteUrl,
                getExperimentDownloadPath(projectId, subjectId, experimentId),
                sessionId, true);
    }

    public static void downloadScans(String siteUrl, String projectId,
            String subjectId, String experimentId, Collection<String> scanIds,
            String sessionId, OutputStream out) throws Throwable {
        InputStream in = getDownloadStream(siteUrl, getScanDownloadPath(
                projectId, subjectId, experimentId, scanIds), sessionId, true);
        try {
            copy(in, out);
        } finally {
            out.close();
            in.close();
        }
    }

    public static InputStream getStreamForDownloadScans(String siteUrl,
            String projectId, String subjectId, String experimentId,
            Collection<String> scanIds, String sessionId) throws Throwable {
        return getDownloadStream(siteUrl, getScanDownloadPath(projectId,
                subjectId, experimentId, scanIds), sessionId, true);
    }

    public static void downloadScan(String siteUrl, String projectId,
            String subjectId, String experimentId, String scanId,
            String sessionId, OutputStream out) throws Throwable {
        InputStream in = getDownloadStream(siteUrl,
                getScanDownloadPath(projectId, subjectId, experimentId, scanId),
                sessionId, true);
        try {
            copy(in, out);
        } finally {
            out.close();
            in.close();
        }
    }

    public static String getExperimentDownloadPath(String projectId,
            String subjectId, String experiementId) throws Throwable {
        StringBuilder sb = new StringBuilder("/data/archive/projects/");
        sb.append(projectId);
        sb.append("/subjects/");
        sb.append(subjectId);
        sb.append("/experiments/");
        sb.append(experiementId);
        sb.append("/scans/ALL/files?format=zip");
        return sb.toString();
    }

    public static String getScanDownloadPath(String projectId, String subjectId,
            String experimentId, Collection<String> scanIds) throws Throwable {
        StringBuilder sb = new StringBuilder("/data/archive/projects/");
        sb.append(projectId);
        sb.append("/subjects/");
        sb.append(subjectId);
        sb.append("/experiments/");
        sb.append(experimentId);
        sb.append("/scans/");
        List<String> scanIdList = new ArrayList<String>(scanIds);
        for (int i = 0; i < scanIdList.size(); i++) {
            String scanId = scanIdList.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append(scanId);
        }
        sb.append("/files?format=zip");
        return sb.toString();
    }

    public static String getScanDownloadPath(String projectId, String subjectId,
            String experimentId, String scanId) throws Throwable {
        List<String> scanIds = new ArrayList<String>();
        scanIds.add(scanId);
        return getScanDownloadPath(projectId, subjectId, experimentId, scanIds);
    }

// @formatter:off
//    public static void main(String[] args) throws Throwable {
//        String sessionId = login("http://45.113.232.237:8080/xnat", "XNAT_USER",
//                "XNAT_PASS");
//        File outputFile = new File("/tmp/11111.zip");
//        downloadScan("http://45.113.232.237:8080/xnat", "TEST_PROJECT1",
//                "SUBJ001", "5Yp0E", "702", sessionId,
//                new BufferedOutputStream(new FileOutputStream(outputFile)));
//    }
// @formatter:on

    static void copy(InputStream in, OutputStream out) throws Throwable {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }

    static String joinPaths(String path1, String path2) {
        if (path1 == null && path2 == null) {
            return null;
        }
        String p1 = trimPathRight(path1);
        String p2 = trimPathLeft(path2);
        StringBuilder sb = new StringBuilder();
        if (p1 != null) {
            sb.append(p1);
        }
        if (p2 != null) {
            sb.append("/").append(p2);
        }
        return sb.toString();
    }

    static String trimPathLeft(String path) {
        if (path == null) {
            return null;
        }
        String r = path;
        while (r.startsWith("/")) {
            r = r.substring(1);
        }
        return r;
    }

    static String trimPathRight(String path) {
        if (path == null) {
            return null;
        }
        String r = path;
        while (r.endsWith("/")) {
            r = r.substring(0, r.length() - 1);
        }
        return r;
    }

}
