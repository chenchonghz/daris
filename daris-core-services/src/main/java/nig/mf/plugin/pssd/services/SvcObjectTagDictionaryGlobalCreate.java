package nig.mf.plugin.pssd.services;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import nig.mf.plugin.pssd.PSSDObject;
import nig.mf.plugin.pssd.tag.GlobalTagDictionary;
import nig.mf.plugin.pssd.tag.TagDictionary;
import nig.mf.plugin.pssd.tag.TagDictionary.IfExists;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectTagDictionaryGlobalCreate extends PluginService {

    private Interface _defn;

    public SvcObjectTagDictionaryGlobalCreate() {
        _defn = new Interface();
        _defn.add(new Interface.Element(
                "type",
                new EnumType(TagDictionary.TYPES),
                "the type(s) of the objects that the tags apply to. If not specified, tag dictionaries for all the object types will be created.",
                0, TagDictionary.TYPES.length));
        _defn.add(new Interface.Element("if-exists", new EnumType(IfExists.values()),
                "The behavior if the dictionary already exists. Defaults to " + IfExists.ignore + ".", 0, 1));
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
        return "Creates global tag dictionaries. ";
    }

    @Override
    public void execute(Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
        Collection<String> ts = args.values("type");
        Set<PSSDObject.Type> types = new HashSet<PSSDObject.Type>();
        if (ts == null || ts.isEmpty()) {
            types.add(PSSDObject.Type.project);
            types.add(PSSDObject.Type.subject);
            types.add(PSSDObject.Type.ex_method);
            types.add(PSSDObject.Type.study);
            types.add(PSSDObject.Type.dataset);
            types.add(PSSDObject.Type.data_object);
        } else {
            for (String t : ts) {
                types.add(PSSDObject.Type.parse(t));
            }
        }
        IfExists ifExists = IfExists.parse(args.value("if-exists"), IfExists.ignore);
        for (PSSDObject.Type type : types) {
            new GlobalTagDictionary(type).create(ifExists);
        }
    }

    @Override
    public String name() {
        return "om.pssd.object.tag.dictionary.global.create";
    }

}
