package daris.client.model.query.options;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.dtype.DocType;
import arc.mf.model.asset.document.MetadataDocument;
import arc.mf.model.asset.document.MetadataDocumentRef;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.xml.defn.Attribute;
import arc.mf.xml.defn.Element;
import arc.mf.xml.defn.Node;

public class XPath {

    private String _ekey;
    private String _ename;
    private String _value;

    private String _dict;
    private String _dictVariant;

    public XPath(XmlElement xe) {
        _value = xe.value();
        _ekey = xe.value("@ekey");
        _ename = xe.value("@ename");
    }

    public XPath(String ekey, String ename, String value, String dict, String dictVariant) {
        _ekey = ekey;
        _ename = ename;
        _value = value;
        _dict = dict;
        _dictVariant = dictVariant;
    }

    public XPath(String ekey, String ename, String value) {
        this(ekey, ename, value, null, null);
    }

    public XPath(String ename, String value) {
        this(null, ename, value);
    }

    public String key() {
        return _ekey;
    }

    public String value() {
        return _value;
    }

    public String name() {
        return _ename;
    }

    public void setName(String name) {
        _ename = name;
    }

    public void save(XmlWriter w) {
        if (_dict != null && _dictVariant != null) {
            w.add("xpath", new String[] { "ekey", key(), "ename", name() }, "dictionary.term.variant('" + _dict
                    + "',xvalue('" + value() + "'),'" + _dictVariant + "')");
        } else {
            w.add("xpath", new String[] { "ekey", key(), "ename", name() }, value());
        }
    }

    public void setDictionaryVariant(String dict, String variant) {
        _dict = dict;
        _dictVariant = variant;
    }

    public void setDictionaryVariant(String variant) {
        _dictVariant = variant;
    }

    public void setDictionary(String dict) {
        _dict = dict;
    }

    public String dictionary() {
        return _dict;
    }

    public String dictionaryVariant() {
        return _dictVariant;
    }
    
    

    public MetadataDocumentRef document() {
        if (_value != null) {
            if (_value.startsWith("meta/")) {
                String path = _value.substring(5);
                int idx = path.indexOf('/');
                if (idx != -1) {
                    path = path.substring(0, idx);
                }
                return new MetadataDocumentRef(path);
            }
        }
        return null;
    }

    public void resolveDefinition(final ObjectResolveHandler<Node> rh) {
        MetadataDocumentRef doc = document();
        if (doc == null) {
            if (rh != null) {
                rh.resolved(null);
            }
            return;
        }
        doc.resolve(new ObjectResolveHandler<MetadataDocument>() {

            @Override
            public void resolved(MetadataDocument o) {
                Node node = null;
                if (o != null) {
                    String path = _value.startsWith("meta/") ? _value.substring(5) : _value;
                    node = findNode(o.definition().root(), path);
                }
                if (rh != null) {
                    rh.resolved(node);
                }
            }
        });
    }

    private static Node findNode(Element doc, String xpath) {

        List<Attribute> as = doc.attributes();
        if (as != null) {
            for (Attribute a : as) {
                if (a.path().equals(xpath)) {
                    return a;
                }
            }
        }
        List<Element> es = doc.elements();
        if (es != null) {
            for (Element e : es) {
                if (e.type() instanceof DocType) {
                    Node node = findNode(e, xpath);
                    if (node != null) {
                        return node;
                    }
                } else {
                    if (e.path().equals(xpath)) {
                        return e;
                    }
                }
            }
        }
        return null;
    }
}
