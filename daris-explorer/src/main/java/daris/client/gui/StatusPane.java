package daris.client.gui;

import daris.client.gui.object.action.DownloadMonitorGUI;
import daris.client.model.task.DownloadTask;
import daris.client.model.task.DownloadTaskManager;
import javafx.collections.ListChangeListener;
import javafx.geometry.Side;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TitledPane;

public class StatusPane extends TitledPane {

    private TabPane _tp;
    private Tab _downloadsTab;
    private Tab _uploadsTab;

    public StatusPane() {
        setText("Transfers");
        setExpanded(false);

        _tp = new TabPane();
        _tp.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        _tp.setSide(Side.LEFT);
        setContent(_tp);

        _downloadsTab = new Tab("Downloads");
        _downloadsTab.setContent(new DownloadMonitorGUI().gui());
        _tp.getTabs().add(_downloadsTab);
        DownloadTaskManager.get().tasksProperty()
                .addListener(new ListChangeListener<DownloadTask>() {

                    @Override
                    public void onChanged(
                            ListChangeListener.Change<? extends DownloadTask> c) {
                        while (c.next()) {
                            if (c.wasAdded()) {
                                StatusPane.this.setExpanded(true);
                                showDownloads();
                                break;
                            }
                        }
                    }
                });

        _uploadsTab = new Tab("Uploads");
        // TODO
        _tp.getTabs().add(_uploadsTab);

    }

    public void showDownloads() {
        _tp.getSelectionModel().select(_downloadsTab);
    }

    public void showUploads() {
        _tp.getSelectionModel().select(_uploadsTab);
    }
}
