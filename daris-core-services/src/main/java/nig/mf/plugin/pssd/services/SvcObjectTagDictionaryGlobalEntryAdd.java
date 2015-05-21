package nig.mf.plugin.pssd.services;

import java.util.List;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.tag.GlobalTagDictionary;
import nig.mf.plugin.pssd.tag.TagDictionary;
import nig.mf.plugin.pssd.tag.TagDictionary.IfExists;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectTagDictionaryGlobalEntryAdd extends PluginService {
    
    public static final String SERVICE_NAME = "om.pssd.object.tag.dictionary.global.entry.add";

    private Interface _defn;

    public SvcObjectTagDictionaryGlobalEntryAdd() {
        _defn = new Interface();
        _defn.add(new Interface.Element("type", new EnumType(TagDictionary.TYPES),
                "the type of the objects that the tags apply to.", 1, 1));
        Interface.Element te = new Interface.Element("tag", XmlDocType.DEFAULT,
                "The tag entry to be added to the dictionary.", 1, Integer.MAX_VALUE);
        te.add(new Interface.Element("name", StringType.DEFAULT, "The name of the tag.", 1, 1));
        te.add(new Interface.Element("description", StringType.DEFAULT, "The description about the tag.", 0, 1));
        _defn.add(te);
        _defn.add(new Interface.Element("if-exists", new EnumType(IfExists.values()),
                "The behavior if the tag entry already exists. Defaults to " + IfExists.ignore + ".", 0, 1));
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
        return "Adds entries to the specified global tag dictionary.";
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        PSSDObject.Type type = PSSDObject.Type.parse(args.value("type"));
        IfExists ifExists = IfExists.parse(args.value("if-exists"), IfExists.ignore);
        GlobalTagDictionary dict = new GlobalTagDictionary(type);
        List<XmlDoc.Element> tes = args.elements("tag");
        for (XmlDoc.Element te : tes) {
            dict.addEntry(te.value("name"), te.value("description"), ifExists);
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
