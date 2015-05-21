package nig.webdav.client.owncloud;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import nig.util.PathUtil;
import nig.webdav.client.UserCredentials;
import nig.webdav.client.WebdavClientImpl;

import com.github.sardine.Sardine;

public class OwncloudClient extends WebdavClientImpl {
	public static final int CHUNK_SIZE = 1000000; // 1MB
	private static final String OC_CHUNKED_HEADER = "OC-Chunked";

	protected OwncloudClient(String serverAddress, UserCredentials user)
			throws Throwable {
		super(serverAddress, user);
	}

	public void chunkedPut(InputStream in, long length, String remotePath)
			throws Throwable {
		if (length < 0 || length < CHUNK_SIZE) {
			// length<0 : unknown length. It will fail if the server requires Content-Length.
			// fall back to unchuncked
			put(in, length, remotePath);
			return;
		}
		String urlPrefix = PathUtil.join(serverAddress(), remotePath)
				+ "-chunking-" + Math.abs((new Random()).nextInt(9000) + 1000)
				+ "-";
		Map<String, String> params = new HashMap<String, String>(1);
		params.put(OC_CHUNKED_HEADER, OC_CHUNKED_HEADER);
		long chunkCount = (long) Math.ceil((double) length / CHUNK_SIZE);
		byte[] buffer = new byte[CHUNK_SIZE];
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(in);
			long remaining = length;
			for (int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++, remaining -= CHUNK_SIZE) {
				String url = urlPrefix + chunkCount + "-" + chunkIndex;
				int size = remaining >= CHUNK_SIZE ? CHUNK_SIZE
						: (int) remaining;
				dis.readFully(buffer, 0, size);
				InputStream cis = new ByteArrayInputStream(buffer, 0, size);
				try {
					Sardine sardine = getSardineInstance();
					try {
						sardine.put(url, cis, params);
					} finally {
						sardine.shutdown();
					}
				} finally {
					if (cis != null) {
						cis.close();
					}
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (dis != null) {
				dis.close();
			}
		}
	}
}
