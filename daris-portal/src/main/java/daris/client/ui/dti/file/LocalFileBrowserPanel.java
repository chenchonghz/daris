package daris.client.ui.dti.file;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.event.dom.client.DoubleClickEvent;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.list.ListGridRowDoubleClickHandler;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.CenteringPanel.Axis;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUI;
import arc.gui.gwt.widget.tree.TreeGUIEventHandler;
import arc.mf.client.dti.DTI;
import arc.mf.client.file.FileHandler;
import arc.mf.client.file.LocalFile;
import arc.mf.client.util.DateTime;
import arc.mf.object.tree.Node;
import daris.client.Resource;
import daris.client.model.file.LocalDirectoryTree;
import daris.client.ui.DObjectGUIRegistry;
import daris.client.util.ByteUtil;

public class LocalFileBrowserPanel extends ContainerWidget {
    public static final String FILE_ICON = Resource.INSTANCE.file16()
            .getSafeUri().asString();
    public static final String DIRECTORY_ICON = Resource.INSTANCE.folderBlue16()
            .getSafeUri().asString();

    private boolean _showHiddenFiles;
    private SimplePanel _sp;
    private HorizontalSplitPanel _hsp;
    private SimplePanel _navSP;
    private TreeGUI _nav;
    private LocalFile _navSelectedDir;
    private ListGrid<LocalFile> _grid;
    private LocalFile _gridSelectedFile;
    private LocalFile _selectAfterOpen;

    public LocalFileBrowserPanel(boolean showHiddenFiles) {
        _showHiddenFiles = showHiddenFiles;
        _sp = new SimplePanel();
        _sp.fitToParent();
        if (!DTI.enabled()) {
            CenteringPanel cp = new CenteringPanel(Axis.BOTH);
            cp.add(new HTML(
                    "Arcitecta Desktop Integration (Java) applet is not enabled. It is required by this local file system browser."));
            _sp.setContent(cp);
        } else {
            _navSP = new SimplePanel();
            _navSP.setHeight100();
            _navSP.setPreferredWidth(0.4);
            updateTreeNav();
            _grid = new ListGrid<LocalFile>() {
                protected void postLoad(long start, long end, long total,
                        List<ListGridEntry<LocalFile>> entries) {
                    if (entries != null) {
                        _grid.select(0);
                    }
                }
            };
            _grid.setDataSource(new DataSource<ListGridEntry<LocalFile>>() {

                @Override
                public boolean isRemote() {
                    return true;
                }

                @Override
                public boolean supportCursor() {
                    return false;
                }

                @Override
                public void load(Filter f, final long start, final long end,
                        final DataLoadHandler<ListGridEntry<LocalFile>> lh) {
                    if (_navSelectedDir != null) {
                        _navSelectedDir.files(LocalFile.Filter.ANY, start, end,
                                new FileHandler() {
                            @Override
                            public void process(long start, long end,
                                    long total, List<LocalFile> files) {
                                if (files != null) {
                                    List<ListGridEntry<LocalFile>> entries = new ArrayList<ListGridEntry<LocalFile>>();
                                    for (LocalFile f : files) {
                                        if (_showHiddenFiles
                                                || f.name().indexOf('.') != 0) {
                                            ListGridEntry<LocalFile> e = new ListGridEntry<LocalFile>(
                                                    f);
                                            e.set("name", f.name());
                                            e.set("size", f.length());
                                            e.set("sizeHR", f.isDirectory()
                                                    ? null
                                                    : ByteUtil
                                                            .humanReadableByteCount(
                                                                    f.length(),
                                                                    true));
                                            e.set("lastModified",
                                                    DateTime.SERVER_DATE_TIME_FORMAT
                                                            .format(new Date(f
                                                                    .lastModified())));
                                            entries.add(e);
                                        }
                                    }
                                    if (!entries.isEmpty()) {
                                        lh.loaded(start, end, entries.size(),
                                                entries,
                                                DataLoadAction.REPLACE);
                                        return;
                                    }
                                }
                                lh.loaded(0, 0, 0, null,
                                        DataLoadAction.REPLACE);
                            }
                        });
                    } else {
                        lh.loaded(0, 0, 0, null, DataLoadAction.REPLACE);
                    }
                }
            });
            _grid.setObjectRegistry(DObjectGUIRegistry.get());
            _grid.fitToParent();
            _grid.setMultiSelect(false);
            _grid.enableRowDrag();
            _grid.setEmptyMessage(null);
            _grid.addColumnDefn("name", "Name", "File Name",
                    new WidgetFormatter<LocalFile, String>() {

                        @Override
                        public BaseWidget format(LocalFile f, String name) {
                            HTML html = new HTML();
                            String icon = f.isDirectory() ? DIRECTORY_ICON
                                    : FILE_ICON;
                            html.setHTML("<div><img src=\"" + icon
                                    + "\" style=\"width:16px;height:16px;vertical-align:middle\"><span style=\"\">&nbsp;"
                                    + name + "</span></div>");
                            return html;
                        }
                    }).setWidth(200);
            _grid.addColumnDefn("sizeHR", "Size").setWidth(120);
            _grid.addColumnDefn("lastModified", "Date Modified").setWidth(200);
            _grid.fitColumnsToWidth();
            _grid.setSelectionHandler(new SelectionHandler<LocalFile>() {

                @Override
                public void selected(LocalFile o) {
                    _gridSelectedFile = o;
                    gridRowSelected(o);
                }

                @Override
                public void deselected(LocalFile o) {
                    gridRowDeselected(o);
                }
            });
            _grid.setRowDoubleClickHandler(
                    new ListGridRowDoubleClickHandler<LocalFile>() {

                        @Override
                        public void doubleClicked(LocalFile o,
                                DoubleClickEvent event) {
                            if (_nav.isOpen(_navSelectedDir)) {
                                _nav.select(o);
                            } else {
                                _selectAfterOpen = o;
                                _nav.open(_navSelectedDir);
                            }
                        }
                    });

            _hsp = new HorizontalSplitPanel();
            _hsp.fitToParent();
            _hsp.add(_navSP);
            _hsp.add(_grid);
            _sp.setContent(_hsp);
        }
        initWidget(_sp);
    }

