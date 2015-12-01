package daris.client.gui.object.action;

import arc.gui.InterfaceComponent;
import arc.mf.client.agent.task.Task;
import arc.mf.client.agent.task.Task.State;
import daris.client.model.task.UploadTask;
import daris.client.model.task.UploadTaskManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
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

public class UploadMonitorGUI implements InterfaceComponent {

    private TableView<UploadTask> _table;

    public UploadMonitorGUI() {
        _table = new TableView<UploadTask>();
        _table.setPlaceholder(new Label("No uploads."));
        TableColumn<UploadTask, String> idColumn = new TableColumn<UploadTask, String>(
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

        TableColumn<UploadTask, String> typeColumn = new TableColumn<UploadTask, String>(
                "Type");
        typeColumn.setCellValueFactory(param -> {
            return new ReadOnlyStringWrapper(param.getValue().type());
        });
        typeColumn.setMinWidth(120.0);
        typeColumn.setMaxWidth(160.0);
        typeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        _table.getColumns().add(typeColumn);

        TableColumn<UploadTask, Task.State> stateColumn = new TableColumn<UploadTask, Task.State>(
                "State");
        stateColumn.setCellValueFactory(param -> {
            return param.getValue().stateProperty();
        });
        stateColumn.setMinWidth(100.0);
        stateColumn.setMaxWidth(100.0);
        stateColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(stateColumn);

        TableColumn<UploadTask, String> messageColumn = new TableColumn<UploadTask, String>(
                "Message");
        messageColumn.setCellValueFactory(param -> {
            return param.getValue().messageProperty();
        });
        messageColumn.setMinWidth(200.0);
        messageColumn.setMinWidth(350.0);
        messageColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        _table.getColumns().add(messageColumn);
        TableColumn<UploadTask, Double> progressColumn = new TableColumn<UploadTask, Double>(
                "Progress");
        progressColumn.setCellValueFactory(param -> {
            return param.getValue().progressProperty();
        });
        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());
        progressColumn.setMinWidth(250.0);
        progressColumn.setMaxWidth(250.0);
        progressColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(progressColumn);

        TableColumn<UploadTask, String> processedFilesMessageColumn = new TableColumn<UploadTask, String>(
                "Processed Files");
        processedFilesMessageColumn.setCellValueFactory(param -> {
            return param.getValue().processedFilesMessageProperty();
        });
        processedFilesMessageColumn.setMinWidth(135.0);
        processedFilesMessageColumn.setMaxWidth(135.0);
        processedFilesMessageColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(processedFilesMessageColumn);

        TableColumn<UploadTask, String> processedSizeMessageColumn = new TableColumn<UploadTask, String>(
                "Processed Size");
        processedSizeMessageColumn.setCellValueFactory(param -> {
            return param.getValue().processedSizeMessageProperty();
        });
        processedSizeMessageColumn.setMinWidth(150.0);
        processedSizeMessageColumn.setMaxWidth(250.0);
        processedSizeMessageColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(processedSizeMessageColumn);

        TableColumn<UploadTask, Task.State> actionButtonColumn = new TableColumn<UploadTask, Task.State>(
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
        _table.itemsProperty().bind(UploadTaskManager.get().tasksProperty());
    }

    @Override
    public Node gui() {
        return _table;
    }

    private class ActionButtonCell extends TableCell<UploadTask, Task.State> {
        private Button _button;

        ActionButtonCell() {
            _button = new Button();
        }

        private UploadTask getTaskObject() {
            return (UploadTask) getTableRow().getItem();
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
