package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.tag.ProjectSpecificTagDictionary;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcObjectTagDictionaryGet extends PluginService {

    private Interface _defn;

    public SvcObjectTagDictionaryGet() {
        _defn = new Interface();
        _defn.add(new Interface.Element("cid", CiteableIdType.DEFAULT, "The citeable id of the object.", 1, 1));
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
        return "Gets the tag dictionary for the specified object.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        String cid = args.value("cid");
        ProjectSpecificTagDictionary dict = ProjectSpecificTagDictionary.dictionaryFor(cid);
        w.push("dictionary", new String[] { "name", dict.name() });
        dict.describeEntries(w);
        w.pop();
    }

    @Override
    public String name() {
        return "om.pssd.object.tag.dictionary.get";
    }
    
    

}
