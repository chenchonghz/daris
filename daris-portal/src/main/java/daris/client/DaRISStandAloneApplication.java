package daris.client;

import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.WindowProperties;

import com.google.gwt.event.logical.shared.ResizeEvent;

import daris.client.ui.DObjectBrowser;

public class DaRISStandAloneApplication {

    private static Window _win;

    public static void start() {

        /*
         * Creates the window
         */
        WindowProperties wp = new WindowProperties();
        wp.setModal(false);
        wp.setCanBeResized(true);
        wp.setCanBeClosed(false);
        wp.setCanBeMoved(false);
        wp.setCentered(true);
        wp.setShowHeader(false);
        wp.setShowFooter(false);
        wp.setTitle("DaRIS");
        wp.setSize(1.0, 1.0);

        _win = Window.create(wp);
        _win.setContent(DObjectBrowser.get(true));
        _win.setTitle("DaRIS Portal");
        _win.addCloseListener(new WindowCloseListener() {

            @Override
            public void closed(Window w) {
                DObjectBrowser.reset();
            }
        });

        com.google.gwt.user.client.Window.addResizeHandler(new com.google.gwt.event.logical.shared.ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {

                if (_win != null) {
                    // Fit to browser window
                    _win.setPosition(0, 0);
                    int dw = event.getWidth() - _win.width();
                    int dh = event.getHeight() - _win.height();
                    _win.resizeBy(dw, dh);
                }
            }
        });

        /*
         * Show it
         */
        _win.show();
    }

    public static void stop() {
        if (_win != null) {
            _win.closeIfOK();
            _win = null;
        }
    }

    public static Window window() {
        return _win;
    }

}
