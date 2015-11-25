package daris.client.model.file;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.widget.BaseWidget;
import arc.gui.image.Image;
import arc.gui.util.HTMLUtil;
import arc.mf.client.file.FileHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.DynamicBoolean;
import arc.mf.client.util.Fuzzy;
import arc.mf.client.util.StringTokenizer;
import arc.mf.object.tree.Container;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.object.tree.TreeNodeAddHandler;
import arc.mf.object.tree.TreeNodeContentsHandler;
import arc.mf.object.tree.TreeNodeDescriptionHandler;
import arc.mf.object.tree.TreeNodeRemoveHandler;
import daris.client.Resource;
import daris.client.util.Filter;

public class LocalDirectoryNode implements Container {

    public static final Image ICON_FOLDER_OPEN = new Image(
            Resource.INSTANCE.folderBlueOpen16().getSafeUri().asString(), 16,
            16);
    public static final Image ICON_FOLDER = new Image(
            Resource.INSTANCE.folderBlue16().getSafeUri().asString(), 16, 16);

    private Container _parent;
    private LocalFile _dir;
    private Filter<LocalFile> _filter;

    LocalDirectoryNode(Container parent, LocalFile dir,
            Filter<LocalFile> filter) {
        _parent = parent;
        _dir = dir;
        _filter = filter;
    }

    public Container parent() {
        return _parent;
    }

    public boolean hasParent() {
        return _parent != null;
    }

    public boolean isTopLevelDirectory() {
        return hasParent() && (_parent instanceof LocalDirectoryRootNode);
    }

    @Override
    public void add(Node n, TreeNodeAddHandler ah) {

    }

    @Override
    public void contents(long start, long end,
            final TreeNodeContentsHandler ch) {

        _dir.files(LocalFile.Filter.DIRECTORIES, start, end, new FileHandler() {
            @Override
            public void process(long start, long end, long total,
                    List<LocalFile> files) {
                if (files == null) {
                    ch.loaded(start, end, total, null);
                } else {
                    List<Node> contents = new Vector<Node>();
                    for (LocalFile f : files) {
                        if (_filter == null || _filter.matches(f)) {
                            contents.add(new LocalDirectoryNode(
                                    LocalDirectoryNode.this, f, _filter));
                        }
                    }
                    ch.loaded(start, end, total, contents);
                }
            }
        });
    }

    @Override
    public void remove(Node n, TreeNodeRemoveHandler rh) {

    }

    @Override
    public String name() {

        return _dir.name();
    }

    @Override
    public Object object() {

        return _dir;
    }

    @Override
    public String type() {

        return getClass().getName();
    }

    @Override
    public Image icon() {

        return ICON_FOLDER;
    }

    @Override
    public Image openIcon() {

        return ICON_FOLDER_OPEN;
    }

    @Override
    public boolean sorted() {

        return true;
    }

    @Override
    public String path() {

        return _dir.path();
    }

    @Override
    public void description(TreeNodeDescriptionHandler dh) {

        String d = "Local directory:<br/>";

        d += "<div style=\"font-size: smaller\">";

        int indent = 2;
        StringTokenizer st = new StringTokenizer(path(), "/");
        while (st.hasMoreTokens()) {
            d += HTMLUtil.indentLeft("/" + st.nextToken(), indent);
            indent += 2;
        }

        d += "</div>";

        if (_dir.description() != null) {
            d += "<br/>" + _dir.description();
        }

        dh.description(d);
    }

    @Override
    public boolean readOnly() {

        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void unsubscribe(Object key) {

        // TODO Auto-generated method stub

    }

    @Override
    public void discard() {

    }

    @Override
    public Fuzzy hasChildren() {

        // TODO Auto-generated method stub
        return Fuzzy.MAYBE;
    }

    @Override
    public List<BaseWidget> adornments() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object subscribe(DynamicBoolean descend, NodeListener l) {
        // TODO Auto-generated method stub
        return null;
    }

}
