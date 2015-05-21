package daris.client.ui.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import arc.gui.InterfaceComponent;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.menu.MenuButton;
import arc.gui.gwt.widget.menu.MenuToolBar;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.Action;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.MustBeValid;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;

import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.ui.dti.file.LocalFileSelectDialog;
import daris.client.ui.dti.file.LocalFileSelectTarget;
import daris.client.util.ByteUtil;

public class LocalFileForm extends ContainerWidget implements InterfaceComponent, MustBeValid {

    public static final String FILE_ICON = Resource.INSTANCE.file16().getSafeUri().asString();
    public static final String DIRECTORY_ICON = Resource.INSTANCE.folderViolet16().getSafeUri().asString();

    private List<StateChangeListener> _changeListeners;

    private List<LocalFile> _files;
    private boolean _multiple;
    private LocalFileSelectTarget _target;

    private VerticalPanel _vp;
    private ListGrid<LocalFile> _fileGrid;

    private ActionEntry _aeAdd;
    private ActionEntry _aeRemove;
    private ActionEntry _aeClear;

    public LocalFileForm(LocalFileSelectTarget target, boolean multiple) {
        this(target, multiple, null);
    }

    public LocalFileForm(LocalFileSelectTarget target, boolean multiple, List<LocalFile> files) {

        _target = target;
        _multiple = multiple;

        _vp = new VerticalPanel();

        /*
         * Action Menu Tool Bar
         */
        MenuToolBar actionMenuToolBar = new MenuToolBar();
        actionMenuToolBar.setHeight(28);
        actionMenuToolBar.setWidth100();
        
        _aeAdd = new ActionEntry("Add a local file ...", new Action() {
            @Override
            public void execute() {
                LocalFileSelectDialog dlg = new LocalFileSelectDialog(_target, null, null,
                        new LocalFileSelectDialog.FileSelectionHandler() {

                            @Override
                            public void fileSelected(LocalFile file) {
                                boolean added = addFile(file);
                                if (added) {
                                    updateFileGrid(true);
                                }
                            }
                        });
                dlg.show(_vp.window());
            }
        });
        _aeRemove = new ActionEntry("Remove file", new Action() {

            @Override
            public void execute() {
                boolean removed = removeFiles(_fileGrid.selections());
                if (removed) {
                    updateFileGrid(true);
                }
            }
        });
        _aeRemove.disable();
        _aeClear = new ActionEntry("Clear all files", new Action() {

            @Override
            public void execute() {
                if (!_files.isEmpty()) {
                    _files.clear();
                    updateFileGrid(true);
                }
            }
        });
        Menu actionMenu = new Menu("Action") {
            public void preShow() {

            }
        };
        actionMenu.add(_aeAdd);
        actionMenu.add(_aeRemove);
        actionMenu.add(_aeClear);
        MenuButton actionMenuButton = new MenuButton(actionMenu);
        actionMenuToolBar.add(actionMenuButton);

        _vp.add(actionMenuToolBar);

        /*
         * File Grid;
         */
        _fileGrid = new ListGrid<LocalFile>(ScrollPolicy.AUTO);
        _fileGrid.setEmptyMessage("");
        _fileGrid.setLoadingMessage("loading...");
        _fileGrid.setCursorSize(1000);
        _fileGrid.addColumnDefn("path", "File", "File", new WidgetFormatter<LocalFile, String>() {

            @Override
            public BaseWidget format(LocalFile f, String path) {
                String icon = f.isFile() ? FILE_ICON : DIRECTORY_ICON;
                HTML html = new HTML("<div><img src=\"" + icon
                        + "\" style=\"width:16px;height:16px;vertical-align:middle\"><span style=\"\">&nbsp;" + path
                        + "</span></div>");
                html.setFontSize(11);
                return html;
            }
        }).setWidth(300);
        _fileGrid.addColumnDefn("type", "Type");
        _fileGrid.addColumnDefn("size", "Size");
        _fileGrid.setBorder(1, new RGB(0xdd, 0xdd, 0xdd));
        _fileGrid.setMultiSelect(false);
        _fileGrid.enableDropTarget(false);
        _fileGrid.setDropHandler(new DropHandler() {

            @Override
            public DropCheck checkCanDrop(Object o) {
                if (o != null) {
                    if (o instanceof LocalFile) {
                        return DropCheck.CAN;
                    }
                }
                return DropCheck.CANNOT;
            }

            @Override
            public void drop(BaseWidget target, List<Object> files, DropListener dl) {
                boolean added = addFiles(files);
                if (added) {
                    updateFileGrid(true);
                }
                dl.dropped(DropCheck.CAN);
            }
        });
        _fileGrid.setSelectionHandler(new SelectionHandler<LocalFile>() {

            @Override
            public void selected(LocalFile o) {
                List<LocalFile> sfiles = _fileGrid.selections();
                _aeRemove.disable();
                if (sfiles != null && !sfiles.isEmpty()) {
                    _aeRemove.enable();
                }
            }

            @Override
            public void deselected(LocalFile o) {
                List<LocalFile> sfiles = _fileGrid.selections();
                _aeRemove.disable();
                if (sfiles != null && !sfiles.isEmpty()) {
                    _aeRemove.enable();
                }
            }
        });
        _fileGrid.fitToParent();
        _vp.add(_fileGrid);

        initWidget(_vp);

        /*
         * set data
         */
        setFiles(files, false);
    }

