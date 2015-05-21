package daris.client;

import arc.gui.gwt.dnd.DragAndDrop;
import arc.mf.client.plugin.Plugin;
import arc.mf.client.util.ThrowableUtil;
import arc.mf.desktop.plugin.PluginApplication;
import arc.mf.event.SystemEventChannel;
import arc.mf.model.Model;
import arc.mf.session.Session;
import arc.mf.session.SessionHandler;

import com.google.gwt.core.client.EntryPoint;

import daris.client.model.announcement.events.AnnouncementEvents;
import daris.client.model.object.events.PSSDObjectEvents;
import daris.client.model.sc.ActiveShoppingCart;
import daris.client.model.sc.ShoppingCartDownloadManager;
import daris.client.model.transform.events.TransformEvents;
import daris.client.ui.announcement.AnnouncementMonitor;
import daris.client.ui.sc.ShoppingCartDialog;
import daris.client.ui.theme.Theme;
import daris.client.ui.transform.TransformBrowser;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class DaRIS implements EntryPoint {

    public static final String PACKAGE = "daris";

    public DaRIS() {
    }

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {

//        Session.setAutoLogonCredentials("system", "manager", "change_me");
        try {
            if (Plugin.isPluginEnvironment()) {
                /*
                 * Arcitecta Desktop Plug-in Application
                 */
                PluginApplication.register(DaRISPluginApplication.get());
            } else {
                /*
                 * Stand-alone Application
                 */
                BrowserCheck.check();
                Session.setLoginDialog(DaRISLoginDialog.get());
                Session.initialize(new SessionHandler() {

                    @Override
                    public void sessionCreated(boolean initial) {

                        DaRIS.initialise();
                        DaRISStandAloneApplication.start();
                    }

                    @Override
                    public void sessionExpired() {

                        DaRIS.finalise();
                        DaRISStandAloneApplication.stop();
                    }

                    @Override
                    public void sessionTerminated() {

                        DaRIS.finalise();
                        DaRISStandAloneApplication.stop();
                    }
                });
            }
        } catch (Throwable t) {
            String st = ThrowableUtil.stackTrack(t);
            com.google.gwt.user.client.Window.alert("Error: " + t.getClass().getName() + ": " + t.getMessage() + ": "
                    + st);
        }

    }

    public static void initialise() {

        /*
         * Enable pssd events
         */
        PSSDObjectEvents.initialize();

        /*
         * Enable transform events
         */
        TransformEvents.initialize();

        /*
         * Enable pssd announcement events
         */
        AnnouncementEvents.initialize();

        /*
         * Enable model events
         */
        Model.initialize();

        /*
         * Enable drag and drop
         */
        DragAndDrop.initialize();

        /*
         * Subscribes to system event channel
         */
        SystemEventChannel.subscribe();

        /*
         * Initialize ActiveShoppingCart (clear the cached cart, it causes
         * problem when switching users).
         */
        ActiveShoppingCart.initialize();

        /*
         * Listening to shopping cart (update) event, if the cart is in
         * "data ready" state and the delivery method is "download", start
         * browser downloading.
         */
        ShoppingCartDownloadManager.subscribe();

        /*
         * Starts announcement monitor
         */
        AnnouncementMonitor.start();

        if (Plugin.isStandaloneApplication()) {
            /*
             * Install/Initialize DTI java applet if it is running as a
             * stand-alone application. (If running as ADesktop plugin, it is
             * not required because the ADesktop app will load the DTI applet.)
             */
            DTI.install();
            /*
             * Override some theme settings.
             */
            Theme.initialize();
        }

    }

    public static void finalise() {

        if (Plugin.isStandaloneApplication()) {

            /*
             * stop listening to system events
             */
            SystemEventChannel.unsubscribe(Session.created());
        }

        /*
         * Unsubscribe the shopping cart download manager
         */
        ShoppingCartDownloadManager.unsubscribe();

        /*
         * Unsubscribe the shopping cart dialog
         */
        ShoppingCartDialog.reset();

        /*
         * Reset TransformMonitor
         */
        // TransformMonitor.reset();
        TransformBrowser.reset();

        /*
         * Reset AnnouncementMonitor
         */
        AnnouncementMonitor.stop();

    }

}
