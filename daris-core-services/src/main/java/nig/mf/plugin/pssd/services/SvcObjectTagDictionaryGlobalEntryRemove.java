package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.tag.GlobalTagDictionary;
import nig.mf.plugin.pssd.tag.TagDictionary;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectTagDictionaryGlobalEntryRemove extends PluginService {
    
    public static final String SERVICE_NAME = "om.pssd.object.tag.dictionary.global.entry.remove";

    private Interface _defn;
    
    public SvcObjectTagDictionaryGlobalEntryRemove(){
        _defn = new Interface();
        _defn.add(new Interface.Element("type", new EnumType(TagDictionary.TYPES),
                "the type of the objects that the tags apply to.", 1, 1));
        _defn.add(new Interface.Element("tag", StringType.DEFAULT, "The tag to be destroyed.", 1, Integer.MAX_VALUE));
    }
    
    @Override
    public Access access() {
        return ACCESS_ADMINISTER;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Removes entries from the specified global tag dictionary.";
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        PSSDObject.Type type = PSSDObject.Type.parse(args.value("type"));
        GlobalTagDictionary dict = new GlobalTagDictionary(type);
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
