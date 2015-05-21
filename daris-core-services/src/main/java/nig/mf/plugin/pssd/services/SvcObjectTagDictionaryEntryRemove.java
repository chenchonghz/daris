package nig.mf.plugin.pssd.services;

import java.util.Collection;

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

public class SvcObjectTagDictionaryEntryRemove extends PluginService {
    
    public static final String SERVICE_NAME = "om.pssd.object.tag.dictionary.entry.remove";
    
    private Interface _defn;

    public SvcObjectTagDictionaryEntryRemove() {
        _defn = new Interface();
        _defn.add(new Interface.Element("project", CiteableIdType.DEFAULT, "The citeable id of the project.", 1, 1));
        _defn.add(new Interface.Element("type", new EnumType(TagDictionary.TYPES),
                "the type of the objects that the tags apply to.", 1, 1));
        _defn.add(new Interface.Element("tag", StringType.DEFAULT, "The tag to be destroyed.", 1, Integer.MAX_VALUE));
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
        return "Remove one or more entries from the specified tag dictionary.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter arg3) throws Throwable {
        String projectCid = args.value("project");
        if (!Project.exists(projectCid)) {
            throw new Exception("Project " + projectCid + " does not exist or it is not valid.");
        }
        PSSDObject.Type type = PSSDObject.Type.parse(args.value("type"));
        ProjectSpecificTagDictionary dict = new ProjectSpecificTagDictionary(projectCid, type);
        Collection<String> tags = args.values("tag");
        for (String tag : tags) {
            dict.removeEntry(tag);
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }
}
