package daris.client.gui.object;

import java.io.InputStream;

import arc.mf.client.ServerClient.InMemoryOutput;
import arc.mf.client.util.UnhandledException;
import arc.mf.desktop.ui.util.ApplicationThread;
import arc.mf.object.ObjectMessageResponse;
import daris.client.gui.xml.KVTreeTableView;
import daris.client.model.archive.ArchiveEntry;
import daris.client.model.archive.ArchiveEntryCollectionRef;
import daris.client.model.archive.messages.ArchiveContentImageGet;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class ArchiveEntryView extends StackPane {

    private ImageView _imageView;
    private KVTreeTableView<String, Object> _infoTable;

    public ArchiveEntryView(ArchiveEntryCollectionRef arc, ArchiveEntry ae) {
        _infoTable = new KVTreeTableView<String, Object>();
        _infoTable.setNameColumnPrefWidth(100.0);
        _infoTable.setValueColumnPrefWidth(150.0);
        if (ae.isViewableImage()) {
            _imageView = new ImageView(new Image(
                    getClass().getResourceAsStream("/images/64loading.gif")));
            // _imageView.fitWidthProperty().bind(widthProperty());
            _imageView.setPreserveRatio(true);
            _imageView.setFitWidth(getWidth());
            _infoTable.setPrefHeight(150.0);
            AnchorPane ap = new AnchorPane();
            ap.getChildren().setAll(_imageView, _infoTable);
            AnchorPane.setTopAnchor(_imageView, 0.0);
            // AnchorPane.setLeftAnchor(_imageView, 0.0);
            // AnchorPane.setRightAnchor(_imageView, 0.0);
            AnchorPane.setLeftAnchor(_infoTable, 0.0);
            AnchorPane.setRightAnchor(_infoTable, 0.0);
            AnchorPane.setBottomAnchor(_infoTable, 0.0);
            getChildren().setAll(ap);
            updateImageView(arc, ae);
        } else {
            getChildren().setAll(_infoTable);
        }
        updateInfoTable(ae);
    }

    private void updateInfoTable(ArchiveEntry ae) {
        _infoTable.addEntry("Name", ae.name());
        if (ae.size() >= 0) {
            _infoTable.addEntry("Size", ae.size());
        }
        String ext = ae.fileExtension();
        if (ext != null) {
            _infoTable.addEntry("File Extension", ext);
            ae.resolveMimeType(new ObjectMessageResponse<String>() {

                @Override
                public void responded(String mimeType) {
                    ApplicationThread.execute(() -> {
                        if (mimeType != null) {
                            _infoTable.addEntry("MIME Type", mimeType);
                        }
                    });
                }
            });
        }
    }

    private void updateImageView(ArchiveEntryCollectionRef arc,
            ArchiveEntry ae) {
        try {
            InMemoryOutput output = new InMemoryOutput();
            new ArchiveContentImageGet(arc, ae.ordinal()).setOutput(output)
                    .setResponseHandler((e, o) -> {
                        ApplicationThread.execute(() -> {
                            try (InputStream in = output.stream()) {
                                _imageView.setImage(new Image(in));
                                double w = getWidth();
                                double h = getHeight();
                                if (w < h) {
                                    _imageView.setFitWidth(w);
                                } else {
                                    _imageView.setFitHeight(h);
                                }
                            } catch (Throwable ex) {
                                UnhandledException.report(
                                        "Loading archive entry image", ex);
                            }
                        });
                    }).execute();
        } catch (Throwable e) {
            UnhandledException.report("Loading archive entry image", e);
        }
    }

}
