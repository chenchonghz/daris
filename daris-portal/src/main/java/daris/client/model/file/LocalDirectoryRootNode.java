package daris.client.model.file;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.image.Image;
import arc.mf.client.dti.DTI;
import arc.mf.client.file.FileHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.DynamicBoolean;
import arc.mf.client.util.Fuzzy;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;
import daris.client.Resource;
import daris.client.util.Filter;

public class LocalDirectoryRootNode implements Container {

    public static final Image ICON_COMPUTER = new Image(
            Resource.INSTANCE.computer16().getSafeUri().asString(), 16, 16);

    public static final String ROOT_NAME = "Local Files";

    private Filter<LocalFile> _filter;

    LocalDirectoryRootNode(Filter<LocalFile> filter) {
        _filter = filter;
    }

    public Container parent() {
        return null;
    }

    public boolean hasParent() {
        return parent() != null;
    }

    @Override
    public void add(Node n, TreeNodeAddHandler ah) {

    }

    @Override
    public void contents(long start, long end,
            final TreeNodeContentsHandler ch) {

        DTI.fileSystem().roots(new FileHandler() {
            @Override
            public void process(long start, long end, long total,
                    List<LocalFile> files) {
                if (files == null) {
                    ch.loaded(0, 0, 0, null);
                } else {
                    List<Node> contents = new ArrayList<Node>();
                    for (LocalFile f : files) {
                        contents.add(new LocalDirectoryNode(
                                LocalDirectoryRootNode.this, f, _filter));
                    }
                    ch.loaded(start, end, contents.size(), contents);
                }
            }
        });
    }

    @Override
    public void remove(Node n, TreeNodeRemoveHandler rh) {

    }

    @Override
    public String name() {

        return ROOT_NAME;
    }

    @Override
    public Object object() {

        return null;
    }

    @Override
    public String type() {

        return getClass().getName();
    }

    @Override
    public Image icon() {

        return ICON_COMPUTER;
    }

    @Override
    public Image openIcon() {

        return ICON_COMPUTER;
    }

    @Override
    public boolean sorted() {

        return true;
    }

    @Override
    public String path() {

        return null;
    }

    @Override
    public void description(TreeNodeDescriptionHandler dh) {

        if (DTI.enabled()) {
            dh.description(
                    "Files on the local file system. These can be copied into/from the server.");
        } else {
            dh.description(
                    "Files on the local file system.<br/>Not available - desktop integration has not activated.");
        }
    }

    @Override
    public boolean readOnly() {

        return true;
    }

    @Override
    public void unsubscribe(Object key) {

    }

    @Override
    public void discard() {

    }

    @Override
    public Fuzzy hasChildren() {

        return Fuzzy.MAYBE;
    }

    @Override
    public List<BaseWidget> adornments() {
        return null;
    }

    @Override
    public Object subscribe(DynamicBoolean descend, NodeListener l) {
        return null;
    }

}