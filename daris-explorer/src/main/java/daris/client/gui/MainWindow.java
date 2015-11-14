package daris.client.gui;

import arc.mf.desktop.HasScene;
import daris.client.gui.object.tree.DObjectTreeView;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainWindow implements HasScene {

    private Scene _scene;

    public MainWindow() {

        BorderPane borderPane = new BorderPane();

        MenuBar menuBar = new MenuBar();

        Menu darisMenu = new Menu("DaRIS");

        MenuItem aboutItem = new MenuItem("About DaRIS");
        aboutItem.setOnAction(event -> {
            // TODO
        });
        darisMenu.getItems().add(aboutItem);

        darisMenu.getItems().add(new SeparatorMenuItem());

        MenuItem preferencesItem = new MenuItem("Preferences");
        preferencesItem.setOnAction(event -> {
            // TODO
        });
        darisMenu.getItems().add(preferencesItem);

        darisMenu.getItems().add(new SeparatorMenuItem());

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.exit(0);
            }
        });
        darisMenu.getItems().add(exitItem);

        menuBar.getMenus().add(darisMenu);

        borderPane.setTop(menuBar);

        StackPane navStackPane = new StackPane();
        navStackPane.getChildren().add(new DObjectTreeView());

        StackPane detailStackPane = new StackPane();

        SplitPane splitPane = new SplitPane(navStackPane, detailStackPane);
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.3);

        borderPane.setCenter(splitPane);

        _scene = new Scene(borderPane, Color.WHITE);
    }

    public void show(Stage stage) {
        stage.setTitle("DaRIS Explorer");
        stage.setMaximized(true);
        stage.setScene(_scene);
        stage.show();
    }

    @Override
    public Node about() {
        return null;
    }

    @Override
    public Scene scene() {
        return _scene;
    }

}
