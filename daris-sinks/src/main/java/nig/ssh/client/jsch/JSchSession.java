package nig.ssh.client.jsch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import nig.ssh.client.Abortable;
import nig.ssh.client.AbstractSession;
import nig.ssh.client.UserDetails;
import nig.util.PathUtil;

import com.jcraft.jsch.ChannelExec;

public class JSchSession extends AbstractSession {

    public static final int MAX_LINE_LENGTH = 8192;

    public static class FileInfo {
        public final String mode;
        public final long length;
        public final String name;

        FileInfo(String mode, long length, String name) {
            this.mode = mode;
            this.length = length;
            this.name = name;
        }

        public void write(OutputStream out) throws Throwable {

            // send "C0600 fileLength fileName\n"
            StringBuilder line = new StringBuilder();
            line.append("C");
            line.append(mode);
            line.append(" ");
            line.append(length);
            line.append(" ");
            line.append(name);
            line.append("\n");
            out.write(line.toString().getBytes());
            out.flush();
        }
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != '\n') {
            if (c < 0) {
                // -1: EOF
                throw new IOException("Remote scp terminated unexpectedly.");
            }
            sb.append((char) c);
            if (sb.length() > MAX_LINE_LENGTH) {
                throw new IOException("Remote scp sent a too long line.");
            }
        }
        return sb.toString();
    }

    private static class ScpOutputStream extends BufferedOutputStream {
        private ChannelExec _channel;
        private InputStream _in;

        public ScpOutputStream(ChannelExec channel, InputStream in, OutputStream out, FileInfo fileInfo)
                throws Throwable {
            super(out, 40000);

            _in = new BufferedInputStream(in);

            checkResponse(_in);

            // C line: 'C0600 200 1.txt'
            fileInfo.write(out);

            checkResponse(_in);

        }

        @Override
        public void close() throws IOException {
            try {
                write(0);
                flush();
                checkResponse(_in);
                write("E\n".getBytes("UTF-8"));
                flush();
            } finally {
                super.close();
                if (_channel != null) {
                    _channel.disconnect();
                }
            }
        }

        private static void checkResponse(InputStream in) throws IOException {

            int c = in.read();

            if (c == 0) {
                // success
                return;
            }

            if (c == -1) {
                throw new IOException("Remote scp terminated unexpectedly.");
            }

            if (c == 1 || c == 2) {
                String error = readLine(in);
                throw new IOException("Remote scp terminated with error code: " + c + ". " + error);
            } else {
                throw new IOException("Remote scp sent illegal error code: " + c);
            }

        }
    }

    private static class ScpInputStream extends BufferedInputStream {
        private long _remaining;
        private ChannelExec _channel;
        private OutputStream _out;

        public ScpInputStream(ChannelExec channel, InputStream in, OutputStream out) throws Throwable {
            super(in);
            _channel = channel;
            _out = new BufferedOutputStream(out);

            // send '\0'
            _out.write(0);
            _out.flush();

            // read C line, e.g. 'C0600 200 1.txt'
            int c;
            String line = null;
            do {
                c = in.read();

                if (c < 0) {
                    throw new IOException("Remote scp terminated unexpectedly.");
                }

                line = readLine(in);

                if (c == 1) {
                    throw new IOException("Remote scp error: " + line);
                }

                if (c == 2) {
                    throw new IOException("Remote scp fatal error: " + line);
                }

                if (c == 'T') {
                    /* Ignore modification times */
                    continue;
                }

            } while (c != 'C');

            final FileInfo info = parseCLine(line);
            _out.write(0x0);
            _out.flush();
            _remaining = info.length;

        }

        @Override
        public int read() throws IOException {
            if (_remaining <= 0) {
                return -1;
            }

            int b = super.read();
            if (b < 0) {
                throw new IOException("Remote scp terminated connection unexpectedly");
            }
            _remaining--;
            return b;
        }

        @Override
        public int read(byte b[], int off, int len) throws IOException {
            if (_remaining <= 0) {
                return -1;
            }

            if (len < _remaining) {
                len = (int) _remaining;
            }

            int n = super.read(b, off, len);
            if (n < 0) {
                throw new IOException("Remote scp terminated connection unexpectedly");
            }

            _remaining -= n;
            return n;
        }

        @Override
        public void close() throws IOException {
            try {
                _out.write(0x0);
                _out.flush();
            } finally {
                super.close();
                if (_channel != null) {
                    _channel.disconnect();
                }
            }
        }

        private static FileInfo parseCLine(String line) throws Throwable {

            // 0600 FileLength FileName
            String mode = line.substring(0, 4);
            int idx = line.lastIndexOf(' ');
            long length = Long.parseLong(line.substring(5, idx));
            String name = line.substring(idx + 1);
            return new FileInfo(mode, length, name);
        }
    }

    private JSchConnection _connection;
    private com.jcraft.jsch.Session _jschSession;
    private boolean _verifyHostKey;

    JSchSession(UserDetails userDetails, JSchConnection connection, boolean verifyHostKey) {
        super(userDetails);
        _connection = connection;
        _verifyHostKey = verifyHostKey;

    }

    @Override
    public OutputStream scpPut(String remoteFilePath, long length, String mode) throws Throwable {

        open();

        if (!Pattern.matches("\\d{4}", mode)) {
            throw new IllegalArgumentException("Invalid file mode: " + mode);
        }

        String dir = PathUtil.getParentDirectory(remoteFilePath);
        String fileName = PathUtil.getFileName(remoteFilePath);

        // exec 'scp -t -d remoteTargetDirectory' remotely
        String command = "scp -t -d \"" + dir + "\"";
        final ChannelExec channel = (ChannelExec) _jschSession.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        final InputStream in = channel.getInputStream();

        channel.connect();

        return new ScpOutputStream(channel, in, out, new FileInfo(mode, length, fileName));

    }

    @Override
    public InputStream scpGet(String remoteFilePath) throws Throwable {

        open();

        // exec 'scp -f remoteFilePath' remotely
        String command = "scp -f " + remoteFilePath;
        ChannelExec channel = (ChannelExec) _jschSession.openChannel("exec");
        channel.setCommand(command);

        // get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        return new ScpInputStream(channel, in, out);

    }

    @Override
    public void open() throws Throwable {
        if (_jschSession != null && _jschSession.isConnected()) {
            return;
        }
        _jschSession = _connection.jsch().getSession(userDetails().username(), _connection.serverDetails().host(),
                _connection.serverDetails().port());
        _jschSession.setConfig("StrictHostKeyChecking", _verifyHostKey ? "yes" : "no");
        _jschSession.setUserInfo(JSchUserInfo.makeUserInfo(userDetails()));
        _jschSession.connect();
        if (!_jschSession.isConnected()) {
            throw new Exception("Failed to connect JSch session.");
        }
        _connection.addSession(this);
    }

    @Override
    public void close() throws Throwable {
        _jschSession.disconnect();
        _connection.removeSession(this);
    }

    @Override
    public int exec(String command, OutputStream stdout, OutputStream stderr, Abortable abortCheck) throws Throwable {

        int exitStatus = -1;
        ChannelExec channel = (ChannelExec) _jschSession.openChannel("exec");
        channel.setCommand(command);
        InputStream is = channel.getInputStream();
        InputStream es = channel.getErrStream();
        try {
            channel.connect();
            byte[] buffer = new byte[1024];
            while (true) {
                if (abortCheck != null && abortCheck.aborted()) {
                    break;
                }
                while (is.available() > 0) {
                    int i = is.read(buffer);
                    if (i < 0) {
                        break;
                    }
                    if (stdout != null) {
                        stdout.write(buffer, 0, i);
                    }
                }
                while (es.available() > 0) {
                    int i = es.read(buffer);
                    if (i < 0) {
                        break;
                    }
                    if (stderr != null) {
                        stderr.write(buffer, 0, i);
                    }
                }
                if (channel.isClosed()) {
                    exitStatus = channel.getExitStatus();
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (es != null) {
                es.close();
            }
            channel.disconnect();
        }
        return exitStatus;
    }

}
