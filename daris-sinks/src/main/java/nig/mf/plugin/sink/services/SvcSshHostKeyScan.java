package nig.mf.plugin.sink.services;

import nig.ssh.client.Algorithm;
import nig.ssh.client.Ssh;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcSshHostKeyScan extends PluginService {

    public final String SERVICE_NAME = "nig.ssh.host-key.scan";

    private Interface _defn;

    public SvcSshHostKeyScan() {
        _defn = new Interface();
        _defn.add(new Interface.Element("host", StringType.DEFAULT, "The host address of the remote ssh server.", 1, 1));
        _defn.add(new Interface.Element("port", new IntegerType(0, 65535),
                "The port number of the remote ssh server. Defaults to 22.", 0, 1));
        _defn.add(new Interface.Element("type", new EnumType(Algorithm.values()),
                "The type of the key. RSA or DSA. If not specified, get both", 0, 1));
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
        return "Gathers the remote ssh host key.";
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        String host = args.value("host");
        int port = args.intValue("port", Ssh.DEFAULT_PORT);

        Algorithm algorithm = Algorithm.fromString(args.value("type"));
        if (algorithm != null) {
            String hostKey = Ssh.get().getServerHostKey(host, port, algorithm);
            w.add("host-key", new String[] { "host", host, "type", algorithm.toString() }, hostKey);
        } else {
            for (Algorithm alg : Algorithm.values()) {
                String hostKey = Ssh.get().getServerHostKey(host, port, alg);
                w.add("host-key", new String[] { "host", host, "type", alg.toString() }, hostKey);
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
