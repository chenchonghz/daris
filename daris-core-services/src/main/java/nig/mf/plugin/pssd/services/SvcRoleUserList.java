package nig.mf.plugin.pssd.services;

import java.util.List;

import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcRoleUserList extends PluginService {

    public static final String SERVICE_NAME = "daris.role-user.list";

    private Interface _defn;

    public SvcRoleUserList() {
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
        return "Gets a list of role users that have been registered as users of the DaRIS PSSD model.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        List<XmlDoc.Element> res = SvcRoleMemberRegList
                .getRoleMembersFromRegistry(executor());
        if (res != null) {
            for (XmlDoc.Element re : res) {
                w.add("role-user", new String[] { "id", re.value("@id") },
                        re.value());
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
