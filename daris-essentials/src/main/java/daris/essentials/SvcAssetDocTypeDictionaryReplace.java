package daris.essentials;

import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcAssetDocTypeDictionaryReplace extends PluginService {

    public static final String SERVICE_NAME = "nig.asset.doc.type.dictionary.replace";

    private Interface _defn;

    public SvcAssetDocTypeDictionaryReplace() {
        _defn = new Interface();
        _defn.add(new Interface.Element("type", StringType.DEFAULT,
                "The document type name.", 1, 1));
        _defn.add(new Interface.Element("dictionary", StringType.DEFAULT,
                "The dictionary name.", 1, 1));
        _defn.add(new Interface.Element("new-dictionary", StringType.DEFAULT,
                "The new dictionary name.", 1, 1));
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
        return "Replaces dictionary in the specified doc type.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs,
            XmlWriter w) throws Throwable {
        String type = args.value("type");
        String dictionary = args.value("dictionary");
        String newDictionary = args.value("new-dictionary");
        replaceDictionary(executor(), type, dictionary, newDictionary);
    }

    public static void replaceDictionary(ServiceExecutor executor, String type,
            String dictionary, String newDictionary) throws Throwable {
        XmlDoc.Element te = executor
                .execute("asset.doc.type.describe",
                        "<args><type>" + type + "</type></args>", null, null)
                .element("type");
        List<XmlDoc.Element> elements = te.elements("definition/element");
        if (elements != null) {
            for (XmlDoc.Element element : elements) {
                replaceDictionary(element, dictionary, newDictionary);
            }
        }
        XmlDoc.Element e = te.element("access");
        if (e != null) {
            te.remove(e);
        }
        e = te.element("creator");
        if (e != null) {
            te.remove(e);
        }
        e = te.element("ctime");
        if (e != null) {
            te.remove(e);
        }

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("type", type);
        dm.add(te, false);

        // modify the doc type
        executor.execute("asset.doc.type.update", dm.root());
    }

    private static void replaceDictionary(XmlDoc.Element element,
            String dictionary, String newDictionary) throws Throwable {
        if (element.value("@type").equals("enumeration")) {
            XmlDoc.Element dictionaryElement = element
                    .element("restriction[@base='enumeration']/dictionary");
            if (dictionaryElement != null
                    && dictionary.equals(dictionaryElement.value())) {
                dictionaryElement.setValue(newDictionary);
            }
        }
        List<XmlDoc.Element> attributes = element.elements("attribute");
        if (attributes != null) {
            for (XmlDoc.Element attribute : attributes) {
                if (attribute.value("@type").equals("enumeration")) {
                    XmlDoc.Element dictionaryElement = attribute.element(
                            "restriction[@base='enumeration']/dictionary");
                    if (dictionaryElement != null
                            && dictionary.equals(dictionaryElement.value())) {
                        dictionaryElement.setValue(newDictionary);
                    }
                }
            }
        }
        List<XmlDoc.Element> subElements = element.elements("element");
        if (subElements != null) {
            for (XmlDoc.Element subElement : subElements) {
                replaceDictionary(subElement, dictionary, newDictionary);
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