    private void updateTreeNav() {
        _nav = new TreeGUI(new LocalDirectoryTree(_showHiddenFiles),
                ScrollPolicy.AUTO);
        _nav.setShowRoot(false);
        _nav.setContextMenuEnabled(true);
        _nav.setObjectRegistry(DObjectGUIRegistry.get());
        _nav.fitToParent();
        _nav.setEventHandler(new TreeGUIEventHandler() {

            @Override
            public void clicked(Node n) {

            }

            @Override
            public void selected(Node n) {
                _navSelectedDir = (LocalFile) (n.object());
                _grid.refresh();
                navNodeSelected(_navSelectedDir);
            }

            @Override
            public void deselected(Node n) {
                navNodeDeselected((LocalFile) (n.object()));
            }

            @Override
            public void opened(Node n) {
                if (_selectAfterOpen != null) {
                    _nav.select(_selectAfterOpen);
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
        _nav.enableNodeDrag();
        _navSP.setContent(_nav);
    }

    protected LocalFile navSelection() {
        return _navSelectedDir;
    }

    protected LocalFile gridSelection() {
        return _gridSelectedFile;
    }

    protected void gridRowSelected(LocalFile f) {

    }

    protected void gridRowDeselected(LocalFile f) {

    }

    protected void navNodeSelected(LocalFile d) {

    }

    protected void navNodeDeselected(LocalFile d) {

    }

    public void setShowHiddenFiles(boolean showHiddenFiles) {
        if (showHiddenFiles == _showHiddenFiles) {
            return;
        }
        _showHiddenFiles = showHiddenFiles;
        _navSelectedDir = null;
        updateTreeNav();
        _grid.refresh();
    }
}
