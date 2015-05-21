package daris.client.ui.query.action;

import java.util.ArrayList;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.gui.object.action.precondition.ActionPrecondition;
import daris.client.model.query.Query;
import daris.client.model.query.filter.Filter;

public class SearchAction extends ActionInterface<Filter> {

    private Query _query;

    public SearchAction(String objectType, Query query, arc.gui.window.Window ownerWindow) {
        super(objectType, new ArrayList<ActionPrecondition>(), ownerWindow, (int) (com.google.gwt.user.client.Window
                .getClientWidth() * 0.9), (int) (com.google.gwt.user.client.Window.getClientHeight() * 0.9));
        _query = query;
    }

    @Override
    public void createInterface(InterfaceCreateHandler ch) {
        ch.created(new SearchForm(_query));
    }

    @Override
    public String actionButtonName() {
        return "Search";
    }

    @Override
    public String actionName() {
        return "Search";
    }

}
