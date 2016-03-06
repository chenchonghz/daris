package nig.mf.plugin.pssd.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.ProjectMember;
import nig.mf.plugin.pssd.user.Authority;
import nig.mf.plugin.pssd.user.UserCredential;
import nig.mf.pssd.Role;
import nig.util.ObjectUtil;

public class SvcUserDescribe extends PluginService {

    public static final String SERVICE_NAME = "daris.user.describe";

    private Interface _defn;

    public SvcUserDescribe() {

        _defn = new Interface();
        Interface.Element ie = new Interface.Element("authority",
                StringType.DEFAULT,
                "The authority of interest for users. Defaults to all.", 0, 1);
        ie.add(new Interface.Attribute("protocol", StringType.DEFAULT,
                "The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.",
                0));
        _defn.add(ie);
        _defn.add(new Interface.Element("domain", StringType.DEFAULT,
                "The authentication domain for users. If not specified, all domains are included.",
                0, 1));
        _defn.add(new Interface.Element("user", StringType.DEFAULT,
                "The user to describe. If not specified, all users are described.",
                0, 1));
        _defn.add(new Interface.Element("exclude-system-domain",
                BooleanType.DEFAULT,
                "Exclude 'system' domain users?  Defaults to true.", 0, 1));
        _defn.add(new Interface.Element("list-projects", BooleanType.DEFAULT,
                "List the (local only) projects to which the user(s) have access? Defaults to false.",
                0, 1));
    }

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "Returns information on local users that have been registered as users of DaRIS PSSD object model.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_ACCESS;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out,
            XmlWriter w) throws Throwable {

        boolean listProjects = args.booleanValue("list-projects", false);
        Boolean excludeSystemDomain = args.booleanValue("exclude-system-domain",
                true);
        XmlDoc.Element authority = args.element("authority");
        String domain = args.value("domain");
        if ("system".equals(domain) && excludeSystemDomain) {
            throw new IllegalArgumentException(
                    "Given argument domain 'system' contradicts with exclude-system-domain argument.");
        }
        String user = args.value("user");
        if (user != null && domain == null) {
            throw new IllegalArgumentException(
                    "Both user and domain must be given if user is given.");
        }

        XmlDocMaker dm = new XmlDocMaker("args");
        if (authority != null) {
            dm.add(authority);
        }
        if (domain != null) {
            dm.add("domain", domain);
        }
        if (user != null) {
            dm.add("user", user);
        }
        dm.add("size", "infinity");
        dm.add("role", new String[] { "type", "role" },
                ModelUser.modelUserRoleName());
        List<XmlDoc.Element> ues = executor()
                .execute("user.describe", dm.root()).elements("user");
        List<XmlDoc.Element> domains = executor()
                .execute("authentication.domain.list").elements("domain");
        if (ues != null) {
            for (XmlDoc.Element ue : ues) {
                describeUser(executor(), w, ue, listProjects,
                        excludeSystemDomain, domains);
            }
        }
    }

    private void describeUser(ServiceExecutor executor, XmlWriter w,
            XmlDoc.Element userElement, boolean listProjects,
            boolean excludeSystemDomain, List<XmlDoc.Element> domains)
                    throws Throwable {

        String protocol = userElement.value("@protocol");
        String authority = userElement.value("@authority");
        String domain = userElement.value("@domain");
        String domainType = getDomainType(domains, protocol, authority, domain);
        // exclude-system-domain?
        if ("system".equals(domain) && excludeSystemDomain) {
            return;
        }
        String user = userElement.value("@user");
        String id = userElement.value("@id");
        if (authority != null) {
            if (protocol != null) {
                w.push("user",
                        new String[] { "id", id, "authority", authority,
                                "protocol", protocol, "domain", domain,
                                "domain-type", domainType, "user", user });
            } else {
                w.push("user",
                        new String[] { "id", id, "authority", authority,
                                "domain", domain, "domain-type", domainType,
                                "user", user });
            }
        } else {
            w.push("user", new String[] { "id", id, "domain", domain,
                    "domain-type", domainType, "user", user });
        }

        // Add generic roles
        ServerRoute route = null;
        if (ModelUser.hasRole(route, executor,
                Authority.instantiate(authority, protocol), domain, user,
                Role.projectCreatorRoleName())) {
            w.add("role", Role.PROJECT_CREATOR_ROLE_NAME);
        }

        if (ModelUser.hasRole(route, executor,
                Authority.instantiate(authority, protocol), domain, user,
                Role.subjectCreatorRoleName())) {
            w.add("role", Role.SUBJECT_CREATOR_ROLE_NAME);
        }

        // email may be in top-level (user/e-mail) or in mf-user/email
        String email = parseEmail(userElement);
        if (email != null) {
            w.add("email", email); // Standardize on email
        }
        // Name may be in two places also.
        // user/name or mf-user/name (multiples)
        // Settle on an attribute style presentation
        List<String[]> names = parseNames(userElement);
        if (names != null && !names.isEmpty()) {
            for (String[] name : names) {
                w.add("name", new String[] { "type", name[1] }, name[0]);
            }
        }

        // Add local projects accessed by this user
        if (listProjects) {
            UserCredential cred = new UserCredential(
                    Authority.instantiate(authority, protocol), domain, user);
            ProjectMember pM = new ProjectMember(cred);
            Collection<Project.ProjectCIDAndRole> ps = pM
                    .projectsAccessed(executor);
            if (ps != null) {
                for (Project.ProjectCIDAndRole pur : ps) {
                    w.add("project", new String[] { "role", pur.role() },
                            pur.projectId());
                }
            }
        }
        w.pop();
    }

    static String getDomainType(List<XmlDoc.Element> des, String protocol,
            String authority, String domain) throws Throwable {
        if (des != null) {
            for (XmlDoc.Element de : des) {
                if (de.value().equals(domain)) {
                    String p = de.value("@protocol");
                    String a = de.value("@authority");
                    if (ObjectUtil.equals(p, protocol)
                            && ObjectUtil.equals(a, authority)) {
                        return de.value("@type");
                    }
                }
            }
        }
        return "local";
    }

    public static String parseEmail(XmlDoc.Element userElement)
            throws Throwable {
        String email = userElement.stringValue("e-mail");
        if (email == null) {
            email = userElement.stringValue("asset/meta/mf-user/email");
        }
        return email;
    }

    public static List<String[]> parseNames(XmlDoc.Element userElement)
            throws Throwable {
        List<String[]> resultNames = new ArrayList<String[]>();
        List<XmlDoc.Element> nes = userElement
                .elements("asset/meta/mf-user/name");
        if (nes != null) {
            for (XmlDoc.Element ne : nes) {
                resultNames.add(new String[] { ne.value(), ne.value("@type") });
            }
        }
        String name = userElement.value("name");
        if (name != null) {
            String[] names = name.split(" ");
            resultNames.add(new String[] { names[0], "first" });
            if (names.length > 2) {
                for (int i = 1; i < names.length - 1; i++) {
                    resultNames.add(new String[] { names[i], "middle" });
                }
            }
            resultNames.add(new String[] { names[names.length - 1], "last" });
        }
        if (resultNames.isEmpty()) {
            return null;
        }
        return resultNames;
    }

}
