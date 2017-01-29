package daris.plugin.services;

import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import daris.plugin.model.DataUse;
import daris.plugin.model.ProjectRole;

public class SvcProjectUserAdd extends PluginService {

    public static final String SERVICE_NAME = "daris.project.user.add";

    private Interface _defn;

    public SvcProjectUserAdd() {
        _defn = new Interface();
        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the project.", 0, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the project.", 0, 1));

        Interface.Element user = new Interface.Element("user", XmlDocType.DEFAULT,
                "User to become a user of the project.", 0, Integer.MAX_VALUE);

        Interface.Element userAuthority = new Interface.Element("authority", StringType.DEFAULT,
                "The authority of interest. Defaults to local.", 0, 1);
        userAuthority.add(new Interface.Attribute("protocol", StringType.DEFAULT,
                "The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.",
                0));
        user.add(userAuthority);

        user.add(new Interface.Element("domain", StringType.DEFAULT, "The domain name of the user.", 1, 1));
        user.add(new Interface.Element("user", StringType.DEFAULT, "The user name within the domain.", 1, 1));
        user.add(new Interface.Element("role", new EnumType(ProjectRole.Type.stringValues()),
                "The project role bestowed on the user.", 1, 1));
        user.add(new Interface.Element("data-use", new EnumType(DataUse.stringValues()),
                "Specify how this user (only if role is 'member' or 'guest') will use data from this project", 0, 1));
        _defn.add(user);

        Interface.Element roleUser = new Interface.Element("role-user", XmlDocType.DEFAULT,
                "Role to become a user of this project.", 0, Integer.MAX_VALUE);
        roleUser.add(new Interface.Element("name", StringType.DEFAULT, "The name of the role.", 1, 1));
        roleUser.add(new Interface.Element("role", new EnumType(ProjectRole.Type.stringValues()),
                "The project role bestowed on the role user.", 1, 1));
        roleUser.add(new Interface.Element("data-use", new EnumType(DataUse.stringValues()),
                "Specify how this role user (only if role is 'member' or 'guest') will use data from this project", 0,
                1));
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
        return "Add project users.";
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
                DataUse projectDataUse = DataUse.fromString(ae.value("meta/daris:pssd-project/data-use"));

                /*
                 * add new users
                 */
                addUsers(executor, projectCid, projectDataUse, args.elements("user"), args.elements("role-user"), true);

                /*
                 * generate system event
                 */
                SvcProjectUserSet.generateSystemEvent(executor, projectCid);
                return false;
            }
        }).execute(executor());

    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    public static void addUser(ServiceExecutor executor, String projectCid, DataUse projectDataUse, String domain,
            String user, String authority, String protocol, ProjectRole.Type type, DataUse dataUse, boolean replace)
            throws Throwable {
        /*
         * remove user
         */
        if (replace) {
            SvcProjectUserRemove.removeUser(executor, domain, user, authority, protocol, projectCid);
        }
        /*
         * add user
         */
        String role = ProjectRole.roleNameOf(projectCid, type);
        grantUserRole(executor, domain, user, authority, protocol, role, true);
        dataUse = dataUseFor(projectDataUse, type, dataUse);
        if (dataUse != null) {
            grantDataUseRole(executor, projectCid, domain, user, authority, protocol, dataUse);
        }
    }

    private static void grantDataUseRole(ServiceExecutor executor, String projectCid, String domain, String user,
            String authority, String protocol, DataUse dataUse) throws Throwable {
        String role = ProjectRole.subjectDataUseRoleNameOf(projectCid, dataUse);
        grantUserRole(executor, domain, user, authority, protocol, role, true);
    }

    public static void addRoleUser(ServiceExecutor executor, String projectCid, DataUse projectDataUse, String roleName,
            ProjectRole.Type type, DataUse dataUse, boolean replace) throws Throwable {
        /*
         * remove role-user
         */
        if (replace) {
            SvcProjectUserRemove.removeRoleUser(executor, roleName, projectCid);
        }
        /*
         * add role-user
         */
        String role = ProjectRole.roleNameOf(projectCid, type);
        grantRoleRole(executor, roleName, role, true);
        dataUse = dataUseFor(projectDataUse, type, dataUse);
        if (dataUse != null) {
            grantDataUseRole(executor, projectCid, roleName, dataUse);
        }
    }

    private static void grantDataUseRole(ServiceExecutor executor, String projectCid, String roleName, DataUse dataUse)
            throws Throwable {
        String role = ProjectRole.subjectDataUseRoleNameOf(projectCid, dataUse);
        grantRoleRole(executor, roleName, role, true);
    }

    static DataUse dataUseFor(DataUse projectDataUse, ProjectRole.Type roleType, DataUse dataUse) {
        if (roleType == ProjectRole.Type.PROJECT_ADMINISTRATOR || roleType == ProjectRole.Type.SUBJECT_ADMINISTRATOR) {
            return null;
        }
        if (projectDataUse == null) {
            projectDataUse = DataUse.SPECIFIC;
        }
        if (dataUse == null) {
            dataUse = DataUse.SPECIFIC;
        }
        if (projectDataUse == DataUse.SPECIFIC) {
            if (dataUse == DataUse.EXTENDED || dataUse == DataUse.UNSPECIFIED) {
                dataUse = projectDataUse;
            }
        } else if (projectDataUse == DataUse.EXTENDED) {
            if (dataUse == DataUse.UNSPECIFIED) {
                dataUse = projectDataUse;
            }
        } else if (projectDataUse == DataUse.UNSPECIFIED) {
            // Nothing to do
        }
        return dataUse;
    }

    static void grantUserRole(ServiceExecutor executor, String domain, String user, String authority, String protocol,
            String role, boolean createRoleIfNotExist) throws Throwable {
        if (!roleExists(executor, role) && createRoleIfNotExist) {
            createRole(executor, role, null);
        }
        XmlDocMaker dm = new XmlDocMaker("args");
        if (authority != null) {
            dm.add("authority", new String[] { "protocol", protocol });
        }
        dm.add("domain", domain);
        dm.add("user", user);
        dm.add("role", new String[] { "type", "role" }, role);
        System.out.println(dm.root());
        executor.execute("user.grant", dm.root());
    }

    static void grantRoleRole(ServiceExecutor executor, String roleName, String role, boolean createRoleIfNotExist)
            throws Throwable {
        if (!roleExists(executor, role) && createRoleIfNotExist) {
            createRole(executor, role, null);
        }
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("name", roleName);
        dm.add("type", "role");
        dm.add("role", new String[] { "type", "role" }, role);
        executor.execute("actor.grant", dm.root());
    }

    static void createRole(ServiceExecutor executor, String role, String description) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("role", role);
        if (description != null) {
            dm.add("description", description);
        }
        dm.add("ifexists", "ignore");
        executor.execute("authorization.role.create", dm.root());
    }

    static boolean roleExists(ServiceExecutor executor, String role) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("role", role);
        return executor.execute("authorization.role.exists", dm.root()).booleanValue("exists");
    }

    static void addUsers(ServiceExecutor executor, String projectCid, DataUse projectDataUse, List<XmlDoc.Element> ues,
            List<XmlDoc.Element> rues, boolean replace) throws Throwable {

        if (ues != null) {
            for (XmlDoc.Element ue : ues) {
                addUser(executor, projectCid, projectDataUse, ue.value("domain"), ue.value("user"),
                        ue.value("authority"), ue.value("authority/@protocol"),
                        ProjectRole.Type.fromString(ue.value("role")), DataUse.fromString(ue.value("data-use")),
                        replace);
            }
        }

        if (rues != null) {
            for (XmlDoc.Element rue : rues) {
                addRoleUser(executor, projectCid, projectDataUse, rue.value("name"),
                        ProjectRole.Type.fromString(rue.value("role")), DataUse.fromString(rue.value("data-use")),
                        replace);
            }
        }
    }

}
