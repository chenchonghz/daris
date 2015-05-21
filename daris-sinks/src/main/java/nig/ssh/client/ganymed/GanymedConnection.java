package nig.ssh.client.ganymed;

import java.util.Arrays;

import nig.ssh.client.Connection;
import nig.ssh.client.ServerDetails;
import nig.ssh.client.Session;
import nig.ssh.client.UserDetails;
import ch.ethz.ssh2.ConnectionInfo;
import ch.ethz.ssh2.ServerHostKeyVerifier;

public class GanymedConnection implements Connection {

    ch.ethz.ssh2.Connection ganymedConn;
    private ServerDetails _serverDetails;

    public GanymedConnection(ServerDetails serverDetails) {
        _serverDetails = serverDetails;
        ganymedConn = new ch.ethz.ssh2.Connection(_serverDetails.host(), _serverDetails.port());
    }

    @Override
    public Session connect(UserDetails userDetails, boolean verifyHostKey) throws Throwable {
        ConnectionInfo connInfo = ganymedConn.connect(verifyHostKey ? new ServerHostKeyVerifier() {

            @Override
            public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm,
                    byte[] serverHostKey) throws Exception {
                if (_serverDetails.hostKey() == null) {
                    // no host key is stored locally.
                    return false;
                }
                if (!_serverDetails.host().equals(hostname)) {
                    // host name not equal
                    return false;
                }
                String algorithm = _serverDetails.hostKeyAlgorithm();
                if (algorithm == null || !algorithm.equals(serverHostKeyAlgorithm)) {
                    // host key algorithm does not match
                    return false;
                }
                byte[] hostKey = _serverDetails.hostKeyBytes();
                return Arrays.equals(hostKey, serverHostKey);
            }
        } : null);
        if (connInfo.serverHostKey != null && _serverDetails.hostKey() == null) {
            _serverDetails.setHostKey(connInfo.serverHostKey);
        }
        boolean authenticated = false;
        if (userDetails.password() == null && userDetails.privateKey() == null) {
            throw new IllegalArgumentException("Expecting user password or private key. None is specified.");
        }
        if (userDetails.password() != null) {
            authenticated = ganymedConn.authenticateWithPassword(userDetails.username(), userDetails.password());
        }
        if (!authenticated && userDetails.privateKey() != null) {
            authenticated = ganymedConn.authenticateWithPublicKey(userDetails.username(), userDetails.privateKey()
                    .toCharArray(), userDetails.passphrase());
        }
        if (!authenticated) {
            throw new RuntimeException("Failed to authenticate user " + userDetails.username());
        }
        GanymedSession session = new GanymedSession(userDetails, this);
        session.open();
        return session;
    }

    @Override
    public void disconnect() throws Throwable {
        ganymedConn.close();
    }

}
