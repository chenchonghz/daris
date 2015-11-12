package daris.client.gui.xml;

import java.util.List;

import arc.xml.XmlDoc;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class XmlTreeTableView extends TreeTableView<XmlTreeNode> {

    public XmlTreeTableView(XmlDoc.Element... es) {
        super(new TreeItem<XmlTreeNode>(null));
        getRoot().setExpanded(true);
        setShowRoot(false);
        /*
         * set columns
         */
        TreeTableColumn<XmlTreeNode, String> nameColumn = new TreeTableColumn<>("Name");
        nameColumn.setPrefWidth(250);
        nameColumn.setCellValueFactory(param -> {
            XmlTreeNode n = param.getValue().getValue();
            String v = n == null ? null : n.name();
            if (n.isXmlAttribute()) {
                v = "@" + v;
            }
            return new ReadOnlyStringWrapper(v);
        });

        TreeTableColumn<XmlTreeNode, String> valueColumn = new TreeTableColumn<>("Value");
        valueColumn.setPrefWidth(200);
        valueColumn.setCellValueFactory(param -> {
            XmlTreeNode n = param.getValue().getValue();
            String v = n == null ? null : n.value();
            return new ReadOnlyStringWrapper(v);
        });

        getColumns().setAll(nameColumn, valueColumn);

        for (XmlDoc.Element e : es) {
            add(e);
        }
    }

    public void add(XmlDoc.Element e) {
        getRoot().getChildren().add(createTreeItem(e));
    }

    private static TreeItem<XmlTreeNode> createTreeItem(XmlDoc.Element e) {
        TreeItem<XmlTreeNode> item = new TreeItem<XmlTreeNode>(new XmlTreeNode(e));
        item.setExpanded(true);
        if (e.hasAttributes()) {
            List<XmlDoc.Attribute> attrs = e.attributes();
            for (XmlDoc.Attribute attr : attrs) {
                TreeItem<XmlTreeNode> attrItem = new TreeItem<XmlTreeNode>(new XmlTreeNode(attr));
                attrItem.setExpanded(true);
                item.getChildren().add(attrItem);
            }
        }
        if (e.hasSubElements()) {
            List<XmlDoc.Element> elems = e.elements();
            for (XmlDoc.Element elem : elems) {
                TreeItem<XmlTreeNode> elemItem = createTreeItem(elem);
                item.getChildren().add(elemItem);
            }
        }
        return item;
    }
}
