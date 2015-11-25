package daris.client.model.file;

import arc.gui.image.Image;
import arc.mf.client.file.LocalFile;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Tree;
import daris.client.util.Filter;

public class LocalDirectoryTree implements Tree {

    private Container _root;

    public LocalDirectoryTree(boolean showHiddenDirectories) {
        _root = new LocalDirectoryRootNode(
                showHiddenDirectories ? null : new Filter<LocalFile>() {

                    @Override
                    public boolean matches(LocalFile o) {
                        return o.isDirectory() && o.name().indexOf('.') != 0;
                    }
                });
    }

    public LocalDirectoryTree() {
        this(false);
    }

    public boolean readOnly() {
        return true;
    }

    public Image icon() {
        return null;
    }

    public Container root() {
        return _root;
    }

    public void setReadOnly(boolean readOnly) {
        // Nothing to do.
    }

    public void discard() {
        _root.discard();
    }

}