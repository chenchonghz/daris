package daris.client.ui.dti.file;

import java.util.List;

import arc.gui.file.FileFilter;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.list.ListGridRowDoubleClickHandler;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUI;
import arc.gui.gwt.widget.tree.TreeGUIEventHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.ObjectUtil;
import arc.mf.object.tree.Node;

import com.google.gwt.event.dom.client.DoubleClickEvent;

import daris.client.model.file.LocalDirectoryTree;

public class LocalFileNavigatorPanel extends HorizontalSplitPanel {

    private TreeGUI _dirTreeGUI;
    private LocalFileGrid _grid;
    private LocalFile _dir;
    private List<LocalFile> _files;
    private LocalFile _selectAfterOpen;

    public LocalFileNavigatorPanel(LocalFile dir, LocalFile.Filter filter,
            FileFilter fileFilter, boolean multiSelect) {

        _grid = new LocalFileGrid(dir, filter, fileFilter, multiSelect);
        _grid.setSelectionHandler(new SelectionHandler<LocalFile>() {

            @Override
            public void selected(LocalFile o) {
                _files = _grid.selections();
                selectedFiles(_files);
            }

            @Override
            public void deselected(LocalFile o) {

            }
        });
        _grid.setRowDoubleClickHandler(new ListGridRowDoubleClickHandler<LocalFile>() {

            @Override
            public void doubleClicked(LocalFile data, DoubleClickEvent event) {
                if (data.isDirectory()) {
                    if (!_dirTreeGUI.isOpen(directory())) {
                        _selectAfterOpen = data;
                        _dirTreeGUI.open(directory());
                    } else {
                        _dirTreeGUI.select(data);
                    }
                }
            }
        });

        _dirTreeGUI = new TreeGUI(new LocalDirectoryTree(), ScrollPolicy.AUTO);
        _dirTreeGUI.setHeight100();
        _dirTreeGUI.setPreferredWidth(0.4);
        _dirTreeGUI.setEventHandler(new TreeGUIEventHandler() {

            @Override
            public void clicked(Node n) {

            }

            @Override
            public void selected(Node n) {
                LocalFile dir = (LocalFile) n.object();
                if (dir.isDirectory() && !ObjectUtil.equals(_dir, dir)) {
                    _dir = dir;
                    _grid.setDirectory(_dir);
                    if (_dir != null) {
                        _files = null;
                        selectedDirectory(_dir);
                    }
                }
            }

            @Override
            public void deselected(Node n) {

            }

            @Override
            public void opened(Node n) {
                if (_selectAfterOpen != null) {
                    // TODO: enable it after Jason add equals() method to
                    // DTIFile.java
                    //
                    _dirTreeGUI.select(_selectAfterOpen);
                    _selectAfterOpen = null;
                }
            }

            @Override
            public void closed(Node n) {

            }

            @Override
            public void added(Node n) {

            }

            @Override
            public void removed(Node n) {

            }

            @Override
            public void changeInMembers(Node n) {

            }
        });
        _dirTreeGUI.enableNodeDrag();

        fitToParent();
        add(_dirTreeGUI);
        add(_grid);
    }

    public LocalFile directory() {
        return _dir;
    }

    public List<LocalFile> files() {
        return _files;
    }

    public FileFilter fileFilter() {
        return _grid.fileFilter();
    }

    public LocalFile.Filter filter() {
        return _grid.filter();
    }

    protected void selectedDirectory(LocalFile dir) {

    }

    protected void selectedFiles(List<LocalFile> files) {

    }
}
