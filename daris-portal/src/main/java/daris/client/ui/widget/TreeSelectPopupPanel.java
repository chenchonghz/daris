package daris.client.ui.widget;

import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.colour.RGBA;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.button.ButtonBar.Alignment;
import arc.gui.gwt.widget.button.ButtonBar.Position;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.gwt.widget.panel.ResizeHandle;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.popup.PopupCloseHandler;
import arc.gui.gwt.widget.popup.PopupPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUI;
import arc.gui.gwt.widget.tree.TreeGUIEventHandler;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.Tree;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;

import daris.client.ui.util.ButtonUtil;

public abstract class TreeSelectPopupPanel<T> {
    public static final int MIN_WIDTH = 200;
    public static final int MIN_HEIGHT = 320;

    public static final int DEFAULT_WIDTH = 320;
    public static final int DEFAULT_HEIGHT = 480;

    public static interface SelectionHandler<T> {
        void selected(T selected);
    }

    private SelectionHandler<T> _sh;
    private BaseWidget _partner;
    private Tree _tree;

    private PopupPanel _pp;
    private AbsolutePanel _ap;
    private VerticalPanel _vp;
    private TreeGUI _treeGUI;
    private ButtonBar _bb;
    private Button _selectButton;
    private Button _cancelButton;
    private Node _selected;
    private boolean _selectionConfirmed;
    private boolean _showRoot;

    public TreeSelectPopupPanel(BaseWidget partner, Tree tree, boolean showRoot) {

        _partner = partner;
        _tree = tree;
        _showRoot = showRoot;
    }

    public void setSelectionHandler(SelectionHandler<T> sh) {
        _sh = sh;
    }

    public void show() {
        if (_pp != null && _pp.isShowing()) {
            return;
        }
        int pLeft = _partner.getAbsoluteLeft();
        int pTop = _partner.getAbsoluteTop();
        int cw = com.google.gwt.user.client.Window.getClientWidth();
        int ch = com.google.gwt.user.client.Window.getClientHeight();
        if (_pp == null) {
            _pp = new PopupPanel();
            _pp.setPartner(_partner);
            _pp.setAutoHideEnabled(true);

            _pp.setBorder(1, new RGB(0xaa, 0xaa, 0xaa));
            _pp.setBackgroundColour(new RGB(0xf8, 0xf8, 0xf8));
            _pp.setBorderRadius(2);

            _pp.setWidth(DEFAULT_WIDTH <= cw - pLeft ? DEFAULT_WIDTH : cw - pLeft);
            _pp.setBoxShadow(2, 2, 4, 2, new RGBA(0, 0, 0, 0.3));

            _vp = new VerticalPanel();
            _vp.fitToParent();

            _treeGUI = new TreeGUI(_tree, ScrollPolicy.AUTO);
            _treeGUI.fitToParent();
            _treeGUI.setShowRoot(_showRoot);
            _treeGUI.setEventHandler(new TreeGUIEventHandler() {

                @Override
                public void clicked(Node n) {
                    if (canSelect(n)) {
                        _treeGUI.select(n);
                    }
                }

                @Override
                public void selected(Node n) {
                    if (canSelect(n)) {
                        _selected = n;
                        _selectButton.setEnabled(true);
                    }
                }

                @Override
                public void deselected(Node n) {

                }

                @Override
                public void opened(Node n) {

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
            _treeGUI.addDoubleClickHandler(new DoubleClickHandler() {

                @Override
                public void onDoubleClick(DoubleClickEvent event) {
                    if (_selected != null) {
                        _selectionConfirmed = true;
                    }
                    _pp.hide();
                }
            });
            _vp.add(_treeGUI);

            _bb = ButtonUtil.createButtonBar(Position.BOTTOM, Alignment.RIGHT, 28);
            _vp.add(_bb);

            _cancelButton = _bb.addButton("Cancel");
            _cancelButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    _pp.hide();
                }
            });
            _cancelButton.focus();

            _selectButton = _bb.addButton("Select");
            _selectButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    if (_selected != null) {
                        _selectionConfirmed = true;
                    }
                    _pp.hide();
                }

            });
            _selectButton.disable();
            _selectButton.setMarginRight(20);

            _pp.setPopupPositionAndShow(new PositionCallback() {
                public void setPosition(int offsetWidth, int offsetHeight) {

                    int x = _partner.absoluteLeft();
                    int y = _partner.absoluteBottom();
                    _pp.setPopupPosition(x, y);
                }
            });

            _pp.addCloseHander(new PopupCloseHandler() {
                public void closed(PopupPanel p) {
                    if (_selectionConfirmed) {
                        if (_sh != null) {
                            _sh.selected(selected());
                        }
                        _selectionConfirmed = false;
                    }
                    _pp = null;
                }
            });

            _ap = new AbsolutePanel();
            _ap.fitToParent();
            _ap.add(new ResizeHandle(_ap, 15));
            _ap.add(_vp);
        }
        _pp.setContent(_ap);
        int h = DEFAULT_HEIGHT <= ch - pTop - 28 ? DEFAULT_HEIGHT : ch - pTop - 28;
        _pp.setHeight(h);
    }

    protected abstract boolean canSelect(Node n);

    protected abstract T transform(Node n);

    public T selected() {
        if (_selected != null) {
            return transform(_selected);
        }
        return null;
    }
}