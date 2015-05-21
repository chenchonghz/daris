package nig.ssh.client.ganymed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nig.ssh.client.Abortable;
import nig.ssh.client.AbstractSession;
import nig.ssh.client.UserDetails;
import nig.util.PathUtil;
import ch.ethz.ssh2.ChannelCondition;

public class GanymedSession extends AbstractSession {
    
    public static final int TIMEOUT_MILLISECS = 5000;

    private GanymedConnection _conn;
    private ch.ethz.ssh2.Session _session;

    GanymedSession(UserDetails userDetails, GanymedConnection conn) {
        super(userDetails);
        _conn = conn;
    }

    @Override
    public int exec(String command, OutputStream stdout, OutputStream stderr, Abortable abortCheck) throws Throwable {
        if (_session == null) {
            open();
        }
        if (_session.getState() != ch.ethz.ssh2.channel.Channel.STATE_OPEN) {
            throw new Exception("Ganymed SSH session is closed.");
        }

        _session.execCommand(command);
        InputStream remoteStdout = _session.getStdout();
        InputStream remoteStderr = _session.getStderr();
        byte[] buffer = new byte[1024];
        int exitStatus = 0;
        try {
            while (true) {
                if (abortCheck != null && abortCheck.aborted()) {
                    break;
                }
                if (remoteStderr.available() > 0) {
                    exitStatus = 1;
                }
                if ((remoteStdout.available() == 0) && (remoteStderr.available() == 0)) {

                    /*
                     * Even though currently there is no data available, it may be that new data arrives and the
                     * session's underlying channel is closed before we call waitForCondition(). This means that EOF and
                     * STDOUT_DATA (or STDERR_DATA, or both) may be set together.
                     */
                    int conditions = _session.waitForCondition(ChannelCondition.STDOUT_DATA
                            | ChannelCondition.STDERR_DATA | ChannelCondition.EOF, TIMEOUT_MILLISECS);

                    /* Wait no longer than 5 seconds (= 5000 milliseconds) */

                    if ((conditions & ChannelCondition.TIMEOUT) != 0) {
                        /* A timeout occured. */
                        throw new IOException("Timeout while waiting for data from peer.");
                    }

                    /* Here we do not need to check separately for CLOSED, since CLOSED implies EOF */
                    if ((conditions & ChannelCondition.EOF) != 0) {
                        /* The remote side won't send us further data... */
                        if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0) {
                            /* ... and we have consumed all data in the local arrival window. */
                            if ((conditions & ChannelCondition.EXIT_STATUS) != 0) {
                                // TODO: this may not be right since some server does not send exit status.
                                // exit status is available
                                if (_session.getExitStatus() != null) {
                                    exitStatus = _session.getExitStatus();
                                }
                                break;
                            }
                        }
                    }

                    /* OK, either STDOUT_DATA or STDERR_DATA (or both) is set. */

                    // You can be paranoid and check that the library is not going nuts:
                    // if ((conditions & (ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA)) == 0)
                    // throw new IllegalStateException("Unexpected condition result (" + conditions + ")");
                }

                /*
                 * If you below replace "while" with "if", then the way the output appears on the local stdout and stder
                 * streams is more "balanced". Addtionally reducing the buffer size will also improve the interleaving,
                 * but performance will slightly suffer. OKOK, that all matters only if you get HUGE amounts of stdout
                 * and stderr data =)
                 */
                while (remoteStdout.available() > 0) {
                    int i = remoteStdout.read(buffer);
                    if (i < 0) {
                        break;
                    }
                    if (stdout != null) {
                        stdout.write(buffer, 0, i);
                    }
                }
                while (remoteStderr.available() > 0) {
                    int i = remoteStderr.read(buffer);
                    if (i < 0) {
                        break;
                    }
                    if (stderr != null) {
                        stderr.write(buffer, 0, i);
                    }
                }
            }
        } finally {
            remoteStdout.close();
            remoteStderr.close();
        }
        close();

        return exitStatus;
    }

    @Override
    public OutputStream scpPut(String remoteFilePath, long length, String mode) throws Throwable {
        ch.ethz.ssh2.SCPClient scpClient = _conn.ganymedConn.createSCPClient();
        String dir = PathUtil.getParentDirectory(remoteFilePath);
        String name = PathUtil.getFileName(remoteFilePath);
        return scpClient.put(name, length, dir, mode);
    }

    @Override
    public InputStream scpGet(String remoteFile) throws Throwable {
        ch.ethz.ssh2.SCPClient scpClient = _conn.ganymedConn.createSCPClient();
        return scpClient.get(remoteFile);
    }

    @Override
    public void open() throws Throwable {
        close();
        _session = _conn.ganymedConn.openSession();
    }

    @Override
    public void close() throws Throwable {
        if (_session != null) {
            _session.close();
            _session = null;
        }
    }

}
