package daris.client.gui.object.tree;

import daris.client.model.object.DObjectRef;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;

public class DObjectTreeCell extends TreeCell<DObjectRef> {

    DObjectTreeCell() {
        super();
    }

    @Override
    public void updateItem(DObjectRef o, final boolean empty) {
        super.updateItem(o, empty);
        setText(null);
        if (empty) {
            setGraphic((Node) null);
        } else {
            setGraphic(getTreeItem().getGraphic());
        }
    }
}
