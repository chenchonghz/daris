package nig.mf.plugin.pssd.tag;

import java.util.List;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.user.Self;
import nig.mf.pssd.CiteableIdUtil;
import arc.mf.plugin.ServiceExecutor;

public class ProjectSpecificTagDictionary extends TagDictionary {

    public static String dictionaryNameOf(String project, PSSDObject.Type type) {
        return TagDictionary.DICTIONARY_NAMESPACE + ":pssd." + type + ".tags." + project;
    }
    

    private String _project;

    public ProjectSpecificTagDictionary(String project, PSSDObject.Type type) {
        super(dictionaryNameOf(project, type), type);
        _project = project;
    }

    public String project() {
        return _project;
    }

    public void create(IfExists ifExists) throws Throwable {
        /*
         * check if the user has sufficient privilege
         */
        if (!Self.isProjectAdmin(project())) {
            throw new Exception("You do not sufficient priviledge to create a dictionary.");
        }
        super.create(ifExists);

        /*
         * import from global dictionary
         */
        GlobalTagDictionary gd = GlobalTagDictionary.dictionaryFor(type());
        List<Entry> entries = gd.entries();
        if (entries != null) {
            for (Entry e : entries) {
                addEntry(e.term(), e.definition(), IfExists.ignore);
            }
        }
    }

    public void destroy() throws Exception, Throwable {

        /*
         * check if the user has sufficient privilege
         */
        if (!Self.isProjectAdmin(project())) {
            throw new Exception("You do not sufficient priviledge to desctory dictionary " + name()
                    + ".");
        }
        super.destroy();
    }

    public void addEntry(String name, String description, IfExists ifExists) throws Throwable {

        /*
         * check if the user has sufficient privilege
         */
        if (!Self.isMember(project()) && !Self.isSystemAdministrator()) {
            throw new Exception("You do not have sufficient priviledge to create tag for project "
                    + project() + ".");
        }
        super.addEntry(name, description, ifExists);
    }

    public void removeEntry(String name) throws Throwable {

        /*
         * check if the user has sufficient privilege
         */
        if (!Self.isMember(project()) && !Self.isSystemAdministrator()) {
            throw new Exception("You do not have sufficient priviledge to remove tag " + name + ".");
        }
        super.removeEntry(name);
    }

    public static ProjectSpecificTagDictionary dictionaryFor(String cid) throws Throwable {
        if (cid == null) {
            return null;
        }
        PSSDObject.Type type = PSSDObject.typeOf(cid);
        String projectCid = CiteableIdUtil.getProjectId(cid);
        if (projectCid == null) {
            throw new Exception("Failed to parse the project that " + type + " " + cid
                    + " belongs to.");
        }
        return new ProjectSpecificTagDictionary(projectCid, type);
    }

    public static void destroyAll(ServiceExecutor executor, String project) throws Throwable {
        PSSDObject.Type[] types = PSSDObject.Type.values();
        for (PSSDObject.Type type : types) {
            String dictName = dictionaryNameOf(project, type);
            String args = "<args><name>" + dictName + "</name></args>";
            if (executor.execute("dictionary.exists", args, null, null).booleanValue("exists")) {
                executor.execute("dictionary.destroy", args, null, null);
            }
        }
    }

}
