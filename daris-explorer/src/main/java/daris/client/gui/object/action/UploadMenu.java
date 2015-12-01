package daris.client.gui.object.action;

import daris.client.model.dataset.DataSet;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.DObject;
import daris.client.model.project.Project;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class UploadMenu extends ContextMenu {

    public UploadMenu(DObject o) {
        if (canUploadDicomData(o)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Upload DICOM data to ");
            sb.append(o.type().typeName());
            sb.append(" ");
            sb.append(o.citeableId());
            MenuItem item = new MenuItem(sb.toString());
            item.setOnAction(event -> {
                // TODO
            });
            getItems().add(item);
        }
        if (canUploadPrimaryDataSet(o)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Create primary dataset in ");
            sb.append(o.type().typeName());
            sb.append(" ");
            sb.append(o.citeableId());
            MenuItem item = new MenuItem(sb.toString());
            item.setOnAction(event -> {
                // TODO
            });
            getItems().add(item);
        }
        if (canUploadDerivedDataSet(o)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Create derived dataset ");
            if(o.type()==DObject.Type.STUDY){
                sb.append("in ");
            } else {
                sb.append("for ");
            }
            sb.append(o.type().typeName());
            sb.append(" ");
            sb.append(o.citeableId());
            MenuItem item = new MenuItem(sb.toString());
            item.setOnAction(event -> {
                // TODO
            });
            getItems().add(item);
        }
    }

    private static boolean canUploadDicomData(DObject o) {
        return ((o instanceof Project) && ((Project) o).numberOfMethods() == 1)
                || (o instanceof Subject) || (o instanceof ExMethod)
                || (o instanceof Study);
    }

    private static boolean canUploadPrimaryDataSet(DObject o) {
        return (o instanceof Study);
    }

    private static boolean canUploadDerivedDataSet(DObject o) {
        return (o instanceof Study) || (o instanceof DataSet);
    }

}
