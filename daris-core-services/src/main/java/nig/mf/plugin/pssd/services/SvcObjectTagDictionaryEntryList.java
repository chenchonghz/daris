package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.tag.ProjectSpecificTagDictionary;
import nig.mf.plugin.pssd.tag.TagDictionary;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectTagDictionaryEntryList extends PluginService {

    private Interface _defn;

    public SvcObjectTagDictionaryEntryList() {
        _defn = new Interface();
        _defn.add(new Interface.Element("project", CiteableIdType.DEFAULT, "The id of the project.", 1, 1));
        _defn.add(new Interface.Element("type", new EnumType(TagDictionary.TYPES),
                "the type of the objects that the tags apply to.", 1, 1));
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
        return "Lists the (tag) entries in the tag dictionary for the specified project.";
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        String projectCid = args.value("project");
        if (!Project.exists(projectCid)) {
            throw new Exception("Project " + projectCid + " does not exist or it is not valid.");
        }
        PSSDObject.Type type = PSSDObject.Type.parse(args.value("type"));
        ProjectSpecificTagDictionary dict = new ProjectSpecificTagDictionary(projectCid, type);
        if (!dict.exists()) {
            return;
        }
        dict.listEntries(w);
    }

    @Override
    public String name() {
        return "om.pssd.object.tag.dictionary.entry.list";
    }

}
