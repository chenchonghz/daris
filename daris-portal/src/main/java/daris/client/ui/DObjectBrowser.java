package daris.client.ui;

import java.util.List;

import arc.gui.gwt.object.ObjectDetailedView;
import arc.gui.gwt.object.ObjectEventHandler;
import arc.gui.gwt.object.ObjectNavigator;
import arc.gui.gwt.object.ObjectNavigatorSelectionHandler;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.menu.MenuButton;
import arc.gui.gwt.widget.menu.MenuToolBar;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.mf.client.dti.DTI;
import arc.mf.client.dti.DTIReadyListener;
import arc.mf.client.plugin.Plugin;
import arc.mf.client.util.Action;
import arc.mf.client.util.DynamicBoolean;
import arc.mf.client.util.ObjectUtil;
import arc.mf.object.CollectionResolveHandler;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.NodeListener;
import arc.mf.session.Session;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import daris.client.BrowserCheck;
import daris.client.Resource;
import daris.client.mf.pkg.PackageExists;
import daris.client.model.IDUtil;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.tree.DObjectTree;
import daris.client.model.object.tree.DObjectTreeNode;
import daris.client.model.query.Query;
import daris.client.model.query.filter.pssd.ObjectQuery;
import daris.client.model.repository.RepositoryRef;
import daris.client.ui.announcement.AnnouncementNavigator;
import daris.client.ui.dicom.DicomAEManager;
import daris.client.ui.dti.file.LocalFileBrowser;
import daris.client.ui.object.DObjectGUI;
import daris.client.ui.object.tree.DObjectNavigator;
import daris.client.ui.query.SavedQueryBrowser;
import daris.client.ui.query.action.SearchForm;
import daris.client.ui.sc.DTIDownloadOptionsDialog;
import daris.client.ui.sc.ShoppingCartDialog;
import daris.client.ui.secure.wallet.SecureWalletExplorer;
import daris.client.ui.transform.TransformBrowser;
import daris.client.ui.user.SelfPasswordSetDialog;
import daris.client.ui.util.ButtonUtil;

public class DObjectBrowser extends ContainerWidget implements DTIReadyListener {

