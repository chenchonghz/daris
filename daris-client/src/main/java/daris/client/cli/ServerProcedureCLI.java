package daris.client.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;

public abstract class ServerProcedureCLI {

    private String _host = null;
    private int _port = -1;
    private String _transport = null;
    private String _auth = null;
    private String _token = null;
    private List<String> _commandArgs = null;

    protected ServerProcedureCLI(String[] args) {
        _host = null;
        _port = -1;
        _transport = null;
        _auth = null;
        _commandArgs = new ArrayList<String>();
        for (int i = 0; i < args.length;) {
            if (args[i].equals("--mf.host")) {
                _host = args[i + 1];
                i += 2;
            } else if (args[i].equals("--mf.port")) {
                _port = Integer.parseInt(args[i + 1]);
                i += 2;
            } else if (args[i].equals("--mf.transport")) {
                _transport = args[i + 1];
                i += 2;
            } else if (args[i].equals("--mf.auth")) {
                _auth = args[i + 1];
                i += 2;
            } else if (args[i].equals("--mf.token")) {
                _token = args[i + 1];
                i += 2;
            } else {
                _commandArgs.add(args[i]);
                i++;
            }
        }
    }

    public void execute() throws Throwable {

        if (_host == null) {
            throw new IllegalArgumentException("Missing --mf.host argument.");
        }
        if (_port < 0) {
            throw new IllegalArgumentException("Missing --mf.port argument.");
        }
        if (_transport == null) {
            throw new IllegalArgumentException("Missing --mf.transport argument.");
        }
        if (_auth == null && _token == null) {
            throw new IllegalArgumentException(
                    "Missing --mf.auth argument or --mf.token argument.");
        }

        /*
         * parse command arguments.
         */
        parseCommandOptions(_commandArgs);

        boolean useHttp = _transport.startsWith("http")
                || _transport.startsWith("HTTP");
        boolean encrypt = _transport.equalsIgnoreCase("https");
        RemoteServer server = new RemoteServer(_host, _port, useHttp, encrypt);
        ServerClient.Connection cxn = server.open();
        try {
            if (_auth != null) {
                String[] parts = _auth.split(",");
                if (parts.length != 3) {
                    throw new IllegalArgumentException(
                            "failed to parse user credentials: " + _auth);
                }
                String domain = parts[0].trim();
                String user = parts[1].trim();
                String password = parts[2].trim();
                cxn.connect(domain, user, password);
            } else {
                cxn.connectWithToken(_token);
            }
            execute(cxn);
        } finally {
            cxn.close();
        }
    }

    protected abstract String name();

    protected abstract void printCommandOptionsUsage(PrintStream s);

    protected void printUsage(PrintStream s) {
        s.println(
                "Usage: " + name() + " <connection-options> <command-options>");
        s.println("");
        s.println("Connection Options:");
        s.println("  --mf.host <host>  Mediaflux server host address");
        s.println("  --mf.port <port>  Mediaflux server port");
        s.println(
                "  --mf.transport <http|https|tcp/ip>  Mediaflux server transport. Can be \"http\", \"https\" or \"tcp/ip\".");
        s.println(
                "  --mf.auth <domain,user,password>  Mediaflux user credentials in the form of \"domain,user,password\"");
        s.println("  --mf.token <token>  Mediaflux secure identity token.");
        s.println("");
        s.println("Command Options:");
        printCommandOptionsUsage(s);
    }

    protected abstract void parseCommandOptions(List<String> args)
            throws Throwable;

    protected abstract void execute(ServerClient.Connection cxn)
            throws Throwable;

}
