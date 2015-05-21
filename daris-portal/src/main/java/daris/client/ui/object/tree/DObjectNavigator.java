package daris.client.ui.object.tree;

import arc.gui.gwt.object.ObjectDetailedView;
import arc.gui.gwt.object.ObjectEventHandler;
import arc.gui.gwt.object.ObjectNavigator;
import arc.gui.gwt.object.ObjectNavigator.DisplayOn;
import arc.gui.gwt.object.ObjectNavigatorSelectionHandler;
import arc.gui.gwt.object.ObjectReadOnlyPolicy;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.panel.SimplePanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUIEventHandler;
import arc.gui.object.register.ObjectMenuFactory;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.Tree;
import daris.client.Resource;
import daris.client.model.object.tree.DObjectTree;
import daris.client.model.repository.RepositoryRef;

public class DObjectNavigator extends ContainerWidget {

    public static final arc.gui.image.Image RELOAD_ICON_GREEN = new arc.gui.image.Image(Resource.INSTANCE
            .refreshGreen16().getSafeUri().asString(), 16, 16);

    public static final arc.gui.image.Image RELOAD_ICON_BLUE = new arc.gui.image.Image(Resource.INSTANCE
            .refreshBlue16().getSafeUri().asString(), 16, 16);

    private DObjectTree _tree;
    private SimplePanel _tp;
    private DObjectTreeGUI _tg;
    private ObjectDetailedView _dv;
    private ObjectNavigator.DisplayOn _displayOn;

    private TreeGUIEventHandler _eh;
    private ScrollPolicy _sp;

    public DObjectNavigator(DObjectTree tree, final ObjectNavigatorSelectionHandler osh, final ObjectEventHandler oeh) {
        this(tree, ScrollPolicy.BOTH, osh, oeh);
    }

    public DObjectNavigator(DObjectTree tree, ScrollPolicy sp, final ObjectNavigatorSelectionHandler osh,
            final ObjectEventHandler oeh) {
        _tree = tree;
        _displayOn = DisplayOn.SELECT;
        _sp = sp;
        _eh = new TreeGUIEventHandler() {

            public void clicked(Node n) {
                Object o = n.object();
                boolean readOnly = _tree.readOnly() ? true : n.readOnly();

                if (_dv != null) {
                    if (_displayOn.displayIfClick()) {
                        _dv.loadAndDisplayObject(o);
                    }
                }

                if (osh != null) {
                    osh.clickedObject(n, o, readOnly);
                }

            }

            public void closed(Node n) {
                DObjectNavigator.this.closedObject(n, n.object());
            }

            public void deselected(Node n) {
                if (osh != null) {
                    osh.deselectedObject(n, n.object());
                }
            }

            public void opened(Node n) {
                DObjectNavigator.this.openedObject(n, n.object());
            }

            public void selected(Node n) {

                Object o = n.object();

                if (_displayOn.displayIfSelect()) {
                    if (_dv != null) {
                        _dv.clear();
                        _dv.loadAndDisplayObject(o);
                    }
                }

                if (osh != null) {
                    osh.selectedObject(n, n.object(), _tree.readOnly() ? true : n.readOnly());
                }

            }

            public void changeInMembers(Node n) {
                if (oeh != null) {
                    oeh.changeInMembers(n.object());
                }
            }

            public void added(Node n) {
                if (oeh != null) {
                    oeh.added(n.object());
                }
            }

            public void removed(Node n) {
                if (_dv != null) {
                    if (_dv.displaying(n.object())) {
                        _dv.clear(n.object());
                    }
                }

                if (oeh != null) {
                    oeh.removed(n.object());
                }
            }

        };

        // _tg = new HorizontalPagingTreeGUI(tree,sp,eh);
        _tg = new DObjectTreeGUI(tree, _sp, _eh);

        _tg.enableNodeDrag();
        _tg.enableDropTarget();

        _tg.setWidth100();
        _tg.setHeight100();

        _dv = null;

        _tp = new SimplePanel();
        _tp.fitToParent();

        _tp.setContent(_tg);

        initWidget(_tp);

    }

