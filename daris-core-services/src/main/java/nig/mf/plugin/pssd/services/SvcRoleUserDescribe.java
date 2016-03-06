package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.ProjectMember;

public class SvcRoleUserDescribe extends PluginService {

    public static final String SERVICE_NAME = "daris.role-user.describe";

    private Interface _defn;

    public SvcRoleUserDescribe() {
        _defn = new Interface();
        _defn.add(new Interface.Element("list-projects", BooleanType.DEFAULT,
                "List the (local only) projects to which the user(s) have access? Defaults to false.",
                0, 1));

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
        return "Returns information on role users that have been registered as users of DaRIS PSSD object model.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outptus,
            XmlWriter w) throws Throwable {
        boolean listProjects = args.booleanValue("list-projects", false);
        List<XmlDoc.Element> res = SvcRoleMemberRegList
                .getRoleMembersFromRegistry(executor());
        if (res != null) {
            for (XmlDoc.Element re : res) {
                describeRoleUser(executor(), w, re.value(), listProjects);
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    void describeRoleUser(ServiceExecutor executor, XmlWriter w, String role,
            boolean listProjects) throws Throwable {

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("type", "role");
        dm.add("name", role);
        XmlDoc.Element r = executor.execute("actor.describe", dm.root());

        if (r != null) {
            w.push("role-user",
                    new String[] { "id", r.value("actor/@id"), "member", role });
        }
        // list associated projects
        if (listProjects) {
            ProjectMember projectMember = new ProjectMember(role);
            Collection<Project.ProjectCIDAndRole> ps = projectMember
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

}
