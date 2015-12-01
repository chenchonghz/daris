package daris.client.gui;

import java.util.List;

import arc.mf.desktop.server.LogonResponseHandler;
import arc.mf.desktop.server.Session;
import arc.mf.desktop.server.Transport;
import arc.mf.desktop.ui.field.IntegerField;
import arc.mf.desktop.ui.util.ApplicationThread;
import daris.client.idp.IdentityProvider;
import daris.client.idp.IdentityProviderList;
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
    private IdentityProvider _provider;
    private String _user;
    private String _password;
    private LogonResponseHandler _rh;
    private List<IdentityProvider> _providers;

    private Stage _stage;
    private GridPane _gridPane;
    private Text _title;
    private Label _hostLabel;
    private ComboBox<String> _hostCombo;
    private Label _transportLabel;
    private ComboBox<Transport> _transportCombo;
    private Label _portLabel;
    private IntegerField _portField;
    private Label _domainLabel;
    private TextField _domainField;
    private Label _providerLabel;
    private ComboBox<IdentityProvider> _providerCombo;
    private Label _userLabel;
    private TextField _userField;
    private Label _passwordLabel;
    private PasswordField _passwordField;

    private Label _statusLabel;
    private Button _logonButton;

    public LogonDialog() {
        _host = null;
        _port = 80;
        _encrypt = false;
        _domain = null;
        _provider = null;
        _user = null;
        _password = null;

        ConnectionSettings settings = ConnectionSettings.getLast();
        if (settings != null) {
            _host = settings.host();
            _encrypt = settings.encrypt();
            _port = settings.port() > 0 ? settings.port()
                    : (settings.encrypt() ? 443 : 80);
            _domain = settings.domain();
            _provider = settings.provider();
            _user = settings.user();
        }

        /*
         * 
         */
        _gridPane = new GridPane();
        _gridPane.setAlignment(Pos.CENTER);
        _gridPane.setHgap(10);
        _gridPane.setVgap(10);
        _gridPane.setPadding(new Insets(25, 25, 25, 25));
        _gridPane.setBorder(new Border(new BorderStroke[] {
                new BorderStroke(Color.BEIGE, BorderStrokeStyle.SOLID,
                        new CornerRadii(5), BorderWidths.DEFAULT) }));

        ColumnConstraints cc = new ColumnConstraints();
        cc.setHalignment(HPos.RIGHT);
        _gridPane.getColumnConstraints().add(cc);
        cc = new ColumnConstraints();
        cc.setHalignment(HPos.LEFT);
        _gridPane.getColumnConstraints().add(cc);

        /*
         * title
         */
        _title = new Text("DaRIS");
        _title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        GridPane.setConstraints(_title, 0, 0, 2, 1, HPos.LEFT, VPos.CENTER);

        /*
         * host
         */
        _hostLabel = new Label("Host:");
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

        /*
         * transport
         */
        _transportLabel = new Label("Transport:");
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

        /*
         * port
         */
        _portLabel = new Label("Port:");
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

        /*
         * domain
         */
        _domainLabel = new Label("Domain:");
        _domainField = new TextField();
        _domainField.setText(_domain);
        _domainField.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                _domain = newValue;
                updateProviders(_domain);
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

        /*
         * provider
         */
        _providerLabel = new Label("Provider:");
        _providerCombo = new ComboBox<IdentityProvider>();
        if (_providers != null && !_providers.isEmpty()) {
            _providerCombo.getItems().setAll(_providers);
        }
        _providerCombo.setValue(_provider);
        _providerCombo.valueProperty()
                .addListener((observable, oldValue, newValue) -> {
                    _provider = newValue;
                    validate();
                });

        /*
         * user
         */
        _userLabel = new Label("User:");
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

        /*
         * password
         */
        _passwordLabel = new Label("Password:");
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

        _statusLabel = new Label();
        _statusLabel.setTextFill(Color.RED);

        _logonButton = new Button("Logon");
        _logonButton.setDisable(true);
        _logonButton.setDefaultButton(true);
        _logonButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                logon();
            }
        });

        /*
         * initial show
         */
        updateGrid();
        if (_domain != null) {
            updateProviders(_domain);
        }

        _stage = new Stage();
        _stage.initStyle(StageStyle.UTILITY);
        _stage.initModality(Modality.APPLICATION_MODAL);
        _stage.setScene(new Scene(_gridPane, 480, 520));
        _stage.setOnCloseRequest(event -> {
            System.exit(0);
        });
    }

    private void updateGrid() {
        _gridPane.getChildren().clear();
        _gridPane.add(_title, 0, 0, 2, 1);
        _gridPane.addRow(1, _hostLabel, _hostCombo);
        _gridPane.addRow(2, _transportLabel, _transportCombo);
        _gridPane.addRow(3, _portLabel, _portField);
        _gridPane.addRow(4, _domainLabel, _domainField);
        if (_providers != null && !_providers.isEmpty()) {
            _providerCombo.getItems().setAll(_providers);
            if (_provider != null && _providers.contains(_provider)) {
                _providerCombo.setValue(_provider);
            } else {
                _provider = _providers.get(0);
                _providerCombo.setValue(_provider);
            }
            _gridPane.addRow(5, _providerLabel, _providerCombo);
            _gridPane.addRow(6, _userLabel, _userField);
            _gridPane.addRow(7, _passwordLabel, _passwordField);
            _gridPane.add(_statusLabel, 0, 8, 2, 1);
            _gridPane.add(_logonButton, 0, 9, 2, 1);
        } else {
            _gridPane.addRow(5, _userLabel, _userField);
            _gridPane.addRow(6, _passwordLabel, _passwordField);
            _gridPane.add(_statusLabel, 0, 7, 2, 1);
            _gridPane.add(_logonButton, 0, 8, 2, 1);
        }
    }

    private void updateProviders(final String domain) {
        if (domain == null || domain.isEmpty() || _host == null
                || _host.isEmpty() || _port <= 0) {
            _providers = null;
            _provider = null;
            ApplicationThread.execute(() -> {
                updateGrid();
            });
            return;
        }
        IdentityProviderList.resolve(_host, _port, _encrypt, domain,
                (providers) -> {
                    _providers = providers;
                    if (_providers == null || _providers.isEmpty()) {
                        _provider = null;
                    }
                    ApplicationThread.execute(() -> {
                        updateGrid();
                    });
                });
    }

    private void logon() {
        _logonButton.setDisable(true);
        new Thread(() -> {
            String provider = _provider == null ? null : _provider.id();
            String user = _user;
            if (provider != null) {
                int idx = _user.indexOf(":");
                if (idx < 0) {
                    if (provider.contains(":")) {
                        provider = "[" + provider + "]";
                    }
                    user = provider.concat(":").concat(_user);
                }
            }
            Session.logon(_encrypt ? Transport.HTTPS : Transport.HTTP, _host,
                    _port, _domain, user, _password,
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
                            _encrypt, _domain, _provider, _user));
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
            // _hostCombo.requestFocus();
            return false;
        }

        if (_transportCombo.getValue() == null) {
            _statusLabel.setText("Missing transport protocol.");
            // _transportCombo.requestFocus();
            return false;
        }

        if (_port <= 0) {
            _statusLabel.setText("Missing server port.");
            // _portField.requestFocus();
            return false;
        }

        if (_domain == null || _domain.trim().isEmpty()) {
            _statusLabel.setText("Missing authentication domain name.");
            // _domainField.requestFocus();
            return false;
        }

        if (_providers != null && _provider == null) {
            _statusLabel.setText("Missing identity provider.");
            // _providerCombo.requestFocus();
            return false;
        }

        if (_user == null || _user.trim().isEmpty()) {
            _statusLabel.setText("Missing user name.");
            // _userField.requestFocus();
            return false;
        }

        if (_password == null || _password.trim().isEmpty()) {
            _statusLabel.setText("Missing password.");
            // _passwordField.requestFocus();
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
