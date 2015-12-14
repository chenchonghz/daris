package daris.client.gui;

import daris.client.gui.object.action.DownloadManagerGUI;
import daris.client.gui.object.action.UploadManagerGUI;
import daris.client.model.task.DownloadTask;
import daris.client.model.task.DownloadTaskManager;
import daris.client.model.task.UploadTask;
import daris.client.model.task.UploadTaskManager;
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
    private Tab _backgroundServiceTab;

    public StatusPane() {
        setText("Transfers");
        setExpanded(false);

        _tp = new TabPane();
        _tp.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        _tp.setSide(Side.TOP);
        setContent(_tp);

        _downloadsTab = new Tab("Downloads");
        _downloadsTab.setContent(new DownloadManagerGUI().gui());
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
        _uploadsTab.setContent(new UploadManagerGUI().gui());
        _tp.getTabs().add(_uploadsTab);
        UploadTaskManager.get().tasksProperty()
                .addListener(new ListChangeListener<UploadTask>() {

                    @Override
                    public void onChanged(
                            ListChangeListener.Change<? extends UploadTask> c) {
                        while (c.next()) {
                            if (c.wasAdded()) {
                                StatusPane.this.setExpanded(true);
                                showUploads();
                                break;
                            }
                        }
                    }
                });
        
        _backgroundServiceTab = new Tab("Background Services");
        // TODO
        _tp.getTabs().add(_backgroundServiceTab);
    }

    public void showDownloads() {
        _tp.getSelectionModel().select(_downloadsTab);
    }

    public void showUploads() {
        _tp.getSelectionModel().select(_uploadsTab);
    }
}
