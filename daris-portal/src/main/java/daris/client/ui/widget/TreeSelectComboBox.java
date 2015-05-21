package daris.client.ui.widget;

import java.util.ArrayList;
import java.util.List;

import arc.gui.InterfaceComponent;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.dimension.FixedDimension;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.input.TextBox;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.mf.client.util.IsNotValid;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.MustBeValid;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Validity;
import arc.mf.object.tree.Node;
import arc.mf.object.tree.Tree;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;

public abstract class TreeSelectComboBox<T> extends ContainerWidget implements MustBeValid, InterfaceComponent,
        StateChangeListener {

    public static interface SelectionHandler<T> {
        void selected(T o);
    }

    public static final int POPUP_WIDTH = 320;

    public static final int POPUP_HEIGHT = 480;

    private T _value;
    private boolean _changed = false;
    private List<StateChangeListener> _scls;
    private List<SelectionHandler<T>> _shs;

    private HorizontalPanel _hp;
    private TextBox _tb;
    private Image _pd;

    private TreeSelectPopupPanel<T> _pp;

    private boolean _readOnly;

    public TreeSelectComboBox(T value, Tree tree, boolean showRoot) {

        _readOnly = false;

        _tb = new TextBox();
        _tb.setFontSize(11);
        _tb.setWidth100();

        setValue(value, false);

        _pp = new TreeSelectPopupPanel<T>(_tb, tree, showRoot) {

            @Override
            protected boolean canSelect(Node n) {
                return TreeSelectComboBox.this.canSelect(n);
            }

            @Override
            protected T transform(Node n) {
                return TreeSelectComboBox.this.transform(n);
            }

        };
        _pp.setSelectionHandler(new TreeSelectPopupPanel.SelectionHandler<T>() {
            @Override
            public void selected(T v) {
                setValue(v, true);
            }
        });

        _tb.addFocusHandler(new FocusHandler() {

            @Override
            public void onFocus(FocusEvent event) {
                if (!_readOnly) {
                    _pp.show();
                }
            }
        });

        _tb.setReadOnly(true);

        _pd = new Image("resources/images/Down.png", 17, 17);
        _pd.setPreferredWidth(new FixedDimension(17));
        _pd.setHoverImage("resources/images/DownHover.png");
        _pd.setMarginTop(3);

        _pd.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                _pd.clearHover();
                if (!_readOnly) {
                    _pp.show();
                }
            }
        });

        _hp = new HorizontalPanel();
        _hp.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        _hp.add(_tb);
        _hp.setSpacing(1);
        _hp.add(_pd);

        initWidget(_hp);

        setPaddingTop(4);
    }

    public T value() {
        return _value;
    }

    public void setValue(T value) {
        setValue(value, true);
    }

    public void setValue(T value, boolean fireEvents) {
        if (ObjectUtil.equals(value, _value)) {
            return;
        }
        _value = value;
        _changed = true;
        _tb.setColour(RGB.BLACK);
        _tb.setValue(toString(value), false);
        if (fireEvents) {
            notifyOfChangeInState();
        }
    }

    public void clear() {
        setValue(null, true);
    }

    protected abstract String toString(T value);

    @Override
    public boolean changed() {
        return _changed;
    }

    public void setReadOnly(boolean readOnly) {
        _readOnly = readOnly;
    }

    public void addSelectionHandler(SelectionHandler<T> sh) {
        if (_shs == null) {
            _shs = new ArrayList<SelectionHandler<T>>();
        }
        _shs.add(sh);
    }

    public void removeSelectionHandler(SelectionHandler<T> sh) {
        if (_shs == null) {
            return;
        }
        _shs.remove(sh);
    }

    @Override
    public void addChangeListener(StateChangeListener listener) {
        if (_scls == null) {
            _scls = new ArrayList<StateChangeListener>();
        }
        _scls.add(listener);
    }

    @Override
    public void removeChangeListener(StateChangeListener listener) {
        if (_scls == null) {
            return;
        }
        _scls.remove(listener);
    }

    @Override
    public Validity valid() {
        if (_value == null) {
            return IsNotValid.INSTANCE;
        } else {
            return IsValid.INSTANCE;
        }
    }

    @Override
    public void notifyOfChangeInState() {
        if (_scls != null) {
            for (StateChangeListener scl : _scls) {
                scl.notifyOfChangeInState();
            }
        }
        if (_shs != null) {
            for (SelectionHandler<T> sh : _shs) {
                sh.selected(value());
            }
        }
    }

    @Override
    public Widget gui() {

        return this;
    }

    protected abstract boolean canSelect(Node n);

    protected abstract T transform(Node n);
}