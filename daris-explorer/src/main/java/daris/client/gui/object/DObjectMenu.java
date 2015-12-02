package daris.client.gui.object;

import java.util.ArrayList;
import java.util.List;

import daris.client.gui.object.action.DownloadAction;
import daris.client.model.dataset.DataSet;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.CanDestroy;
import daris.client.model.object.messages.DObjectDestroy;
import daris.client.model.repository.RepositoryRef;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;
import javafx.scene.control.MenuItem;

public class DObjectMenu {

    private DObjectMenu() {

    }

    public static List<MenuItem> menuItemsFor(DObject o) {
        List<MenuItem> items = new ArrayList<MenuItem>();
        if ((o instanceof Subject) || (o instanceof ExMethod)
                || (o instanceof Study) || (o instanceof DataSet)) {
            MenuItem downloadMenuItem = new MenuItem(
                    menuItemTextFor("Download", o));
            downloadMenuItem.setOnAction(e -> {
                new DownloadAction(null, o).execute();
            });
            items.add(downloadMenuItem);
        }
        if ((o instanceof Subject) || (o instanceof Study)
                || (o instanceof DataSet)) {
            MenuItem destroyMenuItem = new MenuItem(
                    menuItemTextFor("Delete", o));
            destroyMenuItem.setOnAction(e -> {
                DObjectDestroy.destroy(o);
            });
            destroyMenuItem.setDisable(true);
            new CanDestroy(o).send(canDestroy -> {
                destroyMenuItem.setDisable(!canDestroy);
            });
            items.add(destroyMenuItem);
        }
        return items;
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
