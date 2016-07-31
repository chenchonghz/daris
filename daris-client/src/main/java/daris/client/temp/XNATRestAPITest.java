package daris.client.temp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

import arc.streams.StreamCopy;

public class XNATRestAPITest {

	public static final String BASE_URL = "http://45.113.232.237:8080/xnat";
	public static final String URL = "http://45.113.232.237:8080/xnat/data/archive/projects/TEST_PROJECT1/subjects/SUBJ001/experiments/5Yp0E/scans/ALL/files?format=zip";

	public static final String USERNAME = "CHANGE_ME";
	public static final String PASSWORD = "CHANGE_ME";

	public static final String OUTPUT_FILE_PATH = "/tmp/123.zip";

	public static void main(String[] args) throws Throwable {
//		download(URL, USERNAME, PASSWORD, new File(OUTPUT_FILE_PATH));
		System.out.println(sessionFor(BASE_URL, "admin", "Ask_me9"));
	}

	static void download(String url, String username, String password, File of) throws Throwable {
		URL u = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		if (username != null && password != null) {
			conn.setRequestProperty("Authorization",
					"Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
		}
		conn.setRequestMethod("GET");
		conn.setAllowUserInteraction(false);
		conn.setDoInput(true);
		conn.setDoOutput(false);
		InputStream in = new BufferedInputStream(conn.getInputStream());
		OutputStream out = new BufferedOutputStream(new FileOutputStream(of));
		try {
			StreamCopy.copy(in, out);
		} finally {
			out.close();
			in.close();
		}
	}

	public static String sessionFor(String baseUrl, String username, String password) throws Throwable {
		URL url = new URL(baseUrl + "/data/JSESSION");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("Authorization",
				"Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
		conn.setRequestMethod("POST");
		conn.setAllowUserInteraction(false);
		conn.setDoInput(true);
		conn.setDoOutput(false);
		InputStream in = new BufferedInputStream(conn.getInputStream());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			StreamCopy.copy(in, out);
		} finally {
			out.close();
			in.close();
		}
		return out.toString();
	}
}
