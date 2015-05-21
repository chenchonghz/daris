package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.tag.ProjectSpecificTagDictionary;
import nig.mf.plugin.pssd.tag.TagDictionary;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectTagDictionaryEntryExists extends PluginService {
    private Interface _defn;

    public SvcObjectTagDictionaryEntryExists() {
        _defn = new Interface();
        _defn.add(new Interface.Element("project", CiteableIdType.DEFAULT, "The id of the project.", 1, 1));
        _defn.add(new Interface.Element("type", new EnumType(TagDictionary.TYPES),
                "the type of the objects that the tag applys to.", 1, 1));
        _defn.add(new Interface.Element("tag", StringType.DEFAULT, "The tag to check existence.", 1, 1));
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
        return "Checks if the given tag exists.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        String projectCid = args.value("project");
        if (!Project.exists(projectCid)) {
            throw new Exception("Project " + projectCid + " does not exist or it is not valid.");
        }
        PSSDObject.Type type = PSSDObject.Type.parse(args.value("type"));
        ProjectSpecificTagDictionary dict = new ProjectSpecificTagDictionary(projectCid, type);
        String tag = args.value("tag");
        boolean exists = dict.containsEntry(tag);
        w.add("exists", new String[]{"tag", tag}, exists);
    }

    @Override
    public String name() {
        return "om.pssd.object.tag.dictionary.entry.exists";
    }

}
