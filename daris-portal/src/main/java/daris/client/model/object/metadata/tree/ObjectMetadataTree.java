package daris.client.model.object.metadata.tree;

import arc.gui.image.Image;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Tree;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;

public class ObjectMetadataTree implements Tree {

    private ObjectTypeMetadataNode _root;

    public ObjectMetadataTree(DObjectRef project) {
        this(project, null);
    }

    public ObjectMetadataTree(DObjectRef project, DObject.Type type) {
        _root = new ObjectTypeMetadataNode(project, type);
    }

    @Override
    public Image icon() {
        return null;
    }

    @Override
    public Container root() {
        return _root;
    }

    @Override
    public boolean readOnly() {
        return true;
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public void discard() {

    }

}
