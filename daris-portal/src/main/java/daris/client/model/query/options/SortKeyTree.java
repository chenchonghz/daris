package daris.client.model.query.options;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.image.Image;
import arc.mf.client.util.DynamicBoolean;
import arc.mf.client.util.Fuzzy;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.Tree;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;

public class SortKeyTree implements Tree {

    protected static class RootNode implements Container {
        
        private List<Node> _nodes;

        private RootNode() {
            _nodes = new ArrayList<Node>();
        }

        @Override
        public String type() {
            return getClass().getName();
        }

        @Override
        public Image icon() {
            return null;
        }

        @Override
        public String name() {
            return "root";
        }

        @Override
        public String path() {
            return null;
        }

        @Override
        public void description(TreeNodeDescriptionHandler dh) {

        }

        @Override
        public List<BaseWidget> adornments() {
            return null;
        }

        @Override
        public Object object() {
            return null;
        }

        @Override
        public boolean readOnly() {
            return false;
        }

        @Override
        public Object subscribe(DynamicBoolean descend, NodeListener l) {
            return null;
        }

        @Override
        public void unsubscribe(Object key) {

        }

        @Override
        public void discard() {
            for (Node n : _nodes) {
                n.discard();
            }
        }

        @Override
        public boolean sorted() {
            return true;
        }

        @Override
        public Image openIcon() {
            return null;
        }

        @Override
        public Fuzzy hasChildren() {
            return _nodes.isEmpty() ? Fuzzy.NO : Fuzzy.YES;
        }

        @Override
        public void contents(long start, long end, TreeNodeContentsHandler ch) {
            ch.loaded(start, end, _nodes.size(), _nodes);
        }

        @Override
        public void add(Node n, TreeNodeAddHandler ah) {
            _nodes.add(n);
            if (ah != null) {
                ah.added(n);
            }
        }

        @Override
        public void remove(Node n, TreeNodeRemoveHandler rh) {
            _nodes.remove(n);
            if (rh != null) {
                rh.removed(n);
            }
        }
    }

    public static class LeafNode implements Node {

        private String _key;

        public LeafNode(String key) {
            _key = key;
        }

        @Override
        public String type() {
            return getClass().getName();
        }

        @Override
        public Image icon() {
            return null;
        }

        @Override
        public String name() {
            return _key;
        }

        @Override
        public String path() {
            return _key;
        }

        @Override
        public void description(TreeNodeDescriptionHandler dh) {
        }

        @Override
        public List<BaseWidget> adornments() {
            return null;
        }

        @Override
        public Object object() {
            return _key;
        }

        @Override
        public boolean readOnly() {
            return true;
        }

        @Override
        public Object subscribe(DynamicBoolean descend, NodeListener l) {
            return null;
        }

        @Override
        public void unsubscribe(Object key) {

        }

        @Override
        public void discard() {

        }

    }

    public static class MetadataTreeNode implements Container {

        private Tree _tree;

        public MetadataTreeNode(Tree tree) {
            _tree = tree;
        }

        @Override
        public String type() {
            return getClass().getName();
        }

        @Override
        public Image icon() {
            return null;
        }

        @Override
        public String name() {
            return "metadata";
        }

        @Override
        public String path() {
            return null;
        }

        @Override
        public void description(TreeNodeDescriptionHandler dh) {

        }

        @Override
        public List<BaseWidget> adornments() {
            return null;
        }

        @Override
        public Object object() {
            return _tree;
        }

        @Override
        public boolean readOnly() {
            return true;
        }

        @Override
        public Object subscribe(DynamicBoolean descend, NodeListener l) {
            return null;
        }

        @Override
        public void unsubscribe(Object key) {

        }

        @Override
        public void discard() {
            _tree.discard();
        }

        @Override
        public boolean sorted() {
            return false;
        }

        @Override
        public Image openIcon() {
            return _tree.icon();
        }

        @Override
        public Fuzzy hasChildren() {
            return Fuzzy.YES;
        }

        @Override
        public void contents(long start, long end, TreeNodeContentsHandler ch) {
            _tree.root().contents(start, end, ch);
        }

        @Override
        public void add(Node n, TreeNodeAddHandler ah) {

        }

        @Override
        public void remove(Node n, TreeNodeRemoveHandler rh) {

        }

    }

    private RootNode _root;

    public SortKeyTree(Tree metadataTree) {
        _root = new RootNode();
        _root.add(new MetadataTreeNode(metadataTree), null);
        _root.add(new LeafNode(SortKey.CTIME), null);
        _root.add(new LeafNode(SortKey.MTIME), null);
        _root.add(new LeafNode(SortKey.STIME), null);
        _root.add(new LeafNode(SortKey.CSIZE), null);
        _root.add(new LeafNode(SortKey.CONTENT_SIZE), null);
        _root.add(new LeafNode(SortKey.CONTENT_CSUM), null);
        // case object:
        // _nodes.add(new SortKeyLeafNode(Sort.KEY_OBJECT_TYPE));
        // _nodes.add(new SortKeyLeafNode(Sort.KEY_OBJECT_NAME));
        // _nodes.add(new SortKeyLeafNode(Sort.KEY_OBJECT_DESCRIPTION));
        // break;
        // case asset:
        // _nodes.add(new SortKeyLeafNode(Sort.KEY_ASSET_NAME));
        // break;
        // default:
        // break;
        // }
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
        _root.discard();
    }

}
