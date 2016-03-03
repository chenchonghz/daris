package daris.client.gui.object;

import arc.mf.desktop.ui.util.ApplicationThread;
import daris.client.gui.dicom.action.DicomSendAction;
import daris.client.gui.dicom.action.DicomSendDialog;
import daris.client.gui.object.action.DownloadAction;
import daris.client.model.dataset.DataSet;
import daris.client.model.dicom.messages.CollectionDicomDatasetCount;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.CanDestroy;
import daris.client.model.object.messages.DObjectDestroy;
import daris.client.model.project.Project;
import daris.client.model.repository.Repository;
import daris.client.model.repository.RepositoryRef;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;

public class DObjectMenu {

    public enum MenuUpdateAction {
        PREPEND, APPEND, REPLACE
    }

    private DObjectMenu() {

    }

    public static void updateMenuItems(ObservableList<MenuItem> items,
            MenuUpdateAction action, DObjectRef o) {
        if (action == MenuUpdateAction.REPLACE) {
            items.clear();
        }
        if (!o.resolved()) {
            o.reset();
        }
        o.resolve(oo -> {
            new CollectionDicomDatasetCount(oo.citeableId())
                    .send(nbDicomDatasets -> {
                ApplicationThread.execute(() -> {
                    int idx = items.size();
                    if (action == MenuUpdateAction.PREPEND) {
                        idx = 0;
                    }
                    if ((oo instanceof Subject) || (oo instanceof ExMethod)
                            || (oo instanceof Study)
                            || (oo instanceof DataSet)) {
                        MenuItem downloadMenuItem = new MenuItem(
                                menuItemTextFor("Download", o));
                        downloadMenuItem.setOnAction(e -> {
                            new DownloadAction(null, o).execute();
                        });
                        items.add(idx, downloadMenuItem);
                        idx++;
                    }
                    if ((oo instanceof Subject) || (oo instanceof Study)
                            || (oo instanceof DataSet)) {
                        MenuItem destroyMenuItem = new MenuItem(
                                menuItemTextFor("Delete", o));
                        destroyMenuItem.setOnAction(e -> {
                            DObjectDestroy.destroy(oo);
                        });
                        destroyMenuItem.setDisable(true);
                        new CanDestroy(oo).send(canDestroy -> {
                            Platform.runLater(() -> {
                                destroyMenuItem.setDisable(!canDestroy);
                            });
                        });
                        items.add(idx, destroyMenuItem);
                        idx++;
                    }
                    if (nbDicomDatasets > 0 && (!(oo instanceof Repository))
                            && (!(oo instanceof Project))) {
                        MenuItem dicomSendMenuItem = new MenuItem(
                                menuItemTextFor("Send DICOM data in", oo));
                        dicomSendMenuItem.setOnAction(e -> {
                            new DicomSendDialog();
                            new DicomSendAction(null, o).execute();
                        });
                        items.add(idx, dicomSendMenuItem);
                        idx++;
                    }
                });
            });
        });
    }

    public static String menuItemTextFor(String action, DObject o) {
        StringBuilder sb = new StringBuilder();
        sb.append(action);
        if (o != null) {
            sb.append(" ");
            sb.append(o.type().typeName());
            if (o.citeableId() != null) {
                sb.append(" ");
                sb.append(o.citeableId());
            }
        }
        return sb.toString();
    }

    public static String menuItemTextFor(String action, DObjectRef o) {
        StringBuilder sb = new StringBuilder();
        sb.append(action);
        if (o != null) {
            sb.append(" ");
            if (o instanceof RepositoryRef) {
                sb.append("repository");
            } else {
                sb.append(o.referentTypeName());
                if (o.citeableId() != null) {
                    sb.append(" ");
                    sb.append(o.citeableId());
                }
            }
        }
        return sb.toString();
    }

}
