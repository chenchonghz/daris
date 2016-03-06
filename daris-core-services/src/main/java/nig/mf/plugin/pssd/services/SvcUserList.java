package nig.mf.plugin.pssd.services;

import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.pssd.Role;

public class SvcUserList extends PluginService {

    public static final String SERVICE_NAME = "daris.user.list";

    private Interface _defn;

    public SvcUserList() {
        _defn = new Interface();
        Interface.Element ie = new Interface.Element("authority",
                StringType.DEFAULT,
                "The authority of interest for users. Defaults to all.", 0, 1);
        ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
                "The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.",
                0));
        _defn.add(ie);
        _defn.add(new Interface.Element("domain", StringType.DEFAULT,
                "The authentication domain for users. Defaults to all.", 0, 1));
        _defn.add(new Interface.Element("exclude-system-domain",
                BooleanType.DEFAULT,
                "Exclude users in 'system' domain?  Defaults to true.", 0, 1));
    }

    public String name() {
        return SERVICE_NAME;
    }

    public String description() {
        return "Gets a list of users that have been registered as users of the DaRIS PSSD model. Includes external authorities.";
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_ACCESS;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out,
            XmlWriter w) throws Throwable {

        XmlDoc.Element authority = args.element("authority");
        String domain = args.stringValue("domain");
        Boolean excludeSystemDomain = args.booleanValue("exclude-system-domain",
                true);
        if ("system".equals(domain) && excludeSystemDomain) {
            throw new IllegalArgumentException(
                    "Given argument domain 'system' contradicts with 'exclude-system-domain' argument.");
        }

        // Find the users that have the daris:pssd.model.user role. This will
        // work well for LDAP domain users as it will only return the users who
        // have the role (efficiently)
        XmlDocMaker dm = new XmlDocMaker("args");
        if (authority != null) {
            dm.add(authority);
        }
        if (domain != null) {
            dm.add("domain", domain);
        }
        dm.add("role", new String[] { "type", "role" },
                Role.modelUserRoleName());
        List<XmlDoc.Element> users = executor()
                .execute("user.describe", dm.root()).elements("user");
        /*
         * output xml (reformatted)
         */
        if (users != null) {
            for (XmlDoc.Element user : users) {
                String d = user.value("@domain");
                if (!excludeSystemDomain || !"system".equals(d)) {
                    w.add("user",
                            new String[] { "id", user.value("@id"), "authority",
                                    user.value("@authority"), "@protocol",
                                    user.value("protocol"), "domain",
                                    user.value("@domain") },
                            user.value("@user"));
                }
            }
        }

    }
}
