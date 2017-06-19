package daris.plugin.services;

import java.io.ByteArrayInputStream;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.DateType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.PasswordType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import daris.plugin.services.SvcServerAddressPublicGet.ServerAddress;
import daris.util.CryptoUtils;

public class SvcDownloaderManifestGenerate extends PluginService {

    public static final String SERVICE_NAME = "daris.downloader.manifest.generate";

    public static final String DOWNLOADER_APP = "daris-downloader";

    public static final String TAG = "DARIS_DOWNLOADER_MANIFEST";

    private Interface _defn;

    public SvcDownloaderManifestGenerate() {
        _defn = new Interface();
        addToDefinition(_defn);
    }

    public static void addToDefinition(Interface defn) {

        defn.add(new Interface.Element("name", StringType.DEFAULT, "Name for the manifest.", 0, 1));
        defn.add(new Interface.Element("description", StringType.DEFAULT, "Description for the manifest.", 0, 1));

        defn.add(new Interface.Element("where", StringType.DEFAULT, "Query to select the assets to download.", 0,
                Integer.MAX_VALUE));
        defn.add(new Interface.Element("id", AssetType.DEFAULT, "Asset id.", 0, Integer.MAX_VALUE));
        defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "Asset cid.", 0, Integer.MAX_VALUE));
        defn.add(new Interface.Element("parts", new EnumType(new String[] { "content", "meta", "all" }),
                "Asset parts to download. Defaults to content.", 0, 1));
        defn.add(new Interface.Element("output-pattern", StringType.DEFAULT,
                "The expression used to generate the output path. The format of the expression is Asset Path Language (APL).",
                0, 1));
        defn.add(new Interface.Element("unarchive", BooleanType.DEFAULT,
                "Unarchive asset archive content while downloading. Defaults to false.", 0, 1));

        Interface.Element transcode = new Interface.Element("transcode", XmlDocType.DEFAULT, "Transode.", 0,
                Integer.MAX_VALUE);
        transcode.add(new Interface.Element("from", StringType.DEFAULT, "from mime type.", 1, 1));
        transcode.add(new Interface.Element("to", StringType.DEFAULT, "to mime type.", 1, 1));
        defn.add(transcode);

        Interface.Element token = new Interface.Element("token", XmlDocType.DEFAULT, "Token specification.", 0, 1);
        token.add(new Interface.Element("to", DateType.DEFAULT,
                "A time, after which the token is no longer valid. If not supplied token will not expire.", 0, 1));
        token.add(new Interface.Element("use-count", IntegerType.POSITIVE_ONE,
                "The number of times the token may be used.", 0, 1));

        Interface.Element perm = new Interface.Element("perm", XmlDocType.DEFAULT, "Permissions to grant.", 0,
                Integer.MAX_VALUE);
        perm.add(new Interface.Element("access", new StringType(64), "Access type", 1, 1));
        Interface.Element resource = new Interface.Element("resource", new StringType(255), "Pattern for resource", 1,
                1);
        resource.add(new Interface.Attribute("type", new StringType(32), "Resource type.", 1));
        perm.add(resource);
        token.add(perm);

        token.add(new Interface.Element("persistent", BooleanType.DEFAULT,
                "If true (the default), then the identity will remain after server restart. If false, then identity will be gone after a server restart.",
                0, 1));

        Interface.Element restrict = new Interface.Element("restrict", XmlDocType.DEFAULT,
                "Access restrictions, if any.", 0, 1);
        restrict.add(new Interface.Attribute("description", StringType.DEFAULT,
                "Arbitrary description for the purpose of the restriction.", 0));
        Interface.Element network = new Interface.Element("network", XmlDocType.DEFAULT,
                "Network access restrictions, if any.", 1, 1);
        Interface.Element address = new Interface.Element("address", StringType.DEFAULT,
                "IP network address (IPv4 or IPv6) to restrict user access.", 1, 1);
        address.add(new Interface.Attribute("description", StringType.DEFAULT,
                "Arbitrary description for this address.", 0));
        address.add(new Interface.Attribute("mask", StringType.DEFAULT,
                "The netmask for the corresponding network address. Defaults to 255.255.255.255", 0));
        network.add(address);

        network.add(new Interface.Element("encrypted", BooleanType.DEFAULT,
                "Indicates whether encrypted only network transports will be accepted. Defaults to false.", 0, 1));
        restrict.add(network);
        token.add(restrict);

        Interface.Element role = new Interface.Element("role", new StringType(128), "Role (name) to grant.", 0,
                Integer.MAX_VALUE);
        role.add(new Interface.Attribute("type", new StringType(64), "Role type.", 1));
        token.add(role);

        token.add(new Interface.Element("description", StringType.DEFAULT, "An arbitrary description for the token.", 0,
                1));

        token.add(new Interface.Element("password", PasswordType.DEFAULT, "Password for the token.", 0, 1));

        defn.add(token);

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
        return "Geneates manifest XML file to be consumed by DaRIS manifest downloader client application.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

        String manifest = generateManifest(executor(), args);
        byte[] manifestBytes = manifest.getBytes();
        outputs.output(0).setData(new ByteArrayInputStream(manifestBytes), manifestBytes.length, "text/xml");
    }

    public static String generateManifest(ServiceExecutor executor, XmlDoc.Element args) throws Throwable {

        if (!args.elementExists("where") && !args.elementExists("id") && !args.elementExists("cid")) {
            throw new IllegalArgumentException("Missing where, id or cid.");
        }

        ServerAddress server = SvcServerAddressPublicGet.getServerAddress(executor);
        if (server == null) {
            throw new Exception("Server public address is not set. Ask system-administrator to run "
                    + SvcServerAddressPublicSet.SERVICE_NAME + ".");
        }
        String token = null;
        String tokenPassword = null;
        if (args.elementExists("token")) {
            token = createToken(executor, args.element("token"));
            if (args.elementExists("token/password")) {
                tokenPassword = args.value("token/password");
                token = CryptoUtils.encrypt(token, tokenPassword);
            }
        }
        XmlDocMaker dm = new XmlDocMaker("manifest");
        if (args.elementExists("name")) {
            dm.add("name", args.value("name"));
        }
        if (args.elementExists("description")) {
            dm.add("description", args.value("description"));
        }
        dm.push("server");
        dm.add("host", server.host);
        dm.add("port", server.port);
        dm.add("transport", server.transport);
        dm.pop();
        dm.push("authentication");
        if (token != null) {
            if (tokenPassword != null) {
                dm.add("token-encrypted", token);
            } else {
                dm.add("token", token);
            }
        } else {
            String actor = executor.execute("actor.self.describe").value("actor/@name");
            int idx = actor.indexOf(":");
            String domain = actor.substring(0, idx);
            String user = actor.substring(idx + 1);
            dm.add("domain", domain);
            dm.add("user", user);
        }
        dm.pop();
        if (args.elementExists("where")) {
            dm.addAll(args.elements("where"));
        }
        if (args.elementExists("id")) {
            dm.addAll(args.elements("id"));
        }
        if (args.elementExists("cid")) {
            dm.addAll(args.elements("cid"));
        }
        if (args.elementExists("parts")) {
            dm.add(args.element("parts"));
        }
        if (args.elementExists("output-pattern")) {
            dm.add(args.element("output-pattern"));
        }
        if (args.elementExists("unarchive")) {
            dm.add(args.element("unarchive"));
        }
        if (args.elementExists("transcode")) {
            dm.addAll(args.elements("transcode"));
        }
        return dm.root().toString();
    }

    private static String createToken(ServiceExecutor executor, XmlDoc.Element tokenArgs) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("app", DOWNLOADER_APP);
        dm.add("tag", TAG);
        if (!tokenArgs.elementExists("role")) {
            String actor = executor.execute("actor.self.describe").value("actor/@name");
            dm.add("role", new String[] { "type", "user" }, actor);
            dm.add("role", new String[] { "type", "role" }, "user");
        }
        List<XmlDoc.Element> elements = tokenArgs.elements();
        if (elements != null) {
            for (XmlDoc.Element e : elements) {
                if (!e.nameEquals("password")) {
                    dm.add(e);
                }
            }
        }
        return executor.execute("secure.identity.token.create", dm.root()).value("token");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public int maxNumberOfOutputs() {
        return 1;
    }

    public int minNumberOfOutputs() {
        return 1;
    }

}
