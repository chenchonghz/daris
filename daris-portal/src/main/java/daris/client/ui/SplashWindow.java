package daris.client.ui;

import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.menu.ActionEntry;
import arc.gui.window.WindowProperties;

import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.ui.util.ButtonUtil;

public class SplashWindow {

    private static boolean _visible = false;
    private static Window _win;

    public static void show(String message, final ActionEntry ae) {
        if (!_visible) {

            WindowProperties wp = new WindowProperties();
            wp.setModal(true);
            wp.setCanBeResized(false);
            wp.setCanBeClosed(false);
            wp.setCanBeMoved(false);
            wp.setSize(0.4, 0.4);
            wp.setTitle("DaRIS Portal");
            wp.setShowHeader(false);
            wp.setShowFooter(false);

            _win = Window.create(wp);
        }

        HTML content = new HTML();
        content.setFontStyle(FontStyle.NORMAL);
        content.setFontSize(12);
        content.setFontWeight(FontWeight.BOLD);
        content.setHTML(message);
        content.setMargin(20);

        CenteringPanel cp = new CenteringPanel();
        cp.setContent(content);
        cp.setBackgroundColour(new RGB(0xb2, 0xb2, 0xb2));

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();
        vp.add(cp);

        if (ae != null) {
            ButtonBar bb = ButtonUtil.createButtonBar(Position.BOTTOM, Alignment.RIGHT, 28);
            Button b = bb.addButton(ae.label());
            b.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    if (ae.action() != null) {
                        ae.action().execute();
                    }
                }
            });
            b.setMarginRight(15);
            vp.add(bb);
        }

        _win.setContent(vp);
        _win.centerInPage();
        _win.show();
        _visible = true;
    }

    public static void show(String message) {
        show(message, null);
    }

    public static void hide() {
        if (!_visible) {
            return;
        }
        if (_win != null) {
            _win.close();
        }
        _win = null;
        _visible = false;
    }

}
