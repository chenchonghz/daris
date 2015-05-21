package nig.ssh.client;

import java.io.OutputStream;
import java.io.PrintStream;

public class RemoteCommand implements Runnable, Abortable {

    public static interface ResponseHandler {

        void onExit(int exitStatus);

        OutputStream outputStream();

        OutputStream errorStream();
    }

    private Session _session;
    private String _command;
    private ResponseHandler _handler;
    private boolean _aborted;

    public RemoteCommand(Session session, String command, ResponseHandler handler) {
        _session = session;
        _command = command;
        _handler = handler;
        _aborted = false;
    }

    public void execute() throws Throwable {
        int exitStatus = _session.exec(_command, _handler == null ? null : _handler.outputStream(),
                _handler == null ? null : _handler.errorStream(), this);
        if (_handler != null) {
            _handler.onExit(exitStatus);
        }
    }

    @Override
    public synchronized void abort() {
        _aborted = true;
    }

    @Override
    public synchronized boolean aborted() {
        return _aborted;
    }

    @Override
    public void run() {
        try {
            execute();
        } catch (Throwable e) {
            if (_handler != null && _handler.errorStream() != null) {
                e.printStackTrace(new PrintStream(_handler.errorStream()));
            } else {
                e.printStackTrace();
            }
        }
    }

}
