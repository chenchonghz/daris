package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.tag.GlobalTagDictionary;
import nig.mf.plugin.pssd.tag.TagDictionary;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectTagDictionaryGlobalEntryList extends PluginService {

    public static final String SERVICE_NAME = "om.pssd.object.tag.dictionary.global.entry.list";

    private Interface _defn;

    public SvcObjectTagDictionaryGlobalEntryList() {
        _defn = new Interface();
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
        return "Lists the entries in the specified tag dictionary.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        PSSDObject.Type type = PSSDObject.Type.parse(args.value("type"));
        GlobalTagDictionary dict = new GlobalTagDictionary(type);
        if (!dict.exists()) {
            return;
        }
        dict.listEntries(w);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