    public static final arc.gui.image.Image ICON_RELOAD = new arc.gui.image.Image(Resource.INSTANCE
            .reload24().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_CREATE = new arc.gui.image.Image(Resource.INSTANCE
            .create24().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_EDIT = new arc.gui.image.Image(Resource.INSTANCE
            .edit24().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_SHOPPINGCART = new arc.gui.image.Image(
            Resource.INSTANCE.shoppingcart24().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_USER = new arc.gui.image.Image(Resource.INSTANCE
            .user24().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_SECURE_WALLET = new arc.gui.image.Image(
            Resource.INSTANCE.keychain16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_LOGOUT = new arc.gui.image.Image(Resource.INSTANCE
            .logout24().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_IMPORT = new arc.gui.image.Image(Resource.INSTANCE
            .import24().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_FILE_EXPLORER = new arc.gui.image.Image(
            Resource.INSTANCE.fileExplorer16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_ACTION = new arc.gui.image.Image(Resource.INSTANCE
            .action16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_PASSWORD = new arc.gui.image.Image(
            Resource.INSTANCE.key16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_ACTIVE = new arc.gui.image.Image(Resource.INSTANCE
            .active24().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_MANAGER = new arc.gui.image.Image(
            Resource.INSTANCE.manager16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_DICOM = new arc.gui.image.Image(Resource.INSTANCE
            .dicom24().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_SETTINGS = new arc.gui.image.Image(
            Resource.INSTANCE.settings16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_DTI_ENABLED = new arc.gui.image.Image(
            Resource.INSTANCE.connect16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_DTI_DISABLED = new arc.gui.image.Image(
            Resource.INSTANCE.disconnect16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_ABOUT = new arc.gui.image.Image(Resource.INSTANCE
            .about16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_TRANSFORM = new arc.gui.image.Image(
            Resource.INSTANCE.transform16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_HELP = new arc.gui.image.Image(Resource.INSTANCE
            .help16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_PREFERENCES = new arc.gui.image.Image(
            Resource.INSTANCE.preferences16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_ANNOUNCEMENT = new arc.gui.image.Image(
            Resource.INSTANCE.announcement16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_SEARCH = new arc.gui.image.Image(Resource.INSTANCE
            .search16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_DTI_OPTIONS = new arc.gui.image.Image(
            Resource.INSTANCE.folderOptions16().getSafeUri().asString(), 16, 16);
    public static final arc.gui.image.Image ICON_LOAD = new arc.gui.image.Image(Resource.INSTANCE
            .folderBlueOpen16().getSafeUri().asString(), 16, 16);
    private static DObjectBrowser _instance;

    public static DObjectBrowser get(boolean reset) {
        if (reset) {
            reset();
        }
        if (_instance == null) {
            _instance = new DObjectBrowser();
        }
        return _instance;
    }

    public static void reset() {
        if (_instance != null) {
            _instance.discard();
            _instance = null;
        }
    }

    private VerticalPanel _vp;
    private MenuToolBar _tb;
    private MenuButton _actionMenuButton;
    private MenuButton _searchMenuButton;

    private Image _dtiStatusIcon;
    private HTML _dtiStatus;

    private SimplePanel _ovsp;
    private DObjectNavigator _ov;
    private ObjectDetailedView _dv;

    private NodeListener _nl;

    private DObjectRef _selectedObject;

    private DObjectRef _prevSelectedObject;

    private DObjectBrowser() {

        _vp = new VerticalPanel();
        _vp.fitToParent();

        AbsolutePanel ap = new AbsolutePanel();
        ap.setHeight(28);
        ap.setWidth100();

        _tb = new MenuToolBar();
        _tb.fitToParent();
        _tb.setPosition(Position.ABSOLUTE);
        _tb.setLeft(0);
        ap.add(_tb);

        Menu darisMenu = new Menu("DaRIS");
        darisMenu.add(new ActionEntry(ICON_ABOUT, "About DaRIS Portal", new Action() {

            @Override
            public void execute() {
                AboutDialog ad = new AboutDialog();
                ad.show(window());
            }
        }));
        darisMenu.add(new ActionEntry(ICON_ANNOUNCEMENT, "System Announcements...", new Action() {

            @Override
            public void execute() {
                AnnouncementNavigator.get().show(window(), 0.5, 0.5);
            }
        }));

        MenuButton darisMenuButton = ButtonUtil.createMenuButton(ICON_PREFERENCES,
                darisMenu.label(), darisMenu);
        _tb.add(darisMenuButton);

        Menu settingsMenu = new Menu("Settings");
        settingsMenu.add(new ActionEntry(ICON_DICOM, "DICOM Application Entities", new Action() {

            @Override
            public void execute() {
                DicomAEManager.get().show(window());
            }
        }));
        MenuButton settingsButton = ButtonUtil.createMenuButton(ICON_SETTINGS,
                settingsMenu.label(), settingsMenu);
        _tb.add(settingsButton);

        _actionMenuButton = ButtonUtil.createMenuButton(ICON_ACTION, "Action", new Menu("Action"));
        _actionMenuButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                updateActionMenu();
            }
        });
        _tb.add(_actionMenuButton);

        final ActionEntry dtiOptionsEntry = new ActionEntry(ICON_DTI_OPTIONS,
                "DTI download options...", new Action() {

                    @Override
                    public void execute() {
                        DTIDownloadOptionsDialog.show(window(), null);
                    }
                });

        Menu downloadMenu = new Menu("Download") {
            @Override
            public void preShow() {
                if (DTI.enabled()) {
                    dtiOptionsEntry.enable();
                } else {
                    dtiOptionsEntry.softDisable("DTI appet is not enabled.");
                }
            }
        };
        downloadMenu.add(new ActionEntry(ICON_ACTIVE, "Show active shopping cart...", new Action() {

            @Override
            public void execute() {
                // TODO: clean up
                // ActiveShoppingCartDialog.instance().show(window(), 0.6, 0.5);
                ShoppingCartDialog.get().showDialog(window(), true);
            }
        }));
        downloadMenu.add(new ActionEntry(ICON_MANAGER, "Show all shopping carts...", new Action() {

            @Override
            public void execute() {
                // TODO: clean up
                // ShoppingCartManagerDialog.instance().show(window(), 0.6,
                // 0.5);
                ShoppingCartDialog.get().showDialog(window(), false);
            }
        }));
        downloadMenu.add(dtiOptionsEntry);

        downloadMenu.setShowTitle(false);
        MenuButton downloadMenuButton = ButtonUtil.createMenuButton(ICON_SHOPPINGCART,
                downloadMenu.label(), downloadMenu);
        _tb.add(downloadMenuButton);

        Menu importMenu = new Menu("Import");
        importMenu.add(new ActionEntry(ICON_FILE_EXPLORER, "Show local files...",
                "Show local file browser", new Action() {

                    @Override
                    public void execute() {
                        LocalFileBrowser.get().show(window());
                    }
                }, true));
        importMenu.setShowTitle(false);

        MenuButton importMenuButton = ButtonUtil.createMenuButton(ICON_IMPORT, importMenu.label(),
                importMenu);
        _tb.add(importMenuButton);

        _searchMenuButton = ButtonUtil.createMenuButton(ICON_SEARCH, "Search", new Menu("Search"));
        _searchMenuButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                updateSearchMenu();
            }
        });
        _tb.add(_searchMenuButton);

        Menu transformMenu = new Menu("Transform");
        transformMenu.add(new ActionEntry(ICON_TRANSFORM, "Show transform monitor", new Action() {

            @Override
            public void execute() {
                // TransformMonitor.get().show(window(), 0.5, 0.5);
                TransformBrowser.get().show(window());
            }
        }));
        final MenuButton transformMenuButton = ButtonUtil.createMenuButton(ICON_TRANSFORM,
                transformMenu.label(), transformMenu);
        new PackageExists("Transform Framework").send(new ObjectMessageResponse<Boolean>() {

            @Override
            public void responded(Boolean r) {
                if (r != null && r) {
                    _tb.add(transformMenuButton);
                }
            }
        });

        if (Plugin.isStandaloneApplication()) {
            Menu userMenu = new Menu(Plugin.isPluginEnvironment() ? "User" : Session.domainName()
                    + ":" + Session.userName());
            userMenu.add(new ActionEntry(ICON_PASSWORD, "Change Password", new Action() {

                @Override
                public void execute() {
                    SelfPasswordSetDialog.show(window());
                }
            }));

            userMenu.add(new ActionEntry(ICON_SECURE_WALLET, "Secure Wallet", new Action() {

                @Override
                public void execute() {
                    SecureWalletExplorer.show(_vp.window());
                }
            }));

            userMenu.add(new ActionEntry(ICON_LOGOUT, "Log out", new Action() {

                @Override
                public void execute() {
                    Session.logoff(true);
                }
            }));
            MenuButton userMenuButton = ButtonUtil.createMenuButton(ICON_USER, userMenu.label(),
                    userMenu);
            _tb.add(userMenuButton);
        }

        if (Plugin.isStandaloneApplication()) {
            _dtiStatusIcon = new Image(ICON_DTI_DISABLED);
            _dtiStatusIcon.setDisabledImage(ICON_DTI_DISABLED);
            _dtiStatusIcon.setEnabledImage(ICON_DTI_ENABLED);
            _dtiStatusIcon.setEnabled(DTI.enabled());
            _dtiStatus = new HTML("DTI: " + (DTI.enabled() ? "active" : "inactive"));
            _dtiStatus.setFontSize(11);
            _dtiStatus.setMarginTop(3);
            _dtiStatus.setFontWeight(FontWeight.BOLD);
            _dtiStatus.setHTML("DTI: loading...");

            HorizontalPanel dtiStatusHP = new HorizontalPanel();
            dtiStatusHP.setPosition(Position.ABSOLUTE);
            dtiStatusHP.setRight(10);
            dtiStatusHP.add(_dtiStatusIcon);
            dtiStatusHP.setSpacing(2);
            dtiStatusHP.add(_dtiStatus);
            dtiStatusHP.setPaddingTop(4);
            ap.add(dtiStatusHP);

            DTI.addReadyListener(this);
        }

        _vp.add(ap);

        _dv = new ObjectDetailedView(ScrollPolicy.NONE);
        _dv.setDisplayLoadingMessage(true);
        _dv.setForEdit(false);
        _dv.setObjectRegistry(DObjectGUIRegistry.get());
        _dv.fitToParent();

        _nl = new NodeListener() {

            @Override
            public void added(Node pn, Node n, int idx) {

            }

            @Override
            public void removed(Node pn, Node n) {

            }

            @Override
            public void modified(Node n) {

                if (n != null) {
                    DObjectTreeNode dn = (DObjectTreeNode) n;
                    if (_dv.displaying(dn.object())) {
                        _dv.clear();
                        _dv.loadAndDisplayObject(dn.object());
                    }
                }
            }

            @Override
            public void changeInMembers(Node n) {

            }
        };

        _ovsp = new SimplePanel();
        _ovsp.setPreferredWidth(0.4);
        _ovsp.setHeight100();

        RepositoryRef.INSTANCE.reset();
        RepositoryRef.INSTANCE.childrenRef().reset();
        RepositoryRef.INSTANCE.resolve(new ObjectResolveHandler<DObject>() {

            @Override
            public void resolved(DObject o) {
                _ov = new DObjectNavigator(new DObjectTree(RepositoryRef.INSTANCE),
                        new ObjectNavigatorSelectionHandler() {

                            @Override
                            public void clickedObject(Node n, Object o, boolean readOnly) {

                                /*
                                 * [1] called when a (new) node is clicked while selected node has
                                 * not changed.
                                 */
                            }

                            @Override
                            public void selectedObject(Node n, Object o, boolean readOnly) {

                                // loadAndDisplayObject() right before this
                                // method is
                                // called.
                                /*
                                 * [3] called after deselectedObject()
                                 */
                                if (o != null) {
                                    objectSelected((DObjectRef) o);
                                }
                                if (n != null) {
                                    // start listening to the current selected
                                    // node.
                                    n.subscribe(DynamicBoolean.TRUE, _nl);
                                }
                            }

                            @Override
                            public void deselectedObject(Node n, Object o) {

                                /*
                                 * [2] called after clickedObject()
                                 */
                                // cancel the ObjectRef because another node is
                                // clicked
                                if (o != null) {
                                    ((DObjectRef) o).cancel();
                                    objectDeselected((DObjectRef) o);
                                }
                                if (n != null) {
                                    // stop listening to the deselected node.
                                    n.unsubscribe(_nl);
                                }
                            }
                        }, new ObjectEventHandler() {

                            @Override
                            public void added(Object o) {

                            }

                            @Override
                            public void modified(Object o) {

                            }

                            @Override
                            public void changeInMembers(Object o) {

                            }

                            @Override
                            public void removed(Object o) {
                                if (ObjectUtil.equals(o, _selectedObject)) {
                                    _selectedObject = null;
                                    _prevSelectedObject = null;
                                    updateActionMenu();
                                    updateSearchMenu();
                                }
                            }
                        }) {
                    @Override
                    public void openedObject(Node n, Object o) {
                        if (n != null && o != null) {
                            if (o instanceof DObjectRef && n instanceof DObjectTreeNode) {
                                objectOpened((DObjectTreeNode) n, (DObjectRef) o);
                            }
                        }
                    }

                    @Override
                    public void closedObject(Node n, Object o) {
                        if (n != null && o != null) {
                            if (o instanceof DObjectRef && n instanceof DObjectTreeNode) {
                                objectClosed((DObjectTreeNode) n, (DObjectRef) o);
                            }
                        }
                    }
                };

                _ov.setDisplayObjectOn(ObjectNavigator.DisplayOn.SELECT);
                _ov.fitToParent();
                _ov.setObjectDetailView(_dv);
                _ovsp.setContent(_ov);

                // load detail view
                _ov.select(RepositoryRef.INSTANCE);
            }
        });

        HorizontalSplitPanel hsp = new HorizontalSplitPanel();
        hsp.add(_ovsp);
        hsp.add(_dv);
        hsp.fitToParent();

        _vp.add(hsp);

        initWidget(_vp);
    }

    protected void objectSelected(DObjectRef o) {

        _selectedObject = o;
        updateActionMenu();
        updateSearchMenu();

    }

    protected void objectDeselected(DObjectRef o) {
        // record the object type in the cookie. to be used by the object
        // viewer.
        _prevSelectedObject = o;
        _selectedObject = null;
        updateActionMenu();
        updateSearchMenu();
    }

    public void open(DObjectRef o) {
        _ov.open(o);
    }

    protected void objectOpened(DObjectTreeNode n, DObjectRef o) {
        if (o.referentType() == DObject.Type.subject) {
            o.childrenRef().resolve(new CollectionResolveHandler<DObjectRef>() {

                @Override
                public void resolved(List<DObjectRef> cos) {
                    if (cos != null) {
                        if (!cos.isEmpty()) {
                            _ov.open(cos.get(0));
                        }
                    }
                }
            });
        }
    }

    protected void objectClosed(DObjectTreeNode n, DObjectRef o) {

    }

    private void updateActionMenu() {
        Menu menu = _selectedObject == null ? new Menu() : DObjectGUI.INSTANCE.actionMenu(window(),
                _selectedObject, null, false);
        _actionMenuButton.setMenu(menu);
    }

    private void updateSearchMenu() {
        // new ActorSelfHaveRole(Roles.SYSTEM_ADMINISTRATOR).send(new
        // ObjectMessageResponse<Boolean>() {
        //
        // @Override
        // public void responded(Boolean isAdmin) {
        Menu menu = new Menu("search");
        //
        // if (isAdmin) {
        // menu.add(new ActionEntry(ICON_SEARCH, "Admin search...",
        // "Search for assets as system-administrator.", new Action() {
        //
        // @Override
        // public void execute() {
        // new SearchForm(new
        // daris.client.model.query.AssetQuery()).show(window(),
        // "Search for assets...");
        // }
        // }));
        // }

        menu.add(new ActionEntry(ICON_SEARCH, "Search...", "Search for DaRIS/PSSD objects.",
                new Action() {

                    @Override
                    public void execute() {
                        DObjectRef project = null;
                        if (_selectedObject != null && !_selectedObject.isRepository()) {
                            if (_selectedObject.isProject()) {
                                project = _selectedObject;
                            } else {
                                project = new DObjectRef(IDUtil.getProjectId(_selectedObject.id()));
                            }
                        }
                        new SearchForm(new ObjectQuery(project)).show(window(),
                                "Search for DaRIS/PSSD objects...");
                    }
                }));

        menu.add(new ActionEntry(ICON_LOAD, "Saved queries...", "Manage saved queries.",
                new Action() {

                    @Override
                    public void execute() {
                        SavedQueryBrowser.get().show(window(), new ObjectResolveHandler<Query>() {

                            @Override
                            public void resolved(Query query) {
                                new SearchForm(query).show(window(), "Search for DaRIS objects...");
                            }
                        });
                    }
                }));
        _searchMenuButton.setMenu(menu);
        // }
        // });
    }

    public void discard() {
        if (Plugin.isStandaloneApplication()) {
            DTI.removeReadyListener(this);
        }
    }

    private void updateDTITooltip(boolean failed) {
        if (failed) {
            String tooltip = "<div style=\"font-size:9pt;\">Failed to load Arcitecta Desktop Integration (Java) applet. Data importing and a few other functions will not work. See <a href=\""
                    + BrowserCheck.INSTALL_DTI
                    + "\">"
                    + BrowserCheck.INSTALL_DTI
                    + "</a> for more information.</div>";
            _dtiStatus.setToolTip(tooltip);
            _dtiStatusIcon.setToolTip(tooltip);
        } else {
            _dtiStatus.removeToolTip();
            _dtiStatusIcon.removeToolTip();
        }
    }

    @Override
    public void failed(String reason) {
        _dtiStatusIcon.setEnabled(DTI.enabled());
        _dtiStatus.setHTML("DTI: loading failed");
        updateDTITooltip(true);
    }

    @Override
    public void activated() {
        _dtiStatusIcon.setEnabled(DTI.enabled());
        _dtiStatus.setHTML("DTI: active");
        updateDTITooltip(false);
    }

    @Override
    public void deactivated() {
        _dtiStatusIcon.setEnabled(DTI.enabled());
        _dtiStatus.setHTML("DTI: loading...");
        updateDTITooltip(false);
    }

    public void reloadSelected() {
        _ov.reloadSelected();
    }

    public void reloadAll() {
        _ov.reloadAll();
    }

    public DObjectRef prevSelected() {
        return _prevSelectedObject;
    }

}
