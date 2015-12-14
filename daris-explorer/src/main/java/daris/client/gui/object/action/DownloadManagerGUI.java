package daris.client.gui.object.action;

import arc.gui.InterfaceComponent;
import arc.mf.client.agent.task.Task;
import arc.mf.client.agent.task.Task.State;
import daris.client.model.task.DownloadTask;
import daris.client.model.task.DownloadTaskManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.util.StringConverter;

public class DownloadManagerGUI implements InterfaceComponent {

    private TableView<DownloadTask> _table;

    public DownloadManagerGUI() {
        _table = new TableView<DownloadTask>();
        _table.setPlaceholder(new Label("No downloads."));
        TableColumn<DownloadTask, String> idColumn = new TableColumn<DownloadTask, String>(
                "ID");
        idColumn.setCellValueFactory(param -> {
            StringProperty idStringProperty = new SimpleStringProperty();
            Bindings.bindBidirectional(idStringProperty,
                    param.getValue().idProperty(),
                    new StringConverter<Number>() {

                @Override
                public String toString(Number object) {
                    if (object != null && (object instanceof Long)) {
                        if (((Long) object).longValue() > 0) {
                            return object.toString();
                        }
                    }
                    return null;
                }

                @Override
                public Long fromString(String string) {
                    if (string != null) {
                        return Long.parseLong(string);
                    }
                    return null;
                }
            });
            return idStringProperty;
        });
        idColumn.setMaxWidth(60.0);
        idColumn.setMaxWidth(60.0);
        idColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(idColumn);

        TableColumn<DownloadTask, Task.State> stateColumn = new TableColumn<DownloadTask, Task.State>(
                "State");
        stateColumn.setCellValueFactory(param -> {
            return param.getValue().stateProperty();
        });
        stateColumn.setMinWidth(100.0);
        stateColumn.setMaxWidth(100.0);
        stateColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(stateColumn);

        TableColumn<DownloadTask, String> messageColumn = new TableColumn<DownloadTask, String>(
                "Message");
        messageColumn.setCellValueFactory(param -> {
            return param.getValue().messageProperty();
        });
        messageColumn.setMinWidth(200.0);
        messageColumn.setMinWidth(350.0);
        messageColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        _table.getColumns().add(messageColumn);
        TableColumn<DownloadTask, Double> progressColumn = new TableColumn<DownloadTask, Double>(
                "Progress");
        progressColumn.setCellValueFactory(param -> {
            return param.getValue().progressProperty();
        });
        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());
        progressColumn.setMinWidth(250.0);
        progressColumn.setMaxWidth(250.0);
        progressColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(progressColumn);

        TableColumn<DownloadTask, String> objectsProgressMessageColumn = new TableColumn<DownloadTask, String>(
                "Processed Objects");
        objectsProgressMessageColumn.setCellValueFactory(param -> {
            return param.getValue().objectsProgressMessageProperty();
        });
        objectsProgressMessageColumn.setMinWidth(135.0);
        objectsProgressMessageColumn.setMaxWidth(135.0);
        objectsProgressMessageColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(objectsProgressMessageColumn);

        TableColumn<DownloadTask, String> sizeProgressMessageColumn = new TableColumn<DownloadTask, String>(
                "Processed Size");
        sizeProgressMessageColumn.setCellValueFactory(param -> {
            return param.getValue().sizeProgressMessageProperty();
        });
        sizeProgressMessageColumn.setMinWidth(150.0);
        sizeProgressMessageColumn.setMaxWidth(250.0);
        sizeProgressMessageColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(sizeProgressMessageColumn);

        TableColumn<DownloadTask, Number> receivedSizeColumn = new TableColumn<DownloadTask, Number>(
                "Received(Bytes)");
        receivedSizeColumn.setCellValueFactory(param -> {
            return param.getValue().receivedSizeProperty();
        });
        receivedSizeColumn.setMinWidth(125);
        receivedSizeColumn.setMaxWidth(150);
        receivedSizeColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        _table.getColumns().add(receivedSizeColumn);

        TableColumn<DownloadTask, Task.State> actionButtonColumn = new TableColumn<DownloadTask, Task.State>(
                "Action");
        actionButtonColumn.setCellValueFactory(param -> {
            return param.getValue().stateProperty();
        });
        actionButtonColumn.setCellFactory(column -> {
            return new ActionButtonCell();
        });
        actionButtonColumn.setMinWidth(80);
        actionButtonColumn.setMaxWidth(80);
        actionButtonColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(actionButtonColumn);
        _table.itemsProperty().bind(DownloadTaskManager.get().tasksProperty());
    }

    @Override
    public Node gui() {
        return _table;
    }

    private class ActionButtonCell extends TableCell<DownloadTask, Task.State> {
        private Button _button;

        ActionButtonCell() {
            _button = new Button();
        }

        private DownloadTask getTaskObject() {
            return (DownloadTask) getTableRow().getItem();
        }

        @Override
        protected void updateItem(final Task.State state, boolean empty) {
            super.updateItem(state, empty);
            if (empty) {
                setGraphic(null);
            } else {
                if (state.finished()) {
                    _button.setDisable(false);
                    _button.setText("Remove");
                    _button.setOnAction(event -> {
                        getTaskObject().discard();
                    });
                } else {
                    _button.setText("Cancel");
                    _button.setDisable(
                            state == State.ABORTING || state == State.ABORTED);
                    _button.setOnAction(event -> {
                        getTaskObject().abort();
                    });
                }
                setGraphic(_button);
            }
        }
    }

}
