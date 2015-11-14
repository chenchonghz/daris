package daris.client.gui.object.tree;

import arc.mf.client.util.UnhandledException;
import arc.mf.desktop.ui.util.ApplicationThread;
import daris.client.model.CiteableIdUtils;
import daris.client.model.object.DObjectCollectionRef;
import daris.client.model.object.DObjectRef;
import daris.client.model.repository.RepositoryRef;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class DObjectTreeItem extends TreeItem<DObjectRef> {

    public static String ICON_FOLDER_OPEN = "/images/16folder_blue_open.png";
    public static String ICON_FOLDER = "/images/16folder_blue.png";
    public static String ICON_DATASET = "/images/16dataset_primary.png";
    public static String ICON_LOADING = "/images/16loading.gif";

    private DObjectCollectionRef _children;
    private HBox _graphic;
    private HBox _icon;
    private Label _label;

    public DObjectTreeItem(DObjectRef o) {
        super(o);
        _children = new DObjectCollectionRef(o);
        _graphic = new HBox();
        _icon = new HBox(0.0);
        _icon.setPrefWidth(16.0);
        _icon.setPrefHeight(16.0);
        _graphic.getChildren().add(_icon);
        _label = new Label(labelTextFor(o));
        _graphic.getChildren().add(_label);
        setIcon(false);
        addEventHandler(TreeItem.branchExpandedEvent(), event -> {
            updateChildren(false);
            setIcon(true);
        });
        addEventHandler(TreeItem.branchCollapsedEvent(), event -> {
            setIcon(false);
        });

    }

    public boolean isLeaf() {
        return getValue().hasChildren().no();
    }

    void updateGraphic() {
        setIcon(isExpanded());
        _label.setText(labelTextFor(getValue()));
    }

    private void updateChildren(boolean refresh) {
        if (!refresh && !getChildren().isEmpty()) {
            return;
        }
        if (refresh) {
            _children.reset();
        }
        try {
            ApplicationThread.execute(() -> {
                setIcon(ICON_LOADING);
            });
            _children.resolve(cos -> {
                ApplicationThread.execute(() -> {
                    getChildren().clear();
                    if (cos != null) {
                        for (DObjectRef co : cos) {
                            getChildren().add(new DObjectTreeItem(co));
                        }
                    }
                    setIcon(true);
                });
            });
        } catch (Throwable e) {
            UnhandledException.report(
                    "Retrieving members of " + getValue().idToString(), e);
        }
    }

    private void setIcon(boolean open) {
        if (open) {
            setIcon(openIconPathFor(getValue()));
        } else {
            setIcon(iconPathFor(getValue()));
        }
    }

    private void setIcon(String path) {
        _icon.getChildren().clear();
        ImageView i = new ImageView(
                new Image(getClass().getResourceAsStream(path), 16.0, 16.0,
                        false, false));
        i.setFitWidth(16.0);
        i.setFitHeight(16.0);
        _icon.getChildren().add(i);
    }

    private static String iconPathFor(DObjectRef o) {
        String cid = o.citeableId();
        if (CiteableIdUtils.isDataSetCID(cid)) {
            return ICON_DATASET;
        } else {
            return ICON_FOLDER;
        }
    }

    private static String openIconPathFor(DObjectRef o) {
        String cid = o.citeableId();
        if (CiteableIdUtils.isDataSetCID(cid)) {
            return ICON_DATASET;
        } else {
            return ICON_FOLDER_OPEN;
        }
    }

    private static String labelTextFor(DObjectRef o) {
        if (o instanceof RepositoryRef) {
            return o.name() == null ? "DaRIS" : o.name();
        }
        String cid = o.citeableId();
        if (CiteableIdUtils.isProjectCID(cid)) {
            return o.citeableId() + ": " + (o.name() == null ? "" : o.name());
        } else {
            String n = CiteableIdUtils.getLastPart(cid);
            return n + ": " + (o.name() == null ? n : o.name());
        }
    }

}
