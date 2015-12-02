package daris.client.gui.object.tree;

import java.util.ArrayList;
import java.util.List;

import arc.mf.desktop.ui.util.ApplicationThread;
import arc.utils.ObjectUtil;
import daris.client.model.CiteableIdUtils;
import daris.client.model.object.DObjectChildrenRef;
import daris.client.model.object.DObjectRef;
import daris.client.model.repository.Repository;
import javafx.collections.ObservableList;
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

    private DObjectChildrenRef _children;
    private HBox _graphic;
    private HBox _icon;
    private Label _label;

    public DObjectTreeItem(DObjectRef o) {
        super(o);
        _children = new DObjectChildrenRef(o);
        _graphic = new HBox();
        _icon = new HBox(0.0);
        _icon.setPrefWidth(16.0);
        _icon.setPrefHeight(16.0);
        _graphic.getChildren().add(_icon);
        _label = new Label(labelTextFor(o));
        _graphic.getChildren().add(_label);
        setIcon(false);
        setGraphic(_graphic);
        expandedProperty()
                .addListener((observableValue, oldValue, newValue) -> {
                    if (!newValue.equals(oldValue)) {
                        setIcon(newValue);
                    }
                });
    }

    @Override
    public ObservableList<TreeItem<DObjectRef>> getChildren() {
        if (!_children.resolved()) {
            setIcon(ICON_LOADING);
            _children.resolve(cos -> {
                if (cos != null && !cos.isEmpty()) {
                    List<TreeItem<DObjectRef>> items = new ArrayList<TreeItem<DObjectRef>>(
                            cos.size());
                    for (DObjectRef co : cos) {
                        items.add(new DObjectTreeItem(co));
                    }
                    ApplicationThread.execute(() -> {
                        super.getChildren().setAll(items);
                        setIcon(isExpanded());
                    });
                }
            });
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        return getValue().hasChildren().no();
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

    private static String labelTextFor(DObjectRef object) {
        if (object.isRepository()) {
            if (object.resolved()) {
                Repository repo = (Repository) (object.referent());
                if (repo.acronym() != null) {
                    return repo.acronym();
                } else if (repo.name() != null) {
                    return repo.name();
                } else {
                    return "DaRIS";
                }
            } else if (object.name() != null) {
                return object.name();
            } else {
                return "DaRIS";
            }
        } else if (object.isProject()) {
            return object.citeableId()
                    + (object.name() == null ? "" : (": " + object.name()));
        } else {
            return CiteableIdUtils.getLastPart(object.citeableId())
                    + (object.name() == null ? "" : (": " + object.name()));
        }
    }

    boolean containsChild(DObjectRef o) {
        ObservableList<TreeItem<DObjectRef>> children = getChildren();
        for (TreeItem<DObjectRef> item : children) {
            DObjectRef co = item.getValue();
            if (ObjectUtil.equals(co.citeableId(), o.citeableId())) {
                return true;
            }
        }
        return false;
    }

    void refresh(boolean refreshChildren) {
        getValue().reset();
        getValue().resolve(o -> {
            ApplicationThread.execute(() -> {
                _label.setText(labelTextFor(getValue()));
            });
        });
        if (refreshChildren) {
            boolean expanded = isExpanded();
            if (expanded) {
                setExpanded(false);
            }
            _children.reset();
            if (expanded) {
                setExpanded(true);
            }
        }
    }

}
