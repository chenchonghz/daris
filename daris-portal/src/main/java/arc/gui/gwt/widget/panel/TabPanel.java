package arc.gui.gwt.widget.panel;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.colour.Colour;
import arc.gui.gwt.dnd.DragHoverResponder;
import arc.gui.gwt.dnd.DragResponder;
import arc.gui.gwt.style.Style;
import arc.gui.gwt.style.StyleRegistry;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.ContainerWidget;
import arc.gui.gwt.widget.tab.TabBar;
import arc.gui.gwt.widget.tab.TabBar.Event;
import arc.gui.gwt.widget.tab.TabBar.Item;
import arc.mf.client.util.Emitted;
import arc.mf.client.util.EventListener;
import arc.mf.client.util.ObjectUtil;

import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.user.client.ui.Widget;

/**
 * Set of tabs with associated content.
 * 
 * @author jason
 *
 */
public class TabPanel extends ContainerWidget {

    private static final int MARGIN = 2;
    
    static {
        Style s = StyleRegistry.register(TabPanel.class)
        .defineVariant("left")
            .setMarginLeft(MARGIN)
        .parent()
        .defineVariant("right")
            .setMarginRight(MARGIN)
        .parent()
        .defineVariant("top")
            .setMarginTop(MARGIN)
        .parent()
        .defineVariant("bottom")
            .setMarginBottom(MARGIN)
        .parent();
    
        
//      s.relate(
//          StyleRegistry.register(Tabs.class)
//              .defineVariant("left")
//                  .setMarginLeft(MARGIN)
//              .parent()
//              .defineVariant("right")
//                  .setMarginRight(MARGIN)
//              .parent()
//              .defineVariant("top")
//                  .setMarginTop(MARGIN)
//                  .parent()
//              .defineVariant("bottom")
//                  .setMarginBottom(MARGIN)
//              .parent()
//      );
            
    }
    
    
    public enum TabPosition {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM;
    }
    
    private static class Tab {
        private int _id;
        private String _name;
        private String _description;
        private Widget _widget;
        private TabBar.Item _tbItem;
        
        public Tab(int id,String name,String description,Widget widget, TabBar.Item tbItem) {
            _id = id;
            _name = name;
            _description = description;
            _widget = widget;
            _tbItem = tbItem;
            deactivate();
        }
        
        public int id() {
            return _id;
        }
        
        public String name() {
            return _name;
        }
        
        public String description() {
            return _description;
        }
        
        public void activate() {
        }
        
        public void deactivate() {
        }
        
        public Widget widget() {
            return _widget;
        }

        public void setTabContentWidget(BaseWidget widget) {
            _widget = widget;
        }
        
        public TabBar.Item tabBarItem(){
            return _tbItem;
        }
    }
    
//  private static class Tabs extends TabBar {
//      public Tabs(TabBar.LayoutStyle layout) {
//          super(layout);
//      }
//  }

    private List<Tab> _tabs;
    private TabBar    _tb;
    
    private SimplePanel _sp;
    
    private Tab _active;
    private int _id;
    
    public TabPanel() {
        this(TabPosition.TOP);
    }
    
    public TabPanel(TabPosition p) {
        this(p,100,true);
    }
    
    /**
     * Constructor.
     * 
     * @param p
     * @param tabsWidth The width of the tabs - ignored unless tabs are to left or right.
     */
    public TabPanel(TabPosition p,int tabsWidth,boolean resizeable) {
            
        _tabs = null;
    
        _sp = new SimplePanel();
        _sp.fitToParent();
        
        ContainerWidget cw = null;
        switch ( p ) {
        case LEFT:
            _tb = new TabBar(TabBar.LayoutStyle.LEFT);
            _tb.setWidth(tabsWidth);
            _tb.setTabWordWrap(false);

            if ( resizeable ) {
                cw = new HorizontalSplitPanel();
            } else {
                cw = new HorizontalPanel();
            }

            cw.add(_tb);
            cw.add(_sp);
            
            break;
            
        case RIGHT: 
            _tb = new TabBar(TabBar.LayoutStyle.RIGHT);
            _tb.setWidth(tabsWidth);
            _tb.setTabWordWrap(false);
            
            if ( resizeable ) {
                cw = new HorizontalSplitPanel();
            } else {
                cw = new HorizontalPanel();
            }
            
            cw.add(_sp);
            cw.add(_tb);
            
            break;
            
        case TOP:
            _tb = new TabBar(TabBar.LayoutStyle.TOP);

            cw = new VerticalPanel();
            
            cw.add(_tb);
            cw.add(_sp);
            
            setSmallerTabs();

            break;
            
        case BOTTOM:
            _tb = new TabBar(TabBar.LayoutStyle.BOTTOM);
            
            cw = new VerticalPanel();
            
            cw.add(_sp);
            cw.add(_tb);
            
            setSmallerTabs();

            break;
        }

        initWidget(cw);
        
        fitToParent();
        
        _active = null;
        _id = 1;
        
        switch ( p ) {
        case TOP:
            applyStyleVariant("top");
            break;
            
        case BOTTOM:
            applyStyleVariant("bottom");
            break;
            
        case LEFT:
            applyStyleVariant("left");
            break;
            
        case RIGHT:
            applyStyleVariant("right");
            break;
        }
    }

    /**
     * Controls whether or not text wrapping occurs in tabs with text labels.
     * 
     * @param wrap
     */
    public void setTabTextWrap(boolean wrap) {
        _tb.setTabWordWrap(wrap);
    }
    
