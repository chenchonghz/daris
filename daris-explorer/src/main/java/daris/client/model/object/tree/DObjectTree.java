package daris.client.model.object.tree;

import arc.mf.object.tree.Container;
import arc.mf.object.tree.Tree;
import daris.client.model.repository.RepositoryRef;
import javafx.scene.image.Image;

public class DObjectTree implements Tree {

    private DObjectTreeNode _root;

    public DObjectTree() {
        _root = new DObjectTreeNode(new RepositoryRef());
    }

    @Override
    public void discard() {

    }

    @Override
    public Image icon() {
        return null;
    }

    @Override
    public boolean readOnly() {
        return true;
    }

    @Override
    public Container root() throws Throwable {
        return _root;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        // Do nothing, always read-only.
    }

}
