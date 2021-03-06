package daris.client.ui.dti.file;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.input.CheckBox;
import arc.gui.gwt.widget.input.CheckBox.Listener;
import arc.gui.gwt.widget.list.ListGridHeader;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.gwt.widget.window.WindowCloseListener;
import arc.gui.window.WindowProperties;

public class LocalFileBrowser {

    private Window _win;
    private VerticalPanel _vp;
    private LocalFileBrowserPanel _bp;
    private boolean _showing;

    private LocalFileBrowser() {
        _bp = new LocalFileBrowserPanel(false);
        _bp.fitToParent();

        CheckBox<Boolean> cbShowHiddenFiles = new CheckBox<Boolean>(
                "Show Hidden Files");
        cbShowHiddenFiles.setChecked(false);
        cbShowHiddenFiles.addChangeListener(new Listener<Boolean>() {

            @Override
            public void changed(CheckBox<Boolean> cb) {
                _bp.setShowHiddenFiles(cb.checked());
            }
        });

        ButtonBar bb = new ButtonBar(Position.BOTTOM, Alignment.RIGHT);
        bb.setBackgroundImage(
                new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM,
                        ListGridHeader.HEADER_COLOUR_LIGHT,
                        ListGridHeader.HEADER_COLOUR_DARK));
        bb.setHeight(32);
        bb.setButtonSpacing(20);
        bb.add(cbShowHiddenFiles);

        Button closeButton = bb.addButton("Close");
        closeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                _win.hide();
            }
        });
        closeButton.setMarginRight(25);

        _vp = new VerticalPanel();
        _vp.fitToParent();
        _vp.add(_bp);
        _vp.add(bb);
    }

    public void show(Window owner) {

        if (_showing) {
            _win.close();
        }
        WindowProperties wp = new WindowProperties();
        wp.setModal(false);
        wp.setTitle("Local Files");
        wp.setCanBeResized(true);
        wp.setCanBeClosed(true);
        wp.setCanBeMoved(true);
        wp.setOwnerWindow(owner);
        wp.setSize(0.5, 0.5);
        wp.setCenterInPage(true);
        _win = Window.create(wp);
        _win.addCloseListener(new WindowCloseListener() {

            @Override
            public void closed(Window w) {
                _showing = false;
            }
        });
        _win.setContent(_vp);
        _win.centerInPage();
        _win.show();
        _showing = true;
    }

    private static LocalFileBrowser _instance;

    public static LocalFileBrowser get() {
        if (_instance == null) {
            _instance = new LocalFileBrowser();
        }
        return _instance;
    }

}
