package daris.client.app;

import arc.mf.client.archive.Archive;
import arc.mf.client.util.UnhandledException;
import arc.mf.client.util.UnhandledExceptionHandler;
import arc.mf.desktop.server.LogonResponseHandler;
import arc.mf.desktop.server.ServiceCall;
import arc.mf.desktop.server.Session;
import arc.mf.desktop.server.SessionExpiredHandler;
import arc.mf.desktop.ui.util.ApplicationThread;
import arc.mf.event.SystemEventChannel;
import arc.mf.model.server.events.ServerEvents;
import daris.client.gui.LogonDialog;
import daris.client.gui.MainWindow;
import daris.client.model.object.events.PSSDObjectEvents;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static final String APP = "daris-explorer";

    public static void main(String[] args) {
        launch(args);
    }

    private LogonDialog _logonDialog;
    private MainWindow _mainWindow;
    private boolean _initialized = false;

    public MainApp() {
        try {
            /*
             * unhandled exception handler
             */
            UnhandledException.setUnhandledExceptionHandler(
                    new UnhandledExceptionHandler() {
                        @Override
                        public void report(final String context,
                                final Throwable t) {
                            System.err.println("Context: " + context);
                            t.printStackTrace(System.err);
                        }
                    });

            /*
             * 
             */
            _logonDialog = new LogonDialog();
            Session.setLogonHandler(new SessionExpiredHandler() {

                @Override
                public void handleThen(ServiceCall svc) {
                    ApplicationThread.execute(new Runnable() {
                        @Override
                        public void run() {
                            _logonDialog.show(new LogonResponseHandler() {

                                @Override
                                public void failed(String reason) {

                                }

                                @Override
                                public void succeeded() throws Throwable {
                                    svc.execute();
                                }
                            });
                        }
                    });
                }
            });
        } catch (Exception e) {
            UnhandledException.report("Initializing", e);
        }
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
        _logonDialog.show(new LogonResponseHandler() {

            @Override
            public void failed(String reason) {

            }

            @Override
            public void succeeded() throws Throwable {
                initialize();
                ApplicationThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (_mainWindow == null) {
                            _mainWindow = new MainWindow();
                        }
                        _mainWindow.show(primaryStage);
                    }
                });
            }
        });
    }

    private void initialize() {
        if (!_initialized) {
            _initialized = true;
            SystemEventChannel.subscribe();
            ServerEvents.initialize();
            PSSDObjectEvents.initialize();
            Archive.declareSupportForAllTypes();
        }
    }

    public static String css() {
        return MainApp.class.getResource("/css/MainApp.css").toExternalForm();
    }

}
