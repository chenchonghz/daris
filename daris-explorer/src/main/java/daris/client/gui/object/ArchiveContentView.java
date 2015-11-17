package daris.client.gui.object;

import daris.client.model.archive.ArchiveEntryCollectionRef;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;

public class ArchiveContentView extends SplitPane {

    private StackPane _dv;
    private ArchiveEntryCollectionRef _arc;

    public ArchiveContentView(ArchiveEntryCollectionRef arc) {
        _arc = arc;
        ArchiveContentTableView nav = new ArchiveContentTableView(_arc);
        StackPane navStackPane = new StackPane();
        navStackPane.getChildren().add(nav);

        _dv = new StackPane();
        nav.addTableSelectionListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                _dv.getChildren().setAll(new ArchiveEntryView(_arc, newValue));
            } else {
                _dv.getChildren().clear();
            }
        });

        setOrientation(Orientation.HORIZONTAL);
        setDividerPositions(0.45f);
        getItems().setAll(nav, _dv);
    }

}
