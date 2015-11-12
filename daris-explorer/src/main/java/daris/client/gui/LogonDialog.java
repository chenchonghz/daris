package daris.client.gui;

import arc.mf.desktop.server.LogonResponseHandler;
import arc.mf.desktop.server.Session;
import arc.mf.desktop.server.Transport;
import arc.mf.desktop.ui.field.IntegerField;
import arc.mf.desktop.ui.util.ApplicationThread;
import daris.client.settings.ConnectionSettings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LogonDialog {

    private String _host;
    private int _port;
    private boolean _encrypt;
    private String _domain;
    private String _user;
    private String _password;
    private LogonResponseHandler _rh;

    private Stage _stage;
    private ComboBox<String> _hostCombo;
    private ComboBox<Transport> _transportCombo;
    private IntegerField _portField;
    private TextField _domainField;
    private TextField _userField;
    private PasswordField _passwordField;

    private Label _statusLabel;
    private Button _logonButton;

    public LogonDialog() {
        _host = null;
        _port = 80;
        _encrypt = false;
        _domain = null;
        _user = null;
        _password = null;

        ConnectionSettings settings = ConnectionSettings.getLast();
        if (settings != null) {
            _host = settings.host();
            _encrypt = settings.encrypt();
            _port = settings.port() > 0 ? settings.port()
                    : (settings.encrypt() ? 443 : 80);
            _domain = settings.domain();
            _user = settings.user();
        }

        /*
         * 
         */
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));
        gridPane.setBorder(new Border(new BorderStroke[] {
                new BorderStroke(Color.BEIGE, BorderStrokeStyle.SOLID,
                        new CornerRadii(5), BorderWidths.DEFAULT) }));

        ColumnConstraints cc = new ColumnConstraints();
        cc.setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().add(cc);
        cc = new ColumnConstraints();
        cc.setHalignment(HPos.LEFT);
        gridPane.getColumnConstraints().add(cc);

        Text sceneTitle = new Text("DaRIS");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        gridPane.add(sceneTitle, 0, 0, 2, 1);
        GridPane.setConstraints(sceneTitle, 0, 0, 2, 1, HPos.LEFT, VPos.CENTER);

        /*
         * host
         */
        Label serverLabel = new Label("Host:");
        gridPane.add(serverLabel, 0, 1);

        _hostCombo = new ComboBox<String>();
        _hostCombo.setEditable(true);
        _hostCombo.setPromptText("address");
        _hostCombo.getItems().addAll(ConnectionSettings.getHosts());
        _hostCombo.setValue(_host);
        _hostCombo.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                String host = _hostCombo.getSelectionModel().getSelectedItem();
                if (host != null) {
                    ConnectionSettings settings = ConnectionSettings.get(host);
                    if (settings != null) {
                        _transportCombo.setValue(settings.encrypt()
                                ? Transport.HTTPS : Transport.HTTP);
                        _portField.setValue(settings.port());
                        _domainField.setText(settings.domain());
                        _userField.setText(settings.user());
                    }
                }
            }
        });
        _hostCombo.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                _host = newValue;
                validate();
            }
        });
        _hostCombo.getEditor().textProperty()
                .addListener(new ChangeListener<String>() {

                    @Override
                    public void changed(
                            ObservableValue<? extends String> observable,
                            String oldValue, String newValue) {
                        _host = newValue;
                        validate();
                    }
                });
        gridPane.add(_hostCombo, 1, 1);

        /*
         * transport
         */
        Label transportLabel = new Label("Transport:");
        gridPane.add(transportLabel, 0, 2);

        _transportCombo = new ComboBox<Transport>();
        _transportCombo.getItems().addAll(Transport.values());
        _transportCombo.setValue(_encrypt ? Transport.HTTPS : Transport.HTTP);
        _transportCombo.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                Transport transport = _transportCombo.getSelectionModel()
                        .getSelectedItem();
                if (Transport.HTTPS == transport) {
                    _portField.setValue(443);
                } else {
                    _portField.setValue(80);
                }
            }
        });
        _transportCombo.valueProperty()
                .addListener(new ChangeListener<Transport>() {

                    @Override
                    public void changed(
                            ObservableValue<? extends Transport> observable,
                            Transport oldValue, Transport newValue) {
                        _encrypt = Transport.HTTPS == newValue;
                        validate();
                    }
                });
        gridPane.add(_transportCombo, 1, 2);

        /*
         * port
         */
        Label portLabel = new Label("Port:");
        gridPane.add(portLabel, 0, 3);

        _portField = new IntegerField(0, 65535);
        _portField.setPrefWidth(90);
        _portField.setMaxWidth(90);
        _portField.setValue(_port);
        _portField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                _port = Integer.parseInt(newValue);
                validate();
            }
        });
        _portField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                String port = _portField.getText();
                if (port != null && !port.trim().isEmpty()) {
                    _domainField.requestFocus();
                }
            }
        });
        gridPane.add(_portField, 1, 3);

        /*
         * domain
         */
        Label domainLabel = new Label("Domain:");
        gridPane.add(domainLabel, 0, 4);

        _domainField = new TextField();
        _domainField.setText(_domain);
        _domainField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                _domain = newValue;
                validate();
            }
        });
        _domainField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                String domain = _domainField.getText();
                if (domain != null && !domain.trim().isEmpty()) {
                    _userField.requestFocus();
                }
            }
        });
        gridPane.add(_domainField, 1, 4);

        /*
         * user
         */
        Label userLabel = new Label("User:");
        gridPane.add(userLabel, 0, 5);

        _userField = new TextField();
        _userField.setText(_user);
        _userField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                _user = newValue;
                validate();
            }
        });
        _userField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                String user = _userField.getText();
                if (user != null && !user.trim().isEmpty()) {
                    _passwordField.requestFocus();
                }
            }
        });
        gridPane.add(_userField, 1, 5);

        /*
         * password
         */
        Label passwordLabel = new Label("Password:");
        gridPane.add(passwordLabel, 0, 6);

        _passwordField = new PasswordField();
        _passwordField.setText(_password);
        _passwordField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                _password = newValue;
                validate();
            }
        });
        _passwordField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!_logonButton.disabledProperty().getValue()) {
                    _logonButton.requestFocus();
                }
            }
        });
        _passwordField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                if (validate()) {
                    logon();
                }
            }
        });
        gridPane.add(_passwordField, 1, 6);

        _statusLabel = new Label();
        _statusLabel.setTextFill(Color.RED);
        gridPane.add(_statusLabel, 0, 7, 2, 1);

        _logonButton = new Button("Logon");
        _logonButton.setDisable(true);
        _logonButton.setDefaultButton(true);
        _logonButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                logon();
            }
        });
        gridPane.add(_logonButton, 0, 8, 2, 1);

        _stage = new Stage();
        _stage.initStyle(StageStyle.UTILITY);
        _stage.initModality(Modality.APPLICATION_MODAL);
        _stage.setScene(new Scene(gridPane, 480, 480));
    }

    private void logon() {
        _logonButton.setDisable(true);
        new Thread(() -> {
            Session.logon(_encrypt ? Transport.HTTPS : Transport.HTTP, _host,
                    _port, _domain, _user, _password,
                    new LogonResponseHandler() {

                @Override
                public void failed(String reason) {
                    ApplicationThread.execute(() -> {
                        _statusLabel.setText(reason);
                        _logonButton.setDisable(false);
                    });
                    if (_rh != null) {
                        _rh.failed(reason);
                    }
                }

                @Override
                public void succeeded() throws Throwable {
                    ApplicationThread.execute(() -> {
                        _logonButton.setDisable(false);
                        _stage.hide();
                    });
                    ConnectionSettings.add(new ConnectionSettings(_host, _port,
                            _encrypt, _domain, _user));
                    ConnectionSettings.save();
                    if (_rh != null) {
                        _rh.succeeded();
                        _rh = null;
                    }
                }
            });
        }).start();
    }

    private boolean validate() {

        /*
         * validate
         */
        _logonButton.setDisable(true);
        _statusLabel.setText("");

        if (_host == null || _host.trim().isEmpty()) {
            _statusLabel.setText("Missing host address.");
            _hostCombo.requestFocus();
            return false;
        }

        if (_transportCombo.getValue() == null) {
            _statusLabel.setText("Missing transport protocol.");
            _transportCombo.requestFocus();
            return false;
        }

        if (_port <= 0) {
            _statusLabel.setText("Missing server port.");
            _portField.requestFocus();
            return false;
        }

        if (_domain == null || _domain.trim().isEmpty()) {
            _statusLabel.setText("Missing authentication domain name.");
            _domainField.requestFocus();
            return false;
        }

        if (_user == null || _user.trim().isEmpty()) {
            _statusLabel.setText("Missing user name.");
            _userField.requestFocus();
            return false;
        }

        if (_password == null || _password.trim().isEmpty()) {
            _statusLabel.setText("Missing password.");
            _passwordField.requestFocus();
            return false;
        }
        _logonButton.setDisable(false);
        return true;
    }

    public void show(LogonResponseHandler rh) {
        _rh = rh;
        _stage.show();
    }

}
