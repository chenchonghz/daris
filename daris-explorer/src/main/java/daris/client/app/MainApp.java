package daris.client.app;

import arc.gui.document.StyleSheetFactory;
import arc.mf.client.util.UnhandledException;
import arc.mf.client.util.UnhandledExceptionHandler;
import arc.mf.desktop.server.LogonResponseHandler;
import arc.mf.desktop.server.ServiceCall;
import arc.mf.desktop.server.Session;
import arc.mf.desktop.server.SessionExpiredHandler;
import arc.mf.desktop.ui.util.ApplicationThread;
import arc.mf.event.SystemEventChannel;
import arc.mf.model.asset.AssetServices;
import arc.mf.model.asset.namespace.events.NamespaceEvents;
import arc.mf.model.authorization.AccessControlledResourceCache;
import arc.mf.model.server.events.ServerEvents;
import arc.mf.widgets.asset.AssetGUIRegister;
import arc.mf.widgets.asset.importers.FileImporterRegistry;
import arc.mf.widgets.asset.importers.ImportSettings;
import daris.client.gui.LogonDialog;
import daris.client.gui.MainWindow;
import daris.client.model.object.events.PSSDObjectEvents;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private LogonDialog _logonDialog;
    private MainWindow _mainWindow;

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
                    // SystemEventChannel.subscribe();
                    // PSSDObjectEvents.initialize();
                    // ServerEvents.initialize();
                    // NamespaceEvents.initialize();
                    // AssetGUIRegister.initialize();
                    // AssetServices.declare();
                    // AccessControlledResourceCache.load();
                    // StyleSheetFactory.initialize();
                    // FileImporterRegistry.initialize();
                    // ImportSettings.initialise();

            /*
             * 
             */
            _logonDialog = new LogonDialog();
            _mainWindow = new MainWindow();
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
            UnhandledException.report("Initialising", e);
        }
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(final WindowEvent event) {
                System.exit(0);
            }
        });
        _logonDialog.show(new LogonResponseHandler() {

            @Override
            public void failed(String reason) {

            }

            @Override
            public void succeeded() throws Throwable {
                SystemEventChannel.subscribe();
                PSSDObjectEvents.initialize();
                ServerEvents.initialize();
                NamespaceEvents.initialize();
                AssetGUIRegister.initialize();
                AssetServices.declare();
                AccessControlledResourceCache.load();
                StyleSheetFactory.initialize();
                FileImporterRegistry.initialize();
                ImportSettings.initialise();
                ApplicationThread.execute(new Runnable() {
                    @Override
                    public void run() {
                        _mainWindow.show(primaryStage);
                    }
                });
            }
        });
    }

}
