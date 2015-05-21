package daris.client.ui.query;

import arc.mf.model.asset.document.MetadataDocumentRef;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.Tree;
import daris.client.model.query.filter.mf.MetadataPath;
import daris.client.ui.widget.TreeSelectComboBox;

public class MetadataPathSelectComboBox extends TreeSelectComboBox<MetadataPath> {

    public MetadataPathSelectComboBox(MetadataPath value, Tree tree, boolean showRoot) {
        super(value, tree, showRoot);
    }

    @Override
    protected String toString(MetadataPath value) {
        return value == null ? null : value.path();
    }

    @Override
    protected boolean canSelect(Node n) {
        Object o = n.object();
        if (o != null) {
            if (o instanceof MetadataDocumentRef || o instanceof arc.mf.xml.defn.Node) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected MetadataPath transform(Node n) {
        Object o = n.object();
        if (o != null) {
            if (o instanceof MetadataDocumentRef) {
                return new MetadataPath((MetadataDocumentRef) o);
            }
            if (o instanceof arc.mf.xml.defn.Node) {
                return new MetadataPath((arc.mf.xml.defn.Node) o);
            }
        }
        return null;
    }

}
