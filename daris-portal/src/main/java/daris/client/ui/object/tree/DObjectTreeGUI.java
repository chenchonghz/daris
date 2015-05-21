package daris.client.ui.object.tree;

import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUI;
import arc.gui.gwt.widget.tree.TreeGUIEventHandler;
import arc.gui.gwt.widget.tree.TreeNodeGUI;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.tree.DObjectTree;
import daris.client.model.object.tree.DObjectTreeNode;

/**
 * Traditional tree view, with containers and leaves.
 * 
 * @author wilson
 * 
 */
public class DObjectTreeGUI extends TreeGUI {

    private TreeNodeGUI _selectedNodeGUI;

    public DObjectTreeGUI(DObjectTree tree, ScrollPolicy sp, TreeGUIEventHandler teh) {
        super(tree, sp, teh);
    }

    protected boolean selectOrDeselectNode(TreeNodeGUI ng) {
        boolean selectionChanged = super.selectOrDeselectNode(ng);
        if (selectionChanged) {
            _selectedNodeGUI = ng;
        }
        return selectionChanged;
    }

    public void refreshSelectedNode() {
        if (_selectedNodeGUI != null) {
            ((DObjectTreeNode) _selectedNodeGUI.node()).refresh(_selectedNodeGUI);
        }
    }

    TreeNodeGUI selectedNodeGUI() {
        return _selectedNodeGUI;
    }

    public DObjectTreeNode selectedNode() {
        if (_selectedNodeGUI != null) {
            return (DObjectTreeNode) _selectedNodeGUI.node();
        } else {
            return null;
        }
    }

    public DObjectRef selectedObject() {
        DObjectTreeNode node = selectedNode();
        if (node == null) {
            return null;
        }
        return (DObjectRef) node.object();
    }
}
