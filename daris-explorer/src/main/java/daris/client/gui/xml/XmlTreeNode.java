package daris.client.gui.xml;

import java.util.ArrayList;
import java.util.List;

import arc.xml.XmlDoc;

@SuppressWarnings("rawtypes")
public class XmlTreeNode<T extends XmlDoc.Node> {

    private T _obj;

    public XmlTreeNode(T obj) {
        _obj = obj;
    }

    public List<XmlDoc.Attribute> attributes() {
        if (isXmlAttribute()) {
            return null;
        }
        XmlDoc.Element e = ((XmlDoc.Element) _obj);
        return e.attributes();
    }

    public List<XmlDoc.Element> elements() {
        if (isXmlAttribute()) {
            return null;
        }
        XmlDoc.Element e = ((XmlDoc.Element) _obj);
        return e.elements();
    }

    public List<XmlTreeNode> attributeNodes() {
        if (isXmlAttribute()) {
            return null;
        }
        List<XmlDoc.Attribute> attrs = attributes();
        if (attrs != null && !attrs.isEmpty()) {
            List<XmlTreeNode> ans = new ArrayList<XmlTreeNode>(attrs.size());
            for (XmlDoc.Attribute attr : attrs) {
                ans.add(new XmlTreeNode<XmlDoc.Attribute>(attr));
            }
            return ans;
        }
        return null;
    }

    public List<XmlTreeNode> elementNodes() {
        if (isXmlAttribute()) {
            return null;
        }
        List<XmlDoc.Element> elems = elements();
        if (elems != null && !elems.isEmpty()) {
            List<XmlTreeNode> ans = new ArrayList<XmlTreeNode>(elems.size());
            for (XmlDoc.Element elem : elems) {
                ans.add(new XmlTreeNode<XmlDoc.Element>(elem));
            }
            return ans;
        }
        return null;
    }

    public List<XmlTreeNode> subNodes() {
        if (isXmlAttribute()) {
            return null;
        }
        List<XmlTreeNode> ans = attributeNodes();
        List<XmlTreeNode> ens = elementNodes();
        if ((ans == null || ans.isEmpty()) && (ens == null || ens.isEmpty())) {
            return null;
        }
        int size = 0;
        if (ans != null && !ans.isEmpty()) {
            size += ans.size();
        }
        if (ens != null && !ens.isEmpty()) {
            size += ens.size();
        }
        List<XmlTreeNode> ns = new ArrayList<XmlTreeNode>(size);
        if (ans != null) {
            ns.addAll(ans);
        }
        if (ens != null) {
            ns.addAll(ens);
        }
        return ns;
    }

    public boolean isXmlAttribute() {
        return _obj instanceof XmlDoc.Attribute;
    }

    public boolean isXmlElement() {
        return _obj instanceof XmlDoc.Element;
    }

    public String name() {
        return _obj.name();
    }

    public String value() {
        return _obj.value();
    }

}
