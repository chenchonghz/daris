package daris.client.gui.object;

import java.util.ArrayList;
import java.util.List;

import daris.client.gui.object.action.DownloadAction;
import daris.client.model.dataset.DataSet;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.DObject;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;
import javafx.scene.control.MenuItem;

public class DObjectMenu {

    public static List<MenuItem> menuItemsFor(DObject o) {
        List<MenuItem> items = new ArrayList<MenuItem>();
        if ((o instanceof Subject) || (o instanceof ExMethod)
                || (o instanceof Study) || (o instanceof DataSet)) {
            MenuItem downloadMenuItem = new MenuItem("Download");
            downloadMenuItem.setOnAction(e -> {
                new DownloadAction(null, o).execute();
            });
            items.add(downloadMenuItem);
        }
        items.add(new MenuItem("Refresh"));
        return items;
    }

}
