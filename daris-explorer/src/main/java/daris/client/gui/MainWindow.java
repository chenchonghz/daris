package daris.client.gui;

import daris.client.app.MainApp;
import daris.client.gui.object.DObjectMenu;
import daris.client.gui.object.DObjectMenu.MenuUpdateAction;
import daris.client.gui.object.DObjectViewPane;
import daris.client.gui.object.tree.DObjectTreeView;
import daris.client.model.object.DObjectRef;
import daris.client.util.OSUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainWindow {

    private Scene _scene;
    private BorderPane _borderPane;
    private DObjectTreeView _nav;
    private DObjectViewPane _dv;
    private StatusPane _statusPane;
    private Menu _actionMenu;

    public MainWindow() {

        _borderPane = new BorderPane();

        MenuBar menuBar = createMenuBar();

        _borderPane.setTop(menuBar);

        _dv = new DObjectViewPane();

        StackPane navStackPane = new StackPane();
        _nav = new DObjectTreeView() {
            @Override
            protected void selectedItemUpdated(DObjectRef o) {
                _dv.displayObject(o);
            }
        };
        navStackPane.getChildren().add(_nav);

        _nav.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    DObjectRef o = newValue == null ? null
                            : newValue.getValue();
                    DObjectMenu.updateMenuItems(_actionMenu.getItems(),
                            MenuUpdateAction.REPLACE, o);
                    _dv.displayObject(o);
                });

        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.HORIZONTAL);
        splitPane.setDividerPositions(0.3f);
        splitPane.getItems().setAll(navStackPane, _dv);

        _borderPane.setCenter(splitPane);

        _statusPane = new StatusPane();
        _statusPane.setMaxHeight(250);
        _borderPane.setBottom(_statusPane);

        _scene = new Scene(_borderPane, 1280.0, 800.0, Color.WHITE);
        _scene.getStylesheets().add(MainApp.css());

    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu darisMenu = createDarisMenu();
        menuBar.getMenus().add(darisMenu);
        _actionMenu = createActionMenu();
        menuBar.getMenus().add(_actionMenu);
        menuBar.setUseSystemMenuBar(true);
        return menuBar;
    }

    private Menu createDarisMenu() {
        Menu darisMenu = new Menu("DaRIS");
        /*
         * DaRIS -> About
         */
        MenuItem aboutItem = new MenuItem("About DaRIS");
        aboutItem.setOnAction(event -> {
            new AboutDialog().show();
        });
        darisMenu.getItems().add(aboutItem);

        darisMenu.getItems().add(new SeparatorMenuItem());

        /*
         * DaRIS -> Preferences
         */
        MenuItem preferencesItem = new MenuItem("Preferences");
        preferencesItem.setOnAction(event -> {
            // TODO
        });
        darisMenu.getItems().add(preferencesItem);

        darisMenu.getItems().add(new SeparatorMenuItem());

        /*
         * DaRIS -> Exit;
         */
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.exit(0);
            }
        });
        if (OSUtils.isMac()) {
            exitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q,
                    KeyCombination.META_DOWN));
        } else {
            exitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q,
                    KeyCombination.CONTROL_DOWN));
        }
        darisMenu.getItems().add(exitItem);
        return darisMenu;
    }

    private Menu createActionMenu() {
        Menu actionMenu = new Menu("Action");
        MenuItem test = new MenuItem("test");
        actionMenu.getItems().add(test);
        return actionMenu;
    }

    public void show(Stage stage) {
        stage.setTitle("DaRIS Explorer");
        stage.setMaximized(true);
        stage.setScene(_scene);
        stage.show();
    }

}
