package daris.client.gui.object.tree;

import java.io.File;
import java.util.List;

import daris.client.gui.object.action.UploadMenu;
import daris.client.model.dataset.DataSet;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.Project;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class DObjectTreeCell extends TreeCell<DObjectRef> {
    public static final String STYLE_CLASS_DRAG_ENTERED = "tree-cell-graphic-drag-entered";

    DObjectTreeCell() {
        super();
        setOnDragOver(event -> {
            if (event.getGestureSource() != DObjectTreeCell.this
                    && canDrop(event, object())) {
                event.acceptTransferModes(TransferMode.ANY);
            }
            event.consume();
        });
        setOnDragEntered(event -> {
            if (canDrop(event, object())) {
                object().resolve(o -> {
                    if (canDrop(o)) {
                        DObjectTreeCell.this.getStyleClass()
                                .add(STYLE_CLASS_DRAG_ENTERED);
                    }
                });
            }
            event.consume();
        });
        setOnDragExited(event -> {
            if (canDrop(event, object())) {
                object().resolve(o -> {
                    if (canDrop(o)) {
                        DObjectTreeCell.this.getStyleClass()
                                .remove(STYLE_CLASS_DRAG_ENTERED);
                    }
                });
            }
            event.consume();
        });
        setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                final List<File> files = event.getDragboard().getFiles();
                object().resolve(o -> {
                    Platform.runLater(() -> {
                        if (canDrop(o)) {
                            new UploadMenu(o, files).show(DObjectTreeCell.this,
                                    event.getScreenX(), event.getSceneY());
                        }
                    });
                });
            }
            event.setDropCompleted(db.hasFiles());
            event.consume();
        });
    }

    private DObjectRef object() {
        if (getTreeItem() == null) {
            return null;
        }
        return getTreeItem().getValue();
    }

    private static boolean canDrop(DragEvent event, DObjectRef o) {
        if (event == null || o == null) {
            return false;
        }
        if (!event.getDragboard().hasFiles()) {
            return false;
        }
        if (o.resolved() && o.isProject()
                && ((Project) o.referent()).numberOfMethods() != 1) {
            return false;
        }
        return o.isProject() || o.isSubject() || o.isExMethod() || o.isStudy()
                || o.isDataSet();
    }

    private static boolean canDrop(DObject o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Project) {
            if (((Project) o).numberOfMethods() == 1) {
                return true;
            } else {
                return false;
            }
        } else if (o instanceof Subject) {
            return true;
        } else if (o instanceof ExMethod) {
            return true;
        } else if (o instanceof Study) {
            return true;
        } else if (o instanceof DataSet) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void updateItem(DObjectRef o, final boolean empty) {
        super.updateItem(o, empty);
        setText(null);
        if (empty) {
            setGraphic((Node) null);
        } else {
            setGraphic(getTreeItem().getGraphic());
        }
    }
}
