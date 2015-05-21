package nig.ssh.client;

import java.io.UnsupportedEncodingException;

public class ServerDetails {

    public static final int DEFAULT_SSH_PORT = 22;

    private String _host;
    private int _port;
    private String _hostKey;

    public ServerDetails(String host, int port, String hostKey) {
        _host = host;
        _port = port;
        _hostKey = hostKey;
    }

    public ServerDetails(String host, int port) {
        this(host, port, null);
    }

    public ServerDetails(String host) {
        this(host, DEFAULT_SSH_PORT, null);
    }

    public String host() {
        return _host;
    }

    public int port() {
        return _port;
    }

    public void setHostKey(String hostKey) {
        _hostKey = hostKey;
    }

    public String hostKey() {
        return _hostKey;
    }

    public byte[] hostKeyBytes() {
        if (_hostKey != null) {
            return Base64.decode(_hostKey.getBytes());
        } else {
            return null;
        }
    }

    public String hostKeyAlgorithm() {
        byte[] hostKey = hostKeyBytes();
        if (hostKey == null) {
            return null;
        }
        int len = ((hostKey[0] & 0xff) << 24) | ((hostKey[1] & 0xff) << 16) | ((hostKey[2] & 0xff) << 8)
                | (hostKey[3] & 0xff);
        return new String(hostKey, 4, len);
    }

    public void setHostKey(byte[] hostKey) throws UnsupportedEncodingException {
        _hostKey = new String(Base64.encode(hostKey), "UTF-8");
    }

}
