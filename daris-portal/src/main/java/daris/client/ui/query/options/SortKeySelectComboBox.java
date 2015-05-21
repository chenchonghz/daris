package daris.client.ui.query.options;

import arc.mf.object.tree.Node;
import daris.client.model.query.options.SortKeyTree;
import daris.client.ui.widget.TreeSelectComboBox;

public class SortKeySelectComboBox extends TreeSelectComboBox<String> {

    public SortKeySelectComboBox(String key, SortKeyTree sortKeyTree) {
        super(key, sortKeyTree, false);
    }

    @Override
    protected String toString(String value) {
        return value;
    }

    @Override
    protected boolean canSelect(Node n) {
        if (n instanceof SortKeyTree.LeafNode || n instanceof arc.mf.xml.defn.tree.AttributeTreeNode
                || n instanceof arc.mf.xml.defn.tree.ElementTreeNode
                || n instanceof arc.mf.xml.defn.tree.ReferenceTreeNode) {
            return true;
        }
        return false;
    }

    @Override
    protected String transform(Node n) {
        if (n instanceof arc.mf.xml.defn.tree.AttributeTreeNode || n instanceof arc.mf.xml.defn.tree.ElementTreeNode
                || n instanceof arc.mf.xml.defn.tree.ReferenceTreeNode) {
            return "meta/" + n.path();
        } else {
            return n.path();
        }
    }

}
