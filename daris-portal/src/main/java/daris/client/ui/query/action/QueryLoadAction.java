package daris.client.ui.query.action;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.mf.object.ObjectResolveHandler;
import daris.client.model.query.Query;

public class QueryLoadAction extends ActionInterface<Query> {

    public static final int DEFAULT_WIDTH = 480;
    public static final int DEFAULT_HEIGHT = 320;

    private ObjectResolveHandler<Query> _rh;

    public QueryLoadAction(ObjectResolveHandler<Query> objectResolveHandler, arc.gui.window.Window ownerWindow) {
        super("Query", null, ownerWindow, DEFAULT_WIDTH, DEFAULT_WIDTH);
        _rh = objectResolveHandler;
    }

    @Override
    public void createInterface(InterfaceCreateHandler ch) {
        ch.created(new QueryLoadForm(_rh));
    }

    @Override
    public String actionName() {
        return "Load";
    }

}
