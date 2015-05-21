package nig.ssh.client.jsch;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

import nig.ssh.client.Base64;
import nig.ssh.client.Connection;
import nig.ssh.client.ServerDetails;
import nig.ssh.client.Session;
import nig.ssh.client.UserDetails;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSch;

public class JSchConnection implements Connection {

    private JSch _jsch;
    private ServerDetails _serverDetails;
    private Set<JSchSession> _sessions = new HashSet<JSchSession>();

    JSchConnection(ServerDetails serverDetails) {
        _jsch = new JSch();
        _serverDetails = serverDetails;
    }

    JSch jsch() {
        return _jsch;
    }

    public ServerDetails serverDetails() {
        return _serverDetails;
    }

    @Override
    public Session connect(final UserDetails userDetails, boolean verifyHostKey) throws Throwable {

        _jsch.setKnownHosts(new ByteArrayInputStream(new byte[8192]));
        String hostKey = _serverDetails.hostKey();
        if (hostKey != null) {
            _jsch.getHostKeyRepository().add(new HostKey(_serverDetails.host(), Base64.decode(hostKey.getBytes())),
                    JSchUserInfo.makeUserInfo(userDetails));
        }
        /*
         * added user's identity (private & public keys)
         */
        if (userDetails.privateKey() != null) {
            _jsch.addIdentity(userDetails.username() + "_identity", userDetails.privateKey().getBytes(), userDetails
                    .publicKey().getBytes(), userDetails.passphrase() != null ? userDetails.passphrase().getBytes()
                    : null);
        }
        JSchSession session = new JSchSession(userDetails, this, verifyHostKey);
        session.open();
        return session;
    }

    void addSession(JSchSession session) {

        _sessions.add(session);
    }

    void removeSession(JSchSession session) {

        _sessions.remove(session);
    }

    @Override
    public void disconnect() throws Throwable {
        for (JSchSession session : _sessions) {
            session.close();
        }
    }

}
