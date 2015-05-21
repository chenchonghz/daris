package daris.client.ui.query.filter;

import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tree.TreeGUI;
import arc.gui.gwt.widget.tree.TreeGUIEventHandler;
import arc.mf.object.tree.Node;
import daris.client.model.query.filter.FilterTree;
import daris.client.ui.DObjectGUIRegistry;

public class FilterTreeGUI extends TreeGUI {

    public FilterTreeGUI(FilterTree tree) {
        super(tree, ScrollPolicy.AUTO);

        setEventHandler(new TreeGUIEventHandler() {

            @Override
            public void clicked(Node n) {
                select(n);
            }

            @Override
            public void selected(Node n) {

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

        setShowRoot(false);

        setHeight100();

        setObjectRegistry(DObjectGUIRegistry.get());
        
        setShowToolTip(true);
        
        enableNodeDrag();
        
    }

}
