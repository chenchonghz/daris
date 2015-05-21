package nig.ssh.client.ganymed;

import nig.ssh.client.Algorithm;
import nig.ssh.client.Base64;
import nig.ssh.client.Connection;
import nig.ssh.client.ServerDetails;
import nig.ssh.client.SshImpl;
import ch.ethz.ssh2.ConnectionInfo;

public class GanymedSsh implements SshImpl {

    public static final String LIBRARY_NAME = "ganymed";

    public GanymedSsh() {

    }

    @Override
    public Connection getConnection(ServerDetails serverDetails) {
        return new GanymedConnection(serverDetails);
    }

    @Override
    public String getServerHostKey(String host, int port, Algorithm algorithm) throws Throwable {
        ch.ethz.ssh2.Connection conn = new ch.ethz.ssh2.Connection(host, port);
        conn.setServerHostKeyAlgorithms(new String[] { algorithm.signature() });
        String hostKey = null;
        try {
            ConnectionInfo info = conn.connect();
            hostKey = new String(Base64.encode(info.serverHostKey), "UTF-8");
        } finally {
            conn.close();
        }
        return hostKey;
    }

    @Override
    public String libraryName() {
        return LIBRARY_NAME;
    }
}
