package daris.client.gui;

import daris.client.app.Version;
import daris.client.model.pkg.PackageRef;
import daris.client.model.pkg.messages.PackageList;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AboutDialog {

    private Stage _stage;
    private Scene _scene;
    private TableView<PackageRef> _table;
    private ListProperty<PackageRef> _pkgs;

    public AboutDialog() {
        _pkgs = new SimpleListProperty<PackageRef>(
                FXCollections.observableArrayList());

        HBox hbox1 = new HBox();
        hbox1.setAlignment(Pos.CENTER);
        Label title = new Label("DaRIS Explorer");
        title.setFont(new Font("Arial", 20));
        title.setStyle("-fx-font-weight:bold");
        title.setTextAlignment(TextAlignment.CENTER);
        hbox1.getChildren().add(title);

        HBox hbox2 = new HBox();
        hbox2.setAlignment(Pos.CENTER);
        Label version = new Label("Vesion: " + Version.VERSION);
        version.setTextAlignment(TextAlignment.CENTER);
        hbox2.getChildren().add(version);

        _table = new TableView<PackageRef>();
        _table.setEditable(false);
        TableColumn<PackageRef, String> nameCol = new TableColumn<PackageRef, String>(
                "Package");
        nameCol.setCellValueFactory(cellData -> {
            return new ReadOnlyStringWrapper(cellData.getValue().name());
        });
        nameCol.setPrefWidth(250.0);
        TableColumn<PackageRef, String> versionCol = new TableColumn<PackageRef, String>(
                "Version");
        versionCol.setCellValueFactory(cellData -> {
            return new ReadOnlyStringWrapper(cellData.getValue().version());
        });
        versionCol.setPrefWidth(80.0);
        _table.getColumns().add(nameCol);
        _table.getColumns().add(versionCol);
        _table.itemsProperty().bind(_pkgs);

        HBox hbox3 = new HBox();
        hbox3.setAlignment(Pos.CENTER_RIGHT);
        Button button = new Button("OK");
        button.setOnAction(event -> {
            if (_stage != null) {
                _stage.close();
            }
        });
        hbox3.getChildren().add(button);

        VBox vbox = new VBox();
        vbox.setFillWidth(true);
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(20, 20, 20, 20));
        vbox.getChildren().addAll(hbox1, hbox2, _table, hbox3);

        _scene = new Scene(vbox, 420, 360);

        new PackageList().send(pkgs -> {
            Platform.runLater(() -> {
                _pkgs.setAll(pkgs);
            });
        });

    }

    public void show() {

        if (_stage == null) {
            _stage = new Stage();
            _stage.setTitle("About DaRIS Explorer");
            _stage.initStyle(StageStyle.UTILITY);
            _stage.initModality(Modality.APPLICATION_MODAL);
            _stage.setScene(_scene);
        }
        _stage.show();
    }

}
