package daris.client.ui.query.action;

import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.colour.RGBA;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.menu.MenuButton;
import arc.gui.gwt.widget.menu.MenuToolBar;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Entry;
import arc.gui.menu.Menu;
import arc.gui.window.WindowProperties;
import arc.mf.client.util.Action;
import arc.mf.client.util.ActionListener;
import arc.mf.client.util.AsynchronousAction;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.StateChangeListener;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectRef;
import arc.mf.object.ObjectResolveHandler;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.model.object.DObjectRef;
import daris.client.model.query.Query;
import daris.client.model.query.ResultCollectionRef;
import daris.client.model.query.messages.QueryCount;
import daris.client.model.query.options.ObjectQueryOptions;
import daris.client.model.query.options.QueryOptions;
import daris.client.ui.query.SavedQueryBrowser;
import daris.client.ui.query.filter.form.CompositeFilterForm;
import daris.client.ui.query.filter.form.ObjectCompositeFilterForm;
import daris.client.ui.query.filter.form.ObjectCompositeFilterForm.ProjectChangeListener;
import daris.client.ui.query.options.QueryOptionsForm;
import daris.client.ui.query.result.ResultBrowser;

public class SearchForm extends ValidatedInterfaceComponent implements AsynchronousAction {

    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;

    public static final arc.gui.image.Image ICON_LOAD = new arc.gui.image.Image(Resource.INSTANCE.folderBlueOpen16()
            .getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_SAVE = new arc.gui.image.Image(Resource.INSTANCE.save16().getSafeUri()
            .asString(), 16, 16);
    public static final arc.gui.image.Image ICON_SEARCH = new arc.gui.image.Image(Resource.INSTANCE.search16()
            .getSafeUri().asString(), 16, 16);

    public static final arc.gui.image.Image ICON_DOWNLOAD = new arc.gui.image.Image(Resource.INSTANCE.download16()
            .getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_SC = new arc.gui.image.Image(Resource.INSTANCE.shoppingcartGreen16()
            .getSafeUri().asString(), 16, 16);

    public static final arc.gui.image.Image ICON_EXPORT = new arc.gui.image.Image(Resource.INSTANCE.export16()
            .getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_CSV = new arc.gui.image.Image(Resource.INSTANCE.csv16().getSafeUri()
            .asString(), 16, 16);
    public static final arc.gui.image.Image ICON_XML = new arc.gui.image.Image(Resource.INSTANCE.xml16().getSafeUri()
            .asString(), 16, 16);

    public static final arc.gui.image.Image ICON_DICOM_SEND = new arc.gui.image.Image(Resource.INSTANCE.send16()
            .getSafeUri().asString(), 16, 16);

    public static final arc.gui.image.Image ICON_VIEW = new arc.gui.image.Image(Resource.INSTANCE.view16().getSafeUri()
            .asString(), 16, 16);
    public static final arc.gui.image.Image ICON_SHOW = new arc.gui.image.Image(Resource.INSTANCE.hideLeft16()
            .getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_HIDE = new arc.gui.image.Image(Resource.INSTANCE.hideRight16()
            .getSafeUri().asString(), 16, 16);

    private Query _query;

    private VerticalPanel _vp;

    private SimplePanel _menuBarSP;

    private TabPanel _tp;
    private int _activeTabId = 0;

    private int _filterTabId = 0;
    private SimplePanel _filterSP;
    private CompositeFilterForm _filterForm;

    private int _optTabId = 0;
    private SimplePanel _optSP;
    private QueryOptionsForm _optForm;

    private Button _loadQueryButton;
    private Button _saveQueryButton;

    private int _resultTabId = 0;
    private SimplePanel _resultSP;
    private Button _resultDetailButton;
    private ResultBrowser<?> _resultBrowser;

    private Button _cancelButton;
    private Button _searchButton;

    public SearchForm(Query query) {

        _query = query;

        assert _query != null;

        // _rc = ResultCollectionRef.create(_filter, _opts);

        _vp = new VerticalPanel();
        _vp.fitToParent();

        _menuBarSP = new SimplePanel();
        _menuBarSP.setHeight(28);
        _menuBarSP.setWidth100();
        _vp.add(_menuBarSP);

        _tp = new TabPanel() {
            protected void activated(int id) {
                _activeTabId = id;
                updateMenuBar();
            }
        };
        _tp.fitToParent();
        _vp.add(_tp);

        updateFilterTab();
        _tp.setActiveTabById(_filterTabId);

        updateOptionTab();

        addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                boolean v = _query.filter() == null ? false : _query.filter().valid().valid();
                _saveQueryButton.setEnabled(v);
                _searchButton.setEnabled(v);
                _resultBrowser = null;
                updateResultTab(null);
            }
        });
    }

    private void updateMenuBar() {
        _menuBarSP.clear();

        MenuToolBar mb = new MenuToolBar();
        mb.fitToParent();
        if (_activeTabId == _filterTabId || _activeTabId == _optTabId) {
            // load button
            if (_loadQueryButton == null) {
               
                _loadQueryButton = new Button(new arc.gui.gwt.widget.image.Image(ICON_LOAD), "Load", false);
                _loadQueryButton.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        SavedQueryBrowser.get().show(window(), new ObjectResolveHandler<Query>(){

                            @Override
                            public void resolved(Query query) {
                                setQuery(query);
                            }});
//                        new QueryLoadAction(new ObjectResolveHandler<Query>() {
//                            @Override
//                            public void resolved(Query query) {
//                                setQuery(query);
//                            }
//                        }, window()).execute();
                    }
                });
            }
            mb.add(_loadQueryButton);

