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
import arc.mf.client.file.LocalFile;
import daris.client.util.Filter;

public class LocalFileSelector {

    public static interface FileSelectionHandler {
        void selected(LocalFile file);
    }

    private LocalFile _selected;
    private Filter<LocalFile> _filter;
    private FileSelectionHandler _sh;

    private Window _win;
    private VerticalPanel _vp;
    private LocalFileBrowserPanel _bp;
    private Button _selectButton;
    private boolean _showing;

    public LocalFileSelector(final LocalFile.Filter filter,
            FileSelectionHandler sh) {
        this(new Filter<LocalFile>() {

            @Override
            public boolean matches(LocalFile o) {
                if (filter == LocalFile.Filter.FILES) {
                    return o != null && o.isFile();
                } else if (filter == LocalFile.Filter.DIRECTORIES) {
                    return o != null && o.isDirectory();
                } else {
                    return o != null;
                }
            }
        }, sh);
    }

    public LocalFileSelector(Filter<LocalFile> filter, FileSelectionHandler sh) {
        _filter = filter;
        _sh = sh;
        _bp = new LocalFileBrowserPanel(false) {
            protected void gridRowSelected(LocalFile f) {
                _selected = f;
                _selectButton.setEnabled(
                        f != null && (_filter == null || _filter.matches(f)));
            }

            protected void gridRowDeselected(LocalFile f) {
                _selected = null;
                _selectButton.setEnabled(
                        f != null && (_filter == null || _filter.matches(f)));
            }
        };
        _bp.fitToParent();

        CheckBox<Boolean> cbShowHiddenFiles = new CheckBox<Boolean>(
                "Show Hidden Files");
        cbShowHiddenFiles.setChecked(false);
        cbShowHiddenFiles.addChangeListener(new Listener<Boolean>() {

            @Override
            public void changed(CheckBox<Boolean> cb) {
                _selectButton.disable();
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

        Button cancelButton = bb.addButton("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                _win.hide();
            }
        });

        _selectButton = bb.addButton("Select");
        _selectButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                _win.hide();
                _sh.selected(_selected);
            }
        });
        _selectButton.setMarginRight(25);
        _selectButton.disable();
        _selectButton.focus();

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
        wp.setModal(true);
        wp.setTitle("Local Files");
        wp.setCanBeResized(true);
        wp.setCanBeClosed(false);
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

}
