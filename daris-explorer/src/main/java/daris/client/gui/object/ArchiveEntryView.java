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
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class ArchiveEntryView extends Accordion {

    private TitledPane _infoPane;
    private StackPane _infoViewPane;
    private TitledPane _imagePane;
    private StackPane _imageViewPane;

    public ArchiveEntryView(ArchiveEntryCollectionRef arc, ArchiveEntry ae) {
        _infoViewPane = new StackPane();
        _infoPane = new TitledPane("File information", _infoViewPane);
        getPanes().add(_infoPane);
        updateInfoView(ae);

        if (ae.isViewAbleImage()) {
            _imageViewPane = new StackPane();
            _imagePane = new TitledPane("Image view", _imageViewPane);
            getPanes().add(_imagePane);
            updateImageView(arc, ae);
        }
    }

    private void updateInfoView(ArchiveEntry ae) {
        final KVTreeTableView<String, Object> table = new KVTreeTableView<String, Object>();
        table.addEntry("Name", ae.name());
        if (ae.size() >= 0) {
            table.addEntry("Size", ae.size());
        }
        String ext = ae.fileExtension();
        if (ext != null) {
            table.addEntry("File Extension", ext);
            ae.resolveMimeType(new ObjectMessageResponse<String>() {

                @Override
                public void responded(String mimeType) {
                    ApplicationThread.execute(() -> {
                        if (mimeType != null) {
                            table.addEntry("MIME Type", mimeType);
                        }
                    });
                }
            });
        }
        _infoViewPane.getChildren().setAll(table);
        _infoPane.setExpanded(true);
    }

    private void updateImageView(ArchiveEntryCollectionRef arc,
            ArchiveEntry ae) {
        _imageViewPane.getChildren()
                .setAll(new Label("Loading image: " + ae.name() + "..."));
        try {
            InMemoryOutput output = new InMemoryOutput();
            new ArchiveContentImageGet(arc, ae.ordinal()).setOutput(output)
                    .setResponseHandler((e, o) -> {
                        ApplicationThread.execute(() -> {
                            try (InputStream in = output.stream()) {
                                Image i = new Image(in);
                                ImageView iv = new ImageView(i);
                                _imageViewPane.getChildren().setAll(iv);
                                iv.setPreserveRatio(true);
                                // iv.fitWidthProperty().bind(_imageViewPane.widthProperty());

                                _imagePane.setExpanded(true);
                            } catch (Throwable ex) {
                                ApplicationThread.execute(() -> {
                                    _imageViewPane.getChildren()
                                            .setAll(new Label(
                                                    "Failed loading image: "
                                                            + ae.name()));
                                });
                                UnhandledException.report(
                                        "Loading archive entry image", ex);
                            }
                        });
                    }).execute();
        } catch (Throwable e) {
            ApplicationThread.execute(() -> {
                _imageViewPane.getChildren().setAll(
                        new Label("Failed loading image: " + ae.name()));
            });
            UnhandledException.report("Loading archive entry image", e);
        }
    }

}
