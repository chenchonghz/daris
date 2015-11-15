package daris.client.gui.xml;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import arc.xml.XmlDoc;
import javafx.scene.control.TreeItem;

public class XmlTreeTableView extends KVTreeTableView<String, XmlDoc.Node> {

    public XmlTreeTableView(XmlDoc.Element e, boolean self) {
        if (self) {
            addElement(e);
        } else {
            getRoot().setValue(
                    new SimpleEntry<String, XmlDoc.Node>(e.name(), e));
            List<XmlDoc.Element> ses = e.elements();
            if (ses != null) {
                for (XmlDoc.Element se : ses) {
                    addElement(se);
                }
            }
        }
    }

    public XmlTreeTableView(XmlDoc.Element... es) {
        if (es != null) {
            for (XmlDoc.Element e : es) {
                addElement(e);
            }
        }
    }

    public void addElement(XmlDoc.Element e) {
        addElement(getRoot(), e, false);
    }

    public void addElement(TreeItem<Entry<String, XmlDoc.Node>> parent,
            XmlDoc.Element e) {
        addElement(parent, e, true);
    }

    protected void addElement(TreeItem<Entry<String, XmlDoc.Node>> parent,
            XmlDoc.Element e, boolean showAttributes) {
        TreeItem<Entry<String, XmlDoc.Node>> item = addEntry(parent, e.name(),
                e);
        if (e.hasAttributes() && showAttributes) {
            List<XmlDoc.Attribute> attrs = e.attributes();
            for (XmlDoc.Attribute attr : attrs) {
                addEntry(item, attr.name(), attr);
            }
        }
        if (e.hasSubElements()) {
            List<XmlDoc.Element> elems = e.elements();
            for (XmlDoc.Element elem : elems) {
                addElement(item, elem, true);
            }
        }
    }

    @Override
    protected String valueToString(XmlDoc.Node node) {
        return node.value();
    }

    @Override
    protected String keyToString(String key, XmlDoc.Node node) {
        if (node instanceof XmlDoc.Attribute) {
            return "@" + node.name();
        } else {
            return node.name();
        }

    }
}
