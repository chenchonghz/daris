package nig.webdav.client;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import nig.util.PathUtil;

import org.apache.http.entity.InputStreamEntity;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.github.sardine.impl.SardineImpl;

public class WebdavClientImpl implements WebdavClient {
	private String _serverAddress;
	private UserCredentials _user;

	protected WebdavClientImpl(String serverAddress, UserCredentials user)
			throws Throwable {
		_serverAddress = serverAddress;
		_user = user;
	}

	public String serverAddress() {
		return _serverAddress;
	}

	protected Sardine getSardineInstance() throws Throwable {
		Sardine sardine = SardineFactory.begin(_user.userName(),
				_user.password());
		sardine.enablePreemptiveAuthentication(getHostName(_serverAddress));
		return sardine;
	}

	private boolean exists(String uri) throws Throwable {
		Sardine sardine = getSardineInstance();
		boolean exists = false;
		try {
			exists = sardine.exists(uri);
		} finally {
			sardine.shutdown();
		}
		return exists;
	}

	@Override
	public InputStream get(String remotePath) throws Throwable {
		String url = PathUtil.join(_serverAddress, remotePath);
		String uri = URIEncoder.encode(url);
		Sardine sardine = getSardineInstance();
		InputStream in = null;
		try {
			in = sardine.get(uri);
		} finally {
			sardine.shutdown();
		}
		return in;
	}

	@Override
	public void put(InputStream in, long length, String remotePath)
			throws Throwable {
		String url = PathUtil.join(_serverAddress, remotePath);
		String uri = URIEncoder.encode(url);
		Sardine sardine = getSardineInstance();
		try {
			if (length >= 0) {
				((SardineImpl)sardine).put(uri, new InputStreamEntity(in, length), null, false);
			} else {
				// unknown length. It will fail if the server requires Content-Length.
				sardine.put(uri, in);
			}
		} finally {
			sardine.shutdown();
		}
	}

	@Override
	public void mkdir(String remotePath, boolean parents) throws Throwable {
		if (remotePath == null) {
			return;
		}
		remotePath = remotePath.trim();
		if (remotePath.isEmpty() || remotePath.equals("/")) {
			return;
		}
		remotePath = PathUtil.trimTrailingSlash(remotePath);
		String url = PathUtil.join(_serverAddress,
				PathUtil.appendSlash(remotePath));
		String uri = URIEncoder.encode(url);
		if (exists(uri)) {
			// already exists
			return;
		}
		Sardine sardine = getSardineInstance();
		if (!parents) {
			// do not try to create parent directories. It will throw exception
			// if the parent directory does not exist.
			sardine.createDirectory(uri);
		} else {
			mkdir(PathUtil.getParentDirectory(remotePath, true), true);
		//	System.out.println("WebDAV: creating remote directory:" + uri);
			sardine.createDirectory(uri);
		}
	}

	private static String getHostName(String serverAddress)
			throws MalformedURLException {
		return new URL(serverAddress).getHost();
	}

	@Override
	public void delete(String remotePath) throws Throwable {
		String url = PathUtil.join(_serverAddress, remotePath);
		String uri = URIEncoder.encode(url);
		Sardine sardine = getSardineInstance();
		sardine.delete(uri);
	}

	public static void main(String[] args) throws Throwable {
		String s = "https://abc.com/a b c/a b";
		URL url = new URL(s);
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
				url.getPort(), url.getPath(), url.getQuery(), url.getRef());
		System.out.println(uri);
	}
}