            // save button
            if (_saveQueryButton == null) {
                _saveQueryButton = new Button(new arc.gui.gwt.widget.image.Image(ICON_SAVE), "Save", false);
                _saveQueryButton.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        new QuerySaveAction(_query, window()).execute();
                    }
                });
            }
            mb.add(_saveQueryButton);
        } else if (_activeTabId == _resultTabId) {

            Menu downloadMenu = new Menu("Download") {
                public void preShow() {
                    ResultCollectionRef<?> rc = _resultBrowser.collection();
                    List<Entry> entries = entries();
                    if (entries != null) {
                        for (Entry e : entries) {
                            if (e instanceof ActionEntry) {
                                // enable/disable the menu entries based on
                                // whether there is any result.
                                ((ActionEntry) e).setEnabled(rc.totalNumberOfMembers() > 0);
                            }
                        }
                    }
                }
            };
            MenuButton downloadMenuButton = new MenuButton("Download", downloadMenu);
            downloadMenuButton.setEnabled(_resultBrowser != null);
            mb.add(downloadMenuButton);

            downloadMenu.add(new ActionEntry(ICON_SC, "Add the selected result in current page to shopping-cart",
                    new Action() {

                        @Override
                        public void execute() {
                            _resultBrowser.downloadSelected();
                        }
                    }));

            downloadMenu.add(new ActionEntry(ICON_SC, "Add results in current page to shopping-cart", new Action() {

                @Override
                public void execute() {
                    _resultBrowser.downloadCurrentPage();
                }
            }));

            downloadMenu.add(new ActionEntry(ICON_SC, "Add all results to to shopping-cart", new Action() {

                @Override
                public void execute() {
                    _resultBrowser.downloadAll();
                }
            }));

            Menu exportMenu = new Menu("Export") {
                public void preShow() {
                    ResultCollectionRef<?> rc = _resultBrowser.collection();
                    List<Entry> entries = entries();
                    if (entries != null) {
                        for (Entry e : entries) {
                            if (e instanceof ActionEntry) {
                                // enable/disable the menu entries based on
                                // whether there is any result.
                                ((ActionEntry) e).setEnabled(rc.totalNumberOfMembers() > 0);
                            }
                        }
                    }
                }
            };
            MenuButton exportMenuButton = new MenuButton("Export", exportMenu);
            exportMenuButton.setEnabled(_resultBrowser != null);
            mb.add(exportMenuButton);

            exportMenu.add(new ActionEntry(ICON_CSV, "Export results in current page to .csv", new Action() {

                @Override
                public void execute() {
                    _resultBrowser.exportCurrentPageToCSV();
                }
            }));

            exportMenu.add(new ActionEntry(ICON_CSV, "Export all results to .csv", new Action() {
                @Override
                public void execute() {
                    _resultBrowser.exportAllToCSV();
                }
            }));

            exportMenu.add(new ActionEntry(ICON_XML, "Export results in current page to .xml", new Action() {

                @Override
                public void execute() {
                    _resultBrowser.exportCurrentPageToXML();
                }
            }));

            exportMenu.add(new ActionEntry(ICON_XML, "Export all results to .xml", new Action() {

                @Override
                public void execute() {
                    _resultBrowser.exportAllToXML();
                }
            }));

            exportMenu.add(new ActionEntry(ICON_DICOM_SEND, "Send all the contained DICOM datasets", new Action() {

                @Override
                public void execute() {
                    _resultBrowser.dicomSendAll();
                }
            }));

            arc.gui.gwt.widget.image.Image i = new arc.gui.gwt.widget.image.Image(ICON_SHOW);
            i.setDisabledImage(ICON_HIDE);
            _resultDetailButton = new Button(i, "Show Detail", false);
            _resultDetailButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    _resultBrowser.setShowDetailedView(!_resultBrowser.showDetailedView());
                    _resultDetailButton.setText((_resultBrowser.showDetailedView() ? "Hide" : "Show") + " Detail");
                }
            });
            _resultDetailButton.setEnabled(_resultBrowser != null);
            mb.add(_resultDetailButton);
        }
        _menuBarSP.setContent(mb);
    }

    private void updateFilterTab() {
        if (_filterForm != null) {
            removeMustBeValid(_filterForm);
        }
        if (_filterTabId <= 0) {
            _filterSP = new SimplePanel();
            _filterSP.fitToParent();
            _filterSP.setBorderTop(1, BorderStyle.SOLID, new RGB(0x88, 0x88, 0x88));
            _filterTabId = _tp.addTab("Filters", "filters", _filterSP);
        }
        _filterForm = CompositeFilterForm.create(_query.filter(), true);
        _filterForm.addChangeListener(new StateChangeListener() {

            @Override
            public void notifyOfChangeInState() {
                if (_filterForm.valid().valid()) {
                    _query.setFilter(_filterForm.filter());
                }
            }
        });
        if (_filterForm instanceof ObjectCompositeFilterForm && _query.options() instanceof ObjectQueryOptions) {
            ((ObjectCompositeFilterForm) _filterForm).addProjectChangeListener(new ProjectChangeListener() {

                @Override
                public void projectChanged(DObjectRef project) {
                    ((ObjectQueryOptions) _query.options()).setProject(project);
                    if (_optForm != null) {
                        _optForm.refresh();
                    }
                }
            });
        }
        addMustBeValid(_filterForm);
        _filterSP.setContent(_filterForm.gui());
    }

    private void updateOptionTab() {
        if (_optForm != null) {
            removeMustBeValid(_optForm);
        }
        if (_optTabId <= 0) {
            _optSP = new SimplePanel();
            _optSP.fitToParent();
            _optSP.setBorderTop(1, BorderStyle.SOLID, new RGB(0x88, 0x88, 0x88));
            _optTabId = _tp.addTab("Options", "query options", _optSP);
        }
        _optForm = new QueryOptionsForm(_query.options());
        addMustBeValid(_optForm);
        _optSP.setContent(_optForm.gui());
    }

    private void updateResultTab(BaseWidget cw) {
        if (_resultTabId <= 0) {
            _resultSP = new SimplePanel();
            _resultSP.fitToParent();
            _resultSP.setBorderTop(1, BorderStyle.SOLID, new RGB(0x88, 0x88, 0x88));
            _resultTabId = _tp.addTab("Results", null, _resultSP);
        }
        if (cw == null) {
            boolean v = valid().valid();
            HTML msg = new HTML(
                    v ? "No result yet. Click \"Search\" button to get the results."
                            : "No result yet. The query filter is not valid or incomplete. Activate \"Filters\" tab to complete the filters.");
            msg.setFontSize(12);
            msg.setColour(RGBA.RED);
            msg.setMargin(20);
            _resultSP.setContent(msg);
        } else {
            _resultSP.setContent(cw);
        }
        updateMenuBar();
    }

    protected void activateOptionTab() {
        if (_optTabId > 0) {
            _tp.setActiveTabById(_optTabId);
        }
    }

    protected void activateFilterTab() {
        if (_filterTabId > 0) {
            _tp.setActiveTabById(_filterTabId);
        }
    }

    protected void activateResultTab() {
        if (_resultTabId > 0) {
            _tp.setActiveTabById(_resultTabId);
        }
    }

    private void setQuery(Query query) {
        if (ObjectUtil.equals(query, _query)) {
            return;
        }
        _query = query;
        updateFilterTab();
        updateOptionTab();
        updateResultTab(null);
    }

    @Override
    public Widget gui() {
        return _vp;
    }

    public BaseWidget widget() {
        return _vp;
    }

    public arc.gui.window.Window window() {
        return _vp.window();
    }

    @Override
    public void execute(final ActionListener al) {
        if (_query.options().action() == QueryOptions.Action.count) {
            new QueryCount(_query.filter().toString()).send(new ObjectMessageResponse<Long>() {

                @Override
                public void responded(Long n) {
                    if (n == null) {
                        n = 0L;
                    }
                    HTML msg = new HTML(Long.toString(n) + " " + _query.options().entity() + "s found.");
                    msg.setFontSize(12);
                    msg.setColour(RGBA.BLACK);
                    msg.setMargin(20);
                    updateResultTab(msg);
                    activateResultTab();
                    if (al != null) {
                        al.executed(true);
                    }
                }
            });
        } else {
            final ResultCollectionRef<ObjectRef<?>> _rc = ResultCollectionRef.create(_query);
            _rc.resolve(new ActionListener() {
                @Override
                public void executed(boolean succeeded) {
                    if (succeeded) {
                        _resultBrowser = new ResultBrowser<ObjectRef<?>>(_rc, false);
                        updateResultTab(_resultBrowser);
                    } else {
                        HTML msg = new HTML("No " + _query.options().entity() + "s found.");
                        msg.setFontSize(12);
                        msg.setColour(RGBA.BLACK);
                        msg.setMargin(20);
                        updateResultTab(msg);
                    }
                    activateResultTab();
                    if (al != null) {
                        al.executed(succeeded);
                    }
                }
            });
        }
    }

    public void show(arc.gui.window.Window owner, String title) {

        WindowProperties wp = new WindowProperties();
        wp.setCanBeClosed(true);
        wp.setCanBeMoved(true);
        wp.setCanBeMaximised(true);
        wp.setCanBeResized(true);
        wp.setOwnerWindow(owner);
        wp.setTitle(title);
        wp.setSize(0.9, 0.9);
        wp.setModal(false);

        final arc.gui.gwt.widget.window.Window win = arc.gui.gwt.widget.window.Window.create(wp);

        VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();
        vp.add(gui());

        ButtonBar bb = new ButtonBar(Position.BOTTOM, Alignment.CENTER);
        _cancelButton = new Button("Cancel");
        _cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                win.close();
            }
        });
        bb.add(_cancelButton);

        _searchButton = new Button("Search");
        _searchButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                _searchButton.disable();
                execute(new ActionListener() {

                    @Override
                    public void executed(boolean succeeded) {
                        _searchButton.setEnabled(valid().valid());
                    }
                });
            }
        });
        _searchButton.setEnabled(valid().valid());
        bb.add(_searchButton);

        vp.add(bb);

        win.setContent(vp);
        win.centerInPage();
        win.show();

    }
}
