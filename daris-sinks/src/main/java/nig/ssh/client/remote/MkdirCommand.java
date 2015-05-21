package nig.ssh.client.remote;

import java.io.ByteArrayOutputStream;

import nig.ssh.client.CommandResult;
import nig.ssh.client.FileMode;
import nig.ssh.client.Session;

public class MkdirCommand implements RemoteCommand {

    private String _path;
    private boolean _parents;
    private String _mode;

    public MkdirCommand(String path, boolean parents, String mode) throws Throwable {
        if (path == null || path.trim().length() == 0) {
            throw new IllegalArgumentException("path cannot be null");
        }
        if (mode != null && !FileMode.isValid(mode)) {
            throw new IllegalArgumentException("Invalid mode: " + _mode);
        }
        _path = path;
        _parents = parents;
        _mode = mode;
    }

    @Override
    public CommandResult execute(Session session) throws Throwable {

        StringBuilder sb = new StringBuilder();
        sb.append("mkdir ");
        if (_parents) {
            sb.append("-p ");
        }
        if (_mode != null) {
            sb.append(_mode);
            sb.append(" ");
        }
        sb.append('"');
        sb.append(_path);
        sb.append('"');
        String command = sb.toString();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int exitStatus = session.exec(command, null, err, null);
        String msg = new String(err.toByteArray());
        if (!msg.isEmpty()) {
            throw new RuntimeException("Remote command error:" + msg);
        }
        if (exitStatus != 0) {
            throw new RuntimeException("Remote command exit status=" + exitStatus);
        }
        return new CommandResult(exitStatus, null, null);
    }

}