    @Override
    public boolean changed() {

        return false;
    }

    @Override
    public void addChangeListener(StateChangeListener listener) {
        if (_changeListeners == null) {
            _changeListeners = new Vector<StateChangeListener>();
        }
        _changeListeners.add(listener);
    }

    @Override
    public void removeChangeListener(StateChangeListener listener) {
        if (_changeListeners != null) {
            _changeListeners.remove(listener);
        }
    }

    private void notifyOfStateChange() {
        if (_changeListeners != null) {
            for (StateChangeListener l : _changeListeners) {
                l.notifyOfChangeInState();
            }
        }
    }

    @Override
    public Validity valid() {
        if (!_files.isEmpty()) {
            if (!_multiple && _files.size() > 1) {
                return new Validity() {

                    @Override
                    public boolean valid() {
                        return false;
                    }

                    @Override
                    public String reasonForIssue() {
                        return "Expecting one file. Multiple files found.";
                    }
                };
            } else {
                return IsValid.INSTANCE;
            }
        } else {
            return new Validity() {

                @Override
                public boolean valid() {
                    return false;
                }

                @Override
                public String reasonForIssue() {
                    return "Expecting at least one file. Found none.";
                }
            };
        }
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    public List<LocalFile> files() {
        return _files;
    }

    public LocalFile file() {
        if (_files != null && !_files.isEmpty()) {
            return _files.get(0);
        }
        return null;
    }

    public void setFiles(List<LocalFile> files, boolean fireChangeEvent) {
        _files = filterFiles(files, _target, _multiple);
        updateFileGrid(fireChangeEvent);
    }

    public void setFile(LocalFile file, boolean fireChangeEvent) {
        _files.clear();
        if (file != null) {
            _files.add(file);
        }
        updateFileGrid(fireChangeEvent);
    }

    protected boolean removeFiles(List<LocalFile> files) {
        if (files == null || files.isEmpty()) {
            return false;
        }
        boolean changed = false;
        for (LocalFile file : files) {
            int idx = _files.indexOf(file);
            if (idx != -1) {
                _files.remove(idx);
                if (!changed) {
                    changed = true;
                }
            }
        }
        return changed;
    }

    protected boolean addFiles(List<Object> files) {
        boolean changed = false;
        for (Object o : files) {
            LocalFile f = (LocalFile) o;
            boolean added = addFile(f);
            if (!changed && added) {
                changed = true;
            }
        }
        return changed;
    }

    protected boolean addFile(LocalFile file) {
        if (file == null) {
            return false;
        }
        if (_files.contains(file)) {
            return false;
        }
        if (_target == LocalFileSelectTarget.FILE && !file.isFile()) {
            return false;
        }
        if (_target == LocalFileSelectTarget.DIRECTORY && !file.isDirectory()) {
            return false;
        }
        if (!_multiple) {
            _files.clear();
        }
        return _files.add(file);
    }

    private void updateFileGrid(boolean fireChangeEvent) {
        if (_files.isEmpty()) {
            _fileGrid.setData(null, false);
        } else {
            List<ListGridEntry<LocalFile>> es = new Vector<ListGridEntry<LocalFile>>(_files.size());
            for (LocalFile f : _files) {
                ListGridEntry<LocalFile> e = new ListGridEntry<LocalFile>(f);
                e.set("name", f.name());
                e.set("size", f.isFile() ? ByteUtil.humanReadableByteCount(f.length(), true) : "");
                e.set("path", f.path());
                e.set("type", f.isDirectory() ? "directory" : "file");
                es.add(e);
            }
            _fileGrid.setData(es, false);
        }
        if (fireChangeEvent) {
            notifyOfStateChange();
        }
    }

    private static List<LocalFile> filterFiles(List<LocalFile> files, LocalFileSelectTarget target, boolean multiple) {

        List<LocalFile> rfiles = new ArrayList<LocalFile>();
        if (files == null || files.isEmpty()) {
            return rfiles;
        }
        if (target == LocalFileSelectTarget.ANY) {
            if (multiple) {
                rfiles.addAll(files);
            } else {
                rfiles.add(files.get(0));
            }
            return rfiles;
        }
        for (LocalFile f : files) {
            if ((target == LocalFileSelectTarget.DIRECTORY && f.isDirectory())
                    || (target == LocalFileSelectTarget.FILE && f.isFile())) {
                rfiles.add(f);
                if (!multiple) {
                    break;
                }
            }
        }
        return rfiles;
    }

}
