package daris.client.gui.object.action;

import java.io.File;
import java.util.List;

import daris.client.model.dataset.DataSet;
import daris.client.model.dicom.task.DicomIngestTask;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.DObject;
import daris.client.model.project.Project;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class UploadMenu extends ContextMenu {

    private DObject _o;
    private List<File> _fs;

    public UploadMenu(DObject o, List<File> fs) {
        _o = o;
        _fs = fs;
        if (canUploadDicomData(_o)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Upload DICOM data to ");
            sb.append(_o.type().typeName());
            sb.append(" ");
            sb.append(_o.citeableId());
            MenuItem item = new MenuItem(sb.toString());
            item.setOnAction(event -> {
                new DicomIngestTask(_o, _fs.get(0)).start();
            });
            getItems().add(item);
        }
        if (canUploadPrimaryDataSet(_o)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Create primary dataset in ");
            sb.append(_o.type().typeName());
            sb.append(" ");
            sb.append(_o.citeableId());
            MenuItem item = new MenuItem(sb.toString());
            item.setOnAction(event -> {
                // TODO
            });
            getItems().add(item);
        }
        if (canUploadDerivedDataSet(_o)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Create derived dataset ");
            if (_o.type() == DObject.Type.STUDY) {
                sb.append("in ");
            } else {
                sb.append("for ");
            }
            sb.append(_o.type().typeName());
            sb.append(" ");
            sb.append(_o.citeableId());
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
