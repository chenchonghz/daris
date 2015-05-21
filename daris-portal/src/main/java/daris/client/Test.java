package daris.client;

import java.util.ArrayList;
import java.util.List;

import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.button.ButtonBar;
import arc.gui.gwt.widget.panel.TabPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.window.Window;
import arc.gui.window.WindowProperties;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class Test {

    public static void test() {

        final VerticalPanel vp = new VerticalPanel();
        vp.fitToParent();

        final List<Integer> tabIds = new ArrayList<Integer>();
        final TabPanel tp = new TabPanel();
        tp.fitToParent();
        vp.add(tp);

        ButtonBar bb = new ButtonBar(ButtonBar.Position.BOTTOM, ButtonBar.Alignment.CENTER);
        bb.setHeight(32);
        Button addButton = bb.addButton("Add new tab");
        addButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                String tabName = "Tab " + ((int)(tabIds.size() + 1));
                int tabId = tp.addTab(tabName, null, new HTML(tabName));
                System.out.println("Added tab (id="+tabId+").");
                tabIds.add(tabId);
            }
        });
        Button removeButton = bb.addButton("Remove the first tab");
        removeButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if(tabIds.isEmpty()){
                    return;
                }
                int tabId = tabIds.get(0);
                tabIds.remove(0);
                tp.removeTabById(tabId);
                System.out.println("Removed tab (id="+tabId+").");
            }
        });
        Button removeAllButton = bb.addButton("Remove all tabs");
        removeAllButton.addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                tabIds.clear();
                tp.removeAll();
            }});
        
        vp.add(bb);
        
        WindowProperties wp = new WindowProperties();
        wp.setModal(true);
        wp.setCenterInPage(true);
        wp.setSize(0.8, 0.8);
        
        Window win = Window.create(wp);
        win.setContent(vp);
        win.show();
    }

}
