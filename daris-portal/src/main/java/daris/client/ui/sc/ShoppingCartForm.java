package daris.client.ui.sc;

import java.util.List;

import arc.gui.ValidatedInterfaceComponent;
import arc.gui.form.FormEditMode;
import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.list.ListGridHeader;
import arc.gui.gwt.widget.panel.CenteringPanel;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.mf.client.util.StateChangeListener;
import arc.mf.object.Null;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.user.client.ui.Widget;

import daris.client.model.sc.ContentItem;
import daris.client.model.sc.ShoppingCart;
import daris.client.ui.sc.ShoppingCartContentsForm.ContentSelectionListener;

public class ShoppingCartForm extends ValidatedInterfaceComponent {

    public static interface TabListener {
        void activated(int tabId);
    }

    private static final String TAB_NAME_CONTENTS = "Contents";
    private static final String TAB_NAME_SETTINGS = "Settings";

    private ShoppingCart _cart;
    private FormEditMode _mode;

    private VerticalPanel _vp;

    private HTML _header;

    private TabPanel _tp;

    private int _activeTabId;

    private int _contentsTabId;
    private ShoppingCartContentsForm _contentsForm;

    private int _settingsTabId;
    private ShoppingCartSettingsForm _settingsForm;

    public ShoppingCartForm(ShoppingCart cart, FormEditMode mode, boolean showHeader) {

        _mode = mode;

        _vp = new VerticalPanel();
        _vp.fitToParent();

        /*
         * header
         */
        if (showHeader) {
            CenteringPanel headerSP = new CenteringPanel();
            headerSP.setWidth100();
            headerSP.setHeight(20);
            headerSP.setBackgroundImage(new LinearGradient(LinearGradient.Orientation.TOP_TO_BOTTOM,
                    ListGridHeader.HEADER_COLOUR_LIGHT, ListGridHeader.HEADER_COLOUR_DARK));
            _header = new HTML();
            _header.fitToParent();
            _header.setFontSize(11);
            _header.setFontWeight(FontWeight.BOLD);
            headerSP.setContent(_header);
            _vp.add(headerSP);
        }

        /*
         * tabs
         */
        _tp = new TabPanel() {
            protected void activated(int id) {
                _activeTabId = id;
                if (_activeTabId == _contentsTabId) {
                    contentsTabActivated();
                }
                if (_activeTabId == _settingsTabId) {
                    settingsTabActivated();
                }
            }
        };
        _tp.fitToParent();
        _tp.setBodyBorder(1, BorderStyle.SOLID, new RGB(0x97, 0x97, 0x97));

        /*
         * content tab
         */
        _contentsForm = new ShoppingCartContentsForm(null, _mode);
        _contentsTabId = _tp.addTab(TAB_NAME_CONTENTS, null, _contentsForm.gui());
        addMustBeValid(_contentsForm);

        /*
         * settings tab
         */
        _settingsForm = new ShoppingCartSettingsForm(null, _mode);
        _settingsTabId = _tp.addTab(TAB_NAME_SETTINGS, null, _settingsForm.gui());
        addMustBeValid(_settingsForm);

        _tp.setActiveTabById(_contentsTabId);
        _vp.add(_tp);

        setCart(cart);

    }

    public void setCart(ShoppingCart cart) {
        _cart = cart;
        _contentsForm.setCart(_cart);
        _settingsForm.setCart(_cart);
        if (_header != null && cart != null) {
            _header.setHTML("Shopping Cart " + _cart.id());
        }
    }

    public void updateContent() {
        _contentsForm.refresh();
    }

    public void updateSettings() {
        _settingsForm.refresh();
    }

    protected void settingsTabActivated() {

    }

    protected void contentsTabActivated() {

    }

    @Override
    public Widget gui() {
        return _vp;
    }

    public BaseWidget widget() {
        return _vp;
    }

    public boolean contentsChanged() {
        return _contentsForm == null ? false : _contentsForm.changed();
    }

    public boolean contentsValid() {
        return _contentsForm.hasContentItems();
    }

    public void addContentSelectionListener(ContentSelectionListener csl) {
        _contentsForm.addContentSelectionListener(csl);
    }

    public boolean settingsChanged() {
        return _settingsForm == null ? false : _settingsForm.changed();
    }

    public boolean settingsValid() {
        return _settingsForm.valid().valid();
    }

    public void addSettingsChangeLisenter(StateChangeListener scl) {
        if (_settingsForm != null) {
            _settingsForm.addChangeListener(scl);
        }
    }

    public void addContentsChangeListener(StateChangeListener scl) {
        if (_contentsForm != null) {
            _contentsForm.addChangeListener(scl);
        }
    }

    public void removeContentSelectionListener(ContentSelectionListener csl) {
        _contentsForm.removeContentSelectionListener(csl);
    }

    public int contentTabId() {
        return _contentsTabId;
    }

    public int settingsTabId() {
        return _settingsTabId;
    }

    public boolean isContentsTabActivated() {
        return _activeTabId == _contentsTabId;
    }

    public boolean isSettingsTabActivated() {
        return _activeTabId == _settingsTabId;
    }

    public boolean hasSelectedContentItems() {
        List<ContentItem> sls = selectedContentItems();
        return sls != null && !sls.isEmpty();
    }

    public List<ContentItem> selectedContentItems() {
        return _contentsForm.selections();
    }

    public boolean hasContentItems() {
        return _contentsForm.hasContentItems();
    }

    public boolean isSettingsValid() {
        return _settingsForm.valid().valid();
    }

    public void applySettings(boolean saveToUserSelfSettings, ObjectMessageResponse<Null> rh) {
        _settingsForm.applySettings(saveToUserSelfSettings, rh);
    }

    public void refreshContents() {
        _contentsForm.refresh();
    }

    public void refreshSettings() {
        _settingsForm.refresh();
    }

}
