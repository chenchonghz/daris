package daris.client.gui.object.tree;

import java.util.List;

import arc.mf.desktop.ui.util.ApplicationThread;
import arc.mf.event.Filter;
import arc.mf.event.Subscriber;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventChannel;
import daris.client.model.object.DObjectRef;
import daris.client.model.repository.RepositoryRef;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class DObjectTreeView extends TreeView<DObjectRef>
        implements Subscriber {

    public DObjectTreeView() {
        super(new DObjectTreeItem(new RepositoryRef()));
        setCellFactory(tree -> {
            return new DObjectTreeCell();
        });
        RepositoryRef repo = (RepositoryRef) getRoot().getValue();
        repo.resolve(o -> {
            ApplicationThread.execute(() -> {
                SystemEventChannel.add(this);
            });
        });
        getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        if (!newValue.getValue().resolved()) {
                            ((DObjectTreeItem) newValue).refresh(false);
                        }
                    }
                });
        setShowRoot(true);
    }

    private DObjectTreeItem findItem(DObjectRef o) {
        DObjectTreeItem root = (DObjectTreeItem) getRoot();
        if (root == null) {
            return null;
        }
        return findItem(root, o);
    }

    private static DObjectTreeItem findItem(DObjectTreeItem root,
            DObjectRef o) {
        DObjectRef rootObj = root.getValue();
        String rootCid = rootObj.citeableId();
        String cid = o.citeableId();
        if (cid == null) {
            if (rootCid == null) {
                return root;
            } else {
                return null;
            }
        }
        if (cid.equals(rootCid)) {
            return root;
        }
        if (rootCid == null || cid.startsWith(rootCid + ".")) {
            ObservableList<TreeItem<DObjectRef>> children = root.getChildren();
            if (!children.isEmpty()) {
                for (TreeItem<DObjectRef> child : children) {
                    DObjectTreeItem item = findItem((DObjectTreeItem) child, o);
                    if (item != null) {
                        return item;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void process(SystemEvent se) throws Throwable {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Filter> systemEventFilters() {
        // TODO Auto-generated method stub
        return null;
    }
}
