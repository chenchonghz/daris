package daris.plugin.services;

import java.util.Collection;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import daris.plugin.model.ProjectRole;

public class SvcProjectUserRemove extends PluginService {
    public static final String SERVICE_NAME = "daris.project.user.remove";

    private Interface _defn;

    public SvcProjectUserRemove() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the project.", 0, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the project.", 0, 1));

        Interface.Element user = new Interface.Element("user", XmlDocType.DEFAULT,
                "User to revoke access to the project.", 0, Integer.MAX_VALUE);

        Interface.Element userAuthority = new Interface.Element("authority", StringType.DEFAULT,
                "The authority of interest. Defaults to local.", 0, 1);
        userAuthority.add(new Interface.Attribute("protocol", StringType.DEFAULT,
                "The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.",
                0));
        user.add(userAuthority);

        user.add(new Interface.Element("domain", StringType.DEFAULT, "The domain name of the user.", 1, 1));
        user.add(new Interface.Element("user", StringType.DEFAULT, "The user name within the domain.", 1, 1));
        _defn.add(user);

        Interface.Element roleUser = new Interface.Element("role-user", XmlDocType.DEFAULT,
                "Role to revoke access to the project.", 0, Integer.MAX_VALUE);
        roleUser.add(new Interface.Element("name", StringType.DEFAULT, "The name of the role.", 1, 1));
        _defn.add(roleUser);

    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Remove project users.";
    }

    @Override
    public void execute(final Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        final XmlDoc.Element ae = ServiceUtils.getAssetMeta(executor(), args.value("id"), args.value("cid"));

        /*
         * validate if it is a project, also checks the user self has sufficient
         * privilege
         */
        SvcProjectUserSet.validate(executor(), ae);

        new AtomicTransaction(new AtomicOperation() {

            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {

                String projectCid = ae.value("cid");
                List<XmlDoc.Element> ues = args.elements("user");
                if (ues != null) {
                    for (XmlDoc.Element ue : ues) {
                        removeUser(executor, ue.value("domain"), ue.value("user"), ue.value("authority"),
                                ue.value("authority/@protocol"), projectCid);
                    }
                }
                List<XmlDoc.Element> rues = args.elements("role-user");
                if (rues != null) {
                    for (XmlDoc.Element rue : rues) {
                        removeRoleUser(executor, rue.value("name"), projectCid);
                    }
                }
                return false;
            }
        }).execute(executor());

    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    public static void removeAllUsers(ServiceExecutor executor, String projectCid) throws Throwable {

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("cid", projectCid);
        XmlDoc.Element re = executor.execute(SvcProjectUserList.SERVICE_NAME, dm.root());
        List<XmlDoc.Element> ues = re.elements("user");
        if (ues != null) {
            for (XmlDoc.Element ue : ues) {
                removeUser(executor, ue.value("@domain"), ue.value("@user"), ue.value("@authority"),
                        ue.value("@protocol"), projectCid);
            }
        }
        List<XmlDoc.Element> rues = re.elements("role-user");
        if (rues != null) {
            for (XmlDoc.Element rue : rues) {
                removeRoleUser(executor, rue.value("@name"), projectCid);
            }
        }
    }

    static void removeRoleUser(ServiceExecutor executor, String roleUser, String projectCid) throws Throwable {

        Collection<String> roles = executor
                .execute("actor.describe", "<args><type>role</type><name>" + roleUser + "</name></args>", null, null)
                .values("actor/role[@type='role']");
        if (roles == null || roles.isEmpty()) {
            return;
        }
        Collection<String> projectRoles = SvcProjectUserList.filterValues(roles,
                ProjectRole.PROJECT_SPECIFIC_ROLE_PREFIX, "." + projectCid);
        if (projectRoles == null || roles.isEmpty()) {
            return;
        }
        revokeRoles(executor, roleUser, "role", projectRoles);
    }

    static void removeUser(ServiceExecutor executor, String domain, String user, String authority, String protocol,
            String projectCid) throws Throwable {
        String actorName = domain + ":" + user;
        if (authority != null) {
            actorName = authority + ":" + actorName;
        }
        Collection<String> roles = executor
                .execute("actor.describe", "<args><type>user</type><name>" + actorName + "</name></args>", null, null)
                .values("actor/role[@type='role']");
        if (roles == null || roles.isEmpty()) {
            return;
        }
        Collection<String> projectRoles = SvcProjectUserList.filterValues(roles,
                ProjectRole.PROJECT_SPECIFIC_ROLE_PREFIX, "." + projectCid);
        if (projectRoles == null || roles.isEmpty()) {
            return;
        }
        revokeRoles(executor, actorName, "user", projectRoles);
    }

    private static void revokeRoles(ServiceExecutor executor, String actorName, String actorType,
            Collection<String> roles) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("name", actorName);
        dm.add("type", actorType);
        for (String role : roles) {
            dm.add("role", new String[] { "type", "role" }, role);
        }
        executor.execute("actor.revoke", dm.root());
    }

}
