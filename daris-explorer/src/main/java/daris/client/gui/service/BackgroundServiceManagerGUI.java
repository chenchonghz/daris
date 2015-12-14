package daris.client.gui.service;

import java.util.Date;

import arc.gui.InterfaceComponent;
import arc.mf.client.RemoteTask;
import arc.mf.model.service.BackgroundService;
import daris.client.model.service.BackgroundServiceManager;
import daris.client.model.service.BackgroundServiceProperties;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;

public class BackgroundServiceManagerGUI implements InterfaceComponent {

    private TableView<BackgroundServiceProperties> _table;

    public BackgroundServiceManagerGUI() {
        _table = new TableView<BackgroundServiceProperties>();
        _table.setPlaceholder(new Label("No background services."));
        TableColumn<BackgroundServiceProperties, Number> idColumn = new TableColumn<BackgroundServiceProperties, Number>(
                "ID");
        idColumn.setCellValueFactory(param -> {
            return param.getValue().idProperty();
        });
        idColumn.setMaxWidth(60.0);
        idColumn.setMaxWidth(60.0);
        idColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(idColumn);

        TableColumn<BackgroundServiceProperties, RemoteTask.State> stateColumn = new TableColumn<BackgroundServiceProperties, RemoteTask.State>(
                "State");
        stateColumn.setCellValueFactory(param -> {
            return param.getValue().stateProperty();
        });
        stateColumn.setMinWidth(100.0);
        stateColumn.setMaxWidth(100.0);
        stateColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(stateColumn);

        TableColumn<BackgroundServiceProperties, String> nameColumn = new TableColumn<BackgroundServiceProperties, String>(
                "Name");
        nameColumn.setCellValueFactory(param -> {
            return param.getValue().descriptionProperty();
        });
        nameColumn.setMinWidth(200.0);
        nameColumn.setMinWidth(350.0);
        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        _table.getColumns().add(nameColumn);

        TableColumn<BackgroundServiceProperties, String> descriptionColumn = new TableColumn<BackgroundServiceProperties, String>(
                "Description");
        descriptionColumn.setCellValueFactory(param -> {
            return param.getValue().descriptionProperty();
        });
        descriptionColumn.setMinWidth(200.0);
        descriptionColumn.setMinWidth(350.0);
        descriptionColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        _table.getColumns().add(descriptionColumn);

        TableColumn<BackgroundServiceProperties, String> currentActivityColumn = new TableColumn<BackgroundServiceProperties, String>(
                "Current Activity");
        currentActivityColumn.setCellValueFactory(param -> {
            return param.getValue().descriptionProperty();
        });
        currentActivityColumn.setMinWidth(200.0);
        currentActivityColumn.setMinWidth(350.0);
        currentActivityColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        _table.getColumns().add(currentActivityColumn);

        TableColumn<BackgroundServiceProperties, String> progressMessageColumn = new TableColumn<BackgroundServiceProperties, String>(
                "Completed/Total");
        progressMessageColumn.setCellValueFactory(param -> {
            return param.getValue().descriptionProperty();
        });
        progressMessageColumn.setMinWidth(200.0);
        progressMessageColumn.setMinWidth(200.0);
        progressMessageColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        _table.getColumns().add(progressMessageColumn);

        TableColumn<BackgroundServiceProperties, Date> startTimeColumn = new TableColumn<BackgroundServiceProperties, Date>(
                "Started");
        startTimeColumn.setCellValueFactory(param -> {
            return param.getValue().startTimeProperty();
        });
        startTimeColumn.setMinWidth(200.0);
        startTimeColumn.setMinWidth(200.0);
        startTimeColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(startTimeColumn);

        TableColumn<BackgroundServiceProperties, Date> endTimeColumn = new TableColumn<BackgroundServiceProperties, Date>(
                "Ended");
        endTimeColumn.setCellValueFactory(param -> {
            return param.getValue().startTimeProperty();
        });
        endTimeColumn.setMinWidth(200.0);
        endTimeColumn.setMinWidth(200.0);
        endTimeColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(endTimeColumn);

        TableColumn<BackgroundServiceProperties, Double> progressColumn = new TableColumn<BackgroundServiceProperties, Double>(
                "Progress");
        progressColumn.setCellValueFactory(param -> {
            return param.getValue().progressProperty();
        });
        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());
        progressColumn.setMinWidth(250.0);
        progressColumn.setMaxWidth(250.0);
        progressColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(progressColumn);

        TableColumn<BackgroundServiceProperties, Double> executionTimeColumn = new TableColumn<BackgroundServiceProperties, Double>(
                "Execution Time");
        executionTimeColumn.setCellValueFactory(param -> {
            return param.getValue().executionTimeProperty();
        });
        executionTimeColumn.setMinWidth(135.0);
        executionTimeColumn.setMaxWidth(135.0);
        executionTimeColumn.setStyle("-fx-alignment: CENTER;");
        _table.getColumns().add(executionTimeColumn);

        TableColumn<BackgroundServiceProperties, RemoteTask.State> actionButtonColumn = new TableColumn<BackgroundServiceProperties, RemoteTask.State>(
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
        _table.itemsProperty()
                .bind(BackgroundServiceManager.get().servicesProperty());
    }

    @Override
    public Node gui() {
        return _table;
    }

    private class ActionButtonCell
            extends TableCell<BackgroundServiceProperties, RemoteTask.State> {
        private ButtonBar _bb;
        private Button _removeButton;
        private Button _suspendButton;
        private Button _resumeButton;
        private Button _abortButton;

        ActionButtonCell() {
            _bb = new ButtonBar();
            _removeButton = new Button("Remove");
            _removeButton.setOnAction(event -> {
                remove();
            });
            _suspendButton = new Button("Suspend");
            _suspendButton.setOnAction(event -> {
                suspend();
            });
            _resumeButton = new Button("Resume");
            _resumeButton.setOnAction(event -> {
                resume();
            });
            _abortButton = new Button("Abort");
            _abortButton.setOnAction(event -> {
                abort();
            });
        }

        private BackgroundService getTaskObject() {
            return ((BackgroundServiceProperties) getTableRow().getItem())
                    .backgroundServiceProperty().get();
        }

        private void remove() {
            BackgroundService bs = getTaskObject();
            if (bs != null) {
                bs.destroy();
                // TODO
            }
        }

        private void abort() {
            BackgroundService bs = getTaskObject();
            if (bs != null) {
                bs.abort();
                // TODO
            }
        }

        private void suspend() {
            BackgroundService bs = getTaskObject();
            if (bs != null) {
                bs.suspend();
                // TODO
            }
        }

        private void resume() {
            BackgroundService bs = getTaskObject();
            if (bs != null) {
                bs.resume();
                // TODO
            }
        }

        @Override
        protected void updateItem(final RemoteTask.State state, boolean empty) {
            super.updateItem(state, empty);
            BackgroundService bs = getTaskObject();
            if (empty || bs == null) {
                setGraphic(null);
            } else {
                _bb.getButtons().clear();
                if (bs.finished()) {
                    _bb.getButtons().add(_removeButton);
                } else {
                    if (!bs.aborted() && bs.canAbort()) {
                        _bb.getButtons().add(_abortButton);
                    }
                    if (bs.state() != RemoteTask.State.SUSPENDED
                            && bs.canSuspend()) {
                        _bb.getButtons().add(_suspendButton);
                    }
                    if (bs.state() == RemoteTask.State.SUSPENDED) {
                        _bb.getButtons().add(_resumeButton);
                    }
                }
                setGraphic(_bb);
            }
        }
    }

}