    public void reloadAll() {
        _tg.discard();
        RepositoryRef.INSTANCE.reset();
        RepositoryRef.INSTANCE.childrenRef().reset();
        _tg = new DObjectTreeGUI(_tree, _sp, _eh);
        _tg.enableNodeDrag();
        _tg.enableDropTarget();
        _tg.setWidth100();
        _tg.setHeight100();
        _tp.setContent(_tg);
        if (_dv != null) {
            select(_tree.root().object());
        }
    }

    public void reloadSelected() {
        _tg.refreshSelectedNode();
    }

    public Tree tree() {
        return _tree;
    }

    public boolean readOnly() {
        return _tg.readOnly();
    }

    /**
     * Indicates whether or not all objects in the navigator are read-only. If not read-only, then the node will
     * determine whether or not it is read-only. By default read only is set to false.
     * 
     * @param readOnly
     */
    public void setReadOnly(boolean readOnly) {
        _tg.setReadOnly(readOnly);
    }

    /**
     * Some objects in the navigator may be read-only depending on their type or other factors.
     * 
     * @param rop
     */
    public void setReadOnlyPolicy(final ObjectReadOnlyPolicy rop) {
        if (rop == null) {
            _tg.setReadOnlyPolicy(null);
        } else {
            _tg.setReadOnlyPolicy(rop);
        }
    }

    /**
     * Is the context menu enabled?
     * 
     * @param enabled
     */
    public void setContextMenuEnabled(boolean enabled) {
        _tg.setContextMenuEnabled(enabled);
    }

    /**
     * Controls whether or not tool tips are shown for nodes in the tree. By default, they are not shown.
     * 
     * @param show
     */
    public void setShowToolTip(boolean show) {
        _tg.setShowToolTip(show);
    }

    /**
     * Sets the delay before showing subsequent tool tips.
     * 
     * @param delay
     *            Delay in milli-seconds.
     */
    public void setToolTipDelay(int delay) {
        _tg.setToolTipDelay(delay);
    }

    /**
     * Controls the behaviour of object display for nodes in the object tree. If not set, defaults to DisplayOn.SELECT.
     * 
     * @param displayOn
     */
    public void setDisplayObjectOn(DisplayOn displayOn) {
        _displayOn = displayOn;
    }

    /**
     * Sets a linked detailed view with this navigator. If set, the detailed view is kept up to date with the selected
     * object(s) in the tree.
     * 
     * @param dv
     */
    public void setObjectDetailView(ObjectDetailedView dv) {
        _dv = dv;
    }

    /**
     * Sets a factory that will generate a menu (possibly a compound menu) for a given object. If not specified, then
     * the default menu factory will be used to generate an object based menu.
     * 
     * @param mf
     */
    public void setMenuFactory(ObjectMenuFactory mf) {
        _tg.setMenuFactory(mf);
    }

    /**
     * Control whether automatic discard on detach of the navigator. By default the navigator will be discarded on
     * detach. If the navigator is being added/removed from some display but must remain active, then set to false.
     * 
     */
    public void setDiscardOnDetach(boolean discard) {
        _tg.setDiscardOnDetach(discard);
    }

    /**
     * Must be called with the navigator is no longer in use. This will allow the navigator to discard any
     * listeners/subscribers for tree changes. If automatic discard, then no need to call this.
     * 
     */
    public void discard() {
        _tg.discard();
    }

    /**
     * Indicates whether or not the root of the tree should be shown or not.
     * 
     * @param show
     *            true to show and false to hide.
     */
    public void setShowRoot(boolean show) {
        _tg.setShowRoot(show);
    }

    /**
     * Open this node.
     * 
     * @param n
     */
    public void open(Object o) {
        _tg.open(o);
    }

    public void close(Object o) {
        _tg.close(o);
    }

    /**
     * Select this node, and not other. If the parents are not open, then the parent will be opened.
     * 
     * @param n
     */
    public void select(Object o) {
        _tg.select(o);
    }

    /**
     * Add this node to the selections.
     * 
     * @param n
     */
    public void multiSelect(Object o) {
        _tg.multiSelect(o);
    }

    public void deselect(Object o) {
        _tg.deselect(o);
    }

    public void openedObject(Node n, Object o) {

    }

    public void closedObject(Node n, Object o) {

    }

}