    /**
     * Creates more compact tabs.
     * 
     */
    public void setSmallerTabs() {
        _tb.tighter();
    }
    
    /**
     * Sets the border for the tab panel body.
     * 
     * @param w
     * @param s
     * @param colour
     */
    public void setBodyBorder(int w,BorderStyle s,Colour colour) {
        _sp.setBorder(w, s, colour);
    }
    
    public void setBodyBorderTop(int w,BorderStyle s,Colour colour) {
        _sp.setBorderTop(w, s, colour);
    }
    
    public void setBodyBorderLeft(int w,BorderStyle s,Colour colour) {
        _sp.setBorderLeft(w, s, colour);
    }
    
    public void setBodyBorderRight(int w,BorderStyle s,Colour colour) {
        _sp.setBorderRight(w, s, colour);
    }
    
    public void setBodyBorderBottom(int w,BorderStyle s,Colour colour) {
        _sp.setBorderBottom(w, s, colour);
    }
    
    public void setBodyBorderRadius(int r) {
        _sp.setBorderRadius(r);
    }
    
    public void setBodyPadding(int p) {
        _sp.setPadding(p);
    }
    
    /**
     * Adds a tab with the given name, optional description, and widget.
     * 
     * @param name
     * @param description
     * @param widget
     * @return a unique tab identifier.
     */
    public int addTab(String name,String description,Widget widget) {
        if ( _tabs == null ) {
            _tabs = new ArrayList<Tab>();
        }
        
        final Item ti = _tb.addItem(name);
        
        final Tab t = new Tab(_id++,name,description,widget, ti);
        
        _tabs.add(t);
        
        ti.on(Event.ACTIVE, new EventListener<Item>() {
            @Override
            public void onEvent(Emitted<Item> event) {
                setActiveTab(_tb.ordinalForItem(event.target()));
            }
        });
        
        ti.addDragResponder(new DragHoverResponder() {

            @Override
            public BaseWidget widget() {
                return ti;
            }

            @Override
            public DragResponder dragResponderAt(int x, int y, EventTarget target) {
                return this;
            }

            @Override
            public int dragIsHovering(boolean initial, int currentDelay) {
                _tb.setActiveItem(ti);
                return 0;
            }
        });
        
        return t.id();
    }

    private Tab findTabById(int tabId) {

        if (_tabs == null) {
            return null;
        }

        for (Tab t : _tabs) {
            if (t.id() == tabId) {
                return t;
            }
        }

        return null;
    }

    /**
     * Remove the tab identified by the given (unique) tab identifier.
     * 
     * @param tabId
     */
    public void removeTabById(int tabId) {
        Tab t = findTabById(tabId);
        if (t != null) {
            if (t.equals(_active)) {
                int idx = _tabs.indexOf(t);
                if (idx > 0) {
                    setActiveTab(idx - 1);
                } else {
                    if (size() > 1) {
                        setActiveTab(idx + 1);
                    }
                }
            }
            _tabs.remove(t);
            _tb.removeItem(t.tabBarItem());
        }
    }
    

    /**
     * Remove all tabs
     */
    public void removeAll() {
        if (_tabs != null) {
            _tabs.clear();
        }
        if (_tb != null) {
            _tb.removeAll();
        }
        _sp.clear();
    }


    public void setTabContent(int tabId, BaseWidget widget) {

        Tab t = findTabById(tabId);
        if (t != null) {
            t.setTabContentWidget(widget);
            if (t.equals(_active)) {
                _sp.setContent(t.widget());
            }
        }
    }

    /**
     * Returns the number of tabs in the panel.
     * 
     * @return
     */
    public int size() {
        return _tabs.size();
    }
    
    /**
     * Activates the given tab by the given ordinal position (0,..)
     * 
     * @param i
     */
    public void setActiveTab(int i) {
        if ( _tabs == null ) {
            return;
        }
        
        Tab t = _tabs.get(i);
        setActiveTab(t,i);
    }
    
    /**
     * Returns the identity of the active tab, or -1 if no tab is active.
     * 
     * @return
     */
    public int activeTabId() {
        if ( _active == null ) {
            return -1;
        }
        
        return _active.id();
    }
    
    /**
     * Sets the active tab to the tab identifier returned by the "addTab" {@link #addTab(String, String, BaseWidget)}
     * 
     * @param tabId
     * @return The previously active tab, if any.
     */
    public int setActiveTabById(int tabId) {
        if ( tabId == -1 ) {
            return activeTabId();
        }
        
        if ( _tabs == null ) {
            return activeTabId();
        }
        
        int pidx = activeTabId();
        
        int idx = 0;
        for ( Tab t : _tabs ) {
            if ( t.id() == tabId ) {
                setActiveTab(t,idx);
                break;
            }
            
            idx++;
        }
        
        return pidx;
    }
    
    private void setActiveTab(Tab t,int idx) {
        if ( ObjectUtil.equals(t, _active) ) {
            return;
        }
        
        if ( _active != null ) {
            _active.deactivate();
        }
        
        t.activate();
        
        _active = t;
        
        if ( idx == -1 ) {
            idx = _tabs.indexOf(t);
        }
        
        _tb.setActiveItem(idx);
        _sp.setContent(t.widget());
        
        activated(t.id());
    }
    
    protected void activated(int id) {}
    
    
    protected boolean doLayout() {
        return super.doLayout();
    }
    
    protected void doLayoutChildren() {
        super.doLayoutChildren();
    }
}
