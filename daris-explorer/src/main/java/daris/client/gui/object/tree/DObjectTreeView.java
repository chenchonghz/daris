package daris.client.gui.object.tree;

import java.util.ArrayList;
import java.util.List;

import arc.mf.client.util.UnhandledException;
import arc.mf.desktop.ui.util.ApplicationThread;
import arc.mf.event.Filter;
import arc.mf.event.Subscriber;
import arc.mf.event.SystemEvent;
import arc.mf.event.SystemEventChannel;
import arc.mf.object.ObjectMessageResponse;
import daris.client.gui.object.DObjectMenu;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.events.PSSDObjectEvent;
import daris.client.model.object.events.PSSDObjectEvent.Action;
import daris.client.model.object.messages.CanAccess;
import daris.client.model.repository.RepositoryRef;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class DObjectTreeView extends TreeView<DObjectRef>
        implements Subscriber {

    private List<arc.mf.event.Filter> _filters;
    private ContextMenu _contextMenu;

    public DObjectTreeView() {
        super(new DObjectTreeItem(RepositoryRef.get()));
        setCellFactory(tree -> {
            return new DObjectTreeCell();
        });
        RepositoryRef repo = (RepositoryRef) getRoot().getValue();
        repo.resolve(o -> {
            SystemEventChannel.add(this);
            ApplicationThread.execute(() -> {
                getSelectionModel().select(getRoot());
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
        _contextMenu = new ContextMenu(new MenuItem());
        _contextMenu.setOnShowing(e -> {
            DObjectRef o = DObjectTreeView.this.getSelectionModel()
                    .getSelectedItem().getValue();
            o.resolve(oo -> {
                ApplicationThread.execute(() -> {
                    _contextMenu.getItems()
                            .setAll(DObjectMenu.menuItemsFor(oo));
                });
            });
        });
        _contextMenu.setOnHidden(e -> {
            ApplicationThread.execute(() -> {
                _contextMenu.getItems().setAll(new MenuItem());
            });
        });
        setContextMenu(_contextMenu);
        _filters = new ArrayList<arc.mf.event.Filter>(1);
        _filters.add(new arc.mf.event.Filter(PSSDObjectEvent.SYSTEM_EVENT_NAME,
                null) {
            public boolean equals(arc.mf.event.Filter f) {
                return type().equals(f.type());
            }
        });
        SystemEventChannel.add(this);
    }

    private DObjectTreeItem findTreeItem(DObjectRef o) {
        DObjectTreeItem root = (DObjectTreeItem) getRoot();
        if (root == null) {
            return null;
        }
        return findTreeItem(root, o);
    }

    private static DObjectTreeItem findTreeItem(DObjectTreeItem root,
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
                    DObjectTreeItem item = findTreeItem((DObjectTreeItem) child,
                            o);
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
        PSSDObjectEvent e = (PSSDObjectEvent) se;
        DObjectRef o = e.objectRef();
        if (e.action() == Action.CREATE && o.isProject()) {
            new CanAccess(o.citeableId())
                    .send(new ObjectMessageResponse<Boolean>() {
                        @Override
                        public void responded(Boolean canAccess) {
                            if (canAccess) {
                                try {
                                    processEvent(e);
                                } catch (Throwable ex) {
                                    UnhandledException
                                            .report("Processing pssd-object event: "
                                                    + e.object(), ex);
                                }
                            } else {
                                System.out.println("By-passing event: " + e);
                            }
                        }
                    });
        } else {
            processEvent(e);
        }
    }

    private void processEvent(PSSDObjectEvent dse) throws Throwable {
        if (PSSDObjectEvent.Action.DESTROY.equals(dse.action())) {
            onDestroyed(dse);
        } else if (PSSDObjectEvent.Action.CREATE.equals(dse.action())) {
            onCreated(dse);
        } else if (PSSDObjectEvent.Action.MODIFY.equals(dse.action())) {
            onModified(dse);
        } else if (PSSDObjectEvent.Action.MEMBERS.equals(dse.action())) {
            onMembersChanged(dse);
        }
    }

    protected void selectedItemUpdated(DObjectRef o) {

    }

    private void onModified(PSSDObjectEvent e) {
        DObjectRef o = e.objectRef();
        DObjectTreeItem item = findTreeItem(o);
        if (item != null) {
            o.reset();
            o.resolve(oo -> {

                Platform.runLater(() -> {
                    item.refresh(false);
                    boolean selected = getSelectionModel().getSelectedItems()
                            .contains(item);
                    if (selected) {
                        selectedItemUpdated(o);
                    }
                });
            });
        }
    }

    private void onMembersChanged(PSSDObjectEvent e) {
        DObjectRef o = e.objectRef();
        DObjectTreeItem item = findTreeItem(o);
        if (item != null) {
            Platform.runLater(() -> {
                item.refresh(true);
            });
        }
    }

    private void onCreated(PSSDObjectEvent e) {
        DObjectRef o = e.objectRef();
        DObjectRef po = o.parent();
        if (po != null) {
            DObjectTreeItem parentItem = findTreeItem(po);
            if (parentItem != null) {
                o.resolve(oo -> {
                    Platform.runLater(() -> {
                        if (!parentItem.containsChild(o)) {
                            parentItem.getChildren()
                                    .add(new DObjectTreeItem(o));
                        }
                    });
                });
            }
        }
    }

    private void onDestroyed(PSSDObjectEvent e) {
        DObjectRef o = e.objectRef();
        TreeItem<DObjectRef> item = findTreeItem(o);
        if (item != null) {
            Platform.runLater(() -> {
                boolean selected = getSelectionModel().getSelectedItems()
                        .contains(item);
                TreeItem<DObjectRef> parentItem = item.getParent();
                if (parentItem != null) {
                    parentItem.getChildren().remove(item);
                    if (selected) {
                        getSelectionModel().select(parentItem);
                    }
                }
            });
        }
    }

    @Override
    public List<Filter> systemEventFilters() {
        return _filters;
    }
}
