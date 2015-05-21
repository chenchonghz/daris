package daris.client.ui.query.action;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import daris.client.model.query.Query;

public class QuerySaveAction extends ActionInterface<Query> {

    public static final int DEFAULT_WIDTH = 480;
    public static final int DEFAULT_HEIGHT = 320;

    private Query _query;

    public QuerySaveAction(Query query, arc.gui.window.Window owner) {
        super("Query", null, owner, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        _query = query;
    }

    @Override
    public void createInterface(InterfaceCreateHandler ch) {
        ch.created(new QuerySaveForm(_query));
    }

    @Override
    public String actionName() {
        return "Save";
    }

}
