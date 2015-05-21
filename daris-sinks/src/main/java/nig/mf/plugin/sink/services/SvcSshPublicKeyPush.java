package nig.mf.plugin.sink.services;

import nig.ssh.client.Connection;
import nig.ssh.client.ServerDetails;
import nig.ssh.client.Session;
import nig.ssh.client.Ssh;
import nig.ssh.client.UserDetails;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcSshPublicKeyPush extends PluginService {

    public static final String SERVICE_NAME = "nig.ssh.public-key.push";

    private Interface _defn;

    public SvcSshPublicKeyPush() {
        _defn = new Interface();
        _defn.add(new Interface.Element("user", StringType.DEFAULT, "The user login for the remote ssh server.", 1, 1));
        _defn.add(new Interface.Element("password", StringType.DEFAULT, "The user's password.", 0, 1));

        _defn.add(new Interface.Element("host", StringType.DEFAULT, "The host address of the remote ssh server.", 1, 1));
        _defn.add(new Interface.Element("port", new IntegerType(0, 65535),
                "The port number of the remote ssh server. Defaults to 22.", 0, 1));

        _defn.add(new Interface.Element("pubkey", StringType.DEFAULT, "The user's public key to be installed.", 1,
                1));
    }

    @Override
    public Access access() {
        return ACCESS_ACCESS;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Install the public key to the authorized_keys file of the remote ssh host.";
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        String username = args.value("user");
        String password = args.value("password");
        String host = args.value("host");
        int port = args.intValue("port", Ssh.DEFAULT_PORT);

        String publicKey = args.value("pubkey");

        Connection conn = Ssh.get().getConnection(new ServerDetails(host, port));
        try {
            Session session = conn.connect(new UserDetails(username, password), false);
            try {
                session.installPublicKey(publicKey);
            } finally {
                session.close();
            }
        } finally {
            conn.disconnect();
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
