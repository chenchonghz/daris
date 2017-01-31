package daris.plugin.services;

import java.util.ArrayList;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import daris.plugin.DaRIS;

public class SvcProjectRoleUserCandidateList extends PluginService {

    public static final String SERVICE_NAME = "daris.project.role-user.candidate.list";

    public static List<String> getProjectRoleUserCandidates(ServiceExecutor executor) throws Throwable {
        List<String> result = new ArrayList<String>();
        int idx = 1;
        int size = 1000;
        int remaining = -1;
        while (remaining != 0) {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("count", true);
            dm.add("idx", idx);
            dm.add("size", size);
            XmlDoc.Element xe = executor.execute("authorization.role.describe", dm.root());
            List<XmlDoc.Element> res = xe.elements("role");
            if (res != null) {
                for (XmlDoc.Element re : res) {
                    String description = re.value("description");
                    if (description != null && description.endsWith(DaRIS.PROJECT_ROLE_USER_CANDIDATE_TRAILING_MARK)) {
                        result.add(re.value("name"));
                    }
                }
            }
            idx += size;
            remaining = xe.intValue("cursor/remaining", 0);
        }
        if (result.isEmpty()) {
            return null;
        }
        return result;
    }

    public static String getActorId(ServiceExecutor executor, String role) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("type", "role");
        dm.add("name", role);
        return executor.execute("actor.describe", dm.root()).value("actor/@id");
    }

    private Interface _defn;

    public SvcProjectRoleUserCandidateList() {
        _defn = new Interface();
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
        return "List all roles that can be granted as project role users.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        List<String> roles = getProjectRoleUserCandidates(executor());
        if (roles != null) {
            for (String role : roles) {
                String actorId = getActorId(executor(), role);
                w.add("role", new String[] { "id", actorId }, role);
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }
}
