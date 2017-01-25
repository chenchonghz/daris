package daris.plugin.services;

import java.util.Collection;

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
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import daris.plugin.model.DataUse;
import daris.plugin.model.ModelRole;
import daris.plugin.model.ProjectRole;

public class SvcProjectUserSet extends PluginService {

    public static final String SERVICE_NAME = "daris.project.user.set";

    private Interface _defn;

    public SvcProjectUserSet() {

        _defn = new Interface();

        _defn.add(new Interface.Element("id", AssetType.DEFAULT, "The asset id of the project.", 0, 1));
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the project.", 0, 1));

        Interface.Element user = new Interface.Element("user", XmlDocType.DEFAULT,
                "User to become a user of the project.", 1, Integer.MAX_VALUE);

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

    public String name() {

        return SERVICE_NAME;
    }

    public String description() {

        return "Update/Replace the project users with those specified.";
    }

    public Interface definition() {

        return _defn;
    }

    public Access access() {

        return ACCESS_MODIFY;
    }

    public void execute(final XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

        /*
         * check if there is at least one project admin
         */
        checkOneAdmin(executor(), args);

        final XmlDoc.Element ae = ServiceUtils.getAssetMeta(executor(), args.value("id"), args.value("cid"));

        /*
         * validate if it is a project, also checks the user self has sufficient
         * privilege
         */
        validate(executor(), ae);

        new AtomicTransaction(new AtomicOperation() {

            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {

                String projectCid = ae.value("cid");
                DataUse projectDataUse = DataUse.fromString(ae.value("meta/daris:pssd-project/data-use"));

                /*
                 * remove all users
                 */
                SvcProjectUserRemove.removeAllUsers(executor, projectCid);

                /*
                 * add new users
                 */
                SvcProjectUserAdd.addUsers(executor, projectCid, projectDataUse, args.elements("user"),
                        args.elements("role-user"), false);
                return false;
            }
        }).execute(executor());

    }

    private static void checkOneAdmin(ServiceExecutor executor, XmlDoc.Element args) throws Throwable {
        /*
         * check if there is at least one admin
         */
        Collection<String> userRoles = args.values("user/role");
        boolean hasAdmin = userRoles != null && userRoles.contains(ProjectRole.Type.PROJECT_ADMINISTRATOR.toString());
        if (!hasAdmin) {
            Collection<String> roleUserRoles = args.values("role-user/role");
            hasAdmin = roleUserRoles != null
                    && roleUserRoles.contains(ProjectRole.Type.PROJECT_ADMINISTRATOR.toString());
        }
        if (!hasAdmin) {
            throw new IllegalArgumentException(
                    "No project-administrator is set. At least one project administrator is required.");
        }
    }

    static void validate(ServiceExecutor executor, XmlDoc.Element ae) throws Throwable {

        String cid = ae.value("cid");
        /*
         * validate if it is project
         */
        boolean isProject = "project".equals(ae.value("meta/daris:pssd-object/type"));
        if (!isProject) {
            throw new IllegalArgumentException("The specified object " + cid + " is not a DaRIS project.");
        }

        /*
         * Executing user must have specific project admin or overall admin role
         * to run this service
         */
        if (!(selfHaveRole(executor, ProjectRole.projectAdministratorRoleName(cid))
                || selfHaveRole(executor, ModelRole.objectAdminRoleName()))) {
            throw new Exception(
                    "Caller must hold specific project administrator role for this project or the global administrator role.");
        }
    }

    private static boolean selfHaveRole(ServiceExecutor executor, String role) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("role", new String[] { "type", "role" }, role);
        return executor.execute("actor.self.have", dm.root()).booleanValue("role");
    }

}
